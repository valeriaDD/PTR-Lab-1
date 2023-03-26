import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, Props}
import akka.routing._

class TweetPrinterPool()(implicit system: ActorSystem) extends Actor with ActorLogging {

  override val supervisorStrategy: OneForOneStrategy = OneForOneStrategy() {
    case e: Exception =>
      log.error(s"Actor ${sender().path.name} in pool ${sender().path.parent.name} failed with $e, restarting...")
      Restart
    case _ => Escalate
  }

  val resizer: DefaultResizer = DefaultResizer(
    lowerBound = 3,
    upperBound = 5,
    backoffThreshold = 0.3,
    messagesPerResize = 10,
  )

  private val router = system.actorOf(RoundRobinPool(3)
    .withResizer(resizer)
    .withSupervisorStrategy(supervisorStrategy)
    .props(Props[TweetPrinterActor]))

  def receive: Receive = {
    case sseEvent: SSEEvent => router ! sseEvent
  }
}
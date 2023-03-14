import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorLogging, OneForOneStrategy, Props}
import akka.routing.{ActorRefRoutee, Router, SmallestMailboxRoutingLogic}

class TweetPrinterPool extends Actor with ActorLogging {

    private val routees = Vector.fill(3) {
      val r = context.actorOf(Props[TweetPrinterActor])
      context.watch(r)
      ActorRefRoutee(r)
    }

  private val router: Router = Router(SmallestMailboxRoutingLogic(), routees)

  override val supervisorStrategy: OneForOneStrategy = OneForOneStrategy() {
    case e: Exception =>
      log.error(s"Actor ${sender().path.name} in pool ${sender().path.parent.name} failed with $e, restarting...")
      Restart
    case _ => Escalate
  }

  def receive: Receive = {
    case sseEvent: SSEEvent => router.route(sseEvent, sender())
  }
}
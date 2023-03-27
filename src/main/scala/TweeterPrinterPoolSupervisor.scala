import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.routing.RoundRobinPool

class TweeterPrinterPoolSupervisor (numPools: Int, emotionsMap: Map[String, Int])(implicit system: ActorSystem) extends Actor with ActorLogging{
  override def preStart(): Unit = {
    log.info(s"Pool Supervisor ${self.path.name} started!")
  }

  override def postStop(): Unit = {
    log.info(s"Pool Supervisor ${self.path.name} stopped")
  }

  private val router = system.actorOf(RoundRobinPool(numPools)
    .props(Props(new TweetPrinterPool(emotionsMap))))

  def receive: Receive = {
    case sseEvent: SSEEvent => router ! sseEvent
  }
}

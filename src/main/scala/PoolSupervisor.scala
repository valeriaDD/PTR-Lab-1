import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, Props}
import akka.routing.{DefaultResizer, RoundRobinPool}

class PoolSupervisor(numPools: Int, actorClass: Class[_ <: Actor], emotionsMap: Map[String, Int])(implicit system: ActorSystem) extends Actor with ActorLogging{
  override def preStart(): Unit = {
    log.info(s"Pool Supervisor ${self.path.name} started!")
  }

  override def postStop(): Unit = {
    log.info(s"Pool Supervisor ${self.path.name} stopped")
  }

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

  private val router = system.actorOf(RoundRobinPool(numPools)
    .withResizer(resizer)
    .withSupervisorStrategy(supervisorStrategy)
    .props(Props(actorClass, emotionsMap)))

  def receive: Receive = {
    case sseEvent: SSEEvent => router ! sseEvent
  }
}

import akka.actor.{Actor, Cancellable}

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

case object Tick;
class BatcherActor(batchSize: Int = 5, timeWindow: FiniteDuration = 3.seconds) extends Actor {
  private val buffer = new ListBuffer[String]()
  private var timerCancellable: Option[Cancellable] = None

  override def preStart(): Unit = {
    scheduleTimer()
  }


  override def receive: Receive = {
    case tweet: String =>
      buffer += tweet
      if (buffer.length >= batchSize) {
        printBatchAndCancelTimer()
      }

    case Tick =>
      printBatchAndCancelTimer()
  }

  private def printBatchAndCancelTimer(): Unit = {
    timerCancellable.foreach(_.cancel())
    timerCancellable = None
      println(s"Batch of size ${buffer.length}: ${buffer.mkString(", ")}")
      buffer.clear()
  }

  private def scheduleTimer(): Unit = {
    import context.dispatcher
    timerCancellable = Some(context.system.scheduler.scheduleWithFixedDelay(Duration.Zero, timeWindow, self, Tick))
  }

  override def postStop(): Unit = {
    timerCancellable.foreach(_.cancel())
    timerCancellable = None
  }
}
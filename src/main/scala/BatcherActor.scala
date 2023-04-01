import akka.actor.{Actor, Cancellable}

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.{DurationInt, FiniteDuration}


class BatcherActor(batchSize: Int = 5, timeWindow: FiniteDuration = 5.seconds) extends Actor {
  private val buffer = new ListBuffer[String]()
  private var timerCancellable: Option[Cancellable] = None

  override def receive: Receive = {
    case tweet: String =>
      buffer += tweet
      if (buffer.length >= batchSize) {
        printBatchAndCancelTimer()
      } else if (timerCancellable.isEmpty) {
        scheduleTimer()
      }

    case "Tick" =>
      printBatchAndCancelTimer()
  }

  private def printBatchAndCancelTimer(): Unit = {
    timerCancellable.foreach(_.cancel())
    timerCancellable = None
    if (buffer.nonEmpty) {
      println(s"Batch of size ${buffer.length}: ${buffer.mkString(", ")}")
      buffer.clear()
    }
  }

  private def scheduleTimer(): Unit = {
    import context.dispatcher
    timerCancellable = Some(context.system.scheduler.scheduleOnce(timeWindow, self, "Tick"))
  }

  override def postStop(): Unit = {
    timerCancellable.foreach(_.cancel())
  }
}
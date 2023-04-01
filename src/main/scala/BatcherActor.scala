import akka.actor.Actor

import scala.collection.mutable.ListBuffer

class BatcherActor(batchSize: Int) extends Actor {
  private val buffer = new ListBuffer[String]()

  override def receive: Receive = {
    case tweet: String =>
      buffer += tweet
      if (buffer.length >= batchSize) {
        printBatch()
      }
  }

  private def printBatch(): Unit = {
    if (buffer.nonEmpty) {
      println(s"Batch of size ${buffer.length}: ${buffer.mkString(", ")}")
      buffer.clear()
    }
  }
}
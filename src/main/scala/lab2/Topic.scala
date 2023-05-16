package lab2

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.Tcp.Write
import akka.util.ByteString

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

class Topic extends Actor with ActorLogging {
  private var subscribers = Map.empty[Int, ActorRef]

  override def preStart(): Unit = {
    log.info(s"Topic Actor ${self.path.name} created.")
  }

  override def postStop(): Unit = {
    log.info(s"Topic Actor ${self.path.name} died.")
  }

  def receive: Receive = {
    case Subscribe(senderHashCode, sender, message) =>
      Try {
        subscribers += (senderHashCode -> sender)
        println(s"${self.path.name} subscribed ${sender.path.name}")
        sender ! Write(ByteString(s"Successfully subscribed\r\n"))
      } recover {
        case e =>
          logErrorToFile(generateErrorMessage(senderHashCode, message, e.getMessage))
      }
    case Unsubscribe(senderHashCode, sender, message) =>
      Try {
        subscribers.get(senderHashCode).foreach { actorRef =>
          subscribers -= senderHashCode
          println(s"${self.path.name} unsubscribed ${actorRef.path.name}")
          sender ! Write(ByteString(s"Successfully unsubscribed\r\n"))
        }
      } recover {
        case e =>
          logErrorToFile(generateErrorMessage(senderHashCode, message, e.getMessage))
      }
    case Publish(senderHashCode, sender, message) =>
      subscribers.values.foreach(value =>
        Try {
          value ! Write(ByteString("Incoming message: $message\r\n"))
        } recover {
          case e =>
            logErrorToFile(generateErrorMessage(senderHashCode, message, e.getMessage))
        }
      )
      sender ! Write(ByteString("Message published!\r\n"))
    case DeleteConnection(senderHashCode) =>
      subscribers.get(senderHashCode) match {
        case Some(actorRef) =>
          subscribers -= senderHashCode
          println(s"Connection for senderHashCode: $senderHashCode deleted from ${self.path.name}.")
      }
  }

  private def generateErrorMessage(senderHashCode: Int, message: String, errorMessage: String): String = {
    val currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    val fileName = getClass.getSimpleName
    s"[$currentTime] [$fileName] [Sender Hash Code: $senderHashCode] [Message: $message] Error: $errorMessage"
  }


  private def logErrorToFile(errorMessage: String): Unit = {
    val errorLogFile = "C:\\Users\\dubin\\Uni_sem_6\\PTR\\Labs\\Lab1\\src\\main\\scala\\lab2\\error_log.txt"
    val logFile = new java.io.File(errorLogFile)
    val writer = new java.io.FileWriter(logFile, true)
    try {
      writer.write(s"$errorMessage\n")
    } finally {
      writer.close()
    }
  }
}

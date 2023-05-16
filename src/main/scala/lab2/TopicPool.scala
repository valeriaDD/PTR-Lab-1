package lab2

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.{Failure, Try}

case class Subscribe(senderHashCode: Int, sender: ActorRef)
case class Unsubscribe(senderHashCode: Int)
case class Publish(message: String)

class TopicPool(implicit system: ActorSystem) extends Actor with ActorLogging {
  private val errorLogFile = "C:\\Users\\dubin\\Uni_sem_6\\PTR\\Labs\\Lab1\\src\\main\\scala\\lab2\\error_log.txt"

  def receive: Receive = {
    case Message(senderHashCode, sender, message) =>
      Try {
        val messageToArray = message.split(' ')
        val messageType = messageToArray(0)
        val messageTopic = messageToArray(1)
        val topicActorName = s"Topic-$messageTopic"

        context.child(topicActorName) match {
          case Some(topicActor) =>
            messageType.toUpperCase match {
              case "SUBSCRIBE" => topicActor ! Subscribe(senderHashCode, sender)
              case "UNSUBSCRIBE" => topicActor ! Unsubscribe(senderHashCode)
              case "PUBLISH" => topicActor ! Publish(message)
              case _ => val errorMessage = generateErrorMessage(senderHashCode, message, s"Invalid message type, provided ${messageType}")
                logErrorToFile(errorMessage)
            }
          case None =>
            messageType.toUpperCase match {
              case "SUBSCRIBE" => context.actorOf(Props(new Topic()), name = topicActorName) ! Subscribe(senderHashCode, sender)
              case "UNSUBSCRIBE" =>
                val errorMessage = generateErrorMessage(senderHashCode, message, s"No subscribers for topic $messageTopic")
                logErrorToFile(errorMessage)
              case "PUBLISH" => context.actorOf(Props(new Topic()), name = topicActorName) ! Publish(message)
              case _ =>
                val errorMessage = generateErrorMessage(senderHashCode, message, s"Invalid message type, provided ${messageType}")
                logErrorToFile(errorMessage)
            }
        }
      } match {
        case Failure(e) =>
          val errorMessage = generateErrorMessage(senderHashCode, message, e.getMessage)
          logErrorToFile(errorMessage)
        case _ =>
      }
  }

  private def generateErrorMessage(senderHashCode: Int, message: String, errorMessage: String): String = {
    val currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    val fileName = getClass.getSimpleName
    s"[$currentTime] [$fileName] [Sender Hash Code: $senderHashCode] [Message: $message] Error: $errorMessage"
  }


  private def logErrorToFile(errorMessage: String): Unit = {
    val logFile = new java.io.File(errorLogFile)
    val writer = new java.io.FileWriter(logFile, true)
    try {
      writer.write(s"$errorMessage\n")
    } finally {
      writer.close()
    }
  }
}

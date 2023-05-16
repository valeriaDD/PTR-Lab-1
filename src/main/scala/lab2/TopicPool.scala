package lab2

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.{Failure, Try}

case class Subscribe(senderHashCode: Int, sender: ActorRef, message: String)

case class Unsubscribe(senderHashCode: Int, sender: ActorRef, message: String)

case class Publish(senderHashCode: Int, sender: ActorRef, message: String)

class TopicPool(implicit system: ActorSystem) extends Actor with ActorLogging {

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
              case "SUBSCRIBE" => topicActor ! Subscribe(senderHashCode, sender, message)
              case "UNSUBSCRIBE" => topicActor ! Unsubscribe(senderHashCode, sender, message)
              case "PUBLISH" => topicActor ! Publish(senderHashCode, sender, message)
              case _ => val errorMessage = generateErrorMessage(senderHashCode, message, s"Invalid message type, provided ${messageType}")
                logErrorToFile(errorMessage)
            }
          case None =>
            messageType.toUpperCase match {
              case "SUBSCRIBE" => context.actorOf(Props(new Topic()), name = topicActorName) ! Subscribe(senderHashCode, sender, message)
              case "PUBLISH" => context.actorOf(Props(new Topic()), name = topicActorName) ! Publish(senderHashCode,sender ,message)
              case "UNSUBSCRIBE" =>
                logErrorToFile(generateErrorMessage(senderHashCode, message, s"No subscribers for topic $messageTopic"))
              case _ =>
                logErrorToFile( generateErrorMessage(senderHashCode, message, s"Invalid message type, provided ${messageType}"))
            }
        }
      } match {
        case Failure(e) =>
          val errorMessage = generateErrorMessage(senderHashCode, message, e.getMessage)
          logErrorToFile(errorMessage)
        case _ =>
      }
    case DeleteConnection(senderHashCode) =>
      context.children.foreach(childActor => childActor ! DeleteConnection(senderHashCode))
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

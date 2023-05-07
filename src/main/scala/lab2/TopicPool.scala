package lab2

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

case class Subscribe(senderHashCode: Int, sender: ActorRef)
case class Unsubscribe(senderHashCode: Int)
case class Publish(message: String)

class TopicPool(implicit system: ActorSystem) extends Actor with ActorLogging {
  def receive: Receive = {
    case Message(senderHashCode, sender, message) =>
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
            case _ => log.error(s"Invalid message type: $messageType")
          }
        case None =>
          val topicActor = context.actorOf(Props(new Topic()), name = topicActorName)
          messageType.toUpperCase match {
            case "SUBSCRIBE" => topicActor ! Subscribe(senderHashCode, sender)
            case "UNSUBSCRIBE" => log.warning(s"No subscribers for topic $messageTopic")
            case "PUBLISH" => topicActor ! Publish(message)
            case _ => log.error(s"Invalid message type: $messageType")
          }
      }
  }
}

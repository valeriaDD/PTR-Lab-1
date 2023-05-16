package lab2

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.io.Tcp.{ConnectionClosed, Received}

case class Message(senderHashCode: Int, sender: ActorRef, message: String)
case class DeleteConnection(senderHashCode: Int)

class TCPConnectionHandler(topicPool: ActorRef)(implicit system: ActorSystem) extends Actor {
  var text = "";

  override def receive: Actor.Receive = {
    case Received(data) =>
      val decoded = data.utf8String
      if(text.toUpperCase.trim.takeRight(3).equals("END")) {
        topicPool ! Message(sender().hashCode(), sender(), text)

        text = "";
      } else {
        text += decoded
      }
    case message: ConnectionClosed =>
      topicPool ! DeleteConnection(sender().hashCode())
      println(s"Connection with ${sender().hashCode()} has been closed.")
      context stop self
  }
}

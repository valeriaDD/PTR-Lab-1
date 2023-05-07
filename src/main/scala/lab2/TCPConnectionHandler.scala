package lab2

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.io.Tcp.{ConnectionClosed, Received, Write}
import akka.util.ByteString

case class Message(senderHashCode: Int, sender: ActorRef, message: String)

class TCPConnectionHandler(topicPool: ActorRef)(implicit system: ActorSystem) extends Actor {
  var text = "";

  override def receive: Actor.Receive = {
    case Received(data) =>
      val decoded = data.utf8String
      if(text.toUpperCase.takeRight(3).equals("END")) {
        topicPool ! Message(sender().hashCode(), sender(), text)

        text = "";
      } else {
        text += decoded
      }
    case message: ConnectionClosed =>
      println("Connection has been closed")
      context stop self
  }
}

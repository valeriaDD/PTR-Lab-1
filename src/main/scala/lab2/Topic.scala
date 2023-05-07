package lab2

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.Tcp.Write
import akka.util.ByteString

class Topic extends Actor with ActorLogging {
  private var subscribers = Map.empty[Int, ActorRef]

  override def preStart(): Unit = {
    log.info(s"Topic Actor ${self.path.name} created.")
  }

  override def postStop(): Unit = {
    log.info(s"Topic Actor ${self.path.name} died.")
  }

  def receive: Receive = {
    case Subscribe(senderHashCode, sender) =>
      subscribers += (senderHashCode -> sender)
      println(s"${self.path.name} subscribed ${sender.path.name}")
      sender ! Write(ByteString(s"Successfully subscribed\r\n"))
    case Unsubscribe(senderHashCode) =>
      subscribers.get(senderHashCode).foreach { actorRef =>
        subscribers -= senderHashCode
        println(s"${self.path.name} unsubscribed ${actorRef.path.name}")
        sender ! Write(ByteString(s"Successfully unsubscribed\r\n"))
      }
    case Publish(message) =>
      subscribers.values.foreach(_ ! Write(ByteString(s"Incoming message: $message\r\n")))
  }
}

package lab2

import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString

import java.net.InetSocketAddress

class TCPConnectionManager(address: String, port: Int) extends Actor {
  import context.system
  IO(Tcp) ! Bind(self, new InetSocketAddress(address, port))
  private val topicPool: ActorRef = system.actorOf(Props(new TopicPool()), "supervisor")

  override def receive: Receive = {
    case Bound(local) =>
      println(s"Server started on $local")
    case Connected(remote, local) =>
      val handler = context.actorOf(Props(new TCPConnectionHandler(topicPool)))
      println(s"New connection: $local -> $remote")
      sender() ! Register(handler)
      sender() ! Write(ByteString("Welcome\r\n"))
  }
}
package lab2

import akka.actor.{ActorSystem, Props}

object Server extends App {
  val system = ActorSystem()
  val tcpserver = system.actorOf(Props(classOf[TCPConnectionManager], "localhost", 8080))
}
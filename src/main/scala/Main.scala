import akka.actor.{ Props, ActorSystem }
import java.net.{InetSocketAddress, InetAddress}

object Main extends App {

  var serverarg = "-server"
  val system = ActorSystem("server")
  var ip = args.filter(_ != serverarg)

  if (args.contains(serverarg)) {
    val local = new InetSocketAddress(ip(0), 53211)
    println("I am a supaserver on "+local)
    val server = system.actorOf(Props(classOf[Server], local), "Server")
  } else {
    val local = new InetSocketAddress(ip(0), 0)
    val remote = new InetSocketAddress(ip(1), 53211)
    println("I am a supaclient on "+local+" with server on "+remote)
    val client = system.actorOf(Props(classOf[Client], local, remote), "Client")
  }
}
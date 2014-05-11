import akka.actor.{ActorRef, ActorLogging, Actor}
import akka.io.{Udp, IO}
import akka.util.ByteString
import java.net.InetSocketAddress
import scala.concurrent.duration._

class Client(inetsocketaddr: InetSocketAddress, remote: InetSocketAddress) extends Actor with ActorLogging {
  import context.system
  import context.dispatcher
  val punch = """punch\D*(\d+\.\d+\.\d+\.\d+)\D*(\d+)\D*""".r

  IO(Udp) ! Udp.Bind(self, inetsocketaddr)

  def receive = {
    case Udp.Bound(local) =>
      log.debug("bounded to "+local)
      context.become(ready(sender(), local))
      context.system.scheduler.schedule(0.milliseconds,1500.milliseconds) {
        //val s = System.currentTimeMillis.toString
        //log.debug(s)
        self ! "Start holepunching!"
      }
  }

  def ready(socket: ActorRef, local: InetSocketAddress): Receive = {
    case msg: String =>
      log.debug("sending \""+msg+"\" from "+local+ " to "+remote)
      socket ! Udp.Send(ByteString(msg), remote)
    case Udp.Received(data, remote) =>
      log.debug("resieved "+data.utf8String+" from "+remote)
      process(socket, data, remote)
    case Udp.Unbind  => socket ! Udp.Unbind
    case Udp.Unbound => context.stop(self)
  }

  def process(socket: ActorRef, data: ByteString, remote: InetSocketAddress) {
    data.utf8String match {
      case punch(host, port) =>
        var otherremote = new InetSocketAddress(host, Integer.parseInt(port))
        log.debug("pinging to "+otherremote)
        socket ! Udp.Send(ByteString("ping!"), otherremote)
      case "ping!" =>
        log.debug("IT WORKS!!!")
      case _ => {}
    }
  }
}
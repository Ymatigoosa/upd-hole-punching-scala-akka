import akka.actor.{ActorRef, ActorLogging, Actor}
import akka.io.{Udp, IO}
import akka.util.ByteString
import java.net.InetSocketAddress
import scala.collection.mutable.HashMap
import scala.concurrent.duration._

class Server(inetsocketaddr: InetSocketAddress) extends Actor with ActorLogging {
  import context.system
  import context.dispatcher
  private val _sessions = HashMap[String, (Long, InetSocketAddress)]()

  IO(Udp) ! Udp.Bind(self, inetsocketaddr)

  context.system.scheduler.schedule(5.seconds,2.seconds, self, "checkclients")

  def receive = {
    case Udp.Bound(local) =>
      log.debug("bounded to "+local)
      context.become(ready(sender(), local))
  }

  def ready(socket: ActorRef, local: InetSocketAddress): Receive = {
    case Udp.Received(data, remote) =>
      log.debug("resieved \""+data.utf8String+"\" from "+remote)
      process(socket, data, remote)
    case "checkclients" =>
      val currenttime = System.currentTimeMillis
      log.debug("making offline idling users")
      _sessions.retain{ case (_, (time, _)) => time+10000 > currenttime }
      lazy val d = _sessions map {case (t, _) => s"$t"} mkString("; ")
      log.debug(d)
    case Udp.Unbind  => socket ! Udp.Unbind
    case Udp.Unbound => context.stop(self)
  }

  def process(socket: ActorRef, data: ByteString, remote: InetSocketAddress) {
    data.utf8String match {
      case "Start holepunching!" =>
        _sessions.put(remote.toString, (System.currentTimeMillis, remote))
        _sessions
          .filter{case (_, (_, otherremote)) => remote.toString != otherremote.toString}
          .map{
            case (_, (_, otherremote)) =>
              val s = ByteString("punch"+remote.toString)
              socket ! Udp.Send(s, otherremote)
              log.debug("Sending \""+s.utf8String+"\" to "+otherremote.toString)

              val s1 = ByteString("punch"+otherremote.toString)
              socket ! Udp.Send(s1, remote)
              log.debug("Sending \""+s1.utf8String+"\" to "+remote.toString)
        }
        socket ! Udp.Send(ByteString("Hello!"), remote)
    }
  }
}
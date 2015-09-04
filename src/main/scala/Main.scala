import java.io.{FileWriter, BufferedWriter, File, FileInputStream}

import akka.actor.ActorSystem
import slack.SlackUtil
import scala.concurrent.ExecutionContext.Implicits.global
import slack.api.SlackApiClient
import slack.rtm.SlackRtmClient
import scala.pickling.Defaults._, scala.pickling.json._
import scala.pickling.binary.BinaryPickle
import scala.pickling.{Unpickler, Pickler}

import scala.util.{Failure, Success}


case class GameData(var users: Set[String])

object Main {
  implicit val system = ActorSystem("slack")
  val rtmClient = SlackRtmClient(Settings.apiToken)
  val apiClient = SlackApiClient(Settings.apiToken)

  var data = GameData(Set[String]())

  def main(args: Array[String]) {
    data = load

    rtmClient.onMessage { message =>
      val channelId = rtmClient.state.getChannelIdForName(Settings.channel)
      println(s"user: ${message.user}, message: ${message.text}, channel: ${message.channel}")

      if(SlackUtil.extractMentionedIds(message.text).contains(rtmClient.state.self.id) && channelId.get == message.channel) {
        val senderName = rtmClient.state.getUserById(message.user).map { _.name } getOrElse "no user"
        rtmClient.sendMessage(message.channel, s"hello there, ${senderName}")
        data.users = data.users + message.user
      }
    }

    sys.addShutdownHook(save)
  }

  def load() : GameData = {
    JSONPickle(scala.io.Source.fromFile("./" + Settings.saveFile).mkString).unpickle[GameData]
  }

  def save() {
    println(data.pickle)
    val file = new File(Settings.saveFile)
    val bw = new BufferedWriter(new FileWriter(file))

    bw.write(data.pickle.value)
    bw.close()
  }
}

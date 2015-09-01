import akka.actor.ActorSystem
import slack.SlackUtil
import scala.concurrent.ExecutionContext.Implicits.global
import slack.api.SlackApiClient
import slack.rtm.SlackRtmClient

import scala.util.{Failure, Success}


object Main {
  implicit val system = ActorSystem("slack")
  val rtmClient = SlackRtmClient(Settings.apiToken)
  val apiClient = SlackApiClient(Settings.apiToken)

  def main(args: Array[String]) {
    rtmClient.onMessage { message =>
      val channelId = rtmClient.state.getChannelIdForName(Settings.channel)
      println(s"user: ${message.user}, message: ${message.text}, channel: ${message.channel}")

      if(SlackUtil.extractMentionedIds(message.text).contains(rtmClient.state.self.id) && channelId.get == message.channel) {
        val senderName = rtmClient.state.getUserById(message.user).map { _.name } getOrElse "no user"
        rtmClient.sendMessage(message.channel, s"hello there, ${senderName}")
      }
    }
  }
}

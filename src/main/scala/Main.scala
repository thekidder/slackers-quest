import GameCommand.Command
import akka.actor.ActorSystem
import slack.SlackUtil
import slack.models.Message
import scala.concurrent.ExecutionContext.Implicits.global
import slack.api.SlackApiClient
import slack.rtm.SlackRtmClient

import scala.util.{Failure, Success}

object Main {
  implicit val system = ActorSystem("slack")
  val rtmClient = SlackRtmClient(Settings.apiToken)
  val apiClient = SlackApiClient(Settings.apiToken)
  val slackState = SlackState(rtmClient)

  var state = GameState.load()

  def inviteUsers(message: Message): Boolean = {
    val imsToInvite = SlackUtil.extractMentionedIds(message.text)
      .flatMap(slackState.imForUserId)
      .filter(im => state.userState(im.user) == UserState.Unseen)

    if (imsToInvite.isEmpty) {
      rtmClient.sendMessage(message.channel, "No new users to invite.")
    } else {
      val invitedUsers = imsToInvite
        .map(_.user)
        .flatMap(userId => rtmClient.state.users.find(userId == _.id))
        .map(_.name)
        .mkString(", ")
      rtmClient.sendMessage(message.channel, s"Invited $invitedUsers to ${Settings.gameName}!")
    }

    val invitingUser = rtmClient.state.users
      .find(user => user.id == message.user)
      .map(_.name)
      .getOrElse("an unknown entity")

    imsToInvite
      .foreach(im => {
        println(s"Inviting user ${im.user} using IM channel ${im.id}")
        rtmClient.sendMessage(im.id, "The blue glow of your computer monitor flickers and wanes. " +
          s"The pixels flash brightly, and go black. You see a message from $invitingUser in the darkness.\n" +
          "\"Join me for adventure\", it says. ")

        rtmClient.sendMessage(im.id, s"Type ${GameCommand.acceptQuest.command} to join the quest.")
        state.data.users.getOrElseUpdate(im.user, User(im.user, UserState.Unseen)).state = UserState.Invited
      })
    true
  }

  def acceptQuest(message: Message): Boolean = {
    for(
      im <- slackState.imForUserId(message.user);
      channelId <- rtmClient.state.getChannelIdForName(Settings.channel)
    ) {
      println(s"User ${im.user} has accepted the quest!")
      rtmClient.sendMessage(im.id, s"A shiver runs down your spine. " +
        "This feels like something big - even more important than looking at cat pictures on Reddit. " +
        s"You feel a sudden urge to join <#$channelId|${Settings.channel}>. That's where all the action is.")
      state.data.users.getOrElseUpdate(im.user, User(im.user, UserState.Unseen)).state = UserState.Joined
    }
    true
  }

  def leaveQuest(message: Message): Boolean = {
    println("left quest")
    true
  }

  def inventory(message: Message): Boolean = true

  def rejoinQuest(message: Message): Boolean = true

  def handleCommand(message: Message, command: Command): Boolean =
    command match {
      case GameCommand.invite => inviteUsers(message)
      case GameCommand.acceptQuest => acceptQuest(message)
      case GameCommand.leave => leaveQuest(message)
      case GameCommand.inventory => inventory(message)
      case GameCommand.rejoin => rejoinQuest(message)
    }

  def runCommand(message: Message, validCommands: Seq[GameCommand.Command]): Boolean = {
    println(validCommands)
    val result = validCommands
      .filter(command => message.text.startsWith(command.command))
      .map(command => handleCommand(message, command))
    !result.contains(false) && result.nonEmpty
  }

  def handleDM(message: Message): Unit = {
    println("Handling DM...")
    val validCommands = state.userState(message.user) match {
      case UserState.Unseen => Seq(GameCommand.acceptQuest)
      case UserState.Joined => Seq(GameCommand.invite, GameCommand.inventory)
      case UserState.Invited => Seq(GameCommand.acceptQuest)
      case UserState.Declined => Seq(GameCommand.rejoin)
    }
    if (!runCommand(message, validCommands)) {
      println(s"User ${message.user} inputted invalid request ${message.text}!")
      slackState.imForUserId(message.user).foreach(im => {
        rtmClient.sendMessage(im.id, "Sorry - I didn't get that. Valid commands:")
        rtmClient.sendMessage(im.id, validCommands.map(c => s"*${c.command}* - ${c.tooltip}").mkString("\n"))
      })
    }
  }

  def main(args: Array[String]) {
    rtmClient.onMessage { message =>
      if (message.user != rtmClient.state.self.id) {
        println(s"user: ${message.user}, message: ${message.text}, channel: ${message.channel}")

        if (rtmClient.state.ims.exists(_.id == message.channel)) {
          handleDM(message)
        }
      }
    }

    sys.addShutdownHook(state.save())
  }
}

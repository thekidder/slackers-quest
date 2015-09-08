import slack.models.Im
import slack.rtm.SlackRtmClient

object SlackState {
  def apply(rtmClient: SlackRtmClient): SlackState = {
    new SlackState(rtmClient)
  }
}

class SlackState(rtmClient: SlackRtmClient) {
  def imForUserId(userId: String): Option[Im] = {
    rtmClient.state.ims.find(im => im.user == userId)
  }
}

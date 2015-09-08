import com.typesafe.config._

object Settings {
  val conf = ConfigFactory.load()

  def apiToken: String = conf.getString("api-token")
  def channel: String = conf.getString("channel")
  def saveFile: String = conf.getString("save-file")
  def gameName: String = conf.getString("game-name")
}

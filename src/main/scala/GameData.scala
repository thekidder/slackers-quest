import java.io._

import scala.collection.mutable
import scala.pickling.Defaults._, scala.pickling.json._
import scala.pickling.{PicklingException, Unpickler, Pickler}

import scala.pickling.json.JSONPickle

object UserState {
  sealed trait State
  // user has never been invited, or joined
  case object Unseen extends State
  // user has been invited but has not accepted
  case object Invited extends State
  // user is a participant in slacker's quest
  case object Joined extends State
  // user had involvement, but has now left
  case object Declined extends State
}

case class User(id: String, var state: UserState.State)

case class GameData (
  users: mutable.Map[String, User]
)

object GameData {
  def save(gameData: GameData) {
    try {
      val file = new File(Settings.saveFile)
      val bw = new BufferedWriter(new FileWriter(file))

      bw.write(gameData.pickle.value)
      bw.close()

      println(s"Saved current data to ${Settings.saveFile}")
    } catch {
      case ex: IOException => println(s"Could not save data! $ex")
    }
  }

  def load(): GameData = {
    lazy val newGameData = GameData(mutable.Map[String,User]())
    try {
      val data = JSONPickle(scala.io.Source.fromFile(Settings.saveFile).mkString).unpickle[GameData]
      println(s"Loaded ${Settings.saveFile}")
      data
    } catch {
      case ex: FileNotFoundException =>
        println("No save found, creating...")
        newGameData
      case ex: PicklingException =>
        println("Invalid save! Recreating...")
        newGameData
    }
  }
}


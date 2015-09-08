
object GameCommand {
  sealed abstract class Command(
    val command: String,
    val tooltip: String
  )
  case object acceptQuest extends Command("accept", s"Join ${Settings.gameName}")
  case object invite extends Command("invite", s"Invite a list of users to ${Settings.gameName}")
  case object leave extends Command("leave", s"Stop participating in ${Settings.gameName}")
  case object rejoin extends Command("rejoin", s"Rejoin ${Settings.gameName}!")
  case object inventory extends Command("inventory", "Prints your current inventory contents")

}

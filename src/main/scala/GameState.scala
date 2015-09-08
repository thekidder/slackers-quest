
object GameState {
  def load() = {
    new GameState(GameData.load())
  }
}

class GameState(var data: GameData) {
  def userState(userId: String) : UserState.State =
    data.users
      .find(_._1 == userId)
      .map(_._2.state)
      .getOrElse(UserState.Unseen)

//  def joinGame(userId: String) =
//    data.users
//      .getOrElseUpdate(_, User(userId, UserState.Unseen))
//    .state = UserState.Joined

  def save() = {
    GameData.save(data)
  }
}
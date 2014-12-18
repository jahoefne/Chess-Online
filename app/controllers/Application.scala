package controllers

import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.Play.current
import model._
import securesocial.core._
import play.api.mvc.Action
import scala.util.Random


object UUID{def uuid = (Random.alphanumeric take  8).mkString}

class Application(override implicit val env: RuntimeEnvironment[User]) extends securesocial.core.SecureSocial[User] {

  /** Landing Page */
  def index = SecuredAction{  Ok(views.html.index()) }

  /** Create a new game instance */
  def newGame =  Action{
    val gameUUID = UUID.uuid
    GameDB.save(ActiveGame(gameUUID))
    Redirect(routes.Application.game(gameUUID))
  }

  /** Delete existing game **/
  def deleteGame(uuid: String) =  Action{
    Ok(views.html.error("42"))
  }

  /** Access existing game instance */
  def game(uuid: String) =  Action{
    GameDB.exists(uuid) match {
      case true => Ok(views.html.game(gameUUID = uuid, playerUUID = UUID.uuid))
      case false => Ok(views.html.error("The requested game does not exist, please create a:"))
    }
  }

  /** Create websocket for game: uuid */
  def socket (uuid: String, playerID: String) = WebSocket.acceptWithActor[JsValue, JsValue] {
    request => out =>
      UserRefs.addPlayer(uuid = uuid, player = new Player(playerID, out))
      ChessWebSocketActor.props(out = out, playerID = playerID , gameID = uuid)
  }

  /** Return a list of all game instances */
  def gameList =  Action{
    Ok(views.html.gameList(gameList = GameDB.list))
  }
}
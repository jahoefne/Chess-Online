package controllers

import model._
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.Play.current
import java.util.UUID

object Application extends Controller {

  /** Landing Page */
  def index = Action(Ok(views.html.index()))

  /** Create a new game instance */
  def newGame = Action {
    val gameUUID = UUID.randomUUID.toString
    GameDB.save(ActiveGame(gameUUID))
    Redirect(routes.Application.game(gameUUID))
  }

  /** Delete existing game **/
  def deleteGame(uuid: String) = Action { Ok(views.html.error("42"))}

  /** Access existing game instance */
  def game(uuid: String) = Action {
    GameDB.exists(uuid) match {
      case true => Ok(views.html.game(uuid, playerUUID = UUID.randomUUID().toString))
      case false =>Ok(views.html.error("The requested game does not exist, please create a:"))
    }
  }

  /** Create websocket for game: uuid */
  def socket (uuid: String, playerID: String) = WebSocket.acceptWithActor[JsValue, JsValue] {
    request => out =>
      UserRefs.addPlayer(uuid = uuid, player = new Player(playerID, out))
      ChessWebSocketActor.props(out = out, playerID = playerID , gameID = uuid)
  }

  /** Return a list of all game instances */
  def gameList = Action { Ok(views.html.gameList(gameList = GameDB.list)) }
}
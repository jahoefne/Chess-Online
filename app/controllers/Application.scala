package controllers

import controller.GameController
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
    ActiveGameStore.add(gameUUID, ActiveGame(gameUUID, new GameController(), None, None, List()))
    Redirect(routes.Application.game(gameUUID))
  }

  /** Access existing game instance */
  def game(uuid: String) = Action {
    val playerID = UUID.randomUUID().toString
    if(ActiveGameStore.has(uuid)) {
      Ok(views.html.game(uuid, playerID))
    }else{
      Ok(views.html.error("The requested game does not exist, please create a:"))
    }
  }

  /** Create websocket for game: uuid */
  def socket (uuid: String, playerID: String) = WebSocket.acceptWithActor[JsValue, JsValue] {
    request => out =>
      ActiveGameStore.add(uuid, ActiveGameStore.getActiveGame(uuid).addPlayer(new Player(playerID, out)))
      ChessWebSocketActor.props(out, playerID , uuid)
  }

  /** Return a list of all game instances */
  def gameList = Action(Ok(views.html.gameList(GameDB.getGameList())))
}
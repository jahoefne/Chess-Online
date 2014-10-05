package controllers

import controller.GameController
import model._
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.Play.current

object Application extends Controller {

  /** Landing Page */
  def index = Action {
    Ok(views.html.index())
  }

  /** Create a new game instance */
  def newGame = Action {
    val gameUUID = java.util.UUID.randomUUID.toString
    ActiveGameStore.add(gameUUID, ActiveGame(gameUUID, new GameController(), null, null, List()))
    Redirect(routes.Application.game(gameUUID))
  }

  /** Access existing game instance */
  def game(uuid: String) = Action {
    val playerID = java.util.UUID.randomUUID().toString
    Ok(views.html.game(uuid, playerID))
  }

  /** Create websocket for game: uuid */
  def socket (uuid: String, playerID: String) = WebSocket.acceptWithActor[JsValue, JsValue] {
    request => out =>
      val activeGame =  ActiveGameStore.getActiveGame(uuid).addPlayer(new Player(java.util.UUID.fromString(playerID), out))
      ActiveGameStore.add(uuid, activeGame)
      ChessWebSocketActor.props(out, java.util.UUID.fromString(playerID) , uuid)
  }
}
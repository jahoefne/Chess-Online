package controllers

import controller.GameController
import model.{GameState, GameRepository}
import org.json4s._
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.Play.current

object Application extends Controller {

  /**
   * Landing Page
   * @return
   */
  def index = Action {
    Ok(views.html.index())
  }

  /**
   * Create a new game instance
   * @return
   */
  def newGame = Action {
    val uuid = java.util.UUID.randomUUID.toString
    val dbResponse = GameRepository.setGame(uuid, new GameController())
    dbResponse match {
      case ex: Exception => InternalServerError
      case _ => Redirect(routes.Application.game(uuid))
    }
  }

  /**
   * Access existing game instance
   * @param uuid
   * @return
   */
  def game(uuid: String) = Action {
    Ok(views.html.game(uuid))
  }

  /**
   * WebSocket for controlling the game
   */
  def socket (uuid: String) = WebSocket.acceptWithActor[JsValue, JsValue] {
    request =>
      out => {
        WebSocketStore.Store.add(uuid, out)
        WebSocketStore.Socket.ChessWebSocketActor.props(out)
      }
  }
}
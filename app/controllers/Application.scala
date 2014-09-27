package controllers

import controller.GameController
import model.{GameState, GameRepository}
import play.api.mvc._

object Application extends Controller {

/*  def socket = WebSocket.acceptWithActor[String, String] { request => out =>
    WebSocketActor.props(out)
  }*/

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
   * Websocket for controlling the game
   */
  def socket(uuid: String) = {

  }
}
package controllers

import model.{Game, GameDatabase}
import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  /**
   * Create a new game instance
   * @return
   */
  def newGame = Action {
    val uuid = java.util.UUID.randomUUID.toString

    GameDatabase.addGame(Game(uuid)) match {
      case game: Game => Redirect(routes.Application.game(uuid))
      case _ => InternalServerError
    }
  }

  /**
   * Access existing game instance
   * @param uuid
   * @return
   */
  def game(uuid: String) = Action {
      Ok(views.html.game())
  }

}
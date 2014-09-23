package controllers

import model.{Game, GameDatabase}
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def newGame = Action {
    val uuid = java.util.UUID.randomUUID.toString

    GameDatabase.addGame(Game(uuid)) match {
      case game: Game =>  // render new game view
      case _ => InternalServerError
    }
    Ok(views.html.index())
  }




}
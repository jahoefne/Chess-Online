package controllers

import com.mongodb.WriteResult
import play.api.libs.EventSource
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.Play.current
import model._
import securesocial.core._
import play.api.mvc.Action
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Concurrent, Enumeratee}

object UUID{def uuid = (Random.alphanumeric take  8).mkString}

class Application(override implicit val env: RuntimeEnvironment[User])
  extends securesocial.core.SecureSocial[User] {

  /** Landing Page */
  def index = UserAwareAction{ implicit request =>
    Ok(views.html.index(request.user))
  }

  /** Create a new game instance */
  def newGame =  UserAwareAction { implicit request =>
    val gameUUID = UUID.uuid
    GameDB.saveGame(ActiveGame(gameUUID))
    Redirect(routes.Application.game(gameUUID))
  }

  /** Delete existing game **/
  def deleteGame(uuid: String) = SecuredAction { implicit request =>
    GameDB.deleteGame(uuid)
    Ok
  }

  /** Access existing game instance
    * Check if the connection comes from a registered user, if so use
    * it's uuid as playerId, otherwise use a random id */
  def game(uuid: String) = UserAwareAction {
    implicit request =>
      GameDB.doesGameExistWith(uuid) match {
        case true =>
          val playerId = request.user match{
            case Some(user) => user.uuid
            case None => UUID.uuid
          }
          Ok(views.html.game(gameUUID = uuid, playerUUID = playerId, request.user))
        case false =>
          Ok(views.html.error("The requested game does not exist, please create a:"))
      }
  }

  /** Create websocket for game: uuid */
  def socket (uuid: String, playerID: String) = WebSocket.acceptWithActor[JsValue, JsValue] {
    request => out =>
      ChessWebSocketActor.props(out = out, playerID = playerID , gameID = uuid)
  }

  /** Return a list of all game instances */
  def gameList =  SecuredAction { implicit request =>
    Ok(views.html.gameList(gameList = GameDB.list, Some(request.user)))
  }

  /** Render Login Container */
  def login = UserAwareAction { implicit request =>
    Ok(views.html.loginContainer(request.user))
  }
}
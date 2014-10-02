package controllers

import java.util.UUID

import akka.actor.{Actor, Props, ActorRef}
import model.ActiveGameStore
import play.api.libs.json._

/**
 * Defines the WebSocketActor + Companion
 */
class ChessWebSocketActor(out: ActorRef,
                          playerID: UUID,
                          gameID: String) extends Actor {

  def receive = {
    case msg: JsValue => (msg \ "type").as[String] match {

      /** Return current game state */
      case "GetGame" => out ! ActiveGameStore.getActiveGame(gameID).toJson

      /** Return the role of the player: white, black, spectator */
      case "GetRole" => out ! Json.obj(
        "type" -> "Role",
        "role" -> ActiveGameStore.getActiveGame(gameID).getRole(playerID))

      /** Move figure at src to tgt */
      case "Move" => val uuid = (msg \ "uuid").as[String]

      /** Get possible moves for a field x */
      case "PossibleMoves" => println("PossibleMoves")

      case _ => out ! "Unknown Json"
    }

    case _ => out ! "No Message Type supplied!"
  }


  /**
   * Socket was closed from the client
   */
  override def postStop() = {
     println("Websocket closed from client")
    // TODO: remove player from activeGame
  }
}

object ChessWebSocketActor {
  def props(out: ActorRef, playerID: UUID, gameID: String) =
    Props(new ChessWebSocketActor(out, playerID, gameID))
}

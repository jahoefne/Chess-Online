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
      /**
       * Return the gameState as Json
       */
      case "GetGame" =>
        println("GetGame")
        val uuid = (msg \ "uuid").as[String]
        val activeGame = ActiveGameStore.getActiveGame(uuid)
        out !  Json.toJson(activeGame)

      case "Move" =>
        println("Move")
        val uuid = (msg \ "uuid").as[String]



      case _ => out ! "Error parsing incoming Json"
    }

    case _ => out ! "No Message Type supplied!"
  }
}

object ChessWebSocketActor {
  def props(out: ActorRef, playerID: UUID, gameID: String) =
    Props(new ChessWebSocketActor(out, playerID, gameID))
}

package controllers

import java.awt.Point
import akka.actor.{Actor, Props, ActorRef}
import model.ActiveGameStore
import play.api.Logger
import play.api.libs.json._

/**
 * Defines the WebSocketActor + Companion
 */
class ChessWebSocketActor(out: ActorRef,
                          playerID: String,
                          gameID: String) extends Actor {
  val log = Logger(this getClass() getName())

  def receive = {
    case msg: JsValue => (msg \ "type").as[String] match {

      /** Return current game state */
      case "GetGame" => out ! ActiveGameStore.getActiveGame(gameID).toJson

      /** Return the role of the player: white, black, spectator */
      case "GetRole" => out ! Json.obj(
        "type" -> "Role",
        "role" -> ActiveGameStore.getActiveGame(gameID).getRole(playerID))

      /** Move figure at src to tgt */
      case "Move" =>
        val srcX = (msg \ "srcX").as[Int]
        val srcY = (msg \ "srcY").as[Int]
        val dstX = (msg \ "dstX").as[Int]
        val dstY = (msg \ "dstY").as[Int]
        log.info("Move "+srcX +":"+srcY+" -> "+dstX+":"+dstY)
        ActiveGameStore.getActiveGame(gameID).move(new Point(srcX,srcY), new Point(dstX,dstY))


      /** Get possible moves for a field x */
      case "PossibleMoves" =>
        log.info("PossibleMoves")
        val x = (msg \ "x").as[Int]
        val y = (msg \ "y").as[Int]
        println(x+":"+y)
        val moves =
          for(p: Point <- ActiveGameStore.getActiveGame(gameID).getPossibleMoves(new Point(x,y)))
          yield Array[Int](p.x,p.y)

        println(moves.toString)

        out ! Json.obj(
          "type" -> "PossibleMoves",
          "moves" -> moves
        )

      case _ => out ! "Unknown Json"
    }

    case _ => out ! "No Message Type supplied!"
  }


  /**
   * Socket was closed from the client
   */
  override def postStop() = {
    log.info("Websocket closed from client")
    ActiveGameStore.add(
      gameID,
      ActiveGameStore.getActiveGame(gameID).removePlayer(playerID))
    ActiveGameStore.getActiveGame(gameID).broadCastMsg(ActiveGameStore.getActiveGame(gameID).toJson)
  }
}

object ChessWebSocketActor {
  def props(out: ActorRef, playerID: String, gameID: String) =
    Props(new ChessWebSocketActor(out, playerID, gameID))
}

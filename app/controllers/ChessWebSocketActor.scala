package controllers

import java.awt.Point
import akka.actor.{Actor, Props, ActorRef}
import model.{UserRefs, GameDB}
import play.api.Logger
import play.api.libs.json._

/** Defines the WebSocketActor + Companion */
class ChessWebSocketActor(out: ActorRef,
                          playerID: String,
                          gameID: String) extends Actor {

  val log = Logger(this getClass() getName())
  def receive = {
    case msg: JsValue => (msg \ "type").as[String] match {

      case "GetGame" =>
        out ! GameDB.load(gameID)

      case "Move" =>
        val src = new Point((msg \ "srcX").as[Int], (msg \ "srcY").as[Int])
        val dst = new Point((msg \ "dstX").as[Int],  (msg \ "dstY").as[Int])
        GameDB.load(gameID).move(src, dst)

      case "PossibleMoves" =>
        val src = new Point((msg \ "x").as[Int], (msg \ "y").as[Int])
        val moves = for(p: Point <- GameDB.load(gameID).getPossibleMoves(src)) yield Array[Int](p.x,p.y)
        out ! Json.obj( "type" -> "PossibleMoves", "moves" -> moves)

      case "WhitePlayer" => GameDB.load(gameID).setWhite(Some(playerID))
      case "BlackPlayer" => GameDB.load(gameID).setBlack(Some(playerID))
      case "Spectator" => GameDB.load(gameID).joinSpec(Some(playerID))

      case _ => out ! "Unknown Json"
    }

    case _ => out ! "No Message Type supplied!"
  }

  /** Socket was closed from the client */
  override def postStop() = {
   // TODO: REMOVE
   // ActiveGameStore.add(gameID, ActiveGameStore.getActiveGame(gameID).removePlayer(playerID))
    UserRefs.broadCastMsg(gameID, GameDB.load(gameID).asInstanceOf[JsValue])
  }
}

/** WS-Companion - for props */
object ChessWebSocketActor {
  def props(out: ActorRef, playerID: String, gameID: String) =
    Props(new ChessWebSocketActor(out, playerID, gameID))
}

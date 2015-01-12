package controllers

import java.awt.Point
import akka.actor.{Actor, Props, ActorRef}
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.{SubscribeAck, Publish, Subscribe}
import model.GameDB
import play.api.Logger
import play.api.libs.json._

/** Defines the WebSocketActor + Companion */
class ChessWebSocketActor(out: ActorRef, playerID: String, gameID: String) extends Actor {

  val log = Logger(this getClass() getName())
  val mediator = DistributedPubSubExtension(context.system).mediator
  mediator ! Subscribe(gameID, self)

  def receive = {
    case msg: JsValue => (msg \ "type").as[String] match {

      case "GetGame" =>
        out ! GameDB.loadGameWith(gameID).getAsJson

      case "PossibleMoves" =>
        val src = new Point((msg \ "x").as[Int], (msg \ "y").as[Int])
        val moves = Array[Array[Int]](Array[Int](src.x, src.y)).++(for(p: Point <- GameDB loadGameWith gameID getPossibleMoves src) yield Array[Int](p.x,p.y))
        out ! Json.obj( "type" -> "PossibleMoves", "moves" -> moves)

      case "ActiveGame" =>  out ! msg

      /** Mediator routed messages below */
      case "Move" =>
        val src = new Point((msg \ "srcX").as[Int], (msg \ "srcY").as[Int])
        val dst = new Point((msg \ "dstX").as[Int],  (msg \ "dstY").as[Int])
        mediator ! Publish(gameID, GameDB.loadGameWith(gameID).moveFromTo(src, dst).saveGame.getAsJson)

      case "WhitePlayer" =>
        mediator ! Publish(gameID, GameDB.loadGameWith(gameID).setWhitePlayer(Some(playerID)).saveGame.getAsJson)

      case "BlackPlayer" =>
        mediator ! Publish(gameID, GameDB.loadGameWith(gameID).setBlackPlayer(Some(playerID)).saveGame.getAsJson)

      case "Spectator" =>
        mediator ! Publish(gameID, GameDB.loadGameWith(gameID).movePlayerToSpec(Some(playerID)).saveGame.getAsJson)

      case "PlayerInfo" =>
        log.error("This is not implemented yet!")

      case "chatMessageClient" =>
        val updatedJson = msg.as[JsObject] ++ Json.obj("type" -> "chatMessage")
        mediator ! Publish(gameID, updatedJson)

      case "chatMessage" => out ! msg

      case _ => log.error("Unknown Json")
    }

    case mediatorAck: SubscribeAck => log.debug("Subscribed to messages for game instance")

    case _ => log.error("No type supplied")
  }

  /** Socket was closed from the client */
  override def postStop() = {
    mediator ! (gameID, GameDB.loadGameWith(gameID).movePlayerToSpec(Some(playerID)).saveGame.getAsJson)
  }
}

/** WS-Companion - for props */
object ChessWebSocketActor {
  def props(out: ActorRef, playerID: String, gameID: String) =
    Props(new ChessWebSocketActor(out, playerID, gameID))
}

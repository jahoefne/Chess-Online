package controllers

import akka.actor.{Actor, Props, ActorRef}
import controller.GameController
import model.{GameState, GameRepository}
import play.api.libs.json._
import scala.collection.mutable
/**
 * User: Jan Moritz Hoefner
 * Date: 28/09/14
 * Time: 12:17
 */
object WebSocketController {

  /*  def sendMessage(uuid: String) ={
      Store.getSocket(uuid) ! "Hello!"
    }
  */
  /**
   * Defines the WebsocketActor + Companion
   */
  object Socket {
    class ChessWebSocketActor(out: ActorRef) extends Actor {

      def receive = {
        case msg: JsValue => (msg \ "type").as[String] match {
          /**
           * Return the gameState as Json
           */
          case "GetGame" =>  {
            println("GetGame")
            val uuid = (msg \ "uuid").as[String]
            val controller = GameRepository.getGame(uuid)
            val gameState = GameState.fromGameController(controller, uuid)
            out !  Json.toJson(gameState)
          }

          case _ => out ! "Error parsing incoming Json"
        }

        case _ => out ! "No Message Type supplied!"
      }
    }

    object ChessWebSocketActor {
      def props(out: ActorRef) = Props(new ChessWebSocketActor(out))
    }
  }
}
package controllers

import akka.actor.{Actor, Props, ActorRef}
import model.{GameState, GameRepository}
import play.api.libs.json._
import scala.collection.mutable
/**
 * User: Jan Moritz Hoefner
 * Date: 28/09/14
 * Time: 12:17
 */
object WebSocketStore {

  /**
   * Keeps a Map of UUID -> ActorRef in the RAM
   */
  object Store {
    var map = new mutable.HashMap[String, ActorRef]
    def has(uuid: String) : Boolean = map contains uuid
    def getSocket(uuid: String) : ActorRef = map.get(uuid).get
    def add(uuid: String, ref: ActorRef) = map += (uuid -> ref)
    def remove(uuid: String) = map -= uuid
  }

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
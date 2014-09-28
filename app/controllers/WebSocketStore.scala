package controllers

import akka.actor.{Actor, Props, ActorRef}
import play.api.libs.json.JsValue

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

  def sendMessage(uuid: String) ={
     Store.getSocket(uuid) ! "Hello!"
  }

  /**
   * Defines the WebsocketActor + Companion
   */
  object Socket {
    class ChessWebSocketActor(out: ActorRef) extends Actor {
      def receive = {
        case msg: String =>  out ! ("I received your message: " + msg)
        case msg: JsValue =>
          out ! ("I received your message: " + msg)
      }
    }
    object ChessWebSocketActor {
      def props(out: ActorRef) = Props(new ChessWebSocketActor(out))
    }

  }

}

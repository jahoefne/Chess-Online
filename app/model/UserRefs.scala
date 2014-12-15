package model

import akka.actor.ActorRef
import play.api.libs.json.JsValue

import scala.collection.mutable

/** Represents a player */
case class Player(uuid: String, out: ActorRef)

/** Holds a map of Game UUIDs -> Akka ActorRefs in order to broadcast messages to all users of a game instance*/
object UserRefs{
  var map = new mutable.HashMap[String, mutable.ArrayBuffer[Player]]

  def addPlayer(uuid:String, player:Player) = { map.getOrElseUpdate(uuid, mutable.ArrayBuffer[Player]()) += player }

  def broadCastMsg(uuid: String, msg: JsValue) = {
    for (player <- map.getOrElseUpdate(uuid, mutable.ArrayBuffer[Player]())) player.out ! msg
    true
  }

 // def removePlayer(uuid:String, player:Player) = { map}
}

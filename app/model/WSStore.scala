package model

import akka.actor.ActorRef
import controller.GameController

import scala.collection.mutable

/**
 * User: Jan Moritz Hoefner
 * Date: 01/10/14
 * Time: 10:29
 */
object WSStore {
    /**
     * Represents an active game, consists of two players,
     * n spectators, and a controller
     */
    case class ActiveGame( player1: ActorRef,
                           player2: ActorRef,
                           spectators: List[ActorRef],
                           controller: GameController)

    var map = new mutable.HashMap[String, ActorRef]
    def has(uuid: String) : Boolean = map contains uuid
    def getSocket(uuid: String) : ActorRef = map.get(uuid).get
    def add(uuid: String, ref: ActorRef) = map += (uuid -> ref)
    def remove(uuid: String) = map -= uuid
}

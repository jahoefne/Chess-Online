package model

import scala.collection.mutable

/**
 * Contains a Map of UUID -> ActiveGame
 */
object ActiveGameStore {
  var map = new mutable.HashMap[String, ActiveGame]
  def has(uuid: String) : Boolean = map contains uuid
  def getActiveGame(uuid: String) : ActiveGame = map.get(uuid).get
  def add(uuid: String, ref: ActiveGame) = map += (uuid -> ref)
  def remove(uuid: String) = map -= uuid
}

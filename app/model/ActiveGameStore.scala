package model

import scala.collection.mutable

/**
 * Contains a Map of UUID -> ActiveGame
 * to make ActiveGame instances accessible for new users by uuid
 */
object ActiveGameStore {
  var map = new mutable.HashMap[String, ActiveGame]

  def has(uuid: String) : Boolean = map.contains(uuid) || GameDB.exists(uuid)

  def getActiveGame(uuid: String) : ActiveGame = {
    if(map.contains(uuid)) {
      map.get(uuid).get
    }else{
      val game = GameDB.loadGame(uuid)
      this.add(uuid, game)
      game
    }
  }

  def add(uuid: String, ref: ActiveGame) = {
    GameDB.saveGame(uuid, ref)
    map += (uuid -> ref)
  }

  def remove(uuid: String) = map -= uuid
}

package model

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import controller.GameController
import play.api.Logger
import play.api.Play.current

import scala.collection.mutable

/**
 * Contains a Cache and a Connection to the MongoDB
 * Responsible for persisting GameState and keeping the
 * database-load low
 */
object DB {
    val mongoClient = MongoClient("127.0.0.1", 27017)
    val db = mongoClient("Chess-Online")
    val collection = db("Games")

    /**
     * Loads a GameState, based on a uuid from the Database
     */
    def loadGame(uuid: String): GameController = {
      val dbObj = new MongoDBObject(collection.findOne(MongoDBObject("uuid" -> uuid)).get)
      val controller = GameState.toGameController(dbObj)
      Cache addToCache(uuid, controller)
      controller
    }

    /**
     * Saves a game state in the Database, updates if the game existed already,
     * inserts if it did not exist.
     */
    def saveGame(uuid: String): Object = {
      try {
        val query = MongoDBObject("uuid" -> uuid)
        val state = GameState.fromGameController(Cache.getController(uuid), uuid)
        collection.update(query, state, upsert = true)
      } catch {
        case ex: Exception =>
          log.error(ex.toString)
          ex
      }
    }
  }
}

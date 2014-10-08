package model

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import play.api.Logger

/**
 * Connection to the MongoDB stores GameStateObject
 **/
object GameDB {
  val mongoClient = MongoClient("127.0.0.1", 27017)
  val db = mongoClient("Chess-Online")
  val collection = db("Games")

  val log = Logger(this getClass() getName())

  /**
   * Loads a ActiveGame, based on a uuid from the Database
   */
  def loadGame(uuid: String): ActiveGame = {
    val dbObj = new MongoDBObject(collection.findOne(MongoDBObject("uuid" -> uuid)).get)
    ActiveGame.toActiveGame(dbObj)
  }

  def exists(uuid: String): Boolean = {
    try {
      val dbObj = new MongoDBObject(collection.findOne(MongoDBObject("uuid" -> uuid)).get)
      true
    }catch{
      case ex: Exception =>
        false
    }
  }

  /**
   * Saves an ActiveGame in the Database, updates if the game existed already,
   * inserts if it did not exist.
   */
  def saveGame(uuid: String, state: ActiveGame): Object = {
    try {
      val query = MongoDBObject("uuid" -> uuid)
      collection.update(query, state, upsert = true)
    } catch {
      case ex: Exception =>
        log.error(ex.toString)
        ex
    }
  }
}

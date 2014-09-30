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
object GameRepository {

  val log = Logger(this getClass() getName())

  /**
   * Stores a UUID->GameController Mapping in RAM
   */
  object Cache {
    var map = new mutable.HashMap[String, GameController]
    def isInCache(uuid: String) : Boolean = false//map contains uuid
    def getController(uuid: String) : GameController = map.get(uuid).get
    def addToCache(uuid: String, controller: GameController) = map += (uuid -> controller)
    def removeFromCache(uuid: String) = map -= uuid
  }


  /**
   * Loads the game from Cache or if necessary from DB
   */
  def getGame(uuid: String) : GameController = Cache.isInCache(uuid) match {
    case true => Cache.getController(uuid)
    case _ => DB.loadGame(uuid)
  }

  /**
   * Save the game State to Database and update Cache
   */
  def setGame(uuid: String, controller: GameController) = {
    Cache.addToCache(uuid, controller)
    DB.saveGame(uuid)
  }

  def removeFromCache(uuid: String) = Cache.removeFromCache(uuid)


  /**
   * Connection to the Database
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

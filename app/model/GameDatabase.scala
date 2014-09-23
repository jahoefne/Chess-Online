package model

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject

import com.novus.salat._
import com.novus.salat.global._
import play.api.Play.current


/**
 * User: Jan Moritz Hoefner
 * Date: 23/09/14
 * Time: 12:47
 */

object GameDatabase {
  val mongoClient =  MongoClient("127.0.0.1", 27017)

  val db = mongoClient("Chess-Online")
  val collection = db("Games")


  /**
   * Custom Context to make Salat work
   */
  implicit val ctx = new Context {
    val name = "PlaySalatContext"
  }
  ctx.registerClassLoader(current.classloader)


  /**
   * Return an existing game object.
   * Load from DB if not in RAM anymore
   * @param uuid
   * @return
   */
  def getGame(uuid: String) : Game = {
     val game = grater[Game].asObject(
       collection.findOne(MongoDBObject("uuid" -> uuid))
     )
     game
  }


  /**
   * Store a new Game in the mongodb
   * @param game
   * @return
   */
  def addGame(game: Game): Object = {
    try {
     collection.insert(
       grater[Game].asDBObject(game)
     )
    }catch{
      case ex: Exception => ex
    }
    game
  }
}

package model

import com.mongodb.casbah.MongoClient

/**
 * User: Jan Moritz Hoefner
 * Date: 23/09/14
 * Time: 12:47
 */
object GameDatabase {
  val mongoClient =  MongoClient("127.0.0.1", 27017)

  val db = mongoClient("Chess-Online")
  val collection = db("Games")

  def addGame(game: Game): Object ={
    try {
      collection.insert(game)
    }catch{
      case ex: Exception => ex
    }
    game
  }
}

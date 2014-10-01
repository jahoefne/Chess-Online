package model
/*
Database currently not enabled

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import controller.GameController
import play.api.Logger
import play.api.Play.current

import scala.collection.mutable   */

///**
// * Connection to the MongoDB stores GameStateObject
// **/
//object GameDB {
//    val mongoClient = MongoClient("127.0.0.1", 27017)
//    val db = mongoClient("Chess-Online")
//    val collection = db("Games")
//
//    val log = Logger(this getClass() getName())
//
//    /**
//     * Loads a GameState, based on a uuid from the Database
//     */
//    def loadGame(uuid: String): GameState = {
//      val dbObj = new MongoDBObject(collection.findOne(MongoDBObject("uuid" -> uuid)).get)
//      val controller = GameState.toGameController(dbObj)
//      controller
//    }
//
//    /**
//     * Saves a game state in the Database, updates if the game existed already,
//     * inserts if it did not exist.
//     */
//    def saveGame(uuid: String, state: GameState): Object = {
//      try {
//        val query = MongoDBObject("uuid" -> uuid)
//        collection.update(query, state, upsert = true)
//      } catch {
//        case ex: Exception =>
//          log.error(ex.toString)
//          ex
//      }
//    }
//  }
//}

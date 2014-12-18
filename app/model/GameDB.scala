package model

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import com.mongodb.{BasicDBList, DBObject}
import controller.GameController
import play.api.Logger
import play.api.libs.json.JsValue


/** Connection to the MongoDB stores ActivaGames Objects **/
object GameDB {
  val conn = MongoClient("127.0.0.1", 27017)
  val db = conn("Chess-Online")
  val coll = db("Games")
  val users = db("Users")

  val log = Logger(this getClass() getName())

  /** Loads game with uuid from the Database */
  def load(uuid: String) : ActiveGame = new MongoDBObject(coll.findOne(MongoDBObject("uuid" -> uuid)).get)

  /** returns true if a game with uuid exists */
  def exists(uuid: String) = coll.exists((q: DBObject) => new MongoDBObject(q).uuid equals uuid)

  /** Saves an ActiveGame in the Database */
  def save(state: ActiveGame) = coll.update(MongoDBObject("uuid" -> state.uuid), state, upsert = true)

  /** Return a list of all uuids present in the database */
  def list: List[ActiveGame] = for (obj <- coll.find().toList) yield MongoDBObjectToActiveGame(new MongoDBObject(obj))

  def userInfo(uuid: String)  = {
   /* users.findOne(MongoDBObject("uuid" -> uuid)) match {
      case Some(dbObj) => println("We found a user")
      case _ => println("No user for uuid: "+uuid)
    }*/
  }

  /**
   * Implicit converters, because neither Lift not Salat convert Pojo-GameController without pain
   */
  implicit def ActiveGameToMongoDBObject(s: ActiveGame): DBObject =
    MongoDBObject(
      "uuid" -> s.uuid,
      "whitePlayer" -> s.players._1.getOrElse(""),
      "blackPlayer" -> s.players._2.getOrElse(""),
      "field" -> s.getField.getField,
      "check" -> s.getCheck,
      "whiteOrBlack" -> s.getField.getWhiteOrBlack,
      "gameOver" -> s.isGameOver
    )

  implicit def MongoDBObjectToActiveGame(in: MongoDBObject): ActiveGame =  {
    val c = new GameController(
      in.as[Boolean]("gameOver"),
      in.as[Boolean]("check"),
      new Field(
        for (e <- in.as[MongoDBList]("field").toArray)
        yield for (x <- e.asInstanceOf[BasicDBList].toArray) yield x.asInstanceOf[Int],
        in.as[Int]("whiteOrBlack").asInstanceOf[Byte]
      )
    )
    val players = ( Some(in.as[String]("whitePlayer")), Some(in.as[String]("blackPlayer")))
    ActiveGame(in.as[String]("uuid"), Some(c), players)
  }
}

package model

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import com.mongodb.{BasicDBList, DBObject}
import controller.GameController
import org.joda.time.format.DateTimeFormat
import play.api.Logger
import play.api.libs.json.{JsValue, Json}


/** Connection to the MongoDB stores ActivaGames Objects **/
object GameDB {
  val conn = MongoClient("127.0.0.1", 27017)
  val db = conn("Chess-Online")
  val coll = db("Games")
  val users = db("Users")
  val chat = db("Chat")
  val formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")

  val log = Logger(this getClass() getName())

  /** Loads game with uuid from the Database */
  def loadGameWith(uuid: String) : ActiveGame =
    new MongoDBObject(coll.findOne(MongoDBObject("uuid" -> uuid)).get)

  /** returns true if a game with uuid exists */
  def doesGameExistWith(uuid: String) =
    coll.exists((q: DBObject) => new MongoDBObject(q).uuid equals uuid)

  /** Saves an ActiveGame in the Database */
  def saveGame(state: ActiveGame) = {
    coll.update(MongoDBObject("uuid" -> state.uuid), state, upsert = true)
    state
  }

  /** Appends the chat-msg msg to the chat history */
  def appendChatMessage(uuid: String, msg: JsValue) : JsValue= {
    chat.insert(MongoDBObject("uuid" -> uuid, "msg" -> msg.toString()))
    msg
  }

  def getChatMessages(uuid: String) : Array[JsValue]  = {
   chat.find(MongoDBObject("uuid" -> uuid)).map(msg => Json.parse( msg.get("msg").toString)).toArray[JsValue]
  }

  /** Delete the game with uuid from the database */
  def deleteGame(uuid: String) = coll.remove(MongoDBObject("uuid" -> uuid))

  /** Return a list of all games created by uuid */
  def list(uuid: String): List[ActiveGame] =
    for (obj <- coll.find(MongoDBObject("createdBy" -> uuid)).toList)
    yield MongoDBObjectToActiveGame(new MongoDBObject(obj))


  /** Implicit converters, because neither Lift nor Salat convert Pojo-GameController without pain */
  implicit def ActiveGameToMongoDBObject(s: ActiveGame): DBObject =
    MongoDBObject(
      "uuid" -> s.uuid,
      "createdOn" -> s.createdOn.toString("dd/MM/yyyy HH:mm:ss"),
      "createdBy" -> s.createdBy,
      "whitePlayer" -> s.players._1.getOrElse("None"),
      "blackPlayer" -> s.players._2.getOrElse("None"),
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
    val players = (
      in.as[String]("whitePlayer") match {
        case "None" => None
        case uuid => Some(uuid)
      },
      in.as[String]("blackPlayer") match {
        case "None" => None
        case uuid => Some(uuid)
      }
    )
    ActiveGame(
      in.as[String]("uuid"),
      Some(c),
      formatter.parseDateTime(in.as[String]("createdOn")),
      in.as[String]("createdBy"),
      players)
  }
}

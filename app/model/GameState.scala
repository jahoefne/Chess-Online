package model

import com.mongodb.{BasicDBList, DBObject}
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import controller.GameController
import play.api.libs.json._
import play.api.libs.functional.syntax._
/**
 * Represents the state of a chess game, can be stored in Database.
 * Can be used to create GameControllers.
 */
case class GameState(uuid: String,
                     field: Array[Array[Int]],
                     check: Boolean,
                     whiteOrBlack: Byte,
                     gameOver: Boolean)

object GameState {
  /**
   * Extract game state out of a GameController
   */
  def fromGameController(c: GameController, uuid: String): GameState = new GameState(
    uuid,
    c.getField.getField,
    c.getCheck,
    c.getField.getWhiteOrBlack,
    c.isGameOver)

  /**
   * Convert a GameState to a GameController
   */
  def toGameController(state: GameState): GameController = new GameController(
    state.gameOver,
    state.check,
    new Field(state.field, state.whiteOrBlack))

  /**
   * Convert GameState to Json
   */
  implicit val gameStateWrites = new Writes[GameState] {
    def writes(c: GameState): JsValue = {
      Json.obj(
        "uuid" -> c.uuid,
        "field" -> c.field,
        "check" -> c.check,
        "whiteOrBlack" -> c.whiteOrBlack.toInt,
        "gameOver" -> c.gameOver
      )
    }
  }

  implicit def toDBObject(s: GameState) = MongoDBObject(
    "uuid" -> s.uuid,
    "field" -> s.field,
    "check" -> s.check,
    "whiteOrBlack" -> s.whiteOrBlack,
    "gameOver" -> s.gameOver
  )

  implicit def toGameState(in: MongoDBObject) : GameState = {
      val arr : Array[Array[Int]] =
        for( e <- in.as[MongoDBList]("field").toArray)
          yield for (x <- e.asInstanceOf[BasicDBList].toArray) yield x.asInstanceOf[Int]
    GameState(
      in.as[String]("uuid"),
      arr,
      in.as[Boolean]("check"),
      in.as[Int]("whiteOrBlack").asInstanceOf[Byte],
      in.as[Boolean]("gameOver")
    )
  }
}
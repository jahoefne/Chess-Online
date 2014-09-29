package model

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

object GameState{


  /**
   * Extract game state out of a GameController
   */
  def fromGameController(c: GameController, uuid: String) : GameState = new GameState(
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
}
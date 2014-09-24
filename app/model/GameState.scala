package model

import controller.GameController

/**
 * Represents the state of a chess game, can be stored in Database.
 * Can be used to create GameControllers.
 */
case class GameState(uuid: String,
                     field: Array[Array[Byte]],
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
}
package model

import java.awt.Point
import controller.GameController
import play.api.libs.json.{Json, JsValue}


/** Represents an active game instance */
case class ActiveGame( uuid: String,
                       cont: Option[GameController] = Some(new GameController()),
                       var players:(Option[String], Option[String]) = (None, None))
  extends GameController(
    cont.get.isGameOver,
    cont.get.getCheck,
    cont.get.getField.asInstanceOf[Field]) {

  /** move figure from src to dst and persist movement in gamedb */
  override def move(src: Point, dst: Point) = super.move(src, dst) && broadCastAndSave

  /** Sets the white player */
  def setWhite(p: Option[String]) = {
    joinSpec(p)
    players = players.copy(_1 = p)
    broadCastAndSave
  }

  /** Sets the black player */
  def setBlack(p: Option[String]) = {
    joinSpec(p)
    players = players.copy(_2 = p)
    broadCastAndSave
  }

  /** Joins spec */
  def joinSpec(p: Option[String]) = {
    if (players._1 == p) players = players.copy(_1 = None)
    if (players._2 == p) players = players.copy(_2 = None)
    broadCastAndSave
  }

  /** Broadcast the current game state to all users and persist in db */
  def broadCastAndSave = {
    UserRefs.broadCastMsg(this.uuid, this)
    GameDB.save(this)
    true
  }

  /** Converts to Json for sending to the users */
  implicit def toJson(game: ActiveGame): JsValue = Json.obj(
    "type" -> "ActiveGame",
    "uuid" -> game.uuid,
    "field" -> game.getField.getField,
    "check" -> game.getCheck,
    "white" -> game.players._1.getOrElse("").toString,
    "black" -> game.players._2.getOrElse("").toString,
    "whiteOrBlack" -> game.getField.getWhiteOrBlack.toInt,
    "gameOver" -> game.isGameOver
  )
}
package model

import java.awt.Point
import controller.GameController
import play.api.libs.json.{Json, JsValue}


/** Represents an active game instance */
case class ActiveGame( uuid: String,
                       cont: Option[GameController] = Some(new GameController()),
                       var players:(Option[String], Option[String]) = (None, None))
  extends GameController(cont.get.isGameOver, cont.get.getCheck, cont.get.getField.asInstanceOf[Field]) {

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

  def broadCastAndSave = {
    UserRefs.broadCastMsg(this.uuid, this.toJson)
    GameDB.save(this)
    true
  }

  /** Converts to Json for sending to the users */
  def toJson: JsValue = Json.obj(
    "type" -> "ActiveGame",
    "uuid" -> this.uuid,
    "field" -> this.getField.getField,
    "check" -> this.getCheck,
    "white" -> players._1.getOrElse("").toString,
    "black" -> players._2.getOrElse("").toString,
    "whiteOrBlack" -> this.getField.getWhiteOrBlack.toInt,
    "gameOver" -> this.isGameOver
  )
}
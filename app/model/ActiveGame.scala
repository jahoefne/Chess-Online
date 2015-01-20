package model

import java.awt.Point
import controller.GameController
import controllers.DBUserService
import org.joda.time.DateTime
import play.api.libs.json.{Json, JsValue}


/** Represents an active game instance */
case class ActiveGame( uuid: String,
                       cont: Option[GameController] = Some(new GameController()),
                       createdOn: DateTime = DateTime.now,
                       createdBy: String,
                       players:(Option[String], Option[String]) = (None, None))
  extends GameController(
    cont.get.isGameOver,
    cont.get.getCheck,
    cont.get.getField.asInstanceOf[Field]) {

  /** move figure from src to dst and persist movement in gamedb */
  def moveFromTo(src: Point, dst: Point): ActiveGame = {
    super.move(src, dst)
    this
  }

  /** Sets the white player */
  def setWhitePlayer(player: Option[String]) : ActiveGame = {
    val specd = this movePlayerToSpec player
    specd copy (players = specd.players.copy(_1 = player))
  }

  /** Sets the black player */
  def setBlackPlayer(player: Option[String]) : ActiveGame = {
    val specd = this movePlayerToSpec player
    specd copy (players = specd.players.copy(_2 = player))
  }

  /** Joins spec */
  def movePlayerToSpec(p: Option[String]) : ActiveGame = {
    if (players._1 == p) this.copy(players = players.copy(_1 = None))
    else if (players._2 == p) this.copy(players = players.copy(_2 = None))
    else this
  }

  /** Persist in db */
  def saveGame = GameDB saveGame this

  /** Converts ActiveGame to Json for sending to the users */
  def getAsJson : JsValue = Json.obj(
    "type" -> "ActiveGame",
    "uuid" -> this.uuid,
    "field" -> this.getField.getField,
    "check" -> this.getCheck,
    "white" -> this.players._1.getOrElse("").toString,
    "whiteName" -> DBUserService.findNameByUuid(this.players._1),
    "whitePic" -> DBUserService.findPicByUuid(this.players._1).getOrElse("/assets/images/white.png").toString,
    "black" -> this.players._2.getOrElse("").toString,
    "blackName" -> DBUserService.findNameByUuid(this.players._2),
    "blackPic" -> DBUserService.findPicByUuid(this.players._2).getOrElse("/assets/images/black.png").toString,
    "whiteOrBlack" -> this.getField.getWhiteOrBlack.toInt,
    "gameOver" -> this.isGameOver
  )
}
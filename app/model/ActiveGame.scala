package model

import java.awt.Point
import akka.actor.ActorRef
import controller.GameController
import java.util.UUID
import play.api.libs.json.{Json, Writes, JsValue}

/**
 * Represents an ActiveGame
 * this class is immutable, state changing operations return a reference on a new object
 */
case class Player(uuid: UUID, out: ActorRef)
case class ActiveGame(uuid: String,
                      control: GameController,
                      white: Player,
                      black: Player,
                      users: List[Player]) {

  def move(src: Point, dst: Point) = control.move(src, dst)
  def getPossibleMoves(p: Point) = control.getPossibleMoves(p)
  def broadCastMsg(msg: JsValue) = for (player <- users) player.out ! msg
  def switchPlayers() =  this.copy(white=this.black, black = this.white)
  def setWhite(p: Player) = this.copy(white = p, users = p :: users)
  def setBlack(p: Player) = this.copy(black = p, users = p :: users)
  def addSpectator(p: Player) = this.copy(users = p :: users)

  def addPlayer(p: Player) : ActiveGame = {
    white match {
      case null =>
        setWhite(p)
      case _ =>
        black match {
          case null =>
            setBlack(p)
          case _ =>
            addSpectator(p)
        }
    }
  }
}

object ActiveGame{
  /**
   * Convert ActiveGame to Json
   */
  implicit val activeGameWrites = new Writes[ActiveGame] {
    def writes(c: ActiveGame): JsValue = {
      Json.obj(
        "uuid" -> c.uuid,
        "field" -> c.control.getField.getField,
        "check" -> c.control.getCheck,
        "whiteOrBlack" -> c.control.getField.getWhiteOrBlack.toInt,
        "gameOver" -> c.control.isGameOver
      )
    }
  }
//
//  /**
//   * Convert to MongoDBDBObject (only store relevant data)
//   */
//  implicit def toMongoDBObject(s: ActiveGame) = MongoDBObject(
//    "uuid" -> s.uuid,
//    "field" -> s.control.getField.getField,
//    "check" -> s.control.getCheck,
//    "whiteOrBlack" -> s.control.getField.getWhiteOrBlack,
//    "gameOver" -> s.control.isGameOver
//  )
//
//  /**
//   * Convert from MongoDBObject to ActiveGame
//   */
//  implicit def toActiveGame(in: MongoDBObject) =  GameState(
//      in.as[String]("uuid"),
//      for( e <- in.as[MongoDBList]("field").toArray)
//      yield for (x <- e.asInstanceOf[BasicDBList].toArray) yield x.asInstanceOf[Int],
//      in.as[Boolean]("check"),
//      in.as[Int]("whiteOrBlack").asInstanceOf[Byte],
//      in.as[Boolean]("gameOver")
//    )
  }
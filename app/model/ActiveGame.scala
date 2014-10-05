package model

import java.awt.Point
import akka.actor.ActorRef
import controller.GameController
import java.util.UUID
import play.api.libs.json.{Json, JsValue}

/**
 * Represents an ActiveGame
 * this class is immutable, state changing operations return a reference on a new object
 */
case class Player(uuid: UUID, out: ActorRef)

/**
 * Represents an active game instance
 * @param uuid     id of the game
 * @param control  controller of the game
 * @param white    white player
 * @param black    black player
 * @param users    list of all users, white player + black player + spectators
 */
case class ActiveGame(uuid: String,
                      control: GameController,
                      white: Player,
                      black: Player,
                      users: List[Player]
                       ) {

  def move(src: Point, dst: Point) = {
    control.move(src, dst)
    broadCastMsg(this.toJson)
  }

  def getPossibleMoves(p: Point) = control.getPossibleMoves(p)
  def broadCastMsg(msg: JsValue) = for (player <- users) player.out ! msg
  def switchPlayers() =  this.copy(white=this.black, black = this.white)
  def setWhite(p: Player) = this.copy(white = p, users = p :: users)
  def setBlack(p: Player) = this.copy(black = p, users = p :: users)
  def addSpectator(p: Player) = this.copy(users = p :: users)

  /**
   * Which role does the play with playerID have,
   * black player, white player or spectator
   */
  def getRole(playerID: UUID) = {
     playerID match {
       case white.uuid => "white player"
       case black.uuid => "black player"
       case _ => "spectator"
     }
  }


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

 // TODO: implement
 // def removePlayer(playerID: UUID) =


  def toJson : JsValue = Json.obj(
      "type" -> "ActiveGame",
      "uuid" -> this.uuid,
      "field" -> this.control.getField.getField,
      "check" -> this.control.getCheck,
      "white" -> white.uuid.toString,
      "black" -> black.uuid.toString,
      "whiteOrBlack" -> this.control.getField.getWhiteOrBlack.toInt,
      "gameOver" -> this.control.isGameOver
    )
}

  // TODO:
  // Partial code to serialize/deserialize to/from MongoDBObjects
  // removed for now, because it's dependend on the still changing model classes
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
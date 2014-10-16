package model

import java.awt.Point
import akka.actor.ActorRef
import com.mongodb.BasicDBList
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import controller.GameController
import play.api.libs.json.{Json, JsValue}

/**
 * Represents an ActiveGame
 * this class is immutable, state changing operations return a reference on a new object
 */
case class Player(uuid: String, out: ActorRef)

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
                      white: Option[Player],
                      black: Option[Player],
                      users: List[Player]
                       ) {

  def move(src: Point, dst: Point) = {
    control.move(src, dst)
    ActiveGameStore.add(uuid, this)
    broadCastMsg(this.toJson)
  }

  def getPossibleMoves(p: Point) = control.getPossibleMoves(p)

  def broadCastMsg(msg: JsValue) = for (player <- users) player.out ! msg

  def switchPlayers() = this.copy(white = this.black, black = this.white)

  def setWhite(p: Player) = this.copy(white = Option(p), users = p :: users);
  broadCastMsg(this.toJson)

  def setBlack(p: Player) = this.copy(black = Option(p), users = p :: users);
  broadCastMsg(this.toJson)

  def addSpectator(p: Player) = this.copy(users = p :: users)

  /**
   * Which role does the play with playerID have,
   * black player, white player or spectator
   */
  def getRole(playerID: String) = {
    val w = white.getOrElse(Player("", null))
    val b = black.getOrElse(Player("", null))
    playerID match {
      case w.uuid => "white player"
      case b.uuid => "black player"
      case _ => "spectator"
    }
  }

  def removePlayer(pID: String): ActiveGame = {
    val w = white.getOrElse(Player("", null))
    val b = black.getOrElse(Player("", null))

    pID match {
      case w.uuid => this.copy(white = None)
      case b.uuid => this.copy(black = None)
      case _ => this // this.copy(users = users.)
    }
  }

  def addPlayer(p: Player): ActiveGame = {
    if (!white.isDefined) {
      setWhite(p)
    } else if (!black.isDefined) {
      setBlack(p)
    } else {
      addSpectator(p)
    }
  }

  def toJson: JsValue = Json.obj(
    "type" -> "ActiveGame",
    "uuid" -> this.uuid,
    "field" -> this.control.getField.getField,
    "check" -> this.control.getCheck,
    "white" -> white.getOrElse(Player("", null)).uuid,
    "black" -> black.getOrElse(Player("", null)).uuid,
    "whiteOrBlack" -> this.control.getField.getWhiteOrBlack.toInt,
    "gameOver" -> this.control.isGameOver
  )

}

/**
 * Companion for MongoDB conversion
 */
object ActiveGame{

  /**
   * Convert to MongoDBObject
   * @return
   */
  implicit def toMongoDBObject(s: ActiveGame) = MongoDBObject(
    "uuid" -> s.uuid,
    "field" -> s.control.getField.getField,
    "check" -> s.control.getCheck,
    "whiteOrBlack" -> s.control.getField.getWhiteOrBlack,
    "gameOver" -> s.control.isGameOver
  )

  /**
   * Convert from MongoDBObject to ActiveGame
   */
  implicit def toActiveGame(in: MongoDBObject) : ActiveGame = {
    val controller = new GameController(
      in.as[Boolean]("gameOver"),
      in.as[Boolean]("check"),
      new Field(
        // reconstruct out field Array (there is no automatic converter for Multidimensional arrays
        for (e <- in.as[MongoDBList]("field").toArray)
        yield for (x <- e.asInstanceOf[BasicDBList].toArray) yield x.asInstanceOf[Int],
        in.as[Int]("whiteOrBlack").asInstanceOf[Byte]
      )
    )
    ActiveGame(in.as[String]("uuid"), controller,None,None,List())
  }
}
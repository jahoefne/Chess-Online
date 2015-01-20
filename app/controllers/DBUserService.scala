package controllers

import com.mongodb.DBObject
import com.mongodb.casbah.MongoClient
import play.api.{Play, Logger}
import securesocial.core._
import com.mongodb.casbah.Imports._
import securesocial.core.providers.MailToken
import securesocial.core.services.{SaveMode, UserService}
import scala.concurrent.Future
import com.novus.salat._

case class User(uuid: String, main: BasicProfile)

object DBUserService extends UserService[User] {

  import com.mongodb.casbah.commons.conversions.scala._

  RegisterJodaTimeConversionHelpers()

  val conn = MongoClient("127.0.0.1", 27017)
  val db = conn("Chess-Online")
  val users = db("Users")
  val tokens = db("Tokens")

  val log = Logger(this getClass() getName())

  def findByUuid(uuid: Option[String]): String = {
    uuid match {
      case Some(string) =>
        users.find ((obj: DBObject) => obj.uuid == string) match {
          case Some(u) => u.main.email.get
          case _ => "Unregistered"
        }
      case None => "Waiting for Player"
    }
  }
  
  def findPicByUuid(uuid: Option[String]): Option[String] = {
    uuid match {
      case Some(string) =>
        users.find ((obj: DBObject) => obj.uuid == string) match {
          case Some(u) => u.main.avatarUrl
          case _ => None
        }
      case None => None
    }
  }
   def findNameByUuid(uuid: Option[String]): String = {
    uuid match {
      case Some(string) =>
        users.find ((obj: DBObject) => obj.uuid == string) match {
          case Some(u) => u.main.fullName.get
          case _ => "Unregistered"
        }
      case None => "Waiting for Player"
    }
  }

  override def find(providerId: String, userId: String): Future[Option[BasicProfile]] = {
    users.find((obj: DBObject) => obj.main.providerId == providerId && obj.main.userId == userId) match {
      case Some(u) => Future.successful(Some(u.main))
      case _ => Future.successful(None)
    }
  }

  override def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] = {
    users.find((obj: DBObject) => obj.main.email.getOrElse("") == email && obj.main.providerId == providerId) match {
      case Some(u) => Future.successful(Some(u.main))
      case _ => Future.successful(None)
    }
  }

  /** Delete Token */
  override def deleteToken(uuid: String): Future[Option[MailToken]] = {
    val query = MongoDBObject("uuid" -> uuid)
    val token = tokenFromDbObject(tokens.findOne(query).get)
    tokens.remove(query)
    Future.successful(Some(token))
  }

  /** We do not support linking of profiles */
  override def link(current: User, to: BasicProfile): Future[User] = Future.successful(current)


  override def passwordInfoFor(user: User): Future[Option[PasswordInfo]] =
    users.find((obj: DBObject) => obj.main.providerId == user.main.providerId && obj.main.userId == user.main.userId) match {
      case Some(u) => Future.successful(u.main.passwordInfo)
      case _ => Future.successful(None)
    }


  /** Save User Data, depending on 'mode' data has to be inserted or updated */
  override def save(profile: BasicProfile, mode: SaveMode): Future[User] = {
    mode match {

      case SaveMode.SignUp =>
        val newUser = User(UUID.uuid, profile)
        users.save(newUser)
        Future.successful(newUser)

      case SaveMode.LoggedIn =>
        val found = users.find((obj: DBObject) => obj.main.providerId == profile.providerId && obj.main.userId == profile.userId)

        found match {
          case Some(dbObj) =>
            val updated = new User(dbObj.uuid, profile)
            users.update(dbObj, updated, upsert = true)
            Future.successful(updated)

          case None =>
            val newUser = User(UUID.uuid, profile)
            users.save(newUser)
            Future.successful(newUser)
        }


      case SaveMode.PasswordChange =>
        val found = users.find((obj: DBObject) => obj.main.providerId == profile.providerId && obj.main.userId == profile.userId)
        val updated = new User(found.get.uuid, profile)
        users.update(found.get, updated, upsert = true)
        Future.successful(updated)
    }
  }

  /** delete all expired tokens*/
  override def deleteExpiredTokens(): Unit = {
    for (obj <- tokens.iterator) {
      if (tokenFromDbObject(obj).isExpired)
        tokens.remove(obj)
    }
  }

  /** Update the password info for user */
  override def updatePasswordInfo(user: User, info: PasswordInfo): Future[Option[BasicProfile]] = {
    val found = users.find((obj: DBObject) => obj.main.providerId == user.main.providerId && obj.main.userId == user.main.userId)
    val updated = found.get.copy(main = found.get.main.copy(passwordInfo = Some(info)))
    users.update(found.get, updated, upsert = true)
    Future.successful(Some(updated.main))
  }

  /** Find Token based on UUID */
  override def findToken(token: String): Future[Option[MailToken]] = {
    tokens.findOne(MongoDBObject("uuid" -> token)) match {
      case Some(t) => Future.successful(Some(tokenFromDbObject(t)))
      case _ => Future.successful(None)
    }
  }

  /** Save a token to the DB */
  override def saveToken(token: MailToken): Future[MailToken] = {
    println(tokenToDbObject(token).toString())
    Future.successful {
      tokens.save(tokenToDbObject(token))
      token
    }
  }

  /** Conversion Helpers, do the Conversion with Salat */
  implicit val ctx = new Context {
    val name = "Custom_Classloader"
  }
  ctx.registerClassLoader(Play.classloader(Play.current))

  /** Implicit conversions fro UserObject */
  implicit def User2MongoDB(u: User): DBObject = grater[User].asDBObject(u)
  implicit def fromMongoDbObject(o: DBObject): User = grater[User].asObject(o)

  /** Explicit conversion for tokens because implicit methods must be unique per context*/
  def tokenToDbObject(t: MailToken): DBObject = grater[MailToken].asDBObject(t)
  def tokenFromDbObject(o: DBObject): MailToken = grater[MailToken].asObject(o)
}
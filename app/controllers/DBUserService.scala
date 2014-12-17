package controllers

import com.mongodb.DBObject
import com.mongodb.casbah.MongoClient
import org.joda.time.DateTime
import play.api.Logger
import securesocial.core._
import com.mongodb.casbah.Imports._
import securesocial.core.providers.MailToken
import securesocial.core.services.{SaveMode, UserService}
import scala.concurrent.Future

case class User(uuid: String, main: BasicProfile)

class DBUserService extends UserService[User] {
  import com.mongodb.casbah.commons.conversions.scala._
  RegisterJodaTimeConversionHelpers()

  val conn = MongoClient("127.0.0.1", 27017)
  val db = conn("Chess-Online")
  val users = db("Users")
  val tokens = db("Tokens")

  val log = Logger(this getClass() getName())

  implicit def toMongoDbObject(u: User): DBObject =
    MongoDBObject(
      "uuid" -> u.uuid,
      "email" -> u.main.email,
      "passwordInfo" -> MongoDBObject(
        "hasher" -> u.main.passwordInfo.get.hasher,
        "salt" -> u.main.passwordInfo.get.salt.getOrElse(""),
        "password" -> u.main.passwordInfo.get.password
      ),
      "providerId" -> u.main.providerId,
      "userId" -> u.main.userId
    )

  implicit def fromMongoDbObject(o: DBObject): User =
    User(
      o.as[String]("uuid"),
      BasicProfile(
        providerId = o.as[String]("providerId"),
        userId = o.as[String]("userId"),
        firstName = None,
        lastName = None,
        fullName = None,
        email = Option(o.as[String]("email")),
        avatarUrl = None,
        authMethod = AuthenticationMethod.UserPassword,oAuth1Info = None,
        oAuth2Info = None,
        passwordInfo = Option({
          val pwdInf = o.as[MongoDBObject]("passwordInfo")
          PasswordInfo(
            pwdInf.as[String]("hasher"),
            pwdInf.as[String]("password"),
            Option(pwdInf.as[String]("salt"))
          )
        }))
    )
  def tokenToDbObject(t: MailToken): DBObject =
    MongoDBObject(
      "Created" -> t.creationTime,
      "Email" -> t.email,
      "Expires" -> t.expirationTime,
      "SignedUp" -> t.isSignUp,
      "uuid" -> t.uuid
    )

  def tokenFromDbObject(o: DBObject): MailToken =
    MailToken(
      o.as[String]("uuid"),
      o.as[String]("Email"),
      o.as[DateTime]("Created"),
      o.as[DateTime]("Expires"),
      o.as[Boolean]("SignedUp")
    )

  override def find(providerId: String, userId: String): Future[Option[BasicProfile]] =  {
    users.find((obj: DBObject) => obj.main.providerId == providerId && obj.main.userId == userId) match {
      case Some(u) =>   log.error("Find")
        Future.successful(Some(u.main))
      case _ =>   log.error("Find")
        Future.successful(None)
    }
  }

  override def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] ={
    users.find((obj: DBObject) => obj.main.email.getOrElse("") == email && obj.main.providerId == providerId) match {
      case Some(u) => Future.successful(Some(u.main))
      case _ => Future.successful(None)
    }
  }

  override def deleteToken(uuid: String): Future[Option[MailToken]] = {
    val query = MongoDBObject("uuid"->uuid)
    val token = tokenFromDbObject(tokens.findOne(query).get)
    tokens.remove(query)
    Future.successful(Some(token))
  }

  override def link(current: User, to: BasicProfile): Future[User] = Future.successful(current)// We do not support linking of profiles


  override def passwordInfoFor(user: User): Future[Option[PasswordInfo]] =
    users.find((obj: DBObject) => obj.main.providerId == user.main.providerId && obj.main.userId == user.main.userId) match {
      case Some(u) => Future.successful(u.main.passwordInfo)
      case _ => Future.successful(None)
    }


  override def save(profile: BasicProfile, mode: SaveMode): Future[User] ={
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
            users.update(dbObj, updated, upsert=true)
            Future.successful(updated)

          case None =>
            val newUser = User(UUID.uuid, profile)
            users.save(newUser)
            Future.successful(newUser)
        }


      case SaveMode.PasswordChange =>
        val found = users.find((obj: DBObject) => obj.main.providerId == profile.providerId && obj.main.userId == profile.userId)
        val updated = new User(found.get.uuid, profile)
        users.update(found.get, updated, upsert=true)
        Future.successful(updated)
    }  }

  override def deleteExpiredTokens(): Unit = { // not yet implemented
  }

  override def updatePasswordInfo(user: User, info: PasswordInfo): Future[Option[BasicProfile]] = {
    val found = users.find((obj: DBObject) => obj.main.providerId == user.main.providerId && obj.main.userId == user.main.userId)
    val updated = found.get.copy(main = found.get.main.copy(passwordInfo = Some(info)))
    users.update(found.get, updated, upsert = true)
    Future.successful(Some(updated.main))
  }

  override def findToken(token: String): Future[Option[MailToken]] = {
    tokens.findOne(MongoDBObject("uuid" -> token)) match {
      case Some(t) => Future.successful(Some(tokenFromDbObject(t)))
      case _ => Future.successful(None)
    }
  }

  override def saveToken(token: MailToken): Future[MailToken] = {
    println(tokenToDbObject(token).toString())
    Future.successful{
      tokens.save(tokenToDbObject(token))
      token
    }
  }
}
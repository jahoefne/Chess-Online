package model

import com.mongodb.casbah.commons.MongoDBObject


case class Game(uuid: String)

object Game{
  implicit def UserAsDBObject(in: Game) =
    MongoDBObject(
      "uuid" -> in.uuid
    )
}

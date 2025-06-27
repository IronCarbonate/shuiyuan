package Objects.UserAccountService


import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.*
import io.circe.parser.*
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

import com.fasterxml.jackson.core.`type`.TypeReference
import Common.Serialize.JacksonSerializeUtils

import scala.util.Try

import org.joda.time.DateTime
import java.util.UUID
import Objects.UserAccountService.UserPermission

/**
 * User
 * desc: 用户信息实体类，包含用户的基本信息和权限
 * @param userID: String (用户的唯一标识ID)
 * @param accountName: String (用户的账户名)
 * @param password: String (用户的密码)
 * @param nickName: String (用户的昵称)
 * @param isMuted: Boolean (表示用户是否被禁言)
 * @param createdAt: DateTime (用户账户创建时间)
 * @param permission: UserPermission:1055 (用户的权限信息)
 */

case class User(
  userID: String,
  accountName: String,
  password: String,
  nickName: String,
  isMuted: Boolean,
  createdAt: DateTime,
  permission: UserPermission
){

//process class code 预留标志位，不要删除
}


case object User{

    
  import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

  // Circe 默认的 Encoder 和 Decoder
  private val circeEncoder: Encoder[User] = deriveEncoder
  private val circeDecoder: Decoder[User] = deriveDecoder

  // Jackson 对应的 Encoder 和 Decoder
  private val jacksonEncoder: Encoder[User] = Encoder.instance { currentObj =>
    Json.fromString(JacksonSerializeUtils.serialize(currentObj))
  }

  private val jacksonDecoder: Decoder[User] = Decoder.instance { cursor =>
    try { Right(JacksonSerializeUtils.deserialize(cursor.value.noSpaces, new TypeReference[User]() {})) } 
    catch { case e: Throwable => Left(io.circe.DecodingFailure(e.getMessage, cursor.history)) }
  }
  
  // Circe + Jackson 兜底的 Encoder
  given userEncoder: Encoder[User] = Encoder.instance { config =>
    Try(circeEncoder(config)).getOrElse(jacksonEncoder(config))
  }

  // Circe + Jackson 兜底的 Decoder
  given userDecoder: Decoder[User] = Decoder.instance { cursor =>
    circeDecoder.tryDecode(cursor).orElse(jacksonDecoder.tryDecode(cursor))
  }



//process object code 预留标志位，不要删除
}


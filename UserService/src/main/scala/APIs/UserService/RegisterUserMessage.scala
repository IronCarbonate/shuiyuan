package APIs.UserService

import Common.API.API
import Global.ServiceCenter.UserServiceCode

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
import Objects.UserService.UserRole

/**
 * RegisterUserMessage
 * desc: 用户注册，创建新用户信息并返回用户ID。
 * @param accountName: String (用户账号名称，用于唯一标识用户账户。)
 * @param password: String (用户密码，用于账户登录验证。)
 * @param nickname: String (用户昵称，用于显示在平台中。)
 * @param role: UserRole:1105 (用户角色类型，包括管理员和普通用户。)
 * @return userID: String (生成的用户唯一标识。)
 */

case class RegisterUserMessage(
  accountName: String,
  password: String,
  nickname: String,
  role: UserRole = UserRole.Normal
) extends API[String](UserServiceCode)



case object RegisterUserMessage{
    
  import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

  // Circe 默认的 Encoder 和 Decoder
  private val circeEncoder: Encoder[RegisterUserMessage] = deriveEncoder
  private val circeDecoder: Decoder[RegisterUserMessage] = deriveDecoder

  // Jackson 对应的 Encoder 和 Decoder
  private val jacksonEncoder: Encoder[RegisterUserMessage] = Encoder.instance { currentObj =>
    Json.fromString(JacksonSerializeUtils.serialize(currentObj))
  }

  private val jacksonDecoder: Decoder[RegisterUserMessage] = Decoder.instance { cursor =>
    try { Right(JacksonSerializeUtils.deserialize(cursor.value.noSpaces, new TypeReference[RegisterUserMessage]() {})) } 
    catch { case e: Throwable => Left(io.circe.DecodingFailure(e.getMessage, cursor.history)) }
  }
  
  // Circe + Jackson 兜底的 Encoder
  given registerUserMessageEncoder: Encoder[RegisterUserMessage] = Encoder.instance { config =>
    Try(circeEncoder(config)).getOrElse(jacksonEncoder(config))
  }

  // Circe + Jackson 兜底的 Decoder
  given registerUserMessageDecoder: Decoder[RegisterUserMessage] = Decoder.instance { cursor =>
    circeDecoder.tryDecode(cursor).orElse(jacksonDecoder.tryDecode(cursor))
  }


}


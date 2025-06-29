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


/**
 * VerifyAccountPasswordMessage
 * desc: 验证账号名称和密码是否匹配
 * @param accountName: String (用户的账号名称)
 * @param password: String (用户的密码)
 * @return result: Boolean (验证结果，表示账号名称和密码是否匹配)
 */

case class VerifyAccountPasswordMessage(
  accountName: String,
  password: String
) extends API[Boolean](UserServiceCode)



case object VerifyAccountPasswordMessage{
    
  import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

  // Circe 默认的 Encoder 和 Decoder
  private val circeEncoder: Encoder[VerifyAccountPasswordMessage] = deriveEncoder
  private val circeDecoder: Decoder[VerifyAccountPasswordMessage] = deriveDecoder

  // Jackson 对应的 Encoder 和 Decoder
  private val jacksonEncoder: Encoder[VerifyAccountPasswordMessage] = Encoder.instance { currentObj =>
    Json.fromString(JacksonSerializeUtils.serialize(currentObj))
  }

  private val jacksonDecoder: Decoder[VerifyAccountPasswordMessage] = Decoder.instance { cursor =>
    try { Right(JacksonSerializeUtils.deserialize(cursor.value.noSpaces, new TypeReference[VerifyAccountPasswordMessage]() {})) } 
    catch { case e: Throwable => Left(io.circe.DecodingFailure(e.getMessage, cursor.history)) }
  }
  
  // Circe + Jackson 兜底的 Encoder
  given verifyAccountPasswordMessageEncoder: Encoder[VerifyAccountPasswordMessage] = Encoder.instance { config =>
    Try(circeEncoder(config)).getOrElse(jacksonEncoder(config))
  }

  // Circe + Jackson 兜底的 Decoder
  given verifyAccountPasswordMessageDecoder: Decoder[VerifyAccountPasswordMessage] = Decoder.instance { cursor =>
    circeDecoder.tryDecode(cursor).orElse(jacksonDecoder.tryDecode(cursor))
  }


}


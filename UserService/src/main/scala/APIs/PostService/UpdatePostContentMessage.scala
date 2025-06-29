package APIs.PostService

import Common.API.API
import Global.ServiceCenter.PostServiceCode

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
 * UpdatePostContentMessage
 * desc: 更新帖子内容并记录修改历史。
 * @param userToken: String (用户的登录会话令牌，用于验证用户身份。)
 * @param postID: String (帖子ID，用于定位需要更新的帖子。)
 * @param newContent: String (更新后的帖子内容。)
 * @return result: String (操作结果，如“帖子内容更新成功！”。)
 */

case class UpdatePostContentMessage(
  userToken: String,
  postID: String,
  newContent: String
) extends API[String](PostServiceCode)



case object UpdatePostContentMessage{
    
  import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

  // Circe 默认的 Encoder 和 Decoder
  private val circeEncoder: Encoder[UpdatePostContentMessage] = deriveEncoder
  private val circeDecoder: Decoder[UpdatePostContentMessage] = deriveDecoder

  // Jackson 对应的 Encoder 和 Decoder
  private val jacksonEncoder: Encoder[UpdatePostContentMessage] = Encoder.instance { currentObj =>
    Json.fromString(JacksonSerializeUtils.serialize(currentObj))
  }

  private val jacksonDecoder: Decoder[UpdatePostContentMessage] = Decoder.instance { cursor =>
    try { Right(JacksonSerializeUtils.deserialize(cursor.value.noSpaces, new TypeReference[UpdatePostContentMessage]() {})) } 
    catch { case e: Throwable => Left(io.circe.DecodingFailure(e.getMessage, cursor.history)) }
  }
  
  // Circe + Jackson 兜底的 Encoder
  given updatePostContentMessageEncoder: Encoder[UpdatePostContentMessage] = Encoder.instance { config =>
    Try(circeEncoder(config)).getOrElse(jacksonEncoder(config))
  }

  // Circe + Jackson 兜底的 Decoder
  given updatePostContentMessageDecoder: Decoder[UpdatePostContentMessage] = Decoder.instance { cursor =>
    circeDecoder.tryDecode(cursor).orElse(jacksonDecoder.tryDecode(cursor))
  }


}


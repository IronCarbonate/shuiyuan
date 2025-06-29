package APIs.CommentService

import Common.API.API
import Global.ServiceCenter.CommentServiceCode

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
 * AddReplyMessage
 * desc: 为指定评论添加回复并记录
 * @param userToken: String (用户身份令牌，用于验证会话有效性)
 * @param commentID: String (目标评论的唯一标识符，表示回复的评论)
 * @param content: String (回复内容的详细文本信息)
 * @return replyCommentID: String (生成的回复评论唯一标识符)
 */

case class AddReplyMessage(
  userToken: String,
  commentID: String,
  content: String
) extends API[String](CommentServiceCode)



case object AddReplyMessage{
    
  import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

  // Circe 默认的 Encoder 和 Decoder
  private val circeEncoder: Encoder[AddReplyMessage] = deriveEncoder
  private val circeDecoder: Decoder[AddReplyMessage] = deriveDecoder

  // Jackson 对应的 Encoder 和 Decoder
  private val jacksonEncoder: Encoder[AddReplyMessage] = Encoder.instance { currentObj =>
    Json.fromString(JacksonSerializeUtils.serialize(currentObj))
  }

  private val jacksonDecoder: Decoder[AddReplyMessage] = Decoder.instance { cursor =>
    try { Right(JacksonSerializeUtils.deserialize(cursor.value.noSpaces, new TypeReference[AddReplyMessage]() {})) } 
    catch { case e: Throwable => Left(io.circe.DecodingFailure(e.getMessage, cursor.history)) }
  }
  
  // Circe + Jackson 兜底的 Encoder
  given addReplyMessageEncoder: Encoder[AddReplyMessage] = Encoder.instance { config =>
    Try(circeEncoder(config)).getOrElse(jacksonEncoder(config))
  }

  // Circe + Jackson 兜底的 Decoder
  given addReplyMessageDecoder: Decoder[AddReplyMessage] = Decoder.instance { cursor =>
    circeDecoder.tryDecode(cursor).orElse(jacksonDecoder.tryDecode(cursor))
  }


}


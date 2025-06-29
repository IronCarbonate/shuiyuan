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
 * CancelLikeCommentMessage
 * desc: 取消对指定评论的点赞
 * @param userToken: String (用户的会话令牌，用于验证操作的有效性)
 * @param commentID: String (需要取消点赞的评论ID)
 * @return result: String (操作结果，返回操作成功或失败信息)
 */

case class CancelLikeCommentMessage(
  userToken: String,
  commentID: String
) extends API[String](CommentServiceCode)



case object CancelLikeCommentMessage{
    
  import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

  // Circe 默认的 Encoder 和 Decoder
  private val circeEncoder: Encoder[CancelLikeCommentMessage] = deriveEncoder
  private val circeDecoder: Decoder[CancelLikeCommentMessage] = deriveDecoder

  // Jackson 对应的 Encoder 和 Decoder
  private val jacksonEncoder: Encoder[CancelLikeCommentMessage] = Encoder.instance { currentObj =>
    Json.fromString(JacksonSerializeUtils.serialize(currentObj))
  }

  private val jacksonDecoder: Decoder[CancelLikeCommentMessage] = Decoder.instance { cursor =>
    try { Right(JacksonSerializeUtils.deserialize(cursor.value.noSpaces, new TypeReference[CancelLikeCommentMessage]() {})) } 
    catch { case e: Throwable => Left(io.circe.DecodingFailure(e.getMessage, cursor.history)) }
  }
  
  // Circe + Jackson 兜底的 Encoder
  given cancelLikeCommentMessageEncoder: Encoder[CancelLikeCommentMessage] = Encoder.instance { config =>
    Try(circeEncoder(config)).getOrElse(jacksonEncoder(config))
  }

  // Circe + Jackson 兜底的 Decoder
  given cancelLikeCommentMessageDecoder: Decoder[CancelLikeCommentMessage] = Decoder.instance { cursor =>
    circeDecoder.tryDecode(cursor).orElse(jacksonDecoder.tryDecode(cursor))
  }


}


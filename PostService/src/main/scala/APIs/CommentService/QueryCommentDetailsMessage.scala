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
import Objects.CommentService.Comment

/**
 * QueryCommentDetailsMessage
 * desc: 查询指定评论的详细信息。
 * @param userToken: String (用户的会话令牌，用于验证用户身份。)
 * @param commentID: String (需要查询的评论的唯一标识。)
 * @return commentDetails: Comment:1031 (查询到的评论详细信息，包括内容、点赞数及所有回复。)
 */

case class QueryCommentDetailsMessage(
  userToken: String,
  commentID: String
) extends API[Comment](CommentServiceCode)



case object QueryCommentDetailsMessage{
    
  import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

  // Circe 默认的 Encoder 和 Decoder
  private val circeEncoder: Encoder[QueryCommentDetailsMessage] = deriveEncoder
  private val circeDecoder: Decoder[QueryCommentDetailsMessage] = deriveDecoder

  // Jackson 对应的 Encoder 和 Decoder
  private val jacksonEncoder: Encoder[QueryCommentDetailsMessage] = Encoder.instance { currentObj =>
    Json.fromString(JacksonSerializeUtils.serialize(currentObj))
  }

  private val jacksonDecoder: Decoder[QueryCommentDetailsMessage] = Decoder.instance { cursor =>
    try { Right(JacksonSerializeUtils.deserialize(cursor.value.noSpaces, new TypeReference[QueryCommentDetailsMessage]() {})) } 
    catch { case e: Throwable => Left(io.circe.DecodingFailure(e.getMessage, cursor.history)) }
  }
  
  // Circe + Jackson 兜底的 Encoder
  given queryCommentDetailsMessageEncoder: Encoder[QueryCommentDetailsMessage] = Encoder.instance { config =>
    Try(circeEncoder(config)).getOrElse(jacksonEncoder(config))
  }

  // Circe + Jackson 兜底的 Decoder
  given queryCommentDetailsMessageDecoder: Decoder[QueryCommentDetailsMessage] = Decoder.instance { cursor =>
    circeDecoder.tryDecode(cursor).orElse(jacksonDecoder.tryDecode(cursor))
  }


}


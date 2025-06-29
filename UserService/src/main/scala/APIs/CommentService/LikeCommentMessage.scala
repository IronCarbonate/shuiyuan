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
 * LikeCommentMessage
 * desc: 点赞评论并更新点赞列表。
 * @param userToken: String (用于用户身份验证的会话令牌。)
 * @param commentID: String (被点赞的评论ID。)
 * @return result: String (操作结果，例如'点赞成功！'。)
 */

case class LikeCommentMessage(
  userToken: String,
  commentID: String
) extends API[String](CommentServiceCode)



case object LikeCommentMessage{
    
  import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

  // Circe 默认的 Encoder 和 Decoder
  private val circeEncoder: Encoder[LikeCommentMessage] = deriveEncoder
  private val circeDecoder: Decoder[LikeCommentMessage] = deriveDecoder

  // Jackson 对应的 Encoder 和 Decoder
  private val jacksonEncoder: Encoder[LikeCommentMessage] = Encoder.instance { currentObj =>
    Json.fromString(JacksonSerializeUtils.serialize(currentObj))
  }

  private val jacksonDecoder: Decoder[LikeCommentMessage] = Decoder.instance { cursor =>
    try { Right(JacksonSerializeUtils.deserialize(cursor.value.noSpaces, new TypeReference[LikeCommentMessage]() {})) } 
    catch { case e: Throwable => Left(io.circe.DecodingFailure(e.getMessage, cursor.history)) }
  }
  
  // Circe + Jackson 兜底的 Encoder
  given likeCommentMessageEncoder: Encoder[LikeCommentMessage] = Encoder.instance { config =>
    Try(circeEncoder(config)).getOrElse(jacksonEncoder(config))
  }

  // Circe + Jackson 兜底的 Decoder
  given likeCommentMessageDecoder: Decoder[LikeCommentMessage] = Decoder.instance { cursor =>
    circeDecoder.tryDecode(cursor).orElse(jacksonDecoder.tryDecode(cursor))
  }


}


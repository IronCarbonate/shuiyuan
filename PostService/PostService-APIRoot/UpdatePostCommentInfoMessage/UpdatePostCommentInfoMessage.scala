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
 * UpdatePostCommentInfoMessage
 * desc: 更新帖子评论信息，增加评论计数并更新最新评论时间。
 * @param postID: String (帖子唯一标识，用于定位具体帖子。)
 * @param commentTime: DateTime (最新评论的时间，用于更新帖子的最新评论时间字段。)
 * @return result: String (操作结果提示信息，例如更新成功或失败。)
 */

case class UpdatePostCommentInfoMessage(
  postID: String,
  commentTime: DateTime
) extends API[String](PostServiceCode)



case object UpdatePostCommentInfoMessage{
    
  import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

  // Circe 默认的 Encoder 和 Decoder
  private val circeEncoder: Encoder[UpdatePostCommentInfoMessage] = deriveEncoder
  private val circeDecoder: Decoder[UpdatePostCommentInfoMessage] = deriveDecoder

  // Jackson 对应的 Encoder 和 Decoder
  private val jacksonEncoder: Encoder[UpdatePostCommentInfoMessage] = Encoder.instance { currentObj =>
    Json.fromString(JacksonSerializeUtils.serialize(currentObj))
  }

  private val jacksonDecoder: Decoder[UpdatePostCommentInfoMessage] = Decoder.instance { cursor =>
    try { Right(JacksonSerializeUtils.deserialize(cursor.value.noSpaces, new TypeReference[UpdatePostCommentInfoMessage]() {})) } 
    catch { case e: Throwable => Left(io.circe.DecodingFailure(e.getMessage, cursor.history)) }
  }
  
  // Circe + Jackson 兜底的 Encoder
  given updatePostCommentInfoMessageEncoder: Encoder[UpdatePostCommentInfoMessage] = Encoder.instance { config =>
    Try(circeEncoder(config)).getOrElse(jacksonEncoder(config))
  }

  // Circe + Jackson 兜底的 Decoder
  given updatePostCommentInfoMessageDecoder: Decoder[UpdatePostCommentInfoMessage] = Decoder.instance { cursor =>
    circeDecoder.tryDecode(cursor).orElse(jacksonDecoder.tryDecode(cursor))
  }


}


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
import Objects.PostService.PostSummary

/**
 * QueryPinnedPostsMessage
 * desc: 返回所有置顶帖列表。
 * @param userToken: String (用户会话令牌，用于验证登录状态。)
 * @return postsList: PostSummary:1121 (置顶帖子的列表信息，包含标题、标签和评论数。)
 */

case class QueryPinnedPostsMessage(
  userToken: String
) extends API[List[PostSummary]](PostServiceCode)



case object QueryPinnedPostsMessage{
    
  import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

  // Circe 默认的 Encoder 和 Decoder
  private val circeEncoder: Encoder[QueryPinnedPostsMessage] = deriveEncoder
  private val circeDecoder: Decoder[QueryPinnedPostsMessage] = deriveDecoder

  // Jackson 对应的 Encoder 和 Decoder
  private val jacksonEncoder: Encoder[QueryPinnedPostsMessage] = Encoder.instance { currentObj =>
    Json.fromString(JacksonSerializeUtils.serialize(currentObj))
  }

  private val jacksonDecoder: Decoder[QueryPinnedPostsMessage] = Decoder.instance { cursor =>
    try { Right(JacksonSerializeUtils.deserialize(cursor.value.noSpaces, new TypeReference[QueryPinnedPostsMessage]() {})) } 
    catch { case e: Throwable => Left(io.circe.DecodingFailure(e.getMessage, cursor.history)) }
  }
  
  // Circe + Jackson 兜底的 Encoder
  given queryPinnedPostsMessageEncoder: Encoder[QueryPinnedPostsMessage] = Encoder.instance { config =>
    Try(circeEncoder(config)).getOrElse(jacksonEncoder(config))
  }

  // Circe + Jackson 兜底的 Decoder
  given queryPinnedPostsMessageDecoder: Decoder[QueryPinnedPostsMessage] = Decoder.instance { cursor =>
    circeDecoder.tryDecode(cursor).orElse(jacksonDecoder.tryDecode(cursor))
  }


}


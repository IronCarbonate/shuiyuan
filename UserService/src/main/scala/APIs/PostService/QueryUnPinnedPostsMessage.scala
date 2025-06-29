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
 * QueryUnPinnedPostsMessage
 * desc: 返回所有非置顶帖列表
 * @param userToken: String (用户会话令牌，用于验证用户身份和会话有效性)
 * @return postsList: PostSummary:1121 (帖子列表，包含非置顶帖的简要信息)
 */

case class QueryUnPinnedPostsMessage(
  userToken: String
) extends API[List[PostSummary]](PostServiceCode)



case object QueryUnPinnedPostsMessage{
    
  import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

  // Circe 默认的 Encoder 和 Decoder
  private val circeEncoder: Encoder[QueryUnPinnedPostsMessage] = deriveEncoder
  private val circeDecoder: Decoder[QueryUnPinnedPostsMessage] = deriveDecoder

  // Jackson 对应的 Encoder 和 Decoder
  private val jacksonEncoder: Encoder[QueryUnPinnedPostsMessage] = Encoder.instance { currentObj =>
    Json.fromString(JacksonSerializeUtils.serialize(currentObj))
  }

  private val jacksonDecoder: Decoder[QueryUnPinnedPostsMessage] = Decoder.instance { cursor =>
    try { Right(JacksonSerializeUtils.deserialize(cursor.value.noSpaces, new TypeReference[QueryUnPinnedPostsMessage]() {})) } 
    catch { case e: Throwable => Left(io.circe.DecodingFailure(e.getMessage, cursor.history)) }
  }
  
  // Circe + Jackson 兜底的 Encoder
  given queryUnPinnedPostsMessageEncoder: Encoder[QueryUnPinnedPostsMessage] = Encoder.instance { config =>
    Try(circeEncoder(config)).getOrElse(jacksonEncoder(config))
  }

  // Circe + Jackson 兜底的 Decoder
  given queryUnPinnedPostsMessageDecoder: Decoder[QueryUnPinnedPostsMessage] = Decoder.instance { cursor =>
    circeDecoder.tryDecode(cursor).orElse(jacksonDecoder.tryDecode(cursor))
  }


}


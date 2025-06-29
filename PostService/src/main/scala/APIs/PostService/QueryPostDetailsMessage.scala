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
import Objects.PostService.Post

/**
 * QueryPostDetailsMessage
 * desc: 查询指定帖子的详细信息。
 * @param userToken: String (用户会话令牌，用于验证用户身份和权限。)
 * @param postID: String (帖子ID，用于指定需要查询的帖子。)
 * @return postDetails: Post:1120 (包含查询结果的帖子详细信息，包括标题、内容、标签、点赞数、评论列表等。)
 */

case class QueryPostDetailsMessage(
  userToken: String,
  postID: String
) extends API[Post](PostServiceCode)



case object QueryPostDetailsMessage{
    
  import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

  // Circe 默认的 Encoder 和 Decoder
  private val circeEncoder: Encoder[QueryPostDetailsMessage] = deriveEncoder
  private val circeDecoder: Decoder[QueryPostDetailsMessage] = deriveDecoder

  // Jackson 对应的 Encoder 和 Decoder
  private val jacksonEncoder: Encoder[QueryPostDetailsMessage] = Encoder.instance { currentObj =>
    Json.fromString(JacksonSerializeUtils.serialize(currentObj))
  }

  private val jacksonDecoder: Decoder[QueryPostDetailsMessage] = Decoder.instance { cursor =>
    try { Right(JacksonSerializeUtils.deserialize(cursor.value.noSpaces, new TypeReference[QueryPostDetailsMessage]() {})) } 
    catch { case e: Throwable => Left(io.circe.DecodingFailure(e.getMessage, cursor.history)) }
  }
  
  // Circe + Jackson 兜底的 Encoder
  given queryPostDetailsMessageEncoder: Encoder[QueryPostDetailsMessage] = Encoder.instance { config =>
    Try(circeEncoder(config)).getOrElse(jacksonEncoder(config))
  }

  // Circe + Jackson 兜底的 Decoder
  given queryPostDetailsMessageDecoder: Decoder[QueryPostDetailsMessage] = Decoder.instance { cursor =>
    circeDecoder.tryDecode(cursor).orElse(jacksonDecoder.tryDecode(cursor))
  }


}


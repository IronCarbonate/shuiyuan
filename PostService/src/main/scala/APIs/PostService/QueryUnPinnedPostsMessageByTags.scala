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
import Objects.PostService.PostTag
import Objects.PostService.PostSummary

/**
 * QueryUnPinnedPostsMessageByTags
 * desc: 查询非置顶帖子并按Tag筛选
 * @param userToken: String (用户登录后的会话令牌)
 * @param tags: PostTag (筛选的帖子标签列表)
 * @return postsList: PostSummary:1121 (符合条件的帖子概要信息列表)
 */

case class QueryUnPinnedPostsMessageByTags(
  userToken: String,
  tags: List[Option[PostTag]] = List.empty
) extends API[List[PostSummary]](PostServiceCode)



case object QueryUnPinnedPostsMessageByTags{
    
  import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

  // Circe 默认的 Encoder 和 Decoder
  private val circeEncoder: Encoder[QueryUnPinnedPostsMessageByTags] = deriveEncoder
  private val circeDecoder: Decoder[QueryUnPinnedPostsMessageByTags] = deriveDecoder

  // Jackson 对应的 Encoder 和 Decoder
  private val jacksonEncoder: Encoder[QueryUnPinnedPostsMessageByTags] = Encoder.instance { currentObj =>
    Json.fromString(JacksonSerializeUtils.serialize(currentObj))
  }

  private val jacksonDecoder: Decoder[QueryUnPinnedPostsMessageByTags] = Decoder.instance { cursor =>
    try { Right(JacksonSerializeUtils.deserialize(cursor.value.noSpaces, new TypeReference[QueryUnPinnedPostsMessageByTags]() {})) } 
    catch { case e: Throwable => Left(io.circe.DecodingFailure(e.getMessage, cursor.history)) }
  }
  
  // Circe + Jackson 兜底的 Encoder
  given queryUnPinnedPostsMessageByTagsEncoder: Encoder[QueryUnPinnedPostsMessageByTags] = Encoder.instance { config =>
    Try(circeEncoder(config)).getOrElse(jacksonEncoder(config))
  }

  // Circe + Jackson 兜底的 Decoder
  given queryUnPinnedPostsMessageByTagsDecoder: Decoder[QueryUnPinnedPostsMessageByTags] = Decoder.instance { cursor =>
    circeDecoder.tryDecode(cursor).orElse(jacksonDecoder.tryDecode(cursor))
  }


}


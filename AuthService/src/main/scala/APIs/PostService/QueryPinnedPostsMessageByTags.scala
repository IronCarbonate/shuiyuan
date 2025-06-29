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
 * QueryPinnedPostsMessageByTags
 * desc: 查询置顶帖子并按Tag筛选
 * @param userToken: String (用户会话令牌，用于验证用户登录状态。)
 * @param tags: PostTag (帖子所属标签列表，用于筛选指定分类的置顶帖子。)
 * @return postsList: PostSummary:1121 (查询结果，包含所有符合条件的置顶帖列表信息。)
 */

case class QueryPinnedPostsMessageByTags(
  userToken: String,
  tags: List[Option[PostTag]] = List.empty
) extends API[List[PostSummary]](PostServiceCode)



case object QueryPinnedPostsMessageByTags{
    
  import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

  // Circe 默认的 Encoder 和 Decoder
  private val circeEncoder: Encoder[QueryPinnedPostsMessageByTags] = deriveEncoder
  private val circeDecoder: Decoder[QueryPinnedPostsMessageByTags] = deriveDecoder

  // Jackson 对应的 Encoder 和 Decoder
  private val jacksonEncoder: Encoder[QueryPinnedPostsMessageByTags] = Encoder.instance { currentObj =>
    Json.fromString(JacksonSerializeUtils.serialize(currentObj))
  }

  private val jacksonDecoder: Decoder[QueryPinnedPostsMessageByTags] = Decoder.instance { cursor =>
    try { Right(JacksonSerializeUtils.deserialize(cursor.value.noSpaces, new TypeReference[QueryPinnedPostsMessageByTags]() {})) } 
    catch { case e: Throwable => Left(io.circe.DecodingFailure(e.getMessage, cursor.history)) }
  }
  
  // Circe + Jackson 兜底的 Encoder
  given queryPinnedPostsMessageByTagsEncoder: Encoder[QueryPinnedPostsMessageByTags] = Encoder.instance { config =>
    Try(circeEncoder(config)).getOrElse(jacksonEncoder(config))
  }

  // Circe + Jackson 兜底的 Decoder
  given queryPinnedPostsMessageByTagsDecoder: Decoder[QueryPinnedPostsMessageByTags] = Decoder.instance { cursor =>
    circeDecoder.tryDecode(cursor).orElse(jacksonDecoder.tryDecode(cursor))
  }


}


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

/**
 * CreatePostMessage
 * desc: 创建帖子并保存到数据库。
 * @param userToken: String (用户登录会话令牌，用于验证用户身份。)
 * @param title: String (帖子标题，简要描述帖子内容。)
 * @param content: String (帖子正文内容。)
 * @param tag: PostTag:1045 (帖子标签，用于标记帖子分类。)
 * @return postID: String (生成的帖子唯一标识ID。)
 */

case class CreatePostMessage(
  userToken: String,
  title: String,
  content: String,
  tag: PostTag
) extends API[String](PostServiceCode)



case object CreatePostMessage{
    
  import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

  // Circe 默认的 Encoder 和 Decoder
  private val circeEncoder: Encoder[CreatePostMessage] = deriveEncoder
  private val circeDecoder: Decoder[CreatePostMessage] = deriveDecoder

  // Jackson 对应的 Encoder 和 Decoder
  private val jacksonEncoder: Encoder[CreatePostMessage] = Encoder.instance { currentObj =>
    Json.fromString(JacksonSerializeUtils.serialize(currentObj))
  }

  private val jacksonDecoder: Decoder[CreatePostMessage] = Decoder.instance { cursor =>
    try { Right(JacksonSerializeUtils.deserialize(cursor.value.noSpaces, new TypeReference[CreatePostMessage]() {})) } 
    catch { case e: Throwable => Left(io.circe.DecodingFailure(e.getMessage, cursor.history)) }
  }
  
  // Circe + Jackson 兜底的 Encoder
  given createPostMessageEncoder: Encoder[CreatePostMessage] = Encoder.instance { config =>
    Try(circeEncoder(config)).getOrElse(jacksonEncoder(config))
  }

  // Circe + Jackson 兜底的 Decoder
  given createPostMessageDecoder: Decoder[CreatePostMessage] = Decoder.instance { cursor =>
    circeDecoder.tryDecode(cursor).orElse(jacksonDecoder.tryDecode(cursor))
  }


}


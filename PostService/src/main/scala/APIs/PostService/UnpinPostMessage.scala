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
 * UnpinPostMessage
 * desc: 取消帖子置顶状态并返回操作成功结果。
 * @param adminToken: String (管理员身份令牌，用户验证管理员权限。)
 * @param postID: String (帖子ID，用于定位需要更新置顶状态的帖子。)
 * @return result: String (操作结果消息，返回置顶状态更新的成功信息。)
 */

case class UnpinPostMessage(
  adminToken: String,
  postID: String
) extends API[String](PostServiceCode)



case object UnpinPostMessage{
    
  import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

  // Circe 默认的 Encoder 和 Decoder
  private val circeEncoder: Encoder[UnpinPostMessage] = deriveEncoder
  private val circeDecoder: Decoder[UnpinPostMessage] = deriveDecoder

  // Jackson 对应的 Encoder 和 Decoder
  private val jacksonEncoder: Encoder[UnpinPostMessage] = Encoder.instance { currentObj =>
    Json.fromString(JacksonSerializeUtils.serialize(currentObj))
  }

  private val jacksonDecoder: Decoder[UnpinPostMessage] = Decoder.instance { cursor =>
    try { Right(JacksonSerializeUtils.deserialize(cursor.value.noSpaces, new TypeReference[UnpinPostMessage]() {})) } 
    catch { case e: Throwable => Left(io.circe.DecodingFailure(e.getMessage, cursor.history)) }
  }
  
  // Circe + Jackson 兜底的 Encoder
  given unpinPostMessageEncoder: Encoder[UnpinPostMessage] = Encoder.instance { config =>
    Try(circeEncoder(config)).getOrElse(jacksonEncoder(config))
  }

  // Circe + Jackson 兜底的 Decoder
  given unpinPostMessageDecoder: Decoder[UnpinPostMessage] = Decoder.instance { cursor =>
    circeDecoder.tryDecode(cursor).orElse(jacksonDecoder.tryDecode(cursor))
  }


}


package Objects.PostService


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
 * PostSummary
 * desc: 
 * @param postID: String (帖子的唯一标识符)
 * @param userNickName: String (发帖者的昵称)
 * @param title: String (帖子的标题)
 * @param numLiked: String (点赞数)
 * @param numComment: String (评论数)
 * @param updateAt: String (最后一次更新时间)
 */

case class PostSummary(
  postID: String,
  userNickName: String,
  title: String,
  numLiked: String,
  numComment: Int,
  updateAt: String
){

  //process class code 预留标志位，不要删除


}


case object PostSummary{

    
  import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

  // Circe 默认的 Encoder 和 Decoder
  private val circeEncoder: Encoder[PostSummary] = deriveEncoder
  private val circeDecoder: Decoder[PostSummary] = deriveDecoder

  // Jackson 对应的 Encoder 和 Decoder
  private val jacksonEncoder: Encoder[PostSummary] = Encoder.instance { currentObj =>
    Json.fromString(JacksonSerializeUtils.serialize(currentObj))
  }

  private val jacksonDecoder: Decoder[PostSummary] = Decoder.instance { cursor =>
    try { Right(JacksonSerializeUtils.deserialize(cursor.value.noSpaces, new TypeReference[PostSummary]() {})) } 
    catch { case e: Throwable => Left(io.circe.DecodingFailure(e.getMessage, cursor.history)) }
  }
  
  // Circe + Jackson 兜底的 Encoder
  given postSummaryEncoder: Encoder[PostSummary] = Encoder.instance { config =>
    Try(circeEncoder(config)).getOrElse(jacksonEncoder(config))
  }

  // Circe + Jackson 兜底的 Decoder
  given postSummaryDecoder: Decoder[PostSummary] = Decoder.instance { cursor =>
    circeDecoder.tryDecode(cursor).orElse(jacksonDecoder.tryDecode(cursor))
  }



  //process object code 预留标志位，不要删除


}


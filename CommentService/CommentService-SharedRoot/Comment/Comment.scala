package Objects.CommentService


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
 * Comment
 * desc: 评论信息，包括评论的ID、所属帖子、作者ID、内容以及创建时间
 * @param commentID: String (评论的唯一ID)
 * @param postID: String (所属帖子的ID)
 * @param userID: String (发布者的用户ID)
 * @param content: String (评论内容)
 * @param createdAt: DateTime (评论创建时间)
 */

case class Comment(
  commentID: String,
  postID: String,
  userID: String,
  content: String,
  createdAt: DateTime
){

//process class code 预留标志位，不要删除
}


case object Comment{

    
  import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

  // Circe 默认的 Encoder 和 Decoder
  private val circeEncoder: Encoder[Comment] = deriveEncoder
  private val circeDecoder: Decoder[Comment] = deriveDecoder

  // Jackson 对应的 Encoder 和 Decoder
  private val jacksonEncoder: Encoder[Comment] = Encoder.instance { currentObj =>
    Json.fromString(JacksonSerializeUtils.serialize(currentObj))
  }

  private val jacksonDecoder: Decoder[Comment] = Decoder.instance { cursor =>
    try { Right(JacksonSerializeUtils.deserialize(cursor.value.noSpaces, new TypeReference[Comment]() {})) } 
    catch { case e: Throwable => Left(io.circe.DecodingFailure(e.getMessage, cursor.history)) }
  }
  
  // Circe + Jackson 兜底的 Encoder
  given commentEncoder: Encoder[Comment] = Encoder.instance { config =>
    Try(circeEncoder(config)).getOrElse(jacksonEncoder(config))
  }

  // Circe + Jackson 兜底的 Decoder
  given commentDecoder: Decoder[Comment] = Decoder.instance { cursor =>
    circeDecoder.tryDecode(cursor).orElse(jacksonDecoder.tryDecode(cursor))
  }



//process object code 预留标志位，不要删除
}


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
import Objects.CommentService.CommentLikeEntry

/**
 * Comment
 * desc: 评论信息，包括内容、回复、点赞等
 * @param commentID: String (评论的唯一标识)
 * @param replyID: String (回复的评论ID，可选项)
 * @param userID: String (发表评论的用户ID)
 * @param postID: String (评论所属的帖子ID)
 * @param content: String (评论内容)
 * @param createdAt: DateTime (评论的创建时间)
 * @param likes: CommentLikeEntry:1085 (评论的点赞信息列表)
 * @param replies: String (回复的评论ID列表)
 */

case class Comment(
  commentID: String,
  replyID: Option[String] = None,
  userID: String,
  postID: String,
  content: String,
  createdAt: DateTime,
  likes: List[Option[CommentLikeEntry]] = List.empty,
  replies: List[Option[String]] = List.empty
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


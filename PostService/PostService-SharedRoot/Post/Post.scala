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
import Objects.PostService.Comment

/**
 * Post
 * desc: 帖子信息，包含帖子内容、作者和评论相关信息
 * @param postID: String (帖子的唯一ID)
 * @param userID: String (发布者的唯一ID)
 * @param title: String (帖子的标题)
 * @param content: String (帖子的内容)
 * @param createdAt: DateTime (帖子的创建时间)
 * @param commentCount: Int (帖子的评论数量)
 * @param latestCommentTime: DateTime (最近评论的时间)
 * @param commentList: Comment:1054 (帖子的评论列表)
 */

case class Post(
  postID: String,
  userID: String,
  title: String,
  content: String,
  createdAt: DateTime,
  commentCount: Int,
  latestCommentTime: Option[DateTime] = None,
  commentList: List[Option[Comment]] = List.empty
){

//process class code 预留标志位，不要删除
}


case object Post{

    
  import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

  // Circe 默认的 Encoder 和 Decoder
  private val circeEncoder: Encoder[Post] = deriveEncoder
  private val circeDecoder: Decoder[Post] = deriveDecoder

  // Jackson 对应的 Encoder 和 Decoder
  private val jacksonEncoder: Encoder[Post] = Encoder.instance { currentObj =>
    Json.fromString(JacksonSerializeUtils.serialize(currentObj))
  }

  private val jacksonDecoder: Decoder[Post] = Decoder.instance { cursor =>
    try { Right(JacksonSerializeUtils.deserialize(cursor.value.noSpaces, new TypeReference[Post]() {})) } 
    catch { case e: Throwable => Left(io.circe.DecodingFailure(e.getMessage, cursor.history)) }
  }
  
  // Circe + Jackson 兜底的 Encoder
  given postEncoder: Encoder[Post] = Encoder.instance { config =>
    Try(circeEncoder(config)).getOrElse(jacksonEncoder(config))
  }

  // Circe + Jackson 兜底的 Decoder
  given postDecoder: Decoder[Post] = Decoder.instance { cursor =>
    circeDecoder.tryDecode(cursor).orElse(jacksonDecoder.tryDecode(cursor))
  }



//process object code 预留标志位，不要删除
}


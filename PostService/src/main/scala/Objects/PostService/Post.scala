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
import Objects.PostService.PostTag
import Objects.PostService.PostLikeEntry
import Objects.CommentService.Comment

/**
 * Post
 * desc: 帖子信息，包括标题、内容以及交互信息
 * @param postID: String (帖子的唯一标识符)
 * @param userID: String (发布帖子的用户唯一标识符)
 * @param title: String (帖子的标题)
 * @param content: String (帖子的内容)
 * @param tag: PostTag (帖子的标签，表示所属分类)
 * @param isPinned: Boolean (帖子是否置顶)
 * @param createdAt: DateTime (帖子创建时间)
 * @param updatedAt: DateTime (帖子更新时间)
 * @param likes: PostLikeEntry (帖子的点赞信息列表)
 * @param comments: Comment:1031 (帖子的评论列表)
 */

case class Post(
  postID: String,
  userID: String,
  title: String,
  content: String,
  tag: PostTag,
  isPinned: Boolean,
  createdAt: DateTime,
  updatedAt: DateTime,
  likes: List[PostLikeEntry],
  comments: List[Comment]
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


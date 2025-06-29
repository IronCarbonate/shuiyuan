package Objects.PostService

import com.fasterxml.jackson.databind.annotation.{JsonDeserialize, JsonSerialize}
import com.fasterxml.jackson.core.{JsonGenerator, JsonParser}
import com.fasterxml.jackson.databind.{DeserializationContext, JsonDeserializer, JsonSerializer, SerializerProvider}
import io.circe.{Decoder, Encoder}

@JsonSerialize(`using` = classOf[PostTagSerializer])
@JsonDeserialize(`using` = classOf[PostTagDeserializer])
enum PostTag(val desc: String):

  override def toString: String = this.desc

  case CampusLife extends PostTag("校园生活") // 校园生活
  case LifeExperience extends PostTag("人生经验") // 人生经验
  case AcademicCommunication extends PostTag("学术交流") // 学术交流
  case CultureAndArt extends PostTag("文化艺术") // 文化艺术
  case LeisureAndEntertainment extends PostTag("休闲娱乐") // 休闲娱乐
  case DigitalTechnology extends PostTag("数码科技") // 数码科技
  case Announcements extends PostTag("广而告知") // 广而告知


object PostTag:
  given encode: Encoder[PostTag] = Encoder.encodeString.contramap[PostTag](toString)

  given decode: Decoder[PostTag] = Decoder.decodeString.emap(fromStringEither)

  def fromString(s: String):PostTag  = s match
    case "校园生活" => CampusLife
    case "人生经验" => LifeExperience
    case "学术交流" => AcademicCommunication
    case "文化艺术" => CultureAndArt
    case "休闲娱乐" => LeisureAndEntertainment
    case "数码科技" => DigitalTechnology
    case "广而告知" => Announcements
    case _ => throw Exception(s"Unknown PostTag: $s")

  def fromStringEither(s: String):Either[String, PostTag]  = s match
    case "校园生活" => Right(CampusLife)
    case "人生经验" => Right(LifeExperience)
    case "学术交流" => Right(AcademicCommunication)
    case "文化艺术" => Right(CultureAndArt)
    case "休闲娱乐" => Right(LeisureAndEntertainment)
    case "数码科技" => Right(DigitalTechnology)
    case "广而告知" => Right(Announcements)
    case _ => Left(s"Unknown PostTag: $s")

  def toString(t: PostTag): String = t match
    case CampusLife => "校园生活"
    case LifeExperience => "人生经验"
    case AcademicCommunication => "学术交流"
    case CultureAndArt => "文化艺术"
    case LeisureAndEntertainment => "休闲娱乐"
    case DigitalTechnology => "数码科技"
    case Announcements => "广而告知"


// Jackson 序列化器
class PostTagSerializer extends JsonSerializer[PostTag] {
  override def serialize(value: PostTag, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeString(PostTag.toString(value)) // 直接写出字符串
  }
}

// Jackson 反序列化器
class PostTagDeserializer extends JsonDeserializer[PostTag] {
  override def deserialize(p: JsonParser, ctxt: DeserializationContext): PostTag = {
    PostTag.fromString(p.getText)
  }
}


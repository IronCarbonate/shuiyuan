package Objects.UserAccountService

import com.fasterxml.jackson.databind.annotation.{JsonDeserialize, JsonSerialize}
import com.fasterxml.jackson.core.{JsonGenerator, JsonParser}
import com.fasterxml.jackson.databind.{DeserializationContext, JsonDeserializer, JsonSerializer, SerializerProvider}
import io.circe.{Decoder, Encoder}

@JsonSerialize(`using` = classOf[UserPermissionSerializer])
@JsonDeserialize(`using` = classOf[UserPermissionDeserializer])
enum UserPermission(val desc: String):

  override def toString: String = this.desc

  case Normal extends UserPermission("普通用户") // 普通用户
  case Admin extends UserPermission("管理员") // 管理员


object UserPermission:
  given encode: Encoder[UserPermission] = Encoder.encodeString.contramap[UserPermission](toString)

  given decode: Decoder[UserPermission] = Decoder.decodeString.emap(fromStringEither)

  def fromString(s: String):UserPermission  = s match
    case "普通用户" => Normal
    case "管理员" => Admin
    case _ => throw Exception(s"Unknown UserPermission: $s")

  def fromStringEither(s: String):Either[String, UserPermission]  = s match
    case "普通用户" => Right(Normal)
    case "管理员" => Right(Admin)
    case _ => Left(s"Unknown UserPermission: $s")

  def toString(t: UserPermission): String = t match
    case Normal => "普通用户"
    case Admin => "管理员"


// Jackson 序列化器
class UserPermissionSerializer extends JsonSerializer[UserPermission] {
  override def serialize(value: UserPermission, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeString(UserPermission.toString(value)) // 直接写出字符串
  }
}

// Jackson 反序列化器
class UserPermissionDeserializer extends JsonDeserializer[UserPermission] {
  override def deserialize(p: JsonParser, ctxt: DeserializationContext): UserPermission = {
    UserPermission.fromString(p.getText)
  }
}


package Global

object ServiceCenter {
  val projectName: String = "shuiyuanfull"
  val dbManagerServiceCode = "A000001"
  val tongWenDBServiceCode = "A000002"
  val tongWenServiceCode = "A000003"

  val AuthServiceCode = "A000010"
  val UserServiceCode = "A000011"
  val PostServiceCode = "A000012"
  val CommentServiceCode = "A000013"

  val fullNameMap: Map[String, String] = Map(
    tongWenDBServiceCode -> "DB-Manager（DB-Manager）",
    tongWenServiceCode -> "Tong-Wen（Tong-Wen）",
    AuthServiceCode -> "AuthService（AuthService)",
    UserServiceCode -> "UserService（UserService)",
    PostServiceCode -> "PostService（PostService)",
    CommentServiceCode -> "CommentService（CommentService)"
  )

  def serviceName(serviceCode: String): String = {
    fullNameMap(serviceCode).toLowerCase
  }
}

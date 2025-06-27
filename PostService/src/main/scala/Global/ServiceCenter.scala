package Global

object ServiceCenter {
  val projectName: String = "BBSsimple"
  val dbManagerServiceCode = "A000001"
  val tongWenDBServiceCode = "A000002"
  val tongWenServiceCode = "A000003"

  val PostServiceCode = "A000010"
  val UserServiceCode = "A000011"

  val fullNameMap: Map[String, String] = Map(
    tongWenDBServiceCode -> "DB-Manager（DB-Manager）",
    tongWenServiceCode -> "Tong-Wen（Tong-Wen）",
    PostServiceCode -> "PostService（PostService)",
    UserServiceCode -> "UserService（UserService)"
  )

  def serviceName(serviceCode: String): String = {
    fullNameMap(serviceCode).toLowerCase
  }
}

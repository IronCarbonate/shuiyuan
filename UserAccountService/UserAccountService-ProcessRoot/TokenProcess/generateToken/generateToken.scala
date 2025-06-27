import Common.API.{PlanContext, Planner}
import Common.DBAPI.{writeDB, SqlParameter}
import Common.Object.SqlParameter
import Common.ServiceUtils.schemaName
import org.slf4j.LoggerFactory
import java.security.SecureRandom
import java.util.Base64
import org.joda.time.DateTime
import cats.effect.IO
import io.circe._ // For JSON
import io.circe.syntax._ // For .asJson
import io.circe.generic.auto._ // For generic JSON encoding/decoding
import cats.implicits._
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import org.joda.time.DateTime
import cats.implicits.*
import Common.DBAPI._
import Common.API.{PlanContext, Planner}
import cats.effect.IO
import Common.Object.SqlParameter
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}
import Common.ServiceUtils.schemaName

import Common.API.PlanContext
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import cats.implicits.*
import Common.DBAPI._
import Common.Serialize.CustomColumnTypes.{decodeDateTime,encodeDateTime}

def generateToken(userID: String)(using PlanContext): IO[String] = {
  // Logger initialization
  val logger = LoggerFactory.getLogger(getClass)

  // Step 1: Validate the input
  for {
    _ <- if (userID.isEmpty) {
      IO(logger.error("[Step 1] The provided userID is empty.")) >>
      IO.raiseError(new IllegalArgumentException("userID cannot be empty"))
    } else {
      IO(logger.info(s"[Step 1] Validating input parameter userID: ${userID}"))
    }
    
    // Step 2: Generate a secure 32-character token
    userToken <- IO {
      val tokenLengthInBytes = 24 // 24 bytes = 32-character base64 string
      val secureRandom = new SecureRandom()
      val randomBytes = new Array[Byte](tokenLengthInBytes)
      secureRandom.nextBytes(randomBytes)
      Base64.getUrlEncoder.withoutPadding.encodeToString(randomBytes)
    }
    _ <- IO(logger.info(s"[Step 2] Generated a unique token: ${userToken}"))
    
    // Step 3: Save the token in the database
    _ <- {
      val sql = s"INSERT INTO ${schemaName}.user_tokens (user_id, user_token, created_at) VALUES (?, ?, ?)"
      val timestamp = DateTime.now()
      val parameters = List(
        SqlParameter("String", userID),
        SqlParameter("String", userToken),
        SqlParameter("DateTime", timestamp.getMillis.toString)
      )
      writeDB(sql, parameters).flatMap { result =>
        IO(logger.info(s"[Step 3] Token successfully saved for userID: ${userID}, Result: ${result}"))
      }
    }
  } yield userToken
}
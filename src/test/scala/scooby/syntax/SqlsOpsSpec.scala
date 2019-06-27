package scooby.syntax

import cats.Id
import cats.effect.{ContextShift, IO}
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.scalatest.{FlatSpec, Matchers}
import scooby._
import scooby.utils.SqlsBuilder

import scala.concurrent.ExecutionContext

class SqlsOpsSpec extends FlatSpec with Matchers {
  implicit def contextShift: ContextShift[IO] =
    IO.contextShift(ExecutionContext.global)

  val xa = Transactor.fromDriverManager[IO](
    "org.h2.Driver",
    "jdbc:h2:mem:;DB_CLOSE_DELAY=-1",
    "sa", ""
  )

  "Sqls" should "compose multiple ConnectionIOs" in {
    val query = SqlsBuilder[(String, String), String, Id] {
      case (str1, str2) =>
        for {
          a <- sql"select $str1".query[String].unique
          b <- sql"select $str2".query[String].unique
        } yield (a + b)
    }

    Sqls(("Hello", "World"), query).run.transact(xa).unsafeRunSync() shouldEqual "HelloWorld"
  }
}

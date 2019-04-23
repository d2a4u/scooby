package scooby.mixintraits

import cats.effect.{ContextShift, IO}
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.scalatest.{FlatSpec, Matchers}
import scooby.utils.SqlBuilder
import scooby.{Address, Customer}

import scala.concurrent.ExecutionContext

class SearchableSpec extends FlatSpec with Matchers {

  implicit def contextShift: ContextShift[IO] =
    IO.contextShift(ExecutionContext.global)

  val xa = Transactor.fromDriverManager[IO](
    "org.h2.Driver",
    "jdbc:h2:mem:SearchableSpec;DB_CLOSE_DELAY=-1",
    "sa", ""
  )

  "Searchable mixin" should "return single record" in {
    val query = Customer.FindById(1)
    val expect = Customer(query.id, "John Doe", 40, Address("1 street", None, "P0 C03"))
    implicit val queryBuilder = SqlBuilder[Customer.FindById, Customer] { input =>
      sql"""SELECT ${expect.id}, ${expect.name}, ${expect.age}, ${expect.address.line1}, null, ${expect.address.postcode} WHERE ${input.id} = 1"""
    }
    Customer.findOne(query).transact(xa).unsafeRunSync() shouldEqual Some(expect)
  }

  it should "return a CanBuildFrom collection" in {
    val customer1 = Customer(1L, "John Doe", 40, Address("1 street", None, "P0 C03"))
    val customer2 = Customer(2L, "Tom Smith", 40, Address("2 street", None, "P1 C04"))

    val createTable = sql"""
        |CREATE TABLE customers(
        |  id BIGINT NOT NULL PRIMARY KEY,
        |  name VARCHAR NOT NULL,
        |  age INT NOT NULL,
        |  line1 VARCHAR NOT NULL,
        |  line2 VARCHAR,
        |  postcode VARCHAR NOT NULL
        |)
        """.stripMargin.update.run

    val query = Customer.FindByAge(40)
    val expect = List(customer1, customer2)

    val tranx = for {
      _ <- createTable
      _ <- Customer.insert(customer1)
      _ <- Customer.insert(customer2)
      result <- Customer.find[Customer.FindByAge, List](query)
    } yield result
    val foo = tranx.transact(xa).unsafeRunSync()
    tranx.transact(xa).unsafeRunSync() shouldEqual expect
  }
}

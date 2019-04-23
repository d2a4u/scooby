package scooby.mixintraits

import cats.effect.{ContextShift, IO}
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.scalatest.{FlatSpec, Matchers}
import scooby.{Address, Customer}
import scooby.utils.SqlBuilder

import scala.concurrent.ExecutionContext

class SearchableSpec extends FlatSpec with Matchers {

  implicit def contextShift: ContextShift[IO] =
    IO.contextShift(ExecutionContext.global)

  val xa = Transactor.fromDriverManager[IO](
    "org.h2.Driver",
    "jdbc:h2:mem:scooby;DB_CLOSE_DELAY=-1",
    "sa", ""
  )

  "Searchable mixin" should "find one" in {
    val query = Customer.FindById(1)
    val expect = Customer(query.id, "John Doe", 40, Address("1 street", None, "P0 C03"))
    implicit val queryBuilder = SqlBuilder[Customer.FindById, Customer] { input =>
      sql"""SELECT ${expect.id}, ${expect.name}, ${expect.age}, ${expect.address.line1}, null, ${expect.address.postcode} WHERE ${input.id} = 1"""
    }
    Customer.findOne(query).transact(xa).unsafeRunSync() shouldEqual Some(expect)
  }
}

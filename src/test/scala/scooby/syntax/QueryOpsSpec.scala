package scooby.syntax

import cats.effect.{ContextShift, IO}
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.scalatest.{FlatSpec, Matchers}
import scooby.mixins._
import scooby.utils.{SqlBuilder, SqlInserted}
import scooby.{Address, Customer}

import scala.concurrent.ExecutionContext

class syntax extends FlatSpec with Matchers {

  implicit def contextShift: ContextShift[IO] =
    IO.contextShift(ExecutionContext.global)

  val xa = Transactor.fromDriverManager[IO](
    "org.h2.Driver",
    "jdbc:h2:mem:scooby;DB_CLOSE_DELAY=-1",
    "sa", ""
  )

  "Queries" should "find one" in {
    val query = Customer.FindById(1)
    val expect = Customer(query.id, "John Doe", 40, Address("1 street", None, "P0 C03"))
    implicit val queryBuilder = SqlBuilder[Customer.FindById, Customer] { input =>
      sql"""SELECT ${expect.id}, ${expect.name}, ${expect.age}, ${expect.address.line1}, null, ${expect.address.postcode} WHERE ${input.id} = 1"""
    }
    FindOne(query, queryBuilder).run.transact(xa).unsafeRunSync() shouldEqual Some(expect)
  }

  it should "find more than one" in {
    val customer1 = Customer(1L, "John Doe", 40, Address("1 street", None, "P0 C03"))
    val customer2 = Customer(2L, "Tom Smith", 40, Address("2 street", None, "P1 C04"))

    sql"""
         |CREATE TABLE customers(
         |  id BIGINT NOT NULL PRIMARY KEY,
         |  name VARCHAR NOT NULL,
         |  age INT NOT NULL,
         |  line1 VARCHAR NOT NULL,
         |  line2 VARCHAR,
         |  postcode VARCHAR NOT NULL
         |)
       """.stripMargin.update.run.transact(xa).unsafeRunSync()

    implicit val statement = SqlBuilder[Customer, SqlInserted] { input =>
      sql"""INSERT INTO customers (id, name, age, line1, line2, postcode) VALUES 10, 'foo', 40, '1 street', null, 'TEST'"""
    }

    val query = Customer.FindByAge(40)
    val expect = List(customer1, customer2)
    implicit val queryBuilder = SqlBuilder[Customer.FindByAge, Customer] { input =>
      sql"""SELECT * FROM customers WHERE age = ${input.age}""".stripMargin
    }

    val tranx = for {
      _ <- Customer.insert(customer1)
      _ <- Customer.insert(customer2)
      result <- Find[Customer.FindByAge, Customer, List](query, queryBuilder).run
    } yield result

    tranx.transact(xa).unsafeRunSync() shouldEqual expect
  }
}

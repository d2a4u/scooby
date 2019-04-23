package scooby

import doobie.Read
import scooby.mixins._
import scooby.utils.{SqlBuilder, SqlInserted}
import doobie.implicits._

case class Address(
  line1: String,
  line2: Option[String],
  postcode: String
)

case class Customer(
  id: Long,
  name: String,
  age: Int,
  address: Address
)

object Customer extends Searchable[Customer] with Saveable[Customer] {
  implicit val read = Read[Customer]

  case class FindById(id: Long)
  case class FindByAge(age: Int)

  implicit val insert = SqlBuilder[Customer, SqlInserted] { input =>
    sql"""INSERT INTO customers (id, name, age, line1, line2, postcode)
         | VALUES (${input.id}, ${input.name}, ${input.age}, ${input.address.line1}, ${input.address.line2}, ${input.address.postcode})""".stripMargin
  }

  implicit val fba = SqlBuilder[Customer.FindByAge, Customer] { input =>
    sql"""SELECT * FROM customers WHERE age = ${input.age}""".stripMargin
  }
}

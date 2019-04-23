package scooby

import doobie.Read
import scooby.mixins._

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
}

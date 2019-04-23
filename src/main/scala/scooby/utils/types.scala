package scooby.utils

import doobie._
import doobie.free.connection.ConnectionIO

trait SqlStatement[I, O, M[_]] {
  def run: ConnectionIO[M[O]]
}

case class SqlBuilder[I, O](sql: I => Fragment)

case class SqlsBuilder[I, O, M[_]](sqls: I => ConnectionIO[M[O]])

sealed trait SqlResult[T] {
  val underlineValue: T
}

sealed trait SqlExistsResult extends SqlResult[Int]

object SqlExistsResult {
  def apply(i: Int): SqlExistsResult = i match {
    case 0 => SqlNonExists
    case _ => SqlExists
  }

  implicit val get: Get[SqlExistsResult] = Get[Int].tmap(SqlExistsResult.apply)
}

case object SqlExists extends SqlExistsResult {
  val underlineValue: Int = 1
}

case object SqlNonExists extends SqlExistsResult {
  val underlineValue: Int = 0
}

case class SqlInserted(underlineValue: Int) extends SqlResult[Int]

object SqlInserted {
  implicit val get: Get[SqlInserted] = Get[Int].tmap(SqlInserted.apply)
}

case class SqlUpserted(underlineValue: Int) extends SqlResult[Int]

object SqlUpserted {
  implicit val get: Get[SqlUpserted] = Get[Int].tmap(SqlUpserted.apply)
}

case class SqlUpdated(underlineValue: Int) extends SqlResult[Int]

object SqlUpdated {
  implicit val get: Get[SqlUpdated] = Get[Int].tmap(SqlUpdated.apply)
}

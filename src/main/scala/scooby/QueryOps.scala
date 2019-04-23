package scooby

import cats.Id
import doobie.LogHandler
import doobie.free.connection.ConnectionIO
import doobie.util.Read
import scooby.utils._

import scala.collection.generic.CanBuildFrom

trait QueryOps {
  case class FindOne[I, O: Read](input: I, query: SqlBuilder[I, O]) extends SqlStatement[I, O, Option] {
    def run: ConnectionIO[Option[O]] = query.sql(input).query[O].option
  }

  case class Find[I, O: Read, M[_]](input: I, query: SqlBuilder[I, O])(implicit cbf: CanBuildFrom[Nothing, O, M[O]]) extends SqlStatement[I, O, M] {
    def run: ConnectionIO[M[O]] = query.sql(input).query[O].to[M]
  }

  case class FindOneWithLogHandler[I, O: Read](input: I, query: SqlBuilder[I, O], handler: LogHandler) extends SqlStatement[I, O, Option] {
    def run: ConnectionIO[Option[O]] = query.sql(input).queryWithLogHandler[O](handler).option
  }

  case class FindWithLogHandler[I, O: Read, M[_]](input: I, query: SqlBuilder[I, O], handler: LogHandler)(implicit cbf: CanBuildFrom[Nothing, O, M[O]]) extends SqlStatement[I, O, M] {
    def run: ConnectionIO[M[O]] = query.sql(input).queryWithLogHandler[O](handler).to[M]
  }

  case class Exist[I](input: I, query: SqlBuilder[I, SqlExistsResult]) extends SqlStatement[I, SqlExistsResult, Id] {
    def run: ConnectionIO[Id[SqlExistsResult]] = query.sql(input).query[SqlExistsResult].unique
  }
}

object queries extends QueryOps
package scooby

import doobie.free.connection.ConnectionIO
import doobie.util.Read
import scooby.utils.{SqlStatement, SqlsBuilder}

trait SqlsOps {
  case class Sqls[I, O: Read, M[_]](input: I, query: SqlsBuilder[I, O, M]) extends SqlStatement[I, O, M] {
    def run: ConnectionIO[M[O]] = query.sqls(input)
  }
}

object sqls extends SqlsOps

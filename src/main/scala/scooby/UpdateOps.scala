package scooby

import cats.Id
import doobie.free.connection.ConnectionIO
import scooby.utils._

trait UpdateOps {
  case class Insert[I](input: I, statement: SqlBuilder[I, SqlInserted]) extends SqlStatement[I, SqlInserted, Id] {
    def run: ConnectionIO[Id[SqlInserted]] = statement.sql(input).update.run.map(SqlInserted.apply)
  }

  case class Upsert[I](input: I, statement: SqlBuilder[I, SqlUpserted]) extends SqlStatement[I, SqlUpserted, Id] {
    def run: ConnectionIO[Id[SqlUpserted]] = statement.sql(input).update.run.map(SqlUpserted.apply)
  }

  case class Update[I](input: I, statement: SqlBuilder[I, SqlUpdated]) extends SqlStatement[I, SqlUpdated, Id] {
    def run: ConnectionIO[Id[SqlUpdated]] = statement.sql(input).update.run.map(SqlUpdated.apply)
  }
}

object updates extends UpdateOps

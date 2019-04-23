package scooby

import cats.Id
import doobie.free.connection.ConnectionIO
import doobie.util.Read
import doobie.util.log.LogHandler
import scooby.utils._

import scala.collection.generic.CanBuildFrom

object mixins extends QueryOps with UpdateOps {

  trait Searchable[O] {
    def findOne[I](input: I)(implicit query: SqlBuilder[I, O], read: Read[O]): ConnectionIO[Option[O]] =
      FindOne[I, O](input, query).run

    def find[I, M[_]](input: I)(implicit query: SqlBuilder[I, O], cbf: CanBuildFrom[Nothing, O, M[O]], read: Read[O]): ConnectionIO[M[O]] =
      Find[I, O, M](input, query).run

    def findOneWithLogHandler[I](input: I, handler: LogHandler)(implicit query: SqlBuilder[I, O], read: Read[O]): ConnectionIO[Option[O]] =
      FindOneWithLogHandler[I, O](input, query, handler).run

    def findWithLogHandler[I, M[_]](input: I, handler: LogHandler)(implicit query: SqlBuilder[I, O], cbf: CanBuildFrom[Nothing, O, M[O]], read: Read[O]): ConnectionIO[M[O]] =
      FindWithLogHandler[I, O, M](input, query, handler).run

    def exist[I](input: I)(implicit query: SqlBuilder[I, SqlExistsResult], read: Read[O]): ConnectionIO[Id[SqlExistsResult]] =
      Exist[I](input, query).run
  }

  trait Saveable[I] {
    def insert(input: I)(implicit statement: SqlBuilder[I, SqlInserted]): ConnectionIO[SqlInserted] =
      Insert[I](input, statement).run

    def update(input: I)(implicit statement: SqlBuilder[I, SqlUpdated]): ConnectionIO[SqlUpdated] =
      Update[I](input, statement).run

    def upsert(input: I)(implicit statement: SqlBuilder[I, SqlUpserted]): ConnectionIO[SqlUpserted] =
      Upsert[I](input, statement).run
  }
}

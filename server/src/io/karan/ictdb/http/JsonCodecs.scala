package io.karan.ictdb.http

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import smithy4s.json.Json
import smithy4s.schema.Schema

object JsonCodecs:
    given [A](using schema: Schema[A]): JsonValueCodec[A] = Json.jsoniter.fromSchema(schema)

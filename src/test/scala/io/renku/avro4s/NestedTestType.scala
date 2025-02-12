package io.renku.avro4s

import cats.syntax.all.*
import io.renku.avro4s.Schema.Type
import io.renku.avro4s.TypeDecoder.Outcome
import io.renku.avro4s.all.given

final private case class NestedTestType(name: String, nested: TestType)

private object NestedTestType:

  val schema: Type.Record[NestedTestType] = Schema.Type
    .Record[NestedTestType](name = "NestedTestType")
    .addField("name", Schema.Type.StringType.typeOnly)
    .addField("nested", TestType.schema)

  given TypeEncoder[NestedTestType] = TypeEncoder.instance[NestedTestType] { v =>
    List(
      TypeEncoder[String].encodeValue(v.name),
      TypeEncoder[TestType].encodeValue(v.nested)
    ).sequence.map(_.reduce(_ ++ _))
  }

  given TypeDecoder[NestedTestType] = { bv =>
    TypeDecoder[String]
      .decode(bv)
      .flatMap { case Outcome(sv, bv) =>
        TypeDecoder[TestType].decode(bv).map { case Outcome(iv, bv) =>
          Outcome((sv, iv), bv)
        }
      }
      .map { case Outcome(v, bv) => Outcome(NestedTestType.apply.tupled(v), bv) }
  }

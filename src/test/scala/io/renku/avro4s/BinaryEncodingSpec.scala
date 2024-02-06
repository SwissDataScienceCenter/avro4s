package io.renku.avro4s

import io.renku.avro4s.Schema.Type
import io.renku.avro4s.all.given
import org.apache.avro.Schema as AvroSchema
import org.apache.avro.Schema.Parser as AvroParser
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class BinaryEncodingSpec
    extends AnyFlatSpec
    with should.Matchers
    with ScalaCheckPropertyChecks
    with EitherValues:

  it should "serialize/deserialize Null value" in:
    val schema: Schema[Null] = Schema.Type.Null(name = "field")
    val b = AvroEncoder.encode[Null](null, schema).value
    AvroDecoder.decode(b, schema).value shouldBe null

  it should "serialize/deserialize Boolean value" in:
    Set(true, false).foreach { v =>
      val schema: Schema[Boolean] = Schema.Type.Boolean(name = "field")

      val actual = AvroEncoder.encode(v, schema).value
      actual shouldBe prepareExpected(
        Seq(v),
        java.lang.Boolean.valueOf,
        """{"type": "boolean"}"""
      )

      AvroDecoder.decode(actual, schema).value shouldBe v
    }

  it should "serialize/deserialize an Int number value" in:
    val schema: Schema[Int] = Schema.Type.Int(name = "field")

    List(0, 1, Byte.MaxValue / 2 + 1, Short.MaxValue / 2 + 1, Int.MaxValue / 2 + 1)
      .flatMap(v => List(-v, v))
      .foreach { v =>
        val actual = AvroEncoder.encode(v, schema).value
        val expected =
          prepareExpected(Seq(v), Integer.valueOf, """{"type": "int"}""").toBin
        actual.toBin shouldBe expected

        AvroDecoder.decode(actual, schema).value shouldBe v
      }

  it should "serialize/deserialize a Long number value" in:
    val schema: Schema[Long] = Schema.Type.Long(name = "field")

    List(0, 1, Byte.MaxValue / 2 + 1, Short.MaxValue / 2 + 1, Int.MaxValue / 2 + 1)
      .flatMap(v => List(-v.toLong, v.toLong))
      .foreach { v =>
        val actual = AvroEncoder.encode(v, schema).value
        val expected =
          prepareExpected(Seq(v), java.lang.Long.valueOf, """{"type": "long"}""").toBin
        actual.toBin shouldBe expected

        AvroDecoder.decode(actual, schema).value shouldBe v
      }

  it should "serialize/deserialize a Float number value" in:
    val schema: Schema[Int] = Schema.Type.Int(name = "field")

    List(0, 1, Byte.MaxValue / 2 + 1, Short.MaxValue / 2 + 1, Int.MaxValue / 2 + 1)
      .flatMap(v => List(-v, v))
      .foreach { v =>
        val actual = AvroEncoder.encode(v, schema).value
        val expected =
          prepareExpected(Seq(v), Integer.valueOf, """{"type": "int"}""").toBin
        actual.toBin shouldBe expected

        AvroDecoder.decode(actual, schema).value shouldBe v
      }

  private def prepareExpected[A](values: Seq[A], encoder: A => Any, schema: String) =
    AvroWriter(parse(schema)).write(values, encoder)

  private lazy val parse: String => AvroSchema =
    new AvroParser().parse

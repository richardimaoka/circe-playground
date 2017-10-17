package example

import io.circe.syntax._
import io.circe.parser.decode

object EncodingAndDecoding {
  def main(args: Array[String]): Unit ={
    val intsJson = List(1, 2, 3).asJson
    // intsJson: io.circe.Json =
    // [
    //   1,
    //   2,
    //   3
    // ]
    println(intsJson)

    println(1.asJson)

    // println(Nil.asJson) fails to compile
    // could not find implicit value for parameter encoder: io.circe.Encoder[scala.collection.immutable.Nil.type
    println(Nil.asInstanceOf[List[Int]].asJson)

    println(Seq(1,2,3).asJson)

    val ints = intsJson.as[List[Int]]
    // res0: io.circe.Decoder.Result[List[Int]] = Right(List(1, 2, 3))
    println(ints)

    val decoded = decode[List[Int]]("[1, 2, 3]")
    // res1: Either[io.circe.Error,List[Int]] = Right(List(1, 2, 3))
    println(decoded)

    import io.circe._, io.circe.generic.semiauto._

    case class Foo(a: Int, b: String, c: Boolean)

    implicit val fooDecoder: Decoder[Foo] = deriveDecoder[Foo]
    implicit val fooEncoder: Encoder[Foo] = deriveEncoder[Foo]

    //or simply
    //implicit val fooDecoder: Decoder[Foo] = deriveDecoder
    //implicit val fooEncoder: Encoder[Foo] = deriveEncoder

    println(Foo(1, "john", true).asJson)
  }
}

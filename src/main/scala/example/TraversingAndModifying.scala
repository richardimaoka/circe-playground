package example

import cats.syntax.either._
import io.circe._, io.circe.parser._

object TraversingAndModifying {
  def main(args: Array[String]): Unit = {
    val json: String =
      """
        |{
        |  "id": "c730433b-082c-4984-9d66-855c243266f0",
        |  "name": "Foo",
        |  "counts": [1, 2, 3],
        |  "values": {
        |    "bar": true,
        |    "baz": 100.001,
        |    "qux": ["a", "b"]
        |  }
        |}
      """.stripMargin

    val doc: Json = parse(json).getOrElse(Json.Null)

    val cursor: HCursor = doc.hcursor

    val baz = cursor.downField("values").downField("baz").as[Double]
    println(baz)
    // baz: io.circe.Decoder.Result[Double] = Right(100.001)

    // You can also use `get[A](key)` as shorthand for `downField(key).as[A]`
    val baz2: Decoder.Result[Double] =
      cursor.downField("values").get[Double]("baz")
    println(baz2)
    // baz2: io.circe.Decoder.Result[Double] = Right(100.001)

    val secondQux: Decoder.Result[String] =
      cursor.downField("values").downField("qux").downArray.right.as[String]
    println(secondQux)
    // secondQux: io.circe.Decoder.Result[String] = Right(b)

    //just a cursor, doesn't represent "values" object
    val values: ACursor = cursor.downField("values")
    println(values)
    //io.circe.cursor.ObjectCursor@4b4e6a9a

    val invalid = cursor.downField("notexistent")
    println(invalid)
    //io.circe.FailedCursor@7fc0ad64

    val reversedNameCursor: ACursor =
      cursor.downField("name").withFocus(_.mapString(_.reverse))

    val reversedName: Option[Json] = reversedNameCursor.top
    println(reversedName)
    //    Some({
    //      "id" : "c730433b-082c-4984-9d66-855c243266f0",
    //      "name" : "ooF", //reversed!!!
    //      "counts" : [
    //      1,
    //      2,
    //      3
    //      ],
    //      "values" : {
    //        "bar" : true,
    //        "baz" : 100.001,
    //        "qux" : [
    //        "a",
    //        "b"
    //        ]
    //      }
    //    })

  }
}

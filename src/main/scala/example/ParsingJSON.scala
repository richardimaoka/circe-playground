package example

import io.circe.parser._
// import io.circe._
// import io.circe.parser._

object ParsingJSON {
  def main(args: Array[String]): Unit ={
    val rawJson: String =
      """
        |{
        |  "foo": "bar",
        |  "baz": 123,
        |  "list of stuff": [ 4, 5, 6 ]
        |}
      """.stripMargin
    println(rawJson)
    // rawJson: String =
    // "
    // {
    //   "foo": "bar",
    //   "baz": 123,
    //   "list of stuff": [ 4, 5, 6 ]
    // }
    // "

    /**
      * Below is the definition of `parser`
      */
    //    package object parser extends Parser {
    //      private[this] val parser = new JawnParser
    //
    //      def parse(input: String): Either[ParsingFailure, Json] = parser.parse(input)
    //    }
    val parseResult = parse(rawJson)
    println(parseResult)
    // parseResult: Either[io.circe.ParsingFailure,io.circe.Json] =
    // Right({
    //   "foo" : "bar",
    //   "baz" : 123,
    //   "list of stuff" : [
    //     4,
    //     5,
    //     6
    //   ]
    // })

    val invalidJson: String =
      """
        |{
        |  "foo": "bar",
        |  "baz": 123,
        |  'list of stuff': [ 4, 5, 6 ]
        |}
      """.stripMargin

    println(parse(invalidJson))
    //Left(io.circe.ParsingFailure: expected " got ' (line 5, column 36))

    /**
     * Known precision issue ... doesn't happen in Scala but only scala.js
     */
    println(decode[Long]("767946224062369796"))
    //works fine,   Right(767946224062369796)

  }
}

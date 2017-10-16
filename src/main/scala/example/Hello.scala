package example
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
// import io.circe._
// import io.circe.generic.auto._
// import io.circe.parser._
// import io.circe.syntax._


object Hello {
  def main(args: Array[String]): Unit = {
    sealed trait Foo
    // defined trait Foo

    case class Bar(xs: List[String]) extends Foo
    // defined class Bar

    case class Qux(i: Int, d: Option[Double]) extends Foo
    // defined class Qux

    val foo: Foo = Qux(13, Some(14.0))
    println(foo)
    // foo: Foo = Qux(13,Some(14.0))

    println(foo.asJson.noSpaces)
    // res0: String = {"Qux":{"i":13,"d":14.0}}

    println(decode[Foo](foo.asJson.spaces4))
    // res1: Either[io.circe.Error,Foo] = Right(Qux(13,Some(14.0)))
  }
}
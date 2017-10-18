package example


object EncodingAndDecoding {


  def basics(): Unit ={
    import io.circe.syntax._
    import io.circe.parser.decode
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
  }

  def semiAutomaticDerivation: Unit ={
    /**
     * Semi-automatic derivation
     */
    import io.circe.syntax._
    import io.circe._
    import io.circe.generic.semiauto._

    case class Foo(a: Int, b: String, c: Boolean)

    implicit val fooDecoder: Decoder[Foo] = deriveDecoder[Foo]
    implicit val fooEncoder: Encoder[Foo] = deriveEncoder[Foo]

    //or simply
    //implicit val fooDecoder: Decoder[Foo] = deriveDecoder
    //implicit val fooEncoder: Encoder[Foo] = deriveEncoder

    println(Foo(1, "john", true).asJson)

    // what about a nested case?

    case class My(str: String)
    //class My(str: String) doesn't work as deriveDecoder/Encoder does not work for non-case class
    case class MyWrap(a: String, my: My)

    implicit val myEncoder: Encoder[My] = deriveEncoder
    implicit val myDecoder: Decoder[My] = deriveDecoder
    //implicit val myEncoder: Encoder[My] = deriveEncoder[My]
    //implicit val myDecoder: Decoder[My] = deriveDecoder[My]

    //commeting out the above encoder/decoder for [My] will cause the following error:
    // could not find Lazy implicit value of type io.circe.generic.encoding.DerivedObjectEncoder[MyWrap]
    implicit val myWrapEncoder: Encoder[MyWrap] = deriveEncoder
    implicit val myWrapDecoder: Decoder[MyWrap] = deriveDecoder
    //implicit val myWrapEncoder: Encoder[MyWrap] = deriveEncoder[MyWrap]
    //implicit val myWrapDecoder: Decoder[MyWrap] = deriveDecoder[MyWrap]
  }

  def forProductNHelperMethods(): Unit ={
    import io.circe.syntax._
    import io.circe._
    import io.circe.generic.semiauto._
    /**
     * forProductN helper methods
     */
    case class User(id: Long, firstName: String, lastName: String)

    object UserCodec {
      implicit val decodeUser: Decoder[User] =
        Decoder.forProduct3("id", "first_name", "last_name")(User.apply)

      implicit val encodeUser: Encoder[User] =
        Encoder.forProduct3("id", "first_name", "last_name")(u =>
          (u.id, u.firstName, u.lastName)
        )
    }

    val user1 = User(123, "John", "Dohmoto")
    val derivedUserEncoder = deriveEncoder[User]
    if( user1.asJson(UserCodec.encodeUser) == user1.asJson(derivedUserEncoder))
      println("yeeehaaa!!!")
    else{
      println("noooooo waaaaaay")
      //hm equality based on reference equality ... ? Since the JSON contents are same as follows:

      println(user1.asJson(UserCodec.encodeUser))
      //      {
      //        "id" : 123,
      //        "first_name" : "John",
      //        "last_name" : "Dohmoto"
      //      }
      println(user1.asJson(derivedUserEncoder))
      //      {
      //        "id" : 123,
      //        "firstName" : "John",
      //        "lastName" : "Dohmoto"
      //      }
    }

    class MyNonCaseClass(val s: String)
    implicit val myNonCaseClassEncoder : Encoder[MyNonCaseClass] =
      Encoder.forProduct1("s")(m => (m.s))

    val mncc = new MyNonCaseClass("bah")
    println(mncc.asJson)
  }

  def fullyAutomaticDerivation(): Unit ={
    /**
     * Fully automatic derivation
     */
    import io.circe.syntax._
    import io.circe.Encoder
    import io.circe.generic.auto._
    // import io.circe.generic.auto._

    case class Person(name: String)
    // defined class Person

    case class Greeting(salutation: String, person: Person, exclamationMarks: Int)
    // defined class Greeting

    val greeting = Greeting("Hey", Person("Chris"), 3).asJson
    println(greeting)
    // res6: io.circe.Json =
    // {
    //   "salutation" : "Hey",
    //   "person" : {
    //     "name" : "Chris"
    //   },
    //   "exclamationMarks" : 3
    // }

    case class User(id: Long, firstName: String, lastName: String)
    val user1 = User(123, "John", "Dohmoto")
    println(user1.asJson)

    class MyNonCaseClass(val s: String)
    implicit val myNonCaseClassEncoder : Encoder[MyNonCaseClass] =
      Encoder.forProduct1("s")(m => (m.s))

    case class Parent(m: MyNonCaseClass, ss: String)

    val p = Parent(new MyNonCaseClass("bah"), "foo")
    println(p.asJson)
    //    {
    //      "m" : {
    //        "s" : "bah"
    //      },
    //      "ss" : "foo"
    //    }
  }

  def custonEncoderDecoder: Unit = {
    import io.circe.{Encoder, Decoder}
    import io.circe.Json
    import io.circe.HCursor
    import io.circe.syntax._
    import io.circe.parser._

    class Thing(val foo: String, val bar: Int)
    // defined class Thing

    implicit val customEncodeFoo: Encoder[Thing] = new Encoder[Thing] {
      final def apply(a: Thing): Json = Json.obj(
        ("foo", Json.fromString(a.foo)),
        ("bar", Json.fromInt(a.bar))
      )
    }
    // customEncodeFoo: io.circe.Encoder[Thing] = $anon$1@1866a8cd

    implicit val customDecodeFoo: Decoder[Thing] = new Decoder[Thing] {
      final def apply(c: HCursor): Decoder.Result[Thing] =
        for {
          foo <- c.downField("foo").as[String]
          bar <- c.downField("bar").as[Int]
        } yield {
          new Thing(foo, bar)
        }
    }
    // customDecodeFoo: io.circe.Decoder[Thing] = $anon$1@4b5e2e9e

    println(new Thing("fooooo", 20).asJson)
    println(parse(new Thing("fooooo", 20).asJson.toString))

    import cats.syntax.either._
    // import cats.syntax.either._

    import java.time.Instant
    // import java.time.Instant

    /**
     * Encoder.encodeString.contramap:
     * final def contramap[B](f: B => A): Encoder[B]
     *   Create a new [[Encoder]] by applying a function to a value of type `B` before encoding as an `A`.
     */
    implicit val encodeInstant: Encoder[Instant] = Encoder.encodeString.contramap[Instant](_.toString)
    // encodeInstant: io.circe.Encoder[java.time.Instant] = io.circe.Encoder$$anon$11@5cc63bd0

    /**
     * Decoder.decodeString.emap:
     * final def emap[B](f: A => Either[String, B]): Decoder[B]
     *   Create a new decoder that performs some operation on the result if this one succeeds.
     *
     *   @param f a function returning either a value or an error message
     */
    implicit val decodeInstant: Decoder[Instant] = Decoder.decodeString.emap { str =>
      Either.catchNonFatal(Instant.parse(str)).leftMap(t => "Instant")
    }
    // decodeInstant: io.circe.Decoder[java.time.Instant] = io.circe.Decoder$$anon$21@2ef6806d
  }

  /**
   * Doesn't compile in circe 0.8.0
   */
//  def customKeyMappingsAndAnnotations(): Unit ={
//    import io.circe._, io.circe.syntax._
//    // import io.circe.generic.extras._
//    // import io.circe.syntax._
//
//    implicit val config: Configuration = Configuration.default.withSnakeCaseKeys
//    // config: io.circe.generic.extras.Configuration = Configuration(io.circe.generic.extras.Configuration$$$Lambda$2037/501381773@195cef0e,false,None)
//
//    @ConfiguredJsonCodec case class User(firstName: String, lastName: String)
//    // defined class User
//    // defined object User
//
//    User("Foo", "McBar").asJson
//    // res8: io.circe.Json =
//    // {
//    //   "first_name" : "Foo",
//    //   "last_name" : "McBar"
//    // }
//  }

  def main(args: Array[String]): Unit ={
    Wrap("basics")(basics)
    Wrap("semiAutomaticDerivation")(semiAutomaticDerivation)
    Wrap("forProductNHelperMethods")(forProductNHelperMethods)
    Wrap("fullyAutomaticDerivation")(fullyAutomaticDerivation)
    Wrap("custonEncoderDecoder")(custonEncoderDecoder)
  }
}

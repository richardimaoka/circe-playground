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

  def main(args: Array[String]): Unit ={
    Wrap("basics")(basics)
    Wrap("semiAutomaticDerivation")(semiAutomaticDerivation)
    Wrap("forProductNHelperMethods")(forProductNHelperMethods)
    Wrap("fullyAutomaticDerivation")(fullyAutomaticDerivation)
  }
}

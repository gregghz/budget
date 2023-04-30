package com.gregghz.budget.model

import cats.implicits._
import org.scalatest._
import org.scalatest.flatspec._
import org.scalatest.matchers._

class CurrencySpec extends AnyFlatSpec with should.Matchers {
  "Currency" should "be able to add two values" in {
    val a: Currency = Currency(1000)
    val b: Currency = Currency(2000)
    val c: Currency = Numeric[Currency].plus(a, b)
    c should be(Currency(3000))
    c.show should be("$3.00")
  }

  "Currency" should "be able to sum a list" in {
    val a = List(Currency(1000), Currency(2000), Currency(3000))
    val b = a.sum[Currency]
    b should be(Currency(6000))
    b.show should be("$6.00")
  }

  "Currency" should "render as a dollar string" in {
    val a = Currency(1000)
    val b = a.show
    b should be("$1.00")
  }
}

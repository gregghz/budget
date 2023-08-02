package com.gregghz.budget.model

import io.circe._
import cats.Show

opaque type Currency <: Int = Int

object Currency {
  def apply(value: Int): Currency = value

  implicit val decoder: Decoder[Currency] = Decoder.decodeInt.map(Currency(_))

  implicit val show: Show[Currency] = Show.show { c =>
    val value = c / 1000.0
    val prefix = if (value < 0) "-$" else "$"
    val absAmount = math.abs(value)
    f"$prefix$absAmount%1.2f"
  }

  implicit val numeric: Numeric[Currency] = new Numeric[Currency] {

    override def parseString(str: String): Option[Currency] = str.toIntOption

    override def plus(x: Currency, y: Currency): Currency = x + y
    override def minus(x: Currency, y: Currency): Currency = x - y
    override def times(x: Currency, y: Currency): Currency = x * y
    override def negate(x: Currency): Currency = -x
    override def fromInt(x: Int): Currency = x
    override def toInt(x: Currency): Int = x
    override def toLong(x: Currency): Long = x
    override def toFloat(x: Currency): Float = x
    override def toDouble(x: Currency): Double = x
    override def compare(x: Currency, y: Currency): Int = x - y
  }
}

extension (c: Currency) {
  def /(d: Double): Double = c / d
}

// extension (l: List[Currency]) {
//   def sum: Currency = l.sum[Currency]
// }
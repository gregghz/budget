package com.gregghz.budget.model

import io.circe._

opaque type Currency = Int

object Currency {
  def apply(value: Int): Currency = value

  implicit val decoder: Decoder[Currency] = Decoder.decodeInt.map(Currency(_))
}

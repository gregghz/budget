package com.gregghz.budget.model

import io.circe._
import io.circe.generic.semiauto._

final case class CurrencyFormat(
  iso_code: String,
  example_format: String,
  decimal_digits: Int,
  decimal_separator: String,
  symbol_first: Boolean,
  group_separator: String,
  currency_symbol: String,
  display_symbol: Boolean
)

object CurrencyFormat {
  implicit val decoder: Decoder[CurrencyFormat] = deriveDecoder[CurrencyFormat]
}
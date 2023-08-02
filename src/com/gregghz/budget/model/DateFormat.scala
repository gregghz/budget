package com.gregghz.budget.model

import io.circe._
import io.circe.generic.semiauto._

final case class DateFormat(
  format: String
)

object DateFormat {
  implicit val decoder: Decoder[DateFormat] = deriveDecoder[DateFormat]
}

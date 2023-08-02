package com.gregghz.budget.model

import io.circe._
import io.circe.generic.semiauto._

final case class Months(
  months: List[Month],
)

object Months {
  implicit val decoder: Decoder[Months] = deriveDecoder[Months]
}

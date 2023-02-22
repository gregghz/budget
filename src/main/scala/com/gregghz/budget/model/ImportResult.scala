package com.gregghz.budget.model

import io.circe._
import io.circe.generic.semiauto._

final case class ImportResult(
  transaction_ids: List[String],
)

object ImportResult {
  implicit val decoder: Decoder[ImportResult] = deriveDecoder[ImportResult]
}

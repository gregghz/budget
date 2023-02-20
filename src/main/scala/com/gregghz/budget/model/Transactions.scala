package com.gregghz.budget.model

import io.circe._
import io.circe.generic.semiauto._

final case class Transactions(
    transactions: List[Transaction]
)

object Transactions {
  implicit val decoder: Decoder[Transactions] = deriveDecoder[Transactions]
}

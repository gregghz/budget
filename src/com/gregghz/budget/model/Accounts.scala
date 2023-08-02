package com.gregghz.budget.model

import io.circe._
import io.circe.generic.semiauto._

final case class Accounts(
  accounts: List[Account],
)

object Accounts {
  implicit val decoder: Decoder[Accounts] = deriveDecoder[Accounts]
}
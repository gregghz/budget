package com.gregghz.budget.model

import io.circe._
import io.circe.generic.semiauto._

final case class Account(
  id: String,
  name: String,
  `type`: String, // enum
  on_budget: Boolean,
  closed: Boolean,
  note: Option[String],
  balance: Int,
  cleared_balance: Int,
  uncleared_balance: Int,
  transfer_payee_id: Option[String],
  deleted: Boolean,
)

object Account {
  implicit val decoder: Decoder[Account] = deriveDecoder[Account]
}

package com.gregghz.budget.model

import io.circe._
import io.circe.generic.semiauto._

final case class SubTransaction(
  id: String,
  transaction_id: String,
  amount: Int,
  memo: Option[String],
  payee_id: Option[String],
  payee_name: Option[String],
  category_id: Option[String],
  category_name: Option[String],
  transfer_account_id: Option[String],
  transfer_transaction_id: Option[String],
  deleted: Boolean,
)

object SubTransaction {
  implicit val decoder: Decoder[SubTransaction] = deriveDecoder[SubTransaction]
}
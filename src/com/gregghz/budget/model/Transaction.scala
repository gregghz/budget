package com.gregghz.budget.model

import io.circe._
import io.circe.generic.semiauto._

final case class Transaction(
    id: String,
    date: String, // date
    amount: Currency,
    memo: Option[String],
    cleared: String, // enum
    approved: Boolean,
    flag_color: Option[String],
    account_id: String,
    payee_id: Option[String],
    category_id: Option[String],
    transfer_account_id: Option[String],
    transfer_transaction_id: Option[String],
    matched_transaction_id: Option[String],
    import_id: Option[String],
    import_payee_name: Option[String],
    import_payee_name_original: Option[String],
    debt_transaction_type: Option[String], // enum
    deleted: Boolean,
    account_name: String,
    payee_name: Option[String],
    category_name: Option[String],
    subtransactions: List[SubTransaction]
)

object Transaction {
  implicit val decoder: Decoder[Transaction] = deriveDecoder[Transaction]
}

final case class PatchTransaction(
    id: String,
    approved: Boolean,
    category_id: Option[String]
)

object PatchTransaction {
  implicit val decoder: Decoder[PatchTransaction] =
    deriveDecoder[PatchTransaction]

  implicit val encoder: Encoder[PatchTransaction] =
    deriveEncoder[PatchTransaction]
}

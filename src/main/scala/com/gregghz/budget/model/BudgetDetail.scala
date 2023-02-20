package com.gregghz.budget.model

import io.circe._
import io.circe.generic.semiauto._
import java.time.OffsetDateTime

final case class BudgetDetail(
  id: String,
  name: String,
  last_modified_on: OffsetDateTime,
  date_format: DateFormat,
  currency_format: CurrencyFormat,
  first_month: String,
  last_month: String,
  // months: List[MonthDetail]
)

object BudgetDetail {
  implicit val decoder: Decoder[BudgetDetail] = deriveDecoder[BudgetDetail]
}
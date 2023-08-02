package com.gregghz.budget.model

import io.circe._
import io.circe.generic.semiauto._
import java.time.LocalDate

final case class Month(
  month: LocalDate,
  note: Option[String],
  income: Currency,
  budgeted: Currency,
  activity: Currency,
  to_be_budgeted: Currency,
  age_of_money: Option[Int],
  deleted: Boolean,
)

object Month {
  implicit val decoder: Decoder[Month] = deriveDecoder[Month]
}

package com.gregghz.budget.model

import io.circe._
import io.circe.generic.semiauto._

final case class BudgetDetail()

object BudgetDetail {
  implicit val decoder: Decoder[BudgetDetail] = deriveDecoder[BudgetDetail]
}
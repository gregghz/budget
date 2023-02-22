package com.gregghz.budget.model

import io.circe._
import io.circe.generic.semiauto._

final case class Category(
  id: String,
  name: String,
  hidden: Boolean,
  deleted: Boolean,
  category_group_id: String,
  budgeted: Int,
  activity: Int,
  balance: Int,
)

object Category {
  implicit val decoder: Decoder[Category] = deriveDecoder[Category]
}

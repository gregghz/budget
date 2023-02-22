package com.gregghz.budget.model

import io.circe._
import io.circe.generic.semiauto._

final case class CategoryGroup(
  id: String,
  name: String,
  hidden: Boolean,
  deleted: Boolean,
  categories: List[Category],
)

object CategoryGroup {
  implicit val decoder: Decoder[CategoryGroup] = deriveDecoder[CategoryGroup]
}

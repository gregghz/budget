package com.gregghz.budget.model

import io.circe._
import io.circe.generic.semiauto._

final case class Categories(
  category_groups: List[CategoryGroup],
)

object Categories {
  implicit val decoder: Decoder[Categories] = deriveDecoder[Categories]
}
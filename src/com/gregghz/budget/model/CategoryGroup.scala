package com.gregghz.budget.model

import io.circe._
import io.circe.generic.semiauto._
import cats.Show
import cats.implicits._

final case class CategoryGroup(
    id: String,
    name: String,
    hidden: Boolean,
    deleted: Boolean,
    categories: List[Category]
) {
  val indexedCategories: Map[Int, Category] = categories.zipWithIndex.map {
    case (c, i) => (i+1) -> c
  }.toMap
}

object CategoryGroup {
  implicit val decoder: Decoder[CategoryGroup] = deriveDecoder[CategoryGroup]

  implicit val show: Show[CategoryGroup] = Show.show { c =>
    show"(${c.id})\t${c.name}"
  }
}

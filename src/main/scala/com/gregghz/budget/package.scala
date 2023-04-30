package com.gregghz.budget

import cats.Show
import com.gregghz.budget.model.CategoryGroup

given catListShow: Show[List[CategoryGroup]] with {
  def show(l: List[CategoryGroup]): String = {
    // find the shortest unique prefix for each category group
    ???
  }

  private def findUniquePrefixes(l: List[CategoryGroup]): List[String] = {
    val allUuids: List[String] = l.foldLeft(List.empty[String]) { (acc, cg) =>
      val cgUuid = cg.id
      cg.categories.foldLeft(acc) { (acc, c) =>
        val cUuid = c.id
        acc :+ cUuid
      } :+ cgUuid
    }
    ???
  }
}

package com.gregghz.budget

import cats.Show
import cats.data.ValidatedNel
import com.gregghz.budget.model.CategoryGroup
import com.monovore.decline.Argument

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

// extension [A](arg: Argument[A]) {
//   def mapOpt[B](f: A => Option[String]): Argument[B] =
//     arg.mapValidated { a =>
//       f(a) match {
//         case Some(s) => ValidatedNel.valid(s)
//         case None    => ValidatedNel.invalidNel(s"Could not parse $a")
//       }
//     }
// }
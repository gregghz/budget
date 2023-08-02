package com.gregghz.budget.model

import com.monovore.decline.Argument

enum ReportType {
  case Week
  case Month
  case Year
  case All
}

object ReportType {
  given Argument[ReportType] = Argument.fromMap(
    "report-type",
    values.foldLeft(Map.empty) { case (map, arg) =>
      map + (arg.toString.toLowerCase -> arg)
    }
  )
}

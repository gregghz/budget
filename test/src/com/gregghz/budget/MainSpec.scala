package com.gregghz.budget

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import com.gregghz.budget.client.YnabClientMock
import cats.effect.IO
import com.gregghz.budget.model._
import cats.effect.unsafe.implicits.global

class MainSpec extends AnyFlatSpec with should.Matchers {
  "weekReport" should "print dollars" in {
    val client = YnabClientMock[IO](
      List(
        Transaction(
          "",
          "",
          Currency(100000),
          None,
          "",
          true,
          None,
          "",
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          false,
          "",
          None,
          None,
          Nil
        )
      )
    )

    Main.weekReport(client, 0).unsafeRunSync() should be("Uncategorized,$100.00")
  }
}

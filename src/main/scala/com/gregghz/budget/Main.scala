package com.gregghz.budget

import cats.effect._
import sttp.client3._
import sttp.client3.httpclient.cats.HttpClientCatsBackend
import java.time.OffsetDateTime
import com.gregghz.budget.client._

object Main extends IOApp.Simple {
  val now = OffsetDateTime.now()

  val run = HttpClientCatsBackend.resource[IO]().use { backend =>
    val client = YnabClient(backend)

    client.getBudget("12").map { data =>
      pprint.log(data)  
      data
    }
  }
}

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

    client.getTransactions("2ceaf4e4-6da9-4761-ac79-bf6ba66c9060", Seq("uncategorized")).map { data =>
      data.foreach(pprint.log(_))
      pprint.log(data)  
    }
  }
}

package com.gregghz.budget

import cats.effect._
import sttp.client3._
import sttp.client3.httpclient.cats.HttpClientCatsBackend

object Main extends IOApp.Simple {
  HttpClientCatsBackend.resource[IO]().use { backend =>

  }
}
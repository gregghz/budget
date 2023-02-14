package com.gregghz.budget.client

import cats.effect._
import cats.implicits._
import com.gregghz.budget.model._
import sttp.capabilities.WebSockets
import sttp.client3._
import sttp.client3.circe._
import io.circe._
import io.circe.generic.semiauto._

case class YnabResponse[A](
  data: A
)

object YnabResponse {
  implicit def decoder[A: Decoder]: Decoder[YnabResponse[A]] = deriveDecoder[YnabResponse[A]]
}

class YnabClient[F[_]](backend: SttpBackend[F, WebSockets]) {
  val baseHost = uri"https://api.youneedabudget.com/v1" 
  val rootRequest = basicRequest.header("Authorization: Bearer HI2EU6Dn8z-Fi94wr_cppMm8LTrQerwwVGQa9kmnuQs")

  extension [A](response: F[Response[Either[ResponseException[String, Error], YnabResponse[A]]]]) {
    def data(using F: Async[F]): F[A] = {
      response.flatMap(_.body.fold(
        F.raiseError,
        value => F.pure(value.data)
      ))
    }
  }

  def getBudget(budgetId: String)(using F: Async[F]): F[BudgetDetail] = {
    val req = rootRequest.get(uri"$baseHost/budgets/$budgetId").response(asJson[YnabResponse[BudgetDetail]])
    val response = req.send(backend)
    response.data
  }
}
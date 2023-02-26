package com.gregghz.budget.client

import cats.effect._
import cats.implicits._
import com.gregghz.budget.model._
import sttp.capabilities.WebSockets
import sttp.client3._
import sttp.client3.circe._
import io.circe._
import io.circe.generic.semiauto._
import sttp.client3.logging.slf4j.Slf4jLoggingBackend
import sttp.client3.logging.LogLevel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class YnabResponse[A](
    data: A
)

object YnabResponse {
  implicit def decoder[A: Decoder]: Decoder[YnabResponse[A]] =
    deriveDecoder[YnabResponse[A]]
}

class YnabClient[F[_]](_backend: SttpBackend[F, WebSockets]) {
  val baseHost = uri"https://api.youneedabudget.com/v1"
  val rootRequest = basicRequest.header(
    "Authorization",
    "Bearer blah"
  )

  private val backend = Slf4jLoggingBackend(
    _backend,
    logResponseBody = true,
    logRequestHeaders = true,
    sensitiveHeaders = Set.empty,
    beforeRequestSendLogLevel = LogLevel.Info
  )

  extension [A](
      response: F[
        Response[Either[ResponseException[String, Error], YnabResponse[A]]]
      ]
  ) {
    def data(using F: Async[F]): F[A] = {
      response.flatMap(
        _.body.fold(
          F.raiseError,
          value => F.pure(value.data)
        )
      )
    }
  }

  def getBudget(budgetId: String)(using F: Async[F]): F[BudgetDetail] = {
    val req = rootRequest.get(
      uri"$baseHost/budgets/$budgetId"
    ).response(asJson[YnabResponse[BudgetDetail]])
    val response = req.send(backend)
    response.data
  }

  private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def getTransactions(budgetId: String, filters: Iterable[String], sinceDate: Option[LocalDate] = None)(using F: Async[F]): F[List[Transaction]] = {
    val query = (filters.map(f => ("type" -> f)) ++ sinceDate.map(d => ("since_date" -> d.format(fmt)))).toList
    val req = rootRequest.get(
      uri"$baseHost/budgets/$budgetId/transactions".withParams(query: _*)
    ).response(asJson[YnabResponse[Transactions]])
    val response = req.send(backend)
    response.data.map(_.transactions)
  }

  def importTransactions(budgetId: String)(using F: Async[F]): F[ImportResult] = {
    val req = rootRequest.post(
      uri"$baseHost/budgets/$budgetId/transactions/import"
    ).response(asJson[YnabResponse[ImportResult]])
    val response = req.send(backend)
    response.data
  }

  def updateTransactions(budgetId: String, transactions: List[PatchTransaction])(using F: Async[F]): F[Json] = {
    val req = rootRequest.patch(
      uri"$baseHost/budgets/$budgetId/transactions"
    ).body(PatchTransactions(transactions)).response(asJson[YnabResponse[Json]])
    val response = req.send(backend)
    response.data
  }

  def getCategories(budgetId: String)(using F: Async[F]): F[List[CategoryGroup]] = {
    val req = rootRequest.get(
      uri"$baseHost/budgets/$budgetId/categories"
    ).response(asJson[YnabResponse[Categories]])
    val response = req.send(backend)
    response.data.map(_.category_groups)
  }
}

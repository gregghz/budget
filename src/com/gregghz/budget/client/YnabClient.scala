package com.gregghz.budget.client

import cats.ApplicativeError
import cats.effect._
import cats.implicits._
import com.gregghz.budget.model._
import io.circe._
import io.circe.generic.semiauto._
import sttp.capabilities.WebSockets
import sttp.client3._
import sttp.client3.circe._
import sttp.client3.logging.LogLevel
import sttp.client3.logging.slf4j.Slf4jLoggingBackend

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.control.NoStackTrace

case class YnabResponse[A](
    data: A
)

object YnabResponse {
  implicit def decoder[A: Decoder]: Decoder[YnabResponse[A]] =
    deriveDecoder[YnabResponse[A]]
}

trait YnabClient[F[_]] {
  def getBudget(budgetId: String)(using F: Async[F]): F[BudgetDetail]
  def getTransactions(
      budgetId: String,
      filters: Iterable[String],
      sinceDate: Option[LocalDate] = None
  )(using F: Async[F]): F[List[Transaction]]
  def getAccounts(budgetId: String)(using F: Async[F]): F[List[Account]]
  def getMonths(budgetId: String)(using F: Async[F]): F[List[Month]]
  def importTransactions(
      budgetId: String
  )(using F: Async[F]): F[ImportResult]
  def updateTransactions(
      budgetId: String,
      transactions: List[PatchTransaction]
  )(using F: Async[F]): F[Json]
  def getCategories(
      budgetId: String
  )(using F: Async[F]): F[List[CategoryGroup]]
}

class YnabClientImpl[F[_]](
    _backend: SttpBackend[F, WebSockets],
    auth: YnabAuthentication
) extends YnabClient[F] {
  val baseHost = uri"https://api.youneedabudget.com/v1"
  val rootRequest = basicRequest.header(
    "Authorization",
    s"Bearer ${auth.token}"
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
    val req = rootRequest
      .get(
        uri"$baseHost/budgets/$budgetId"
      )
      .response(asJson[YnabResponse[BudgetDetail]])
    val response = req.send(backend)
    response.data
  }

  private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def getTransactions(
      budgetId: String,
      filters: Iterable[String],
      sinceDate: Option[LocalDate] = None
  )(using F: Async[F]): F[List[Transaction]] = {
    val query = (filters.map(f => ("type" -> f)) ++ sinceDate.map(d =>
      ("since_date" -> d.format(fmt))
    )).toList
    val req = rootRequest
      .get(
        uri"$baseHost/budgets/$budgetId/transactions".withParams(query: _*)
      )
      .response(asJson[YnabResponse[Transactions]])
    val response = req.send(backend)
    response.data.map(_.transactions)
  }

  def importTransactions(
      budgetId: String
  )(using F: Async[F]): F[ImportResult] = {
    val req = rootRequest
      .post(
        uri"$baseHost/budgets/$budgetId/transactions/import"
      )
      .response(asJson[YnabResponse[ImportResult]])
    val response = req.send(backend)
    response.data
  }

  def updateTransactions(
      budgetId: String,
      transactions: List[PatchTransaction]
  )(using F: Async[F]): F[Json] = {
    val req = rootRequest
      .patch(
        uri"$baseHost/budgets/$budgetId/transactions"
      )
      .body(PatchTransactions(transactions))
      .response(asJson[YnabResponse[Json]])
    val response = req.send(backend)
    response.data.handleErrorWith { error =>
      pprint.log(transactions, "failed transactions")
      F.raiseError(error)  
    }
  }

  def getCategories(
      budgetId: String
  )(using F: Async[F]): F[List[CategoryGroup]] = {
    val req = rootRequest
      .get(
        uri"$baseHost/budgets/$budgetId/categories"
      )
      .response(asJson[YnabResponse[Categories]])
    val response = req.send(backend)
    response.data.map(_.category_groups)
  }

  def getAccounts(budgetId: String)(using F: Async[F]): F[List[Account]] = {
    val req = rootRequest
      .get(
        uri"$baseHost/budgets/$budgetId/accounts"
      )
      .response(asJson[YnabResponse[Accounts]])
    val response = req.send(backend)
    response.data.map(_.accounts)
  }

  def getMonths(budgetId: String)(using F: Async[F]): F[List[Month]] = {
    val req = rootRequest
      .get(
        uri"$baseHost/budgets/$budgetId/months"
      )
      .response(asJson[YnabResponse[Months]])
    val response = req.send(backend)
    response.data.map(_.months)
  }
}

object YnabClient {
  def loadAuthentication[F[_]](using
      F: ApplicativeError[F, Throwable]
  ): F[YnabAuthentication] = {
    val token = sys.env.get("YNAB_TOKEN")

    token match {
      case Some(t) => YnabAuthentication(t).pure[F]
      case None =>
        F.raiseError(new Exception("YNAB_TOKEN not set") with NoStackTrace)
    }
  }
}

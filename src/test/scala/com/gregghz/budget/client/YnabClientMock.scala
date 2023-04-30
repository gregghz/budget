package com.gregghz.budget.client

import cats.effect.kernel.Ref
import cats.effect.kernel.Async
import com.gregghz.budget.model._
import java.time.LocalDate
import cats.effect.kernel.Sync
import io.circe.Json

class YnabClientMock[F[_]: Sync](
  initialTransactions: List[Transaction] = Nil
) extends YnabClient[F] {

  val transactionsState = Ref.unsafe[F, List[Transaction]](initialTransactions)

  override def updateTransactions(
      budgetId: String,
      transactions: List[PatchTransaction]
  )(using F: Async[F]): F[Json] = ???

  override def getCategories(budgetId: String)(using
      F: Async[F]
  ): F[List[CategoryGroup]] = ???

  override def importTransactions(budgetId: String)(using
      F: Async[F]
  ): F[ImportResult] = ???

  override def getBudget(budgetId: String)(using F: Async[F]): F[BudgetDetail] =
    ???

  override def getTransactions(
      budgetId: String,
      filters: Iterable[String],
      sinceDate: Option[LocalDate] = None
  )(using F: Async[F]): F[List[Transaction]] = {
    transactionsState.get
  }

  override def getAccounts(budgetId: String)(using
      F: Async[F]
  ): F[List[Account]] = ???

  override def getMonths(budgetId: String)(using F: Async[F]): F[List[Month]] =
    ???
}

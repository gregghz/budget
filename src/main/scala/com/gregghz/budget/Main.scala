package com.gregghz.budget

import cats.effect._
import cats.implicits._
import com.gregghz.budget.client._
import com.gregghz.budget.model._
import sttp.client3._
import sttp.client3.httpclient.cats.HttpClientCatsBackend

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.temporal.WeekFields
import java.util.Locale
import scala.io.Source
import scala.io.StdIn
import scala.util.control.NoStackTrace

object Main extends IOApp {
  val now = OffsetDateTime.now()

  def run(args: List[String]): IO[ExitCode] =
    HttpClientCatsBackend.resource[IO]().use { backend =>
      YnabClient.loadAuthentication[IO].flatMap { auth =>
        val client = YnabClientImpl(backend, auth)

        val task: IO[Unit] = args match {
          case "categorize" :: _        => categorize(client)
          case "report" :: "week" :: _  => weekReport(client).map(println)
          case "report" :: "month" :: _ => monthReport(client).map(println)
          case _ =>
            IO.raiseError(new Exception("Unknown command") with NoStackTrace)
        }

        task.map(_ => ExitCode.Success)
      }
    }

  private def monthReport(client: YnabClient[IO]): IO[String] = {
    val startDate = LocalDate.now().withDayOfMonth(1)

    for {
      transactions <- client.getTransactions(
        "2ceaf4e4-6da9-4761-ac79-bf6ba66c9060",
        Seq("approved"),
        Some(startDate)
      )
    } yield {
      transactions
        .groupBy(_.category_name)
        .map { case (categoryOpt, transactions) =>
          val category = categoryOpt.getOrElse("Uncategorized")
          val amount = transactions.map(_.amount).sum[Currency]

          (category, amount)
        }
        .toList
        .sortBy(_._2)
        .map { case (cat, amount) =>
          show"$cat,${amount.show}"
        }
        .mkString("\n")
    }
  }

  private[budget] def weekReport(client: YnabClient[IO]): IO[String] = {
    val now = LocalDate.now()
    val fieldUS = WeekFields.of(Locale.US).dayOfWeek()
    val sunday = now.`with`(fieldUS, 1)
    val startDate = if (sunday == now) sunday.minusWeeks(1) else sunday

    for {
      transactions <- client.getTransactions(
        "2ceaf4e4-6da9-4761-ac79-bf6ba66c9060",
        Seq("approved"),
        Some(startDate)
      )
    } yield {
      transactions
        .groupBy(_.category_name)
        .map { case (categoryOpt, transactions) =>
          val category = categoryOpt.getOrElse("Uncategorized")
          val amount: Currency = transactions.map(_.amount).sum[Currency]

          (category, amount)
        }
        .toList
        .sortBy(_._2)
        .map { case (cat, amount) => show"$cat,${amount.show}" }
        .mkString("\n")
    }
  }

  private def categorize(client: YnabClient[IO]): IO[Unit] = {
    val updates = for {
      importResult <- client
        .importTransactions("2ceaf4e4-6da9-4761-ac79-bf6ba66c9060")
      _ <- IO.blocking(
        println(
          show"Imported ${importResult.transaction_ids.length} transactions"
        )
      )
      data <- client.getTransactions(
        "2ceaf4e4-6da9-4761-ac79-bf6ba66c9060",
        Seq("unapproved")
      )
      categories <- client.getCategories("2ceaf4e4-6da9-4761-ac79-bf6ba66c9060")
    } yield {
      val count = data.length
      if (count == 0) {
        println("No transactions found")
        Nil
      } else {
        val categoryGroups: List[CategoryGroup] = categories

        data.zipWithIndex.flatMap { case (transaction, index) =>
          val i = index + 1

          val amountStr = transaction.amount.show
          val padding = " " * (8 - amountStr.length)

          val category =
            transaction.category_name.getOrElse("(uncategorized)")
          val catPad = " " * (20 - category.length)

          println(
            show"($i/$count)\t${transaction.date}\t$padding${amountStr}\t${transaction.account_name}\t${category}${catPad}\t${transaction.payee_name
                .getOrElse("(unknown)")}"
          )
          print("(a)pprove, (s)kip, (c)ategorize: ")
          Console.flush()
          StdIn.readChar() match {
            case 'a' =>
              Some(
                PatchTransaction(transaction.id, true, transaction.category_id)
              )
            case 's' =>
              None
            case 'c' =>
              categoryGroups.foreach { category =>
                println(show"(${category.id})\t${category.name}")
                category.categories.foreach { cat =>
                  println(show"\t${cat.id}\t${cat.name}")
                }
              }
              print("Enter category id: ")
              StdIn.readLine() match {
                case "" =>
                  None
                case id =>
                  Some(PatchTransaction(transaction.id, true, Some(id.trim)))
              }
            case _ =>
              None
          }

        }
      }

    }

    for {
      updates <- updates
      json <- client.updateTransactions(
        "2ceaf4e4-6da9-4761-ac79-bf6ba66c9060",
        updates
      )
    } yield {
      pprint.log(json)
      println("Done")
      ExitCode.Success
    }
  }
}

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
import com.monovore.decline._
import com.monovore.decline.effect._

sealed trait Command
case class Categorize() extends Command
case class Retry(upTo: Int) extends Command
case class Report(reportType: ReportType, offset: Int) extends Command

// final case class IOT[F[_]]

object Main extends CommandIOApp("budget", "Budget shit", version = "0.0.1") {
  val now = OffsetDateTime.now()

  val reportCommand = Opts.subcommand("report", "Generate a report") {
    val reportType = Opts.argument[ReportType]("reportType")
    val offset = Opts.option[Int]("offset", "", short = "o").withDefault(0)
    (reportType, offset).mapN(Report.apply)
  }

  val categorizeCommand = Opts.subcommand("categorize", "Categorize") {
    Opts(Categorize())
  }

  val retryCommand = Opts.subcommand("retry", "Retry") {
    val index = Opts.argument[Int]("index")
    index.map(i => Opts(Retry(i)))
  }

  private def run(command: Command): IO[ExitCode] = {
    HttpClientCatsBackend.resource[IO]().use { backend =>
      YnabClient.loadAuthentication[IO].flatMap { auth =>
        val client = YnabClientImpl(backend, auth)

        execute(command, client)
      }
    }
  }

  override def main: Opts[IO[ExitCode]] = {
    (reportCommand
      .orElse(categorizeCommand))
      .map(cmd =>
        run(cmd).handleError { case e: Throwable =>
          e.printStackTrace()
          ExitCode.Error
        }
      )
  }

  private def execute(
      command: Command,
      client: YnabClient[IO]
  ): IO[ExitCode] = {
    command match {
      case Categorize() =>
        categorize(client).map(_ => ExitCode.Success)
      case Report(ReportType.Week, offset) =>
        weekReport(client, offset).map(println).map(_ => ExitCode.Success)
      case Report(ReportType.Month, offset) =>
        monthReport(client, offset).map(println).map(_ => ExitCode.Success)
      case Retry(upTo) =>
        retry(client, upTo).map(println).map(_ => ExitCode.Success)
      case _ =>
        IO.raiseError(new Exception("Unknown command") with NoStackTrace)
    }
  }

  private def monthReport(client: YnabClient[IO], offset: Int): IO[String] = {
    val startDate = LocalDate.now().withDayOfMonth(1).minusMonths(offset)
    pprint.pprintln(startDate)

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

  private[budget] def weekReport(
      client: YnabClient[IO],
      offset: Int
  ): IO[String] = {
    val now = LocalDate.now().minusWeeks(offset)
    pprint.pprintln(now)
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

  private var lastUpdateAttempt: List[PatchTransaction] = Nil

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
      json <- client
        .updateTransactions(
          "2ceaf4e4-6da9-4761-ac79-bf6ba66c9060",
          updates
        )
        .handleErrorWith { error =>
          lastUpdateAttempt = updates
          IO.raiseError(error)
        }
    } yield {
      pprint.log(json)
      println("Done")
      ExitCode.Success
    }
  }

  private def retry(client: YnabClient[IO], upTo: Int): IO[String] = {
    for {
      json <- client
        .updateTransactions(
          "2ceaf4e4-6da9-4761-ac79-bf6ba66c9060",
          lastUpdateAttempt.take(upTo)
        )
    } yield {
      pprint.log(json)
      println("Done")
      "Done"
    }
  }
}

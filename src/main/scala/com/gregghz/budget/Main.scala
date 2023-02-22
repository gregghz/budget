package com.gregghz.budget

import cats.effect._
import sttp.client3._
import sttp.client3.httpclient.cats.HttpClientCatsBackend
import java.time.OffsetDateTime
import com.gregghz.budget.client._
import com.gregghz.budget.model._
import cats.implicits._
import scala.io.Source
import scala.io.StdIn
import scala.util.control.NoStackTrace

object Main extends IOApp.Simple {
  val now = OffsetDateTime.now()

  val run = HttpClientCatsBackend.resource[IO]().use { backend =>
    val client = YnabClient(backend)

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
        data.zipWithIndex.flatMap { case (transaction, index) =>
          val i = index + 1

          val amount =
            transaction.amount / 1000.0 // for some reason the amount is in milliunits
          val prefix = if (amount < 0) "-$" else "$"
          val absAmount = math.abs(amount)
          val amountStr = f"$prefix$absAmount%1.2f"
          val padding = " " * (8 - amountStr.length)

          val category =
            transaction.category_name.getOrElse("(uncategorized)")
          val catPad = " " * (20 - category.length)

          println(
            show"($i/$count)\t${transaction.date}\t$padding${amountStr}\t${transaction.account_name}\t${category}${catPad}\t${transaction.payee_name
                .getOrElse("(unknown)")}"
          )
          print("(a)pprove, (s)kip, (c)ategorize: ")
          StdIn.readChar() match {
            case 'a' =>
              // client.approveTransaction(
              //   "2ceaf4e4-6da9-4761-ac79-bf6ba66c9060",
              //   transaction.id
              // )
              Some(PatchTransaction(transaction.id, true, None))
            case 's' =>
              None
            case 'c' =>
              categories.foreach { category =>
                println(show"${category.id}\t${category.name}")
                category.categories.foreach { cat =>
                  println(show"\t${cat.id}\t${cat.name}")
                }
              }
              print("Enter category id: ")
              StdIn.readLine() match {
                case "" =>
                  None
                case id =>
                  Some(PatchTransaction(transaction.id, true, Some(id)))
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
    }
  }
}

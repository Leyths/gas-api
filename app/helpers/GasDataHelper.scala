package helpers

import play.api.{Configuration, Logger}
import play.api.cache.CacheApi
import play.api.libs.ws.{WSClient, WSRequest}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

object GasDataHelper {

  def refresh(ws: WSClient, executionContext: ExecutionContext, cache : CacheApi, config : Configuration) = {

    val body = GasRequestHelper.requestLatestData(config, ws)

    var lines = body.split("\n") // Break up CSV into lines
    lines = lines.takeWhile(line => !haveReachedTerminalTotals(line)) // Take everything up to total values

    var terminals : List[String] = List()

    // Pull out terminal names, reverse, remove the CSV heading and flatten to unique values
    lines.foreach(line => terminals = terminals.::(getTerminalFromLine(line)))
    terminals = terminals.reverse.slice(1, terminals.length).distinct

    cache.set("terminals", terminals)

    terminals.foreach(terminal => {
      val data : List[String] = getTerminal(cache, terminal).getOrElse(List[String]())
      val newData = getValuesForTerminal(terminal, lines)

      Logger.info(newData.toString())
      cache.set(terminal, (data.:::(newData)).distinct)
    })

  }

  def getTerminals(cache : CacheApi) : Option[List[String]] = {
    cache.get("terminals")
  }

  def getTerminal(cache : CacheApi, terminal : String) : Option[List[String]] = {
    cache.get(terminal)
  }

  private def getTerminalFromLine(line : String) : String = {
    line.split(",").apply(0)
  }

  private def haveReachedTerminalTotals(line :String) : Boolean = {
    line.startsWith("Terminal Totals")
  }

  private def getValuesForTerminal(terminal : String, lines : Array[String]) : List[String] = {
    var timeAndFlow = List[String]()
    val dataLines = lines.filter(line => line.startsWith(terminal))
    dataLines.foreach(line => timeAndFlow = timeAndFlow.::(line.split(",").apply(2) + " " + line.split(",").apply(3)))

    return timeAndFlow
  }
}

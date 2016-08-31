package controllers

import javax.inject.Inject

import helpers.GasDataHelper
import play.api._
import play.api.cache.CacheApi
import play.api.libs.ws.{WSRequest, WSClient}
import play.api.mvc._
import play.api.libs.json.{JsObject, Json}

import scala.collection
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

class Application @Inject() (ws: WSClient, implicit val executionContext: ExecutionContext, cache : CacheApi) extends Controller {

  def index = Action {

    val terminals = GasDataHelper.getTerminals(cache).getOrElse(List[String]())

    var terminalMap : List[JsObject] = List()

    terminals.foreach(terminal => {
      terminalMap = terminalMap.::(Json.obj(
        "name" -> terminal,
        "values" -> GasDataHelper.getTerminal(cache, terminal)
      ))
    })

    Logger.info(terminalMap.toString())

    Ok(Json.obj(
      "terminals" -> terminalMap
    ))
  }


}
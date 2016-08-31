package helpers

import play.api.Configuration
import play.api.libs.ws.{WSClient, WSRequest}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object GasRequestHelper {

  def requestLatestData(config : Configuration, ws : WSClient) : String = {
    val values = Map(
      "__EVENTTARGET" -> Seq(config.getString("request.eventTarget").getOrElse("")),
      "__EVENTARGUMENT" -> Seq(config.getString("request.eventArgument").getOrElse("")),
      "__VIEWSTATE" -> Seq(config.getString("request.viewState").getOrElse("")),
      "__VIEWSTATEGENERATOR" -> Seq(config.getString("request.viewStateGenerator").getOrElse("")),
      "__EVENTVALIDATION" -> Seq(config.getString("request.eventValidation").getOrElse(""))
    )
    val request: WSRequest = ws.url(config.getString("request.url").getOrElse(""))
    val future = request.post(values)

    val response = Await.result(future, Duration.create("10s"))

    return response.body
  }
}

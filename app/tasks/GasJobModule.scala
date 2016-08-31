package tasks

import javax.inject.{Singleton, Inject}

import akka.actor.ActorSystem
import com.google.inject.AbstractModule
import helpers.GasDataHelper
import play.api.{Configuration, Logger}
import play.api.cache.CacheApi

import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient

// Using the default ExecutionContext, but you can configure
// your own as described here:
// https://www.playframework.com/documentation/2.4.x/ThreadPools
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import scala.concurrent.duration._

class GasJobModule extends AbstractModule {
  override def configure() = {
    // binding the RecurrentTask as a eager singleton will force
    // its initialization even if RecurrentTask is not injected in
    // any other object. In other words, it will starts with when
    // your application starts.
    bind(classOf[RecurrentTask]).asEagerSingleton()
  }
}

@Singleton
class RecurrentTask @Inject() (actorSystem: ActorSystem, lifecycle: ApplicationLifecycle, wSClient: WSClient, cache : CacheApi, config : Configuration) {

  // Just scheduling your task using the injected ActorSystem
  actorSystem.scheduler.schedule(1.second, 1.minute) {
    try {
      GasDataHelper.refresh(wSClient, defaultContext, cache, config)
    } catch {
      case e: Exception => Logger.error(e.toString)
    }
  }

  // This is necessary to avoid thread leaks, specially if you are
  // using a custom ExecutionContext
  lifecycle.addStopHook{ () =>
    Future.successful(actorSystem.shutdown())
  }

}
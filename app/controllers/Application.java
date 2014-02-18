package controllers;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.fasterxml.jackson.databind.JsonNode;

import models.CrawlTeacherController;
import models.CrawlUnitController;
import models.Pinger;
import play.libs.Akka;
import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import scala.concurrent.duration.Duration;
import views.html.index;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import views.html.*;

public class Application extends Controller {

	public static Result index() {
		return ok(index.render("Your new application is ready."));
	}
	
	/**
   * Display the crawling zone.
   */
  public static Result crawlingZone(String rootURL, int numberOfCrawlers, int maxDepthOfCrawling, int politenessDelay, int maxPagesToFetch) {
  	return ok(crawlingZone.render(rootURL, numberOfCrawlers, maxDepthOfCrawling, politenessDelay, maxPagesToFetch));
  }
  
  public static WebSocket<JsonNode> crawlingWs(final String rootURL, final int numberOfCrawlers, final int maxDepthOfCrawling, final int politenessDelay, final int maxPagesToFetch) {
  	return new WebSocket<JsonNode>() {
  		// Called when the Websocket Handshake is done.
  		public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
  			final ActorRef crawlerUnitActor = Akka.system().actorOf(
						Props.create(CrawlUnitController.class, in, out, rootURL, numberOfCrawlers, maxDepthOfCrawling, maxDepthOfCrawling, maxPagesToFetch));
				final Cancellable cancellable = Akka
						.system()
						.scheduler()
						.schedule(Duration.create(1, SECONDS), Duration.create(1, SECONDS),
								crawlerUnitActor, "Tick", Akka.system().dispatcher(), null);

				in.onClose(new Callback0() {
					@Override
					public void invoke() throws Throwable {
						cancellable.cancel();
					}
				});
  		}
  	};
  }
  
  public static WebSocket<JsonNode> crawlingWsTeacher(final String rootURL, final int numberOfCrawlers, final int maxDepthOfCrawling, final int politenessDelay, final int maxPagesToFetch) {
  	return new WebSocket<JsonNode>() {
  		// Called when the Websocket Handshake is done.
  		public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
  			final ActorRef crawlerTeacherActor = Akka.system().actorOf(
						Props.create(CrawlTeacherController.class, in, out, rootURL, numberOfCrawlers, maxDepthOfCrawling, maxDepthOfCrawling, maxPagesToFetch));
				final Cancellable cancellable = Akka
						.system()
						.scheduler()
						.schedule(Duration.create(1, SECONDS), Duration.create(1, SECONDS),
								crawlerTeacherActor, "Tick", Akka.system().dispatcher(), null);

				in.onClose(new Callback0() {
					@Override
					public void invoke() throws Throwable {
						cancellable.cancel();
					}
				});
  		}
  	};
  }
	
	public static Result pingJs() {
		return ok(views.js.ping.render());
	}

	public static WebSocket<String> pingWs() {
		return new WebSocket<String>() {
			public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
				final ActorRef pingActor = Akka.system().actorOf(
						Props.create(Pinger.class, in, out));
				final Cancellable cancellable = Akka
						.system()
						.scheduler()
						.schedule(Duration.create(1, SECONDS), Duration.create(1, SECONDS),
								pingActor, "Tick", Akka.system().dispatcher(), null);

				in.onClose(new Callback0() {
					@Override
					public void invoke() throws Throwable {
						cancellable.cancel();
					}
				});
			}

		};
	}
}

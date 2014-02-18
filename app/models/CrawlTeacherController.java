package models;

import play.libs.Json;
import play.mvc.WebSocket;
import akka.actor.UntypedActor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class CrawlTeacherController extends UntypedActor {
	WebSocket.In<JsonNode> in;
  WebSocket.Out<JsonNode> out;
  int millis = 0;
  CrawlController controller = null;
	
	public CrawlTeacherController(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out, final String rootURL, final int numberOfCrawlers, final int maxDepthOfCrawling, final int politenessDelay, final int maxPagesToFetch) throws Exception {
		this.in = in;
    this.out = out;
    
		CrawlConfig config = new CrawlConfig();
    config.setCrawlStorageFolder("/home/valentina/crawlerstorage2");
    config.setMaxPagesToFetch(maxPagesToFetch);
    config.setPolitenessDelay(politenessDelay);
    //config.setMaxDepthOfCrawling(maxDepthOfCrawling);
    config.setMaxDepthOfCrawling(0);
    config.setResumableCrawling(false);
    
    PageFetcher pageFetcher = new PageFetcher(config);
    RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
    RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
    controller = new CrawlController(config, pageFetcher, robotstxtServer);

    controller.addSeed("http://facciencias.uniandes.edu.co/cv/AcademiaFacultad.php?Profesores=CBIO");
    controller.addSeed("http://facciencias.uniandes.edu.co/cv/AcademiaFacultad.php?Profesores=MATE");
    controller.addSeed("http://facciencias.uniandes.edu.co/cv/AcademiaFacultad.php?Profesores=FISI");
    controller.addSeed("http://facciencias.uniandes.edu.co/cv/AcademiaFacultad.php?Profesores=QUIM");
    controller.setCustomData(out);
    controller.startNonBlocking(CrawlerTeacher.class, numberOfCrawlers);
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message.equals("Tick")) {
			this.millis = millis+1000;
			ObjectNode event = Json.newObject();
			event.put("time", String.valueOf(millis));
			
	    if(controller.isFinished()) {
	    	controller.shutdown();
	    	context().stop(getSelf());
	    }
			out.write(event);
		} else {
			unhandled(message);
		}
	}
}
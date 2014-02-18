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

public class CrawlUnitController extends UntypedActor {
	WebSocket.In<JsonNode> in;
  WebSocket.Out<JsonNode> out;
  int millis = 0;
  CrawlController controller = null;
	
	public CrawlUnitController(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out, final String rootURL, final int numberOfCrawlers, final int maxDepthOfCrawling, final int politenessDelay, final int maxPagesToFetch) throws Exception {
		this.in = in;
    this.out = out;
    
		CrawlConfig config = new CrawlConfig();
    config.setCrawlStorageFolder("/home/valentina/crawlerstorage");
    config.setMaxPagesToFetch(maxPagesToFetch);
    config.setPolitenessDelay(politenessDelay);
    //config.setMaxDepthOfCrawling(maxDepthOfCrawling);
    config.setMaxDepthOfCrawling(0);
    config.setResumableCrawling(false);
    
    PageFetcher pageFetcher = new PageFetcher(config);
    RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
    RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
    controller = new CrawlController(config, pageFetcher, robotstxtServer);

    controller.addSeed(rootURL);
    controller.setCustomData(out);
    controller.startNonBlocking(CrawlerUnit.class, numberOfCrawlers);
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message.equals("Tick")) {
			this.millis = millis+1000;
			ObjectNode event = Json.newObject();
			event.put("time", String.valueOf(millis));
			
			//List<Object> crawlersLocalData = controller.getCrawlersLocalData();
	    /*long totalLinks = 0;
	    long totalTextSize = 0;
	    int totalProcessedPages = 0;
	    for (Object localData : crawlersLocalData) {
	            CrawlStat stat = (CrawlStat) localData;
	            totalLinks += stat.getTotalLinks();
	            totalTextSize += stat.getTotalTextSize();
	            totalProcessedPages += stat.getTotalProcessedPages();
	    }*/
	    //System.out.println("Aggregated Statistics:");
	    //System.out.println("   Processed Pages: " + totalProcessedPages);
	    //System.out.println("   Total Links found: " + totalLinks);
	    //System.out.println("   Total Text Size: " + totalTextSize);
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
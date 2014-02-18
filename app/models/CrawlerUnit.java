package models;

import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Pattern;

import play.libs.Json;
import play.mvc.WebSocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CrawlerUnit extends WebCrawler {
	Pattern filters = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g"
			+ "|png|tiff?|mid|mp2|mp3|mp4" + "|wav|avi|mov|mpeg|ram|m4v|pdf"
			+ "|rm|smil|wmv|swf|wma|zip|rar|gz))$");
	
	@Override
	public boolean shouldVisit(WebURL url) {
		String href = url.getURL().toLowerCase();
		return !filters.matcher(href).matches();
	}

	@Override
	public void visit(Page page) {
		System.out.println("Visited: " + page.getWebURL().getURL());

		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData parseData = (HtmlParseData) page.getParseData();
			List<WebURL> links = parseData.getOutgoingUrls();
			WebSocket.Out<JsonNode> out = (WebSocket.Out<JsonNode>)this.myController.getCustomData();
			String pattern = "(Departamento|departamento|facultad|Facultad|DEPARTAMENTO|FACULTAD)++.*";
			for (WebURL webURL : links) {
				if(webURL.getAnchor() != null) {
					if(webURL.getAnchor().matches(pattern)) {
						//System.out.println(webURL.getAnchor());
						//System.out.println(webURL.getURL());
						ObjectNode event = Json.newObject();
						event.put("AcademicUnit", webURL.getAnchor());
						event.put("URL", webURL.getURL());
						out.write(event);
					}
				}
			}
		}
	}
}

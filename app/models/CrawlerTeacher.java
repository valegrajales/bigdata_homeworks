package models;

import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import play.libs.Json;
import play.mvc.WebSocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CrawlerTeacher extends WebCrawler {
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
			//System.out.println(parseData.getHtml());
			List<WebURL> links = parseData.getOutgoingUrls();
			WebSocket.Out<JsonNode> out = (WebSocket.Out<JsonNode>)this.myController.getCustomData();
			Pattern patternDepartment = Pattern.compile("<body>.*?<h3>(.*?)</h3>.*?</body>", Pattern.DOTALL);
			Matcher m = patternDepartment.matcher(parseData.getHtml());
			String departamento = null;
			while (m.find()) {
				departamento = m.group(1);
			}
			Pattern nombreProfesor = Pattern.compile("<div id=\"nombre\".*?>(?s).*?<strong>(.*?)</strong>", Pattern.DOTALL);
			Matcher m2 = nombreProfesor.matcher(parseData.getHtml());
			List<String> profesores = new ArrayList<String>();
			while (m2.find()) {
				profesores.add(m2.group(1));
			}
			Pattern emailProfesor = Pattern.compile(">E-Mail:(.*?)<", Pattern.DOTALL);
			Matcher m3 = emailProfesor.matcher(parseData.getHtml());
			List<String> emails = new ArrayList<String>();
			while (m3.find()) {
				emails.add(m3.group(1));
			}
			Pattern extensionProfesor = Pattern.compile("Ext.:(.*?)<", Pattern.DOTALL);
			Matcher m4 = extensionProfesor.matcher(parseData.getHtml());
			List<String> extensiones = new ArrayList<String>();
			while (m4.find()) {
				extensiones.add(m4.group(1));
			}
			Pattern paginaWebProfesor = Pattern.compile("<a href='(.*?)'.*?gina web Academia.*?</a>", Pattern.DOTALL);
			Matcher m5 = paginaWebProfesor.matcher(parseData.getHtml());
			List<String> paginasWeb = new ArrayList<String>();
			while (m5.find()) {
				paginasWeb.add(m5.group(1));
			}
			for(int i = 0; i < profesores.size(); i++) {
				ObjectNode event = Json.newObject();
				event.put("TeacherDepartment", departamento);
				event.put("TeacherName", profesores.get(i));
				event.put("TeacherEmail", emails.get(i));
				event.put("TeacherExtension", extensiones.get(i));
				event.put("TeacherWebSite", paginasWeb.get(i));
				i++;
				out.write(event);
			}
			/*for (WebURL webURL : links) {
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
			}*/
		}
	}
}

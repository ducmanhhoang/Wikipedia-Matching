package crawler;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.client.ClientConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import constant.Constant;
import fileoperation.FileOperation;
import info.bliki.html.HTML2WikiConverter;
import info.bliki.html.wikipedia.ToWikipedia;
import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;

public class WikiCrawler {
    public Pattern pattern;
    public Matcher matcher;
    public FileOperation fileOperation;
    
    
    public void writeData(ArrayList<ContentOfArticle> contentOfArticles, String authorLang) {
    	try {
	    	fileOperation = new FileOperation();
	    	for (ContentOfArticle contentOfArticle : contentOfArticles) {
	    		fileOperation.createFolder(Constant.INPUT_DIRECTORY + "/" + authorLang + "/" + contentOfArticle.getTitle());
	    		for (String language : Constant.LANGUAGE) {
	    			if (contentOfArticle.getContent().get(language) != null) {
	    				fileOperation.writeFile(Constant.INPUT_DIRECTORY + "/" + authorLang + "/" + contentOfArticle.getTitle() + "/" + language, contentOfArticle.getContent().get(language));
	    			}
	    		}
	    	}
    	} catch (Exception e) {
    		
    	}
    }
    
    public ArrayList<ContentOfArticle> getContentOfArticles(String username, String language, String start, String end) {
    	ArrayList<ContentOfArticle> contentOfArticles = new ArrayList<ContentOfArticle>();
    	ArrayList<Contribution> contributions = getListPagesContributedByUser(/*"Financier73", "fr", "2014-03-01T20%3A39%3A12.000Z", "2014-04-01T20%3A39%3A12.000Z"*/ username, language, start, end);
    	ArrayList<Article> topic;
    	for (Contribution contribution : contributions) {
    		topic = getListOfLanguageEditionArticles(contribution.getTitle(), contribution.getPageid(), language);
    		for (Article article : topic) {
    			ContentOfArticle contentOfArticle = new ContentOfArticle(article.getTitle());
    			contentOfArticle.getContent().put(language, getContentOfPageByDate(article.getTitle(), article.getPageid(), language, contribution.getTimestamp()));
    			for (LangLink langlink : article.getLanglinks()) {
    				contentOfArticle.getContent().put(langlink.getLanguage(), getContentOfPageByDate(langlink.getTitle(), 0, langlink.getLanguage(), contribution.getTimestamp()));
    			}
    			contentOfArticles.add(contentOfArticle);
    		}
    	}
    	return contentOfArticles;
    }
    
    public ArrayList<Article> getListOfLanguageEditionArticles(String title, long pageid, String language) {
    	pattern = Pattern.compile("%%");
		matcher = pattern.matcher(Constant.WIKI_API_URL);
		String wiki_api_url = "";
		if (matcher.find())
			wiki_api_url = matcher.replaceAll(language);
		//wiki_api_url = "https://it.wikipedia.org/w/api.php";
		URI uri = UriBuilder.fromUri(wiki_api_url).build();
		System.out.println("Getting page content...");

		ClientConfig clientConfig = new ClientConfig();
		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget service = client.target(uri).queryParam("action", "query")
				.queryParam("format", "json")
				.queryParam("prop", "langlinks")
				.queryParam("titles", title)
				//.queryParam("pageids", pageid)
				.queryParam("llprop", "url|langname|autonym")
				.queryParam("lllimit", "500");
		
		System.out.println(service.toString());
		
		Response response = service.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).get();
		int httpStatus = response.getStatus();
		
		String json = response.readEntity(String.class);
		
		ArrayList<Article> articles = new ArrayList<Article>();
		
		if (httpStatus == 200) {
			JSONObject jobj = new JSONObject(json);
			JSONObject jquery = jobj.getJSONObject("query");
			JSONObject jpages = jquery.getJSONObject("pages");
			
			Iterator<String> keys = jpages.keys();
			
			while( keys.hasNext() ) {
			    String key = (String) keys.next();
			    System.out.println("Key: " + key);
			    jpages = jpages.getJSONObject(key);
				try {
					JSONArray jlanglinks = jpages.getJSONArray("langlinks");
					if (jlanglinks.length() >= 3) {
						ArrayList<LangLink> langlinks = new ArrayList<LangLink>();
						int count = 0;
						for (int i = 0; i < jlanglinks.length(); i++) {
							String lang = jlanglinks.getJSONObject(i).getString("lang");
							if (Arrays.asList(Constant.LANGUAGE).contains(lang)) {
								LangLink langlink = new LangLink(lang, jlanglinks.getJSONObject(i).getString("url"), jlanglinks.getJSONObject(i).getString("*"));
								langlinks.add(langlink);
								count = count + 1;
							}
						}
						if (count == 3) {
							Article article = new Article(jpages.getLong("pageid"), jpages.getString("title"), langlinks);
							articles.add(article);
						}
					}
				} catch (Exception e) {
					continue;
				}
			}
		}
		return articles;
    }
    
	public String getContentOfPage(String title, long pageid, String language) {
		pattern = Pattern.compile("%%");
		matcher = pattern.matcher(Constant.WIKI_API_URL);
		String wiki_api_url = "";
		if (matcher.find())
			wiki_api_url = matcher.replaceAll(language);
		// wiki_api_url = "https://fr.wikipedia.org/w/api.php";
		URI uri = UriBuilder.fromUri(wiki_api_url).build();
		System.out.println("Getting " + title + " content...");

		ClientConfig clientConfig = new ClientConfig();
		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget service = client.target(uri).queryParam("action", "query")
				.queryParam("format", "json")
				.queryParam("errorformat", "plaintext")
				.queryParam("prop", "revisions")
				.queryParam("titles", title)
				//.queryParam("pageids", pageid)
				.queryParam("rvprop", "ids|flags|timestamp|user|userid|size|contentmodel|comment|tags|content|parsetree|flagged")
				.queryParam("rvlimit", "1")
				.queryParam("rvgeneratexml", "1")
				.queryParam("rvdir", "older");

		System.out.println(service.toString());

		Response response = service.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).get();
		int httpStatus = response.getStatus();

		String json = response.readEntity(String.class);
		if (httpStatus == 200) {
			JSONObject jobj = new JSONObject(json);

			JSONObject jquery = jobj.getJSONObject("query");
			JSONObject jpages = jquery.getJSONObject("pages");

			Iterator<String> keys = jpages.keys();

			while (keys.hasNext()) {
				String key = (String) keys.next();
				// System.out.println("Key: " + key);
				jpages = jpages.getJSONObject(key);
				System.out.println("Page ID: " + jpages.getLong("pageid"));
				System.out.println("Page tile: " + jpages.getString("title"));
				JSONArray jrevisions = jpages.getJSONArray("revisions");
				for (int i = 0; i < jrevisions.length(); i++) {
					JSONObject jrevision = jrevisions.getJSONObject(i);
					System.out.println("Revision ID: " + jrevision.getLong("revid"));
					System.out.println("User: " + jrevision.getString("user"));
					System.out.println("Timestamp: " + jrevision.getString("timestamp"));

					HTML2WikiConverter conv = new HTML2WikiConverter();
					conv.setInputHTML(jrevision.getString("*"));
					String wikitext = conv.toWiki(new ToWikipedia());
					WikiModel wikiModel = new WikiModel("https://www.mywiki.com/wiki/${image}",	"https://www.mywiki.com/wiki/${title}");
					String plainStr = wikiModel.render(new PlainTextConverter(), wikitext);
					
					plainStr = normalizeContent(plainStr);

					System.out.println(plainStr);
					System.out.println("***********************************************************************************");
					return plainStr;
				}
			}
		}
		return null;
	}
	
	public ArrayList<Contribution> getListPagesContributedByUser(String username, String language, String start, String end) {
		pattern = Pattern.compile("%%");
		matcher = pattern.matcher(Constant.WIKI_API_URL);
		String wiki_api_url = "";
		if (matcher.find())
			wiki_api_url = matcher.replaceAll(language);
		//wiki_api_url = "https://it.wikipedia.org/w/api.php";
		URI uri = UriBuilder.fromUri(wiki_api_url).build();
		System.out.println("Getting page content...");

		ClientConfig clientConfig = new ClientConfig();
		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget service = client.target(uri).queryParam("action", "query")
				.queryParam("format", "json")
				.queryParam("list", "usercontribs")
				.queryParam("ucuser", username)
				.queryParam("uclimit", "500")
				.queryParam("ucdir", "newer")
				.queryParam("ucstart", start)
				.queryParam("ucend", end)
				.queryParam("ucprop", "ids|title|timestamp|comment|parsedcomment|size|sizediff");
		
		
		System.out.println(service.toString());
		
		Response response = service.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).get();
		int httpStatus = response.getStatus();
		
		String json = response.readEntity(String.class);
		ArrayList<Contribution> contributions = new ArrayList<Contribution>();
		
		if (httpStatus == 200) {
			JSONObject jobj = new JSONObject(json);
			JSONObject jquery = jobj.getJSONObject("query");
			JSONArray jusercontribs = jquery.getJSONArray("usercontribs");
			Contribution contribution;
			for (int i = 0; i < jusercontribs.length(); i ++) {
				if ((jusercontribs.getJSONObject(i).getLong("size") > 0) && ((jusercontribs.getJSONObject(i).getLong("sizediff")/jusercontribs.getJSONObject(i).getLong("size")) > 0.9)) {
					contribution = new Contribution(jusercontribs.getJSONObject(i).getLong("userid"), 
							jusercontribs.getJSONObject(i).getString("user"), 
							jusercontribs.getJSONObject(i).getString("title"), 
							jusercontribs.getJSONObject(i).getLong("pageid"), 
							jusercontribs.getJSONObject(i).getString("timestamp"), 
							jusercontribs.getJSONObject(i).getLong("size"), 
							jusercontribs.getJSONObject(i).getLong("sizediff"));
					contributions.add(contribution);
					
				    System.out.println("User ID: " + contribution.getUserid());
				    System.out.println("User name: " + contribution.getUser());
				    System.out.println("Title: " + contribution.getTitle());
				    System.out.println("Page ID: " + contribution.getPageid());
				    System.out.println("Timestamp: " + contribution.getTimestamp());
				    System.out.println("Size: " + contribution.getSize());
				    System.out.println("Size different: " + contribution.getSizediff());
				    System.out.println("***********************************************************************************");
				}
			 }
		}
		return contributions;
	}

	public String getContentOfPageByDate(String title, long pageid, String language, String timestamp) {
		pattern = Pattern.compile("%%");
		matcher = pattern.matcher(Constant.WIKI_API_URL);
		String wiki_api_url = "";
		if (matcher.find())
			wiki_api_url = matcher.replaceAll(language);
		// wiki_api_url = "https://fr.wikipedia.org/w/api.php";
		URI uri = UriBuilder.fromUri(wiki_api_url).build();
		System.out.println("Getting " + title + " content...");

		ClientConfig clientConfig = new ClientConfig();
		Client client = ClientBuilder.newClient(clientConfig);
		
		WebTarget service = client.target(uri).queryParam("action", "query")
				.queryParam("format", "json")
				.queryParam("errorformat", "plaintext")
				.queryParam("prop", "revisions")
				.queryParam("titles", title)
				//.queryParam("pageids", pageid)
				.queryParam("rvprop", "ids|flags|timestamp|user|userid|size|contentmodel|comment|tags|content|parsetree|flagged")
				.queryParam("rvlimit", "1")
				.queryParam("rvgeneratexml", "1")
				.queryParam("rvstart", timestamp)
				.queryParam("rvdir", "older");

		System.out.println(service.toString());

		Response response = service.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).get();
		int httpStatus = response.getStatus();

		String json = response.readEntity(String.class);
		if (httpStatus == 200) {
			JSONObject jobj = new JSONObject(json);

			JSONObject jquery = jobj.getJSONObject("query");
			JSONObject jpages = jquery.getJSONObject("pages");

			Iterator<String> keys = jpages.keys();

			while (keys.hasNext()) {
				try {
					String key = (String) keys.next();
					// System.out.println("Key: " + key);
					jpages = jpages.getJSONObject(key);
					System.out.println("Page ID: " + jpages.getLong("pageid"));
					System.out.println("Page tile: " + jpages.getString("title"));
					JSONArray jrevisions = jpages.getJSONArray("revisions");
					for (int i = 0; i < jrevisions.length(); i++) {
						JSONObject jrevision = jrevisions.getJSONObject(i);
						System.out.println("Revision ID: " + jrevision.getLong("revid"));
						System.out.println("User: " + jrevision.getString("user"));
						System.out.println("Timestamp: " + jrevision.getString("timestamp"));
	
						HTML2WikiConverter conv = new HTML2WikiConverter();
						conv.setInputHTML(cutContent(jrevision.getString("*")));
						String wikitext = conv.toWiki(new ToWikipedia());
						WikiModel wikiModel = new WikiModel("https://www.mywiki.com/wiki/${image}",	"https://www.mywiki.com/wiki/${title}");
						String plainStr = wikiModel.render(new PlainTextConverter(), wikitext);
						
						plainStr = normalizeContent(plainStr);
	
						System.out.println(plainStr);
						System.out.println("***********************************************************************************");
						return plainStr;
					}
				}catch (Exception e) {
					return null;
				}
			}
		}
		return null;
	}
	
	public String cutContent(String content) {
		String[] stps = {"== Siehe auch ==", "== Literatur ==", "== Weblinks ==", "== Einzelnachweise ==", "== Quellen ==",
				"== See also ==", "== References ==", "== External links ==", "== Further reading ==", "== Sources ==",
				"== Notes et références ==", "== Voir aussi ==", "== Articles connexes ==", "== Lien externe ==", "== Liens extérieurs ==", "== Bibliographie ==", "== Notes ==",
				"== Note ==", "== Bibliografia ==", "== Altri progetti ==", "== Collegamenti esterni ==", "== Collegamenti esterni ==", "== Voci correlate ==", "== Fonti e bibliografia ==",
				"==Siehe auch==", "==Literatur==", "==Weblinks==", "==Einzelnachweise==", "==Quellen==",
				"==See also==", "==References==", "==External links==", "==Further reading==", "==Sources==",
				"==Notes et références==", "==Voir aussi==", "==Articles connexes==", "==Lien externe==", "==Liens extérieurs==", "==Bibliographie==", "==Notes==",
				"==Note==", "==Bibliografia==", "==Altri progetti==", "==Collegamenti esterni==", "==Collegamenti esterni==", "==Voci correlate==", "==Fonti e bibliografia=="};
		int min = 0;
		boolean first = true;
		for (String stp: stps) {
			pattern = Pattern.compile(stp);
			matcher = pattern.matcher(content);
	        while (matcher.find()) {
	        	int point = matcher.start();
	        	if ((first == true) && (min == 0)) {
	        		min = point;
	        		first = false;
	        	}
	        	if (min > point) {
		        	min = point;
	        	}
	        }
		}
		if (min != 0) {
			content = content.substring(0, min);
		}
//		System.out.println(content);
		return content;
	}
	
	public String normalizeContent(String content) {
		String[] contentArr = content.split("\n");
		
		content = "";
		for (int j = 0; j < contentArr.length; j++) {
			int length = contentArr[j].split(" ").length;
			if (length > 20) {
				content = content + contentArr[j] + "\n";
			}
		}
		
		for (String regex : Constant.REMOVING_PATTERN) {
			pattern = Pattern.compile(regex);
			matcher = pattern.matcher(content);

			if (matcher.find())
				content = matcher.replaceAll(" ");
		}
		
		pattern = Pattern.compile("\\{\\{");
		matcher = pattern.matcher(content);
		ArrayList<Integer> starts = new ArrayList<Integer>();
        while (matcher.find()) {
        	starts.add(matcher.start());
        }
		
		
		pattern = Pattern.compile("\\}\\}");
		matcher = pattern.matcher(content);
		ArrayList<Integer> ends = new ArrayList<Integer>();
		while (matcher.find()) {
        	ends.add(matcher.end());
        }
		
		StringBuffer contentbuf = new StringBuffer(content);
		
		int complement = 0;
		if (starts.size() == ends.size()) {
			for (int i = 0; i < starts.size(); i ++) {
				System.out.println(starts.get(i) + "||||" + ends.get(i));
				contentbuf.replace(starts.get(i)-complement, ends.get(i)-complement, "");
				complement = complement + ends.get(i);
			}
		}
		content = contentbuf.toString();
		
		//System.out.println(content);
		
		return content;
	}
}


//should remember we can use both id and name of topic


//get current revision
//https://en.wikipedia.org/w/api.php?action=query&action=query&format=json&errorformat=plaintext&prop=revisions&titles=Apple&rvprop=ids|flags|timestamp|user|userid|size|contentmodel|comment|tags|content|parsetree|flagged&rvlimit=10&rvgeneratexml=1&rvsection=&rvdir=older&rvtoken=
//https://en.wikipedia.org/w/api.php?action=query&action=query&format=json&errorformat=plaintext&prop=revisions&titles=Apple&rvprop=ids|flags|timestamp|user|userid|size|contentmodel|comment|tags|content|parsetree|flagged&rvlimit=10&rvgeneratexml=1&rvsection=&rvdir=older&rvtoken=
//https://en.wikipedia.org/wiki/Special:ApiSandbox#action=query&format=json&errorformat=plaintext&prop=revisions&titles=Apple&rvprop=ids%7Cflags%7Ctimestamp%7Cuser%7Cuserid%7Csize%7Ccontentmodel%7Ccomment%7Ctags%7Ccontent%7Cparsetree%7Cflagged&rvlimit=10&rvgeneratexml=1&rvsection=&rvdir=older&rvtoken=
//https://en.wikipedia.org/w/api.php?action=query&prop=revisions&format=jsonfm&titles=Apple&rvprop=content


//get list of topic contributed by user
//https://fr.wikipedia.org/w/api.php?action=query&list=usercontribs&ucuser=Financier73&uclimit=500&ucdir=newer&ucstart=2014-03-01T20%3A39%3A12.000Z&ucend=2014-04-01T20%3A39%3A12.000Z&ucprop=ids|title|timestamp|comment|parsedcomment|size|sizediff&format=json
//https://fr.wikipedia.org/w/api.php?action=query&list=usercontribs&ucuser=Financier73&uclimit=500&ucdir=newer&ucstart=2014-03-01T20%3A39%3A12.000Z&ucend=2014-04-01T20%3A39%3A12.000Z&ucprop=ids|title|timestamp|comment|parsedcomment|size|sizediff&format=json
//https://fr.wikipedia.org/w/api.php?action=query&list=usercontribs&ucuser=Financier73&uclimit=500&ucdir=newer&ucstart=2014-03-01T20%3A39%3A12.000Z&ucend=2014-04-01T20%3A39%3A12.000Z&ucprop=ids|title|timestamp|comment|parsedcomment|size|sizediff&format=json


//get list of older revision
//https://en.wikipedia.org/wiki/Special:ApiSandbox#action=query&format=json&prop=revisions&list=&meta=&titles=Main+Page&rvprop=content%7Ctimestamp&rvlimit=50&rvstart=2016-11-01T18%3A02%3A01.000Z&rvdir=older
//https://en.wikipedia.org/wiki/Special:ApiSandbox#action=query&format=json&prop=revisions&list=&meta=&titles=Main+Page&rvprop=content%7Ctimestamp&rvlimit=50&rvstart=2016-11-01T18%3A02%3A01.000Z&rvdir=older
//https://en.wikipedia.org/wiki/Special:ApiSandbox#action=query&format=json&prop=revisions&list=&meta=&titles=Main+Page&rvprop=content%7Ctimestamp&rvlimit=50&rvstart=2016-11-01T18%3A02%3A01.000Z&rvdir=older
///w/api.php?action=query&format=json&prop=revisions&list=&meta=&titles=Main+Page&rvprop=content%7Ctimestamp&rvlimit=50&rvstart=2016-11-01T18%3A02%3A01.000Z&rvdir=older
///w/api.php?action=query&format=json&prop=revisions&list=&meta=&titles=Main+Page&rvprop=content%7Ctimestamp&rvlimit=50&rvstart=2016-11-01T18%3A02%3A01.000Z&rvdir=older

//get list of languages of a topic
//https://en.wikipedia.org/wiki/Special:ApiSandbox#action=query&format=json&prop=langlinks&titles=FS+Class+E.428&llprop=url%7Clangname%7Cautonym&lllimit=500
//https://en.wikipedia.org/wiki/Special:ApiSandbox#action=query&format=json&prop=langlinks&titles=FS+Class+E.428&llprop=url%7Clangname%7Cautonym&lllimit=500
///w/api.php?action=query&format=json&prop=langlinks&titles=FS+Class+E.428&llprop=url%7Clangname%7Cautonym&lllimit=500
///w/api.php?action=query&format=json&prop=langlinks&titles=FS+Class+E.428&llprop=url%7Clangname%7Cautonym&lllimit=500
//https://fr.wikipedia.org/wiki/Sp%C3%A9cial:ApiSandbox#action=query&format=json&prop=langlinks&pageids=7828508%7C7828641%7C7830709&llprop=url%7Clangname%7Cautonym&lllimit=500
//https://fr.wikipedia.org/wiki/Sp%C3%A9cial:ApiSandbox#action=query&format=json&prop=langlinks&pageids=7828508%7C7828641%7C7830709&llprop=url%7Clangname%7Cautonym&lllimit=500
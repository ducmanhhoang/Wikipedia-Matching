package crawler;

import java.util.ArrayList;

public class Article {
	public long pageid;
	public String title;
	public ArrayList<LangLink> langlinks;

	public Article(long pageid, String title, ArrayList<LangLink> langlinks) {
		super();
		this.pageid = pageid;
		this.title = title;
		this.langlinks = langlinks;
	}

	public long getPageid() {
		return pageid;
	}

	public void setPageid(long pageid) {
		this.pageid = pageid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public ArrayList<LangLink> getLanglinks() {
		return langlinks;
	}

	public void setLanglinks(ArrayList<LangLink> langlinks) {
		this.langlinks = langlinks;
	}

	@Override
	public String toString() {
		return "Topic [pageid=" + pageid + ", title=" + title + ", langlinks=" + langlinks + "]";
	}
}

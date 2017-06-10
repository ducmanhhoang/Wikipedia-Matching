package crawler;

class LangLink {
	public String language;
	public String url;
	public String title;

	public LangLink(String language, String url, String title) {
		super();
		this.language = language;
		this.url = url;
		this.title = title;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		return "LangLink [language=" + language + ", url=" + url + ", title=" + title + "]";
	}

}

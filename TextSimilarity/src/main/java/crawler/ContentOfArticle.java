package crawler;
import java.util.HashMap;

public class ContentOfArticle {
	public String title;
	public HashMap<String, String> content;
	public ContentOfArticle(String title) {
		super();
		this.title = title;
		this.content = new HashMap<String, String>();
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public HashMap<String, String> getContent() {
		return content;
	}
	public void setContent(HashMap<String, String> content) {
		this.content = content;
	}
	@Override
	public String toString() {
		return "ContentOfTopic [title=" + title + ", content=" + content + "]";
	}
}

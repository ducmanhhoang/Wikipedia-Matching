package crawler;

public class Contribution {
	public long userid;
    public String user;
    public String title;
    public long pageid;
    public String timestamp;
    public long size;
    public long sizediff;
	public Contribution(long userid, String user, String title, long pageid, String timestamp, long size,
			long sizediff) {
		super();
		this.userid = userid;
		this.user = user;
		this.title = title;
		this.pageid = pageid;
		this.timestamp = timestamp;
		this.size = size;
		this.sizediff = sizediff;
	}
	public long getUserid() {
		return userid;
	}
	public void setUserid(long userid) {
		this.userid = userid;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public long getPageid() {
		return pageid;
	}
	public void setPageid(long pageid) {
		this.pageid = pageid;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public long getSizediff() {
		return sizediff;
	}
	public void setSizediff(long sizediff) {
		this.sizediff = sizediff;
	}
	@Override
	public String toString() {
		return "Contribution [userid=" + userid + ", user=" + user + ", title=" + title + ", pageid=" + pageid
				+ ", timestamp=" + timestamp + ", size=" + size + ", sizediff=" + sizediff + "]";
	}
    
}

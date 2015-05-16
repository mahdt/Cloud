package Cloud.Tweets;

import java.io.Serializable;

public class MyStatus {
	
	private long id;
	private String text;
	public MyStatus() {
	}

	public MyStatus(long id, String text) {
		this.id = id;
		this.text = text;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}


	

}

package br.unioeste.sd.news;

public class Noticia {

	private int id;
	private String title;
	private String text;
	
	public Noticia(int id, String title, String text) {
		super();
		this.id = id;
		this.title = title;
		this.text = text;
	}
	
	public Noticia(){}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	
}

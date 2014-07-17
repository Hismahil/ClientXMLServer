package br.unioeste.sd.news;

import java.util.List;

public class Assunto {

	private String type;
	private List<Noticia> noticias;
	
	public Assunto(String type, List<Noticia> noticias) {
		super();
		this.type = type;
		this.noticias = noticias;
	}

	public Assunto(){}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Noticia> getNoticias() {
		return noticias;
	}

	public void setNoticias(List<Noticia> noticias) {
		this.noticias = noticias;
	}
	
	
}

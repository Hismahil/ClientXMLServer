package br.unioeste.sd.news;

import java.util.List;


public class Noticias {

	private List<Assunto> assuntos;
	
	public Noticias(List<Assunto> assuntos) {
		super();
		this.assuntos = assuntos;
	}

	public Noticias(){}
	
	public List<Assunto> getAssuntos() {
		return assuntos;
	}

	public void setAssuntos(List<Assunto> assuntos) {
		this.assuntos = assuntos;
	}
	
	
}

package br.unioeste.sd.main;

import java.util.ArrayList;
import java.util.List;

import br.unioeste.sd.client.Client;
import br.unioeste.sd.msg.Command;
import br.unioeste.sd.news.Assunto;
import br.unioeste.sd.news.Noticia;
import br.unioeste.sd.error.Error;

public class Main {

	public static void main(String[] args) {
		Client client = new Client("127.0.0.1", 7777);
		client.connect();
		Command cmd = new Command();
		
		List<Assunto> assuntos = new ArrayList<Assunto>();
		
		Error err = client.getSubjectsFromServer(assuntos);
		
		for(int i = 0; i < assuntos.size(); i++){
			System.out.println(assuntos.get(i).getType());
		}
		
		List<Noticia> noticias = new ArrayList<Noticia>();
		
		err = client.getNewsFromServer(assuntos.get(1).getType(), noticias);
		
		for(int i = 0; i < noticias.size(); i++){
			System.out.println(noticias.get(i).getId());
			System.out.println(noticias.get(i).getTitle());
			System.out.println(noticias.get(i).getText());
		}
		
		assuntos.get(1).setNoticias(noticias);
		
		err = client.removeNews(assuntos.get(1).getType(), 1, cmd);
		
		System.out.println(cmd.getType());
		System.out.println(cmd.getCmdString());
		
		Noticia noticia = new Noticia(4, "Corrupcao", "corrupt");
		
		err = client.insertNews(assuntos.get(1).getType(), noticia, cmd);
		
		System.out.println(cmd.getType());
		System.out.println(cmd.getCmdString());
		
		noticia.setText("Brasil pais da palhacada");
		
		err = client.updateNews(assuntos.get(1).getType(), noticia, cmd);
		
		System.out.println(cmd.getType());
		System.out.println(cmd.getCmdString());
		
		Assunto assunto = new Assunto();
		err = client.insertSubject("tecnologia", assunto, cmd);
		assuntos.add(assunto);
		
		System.out.println(assuntos.get(3).getType());
		
		err = client.close(cmd);
		
		System.out.println(cmd.getType());
		System.out.println(cmd.getCmdString());
	}

}

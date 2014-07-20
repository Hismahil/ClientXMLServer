package br.unioeste.sd.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import br.unioeste.sd.client.Client;
import br.unioeste.sd.msg.Command;
import br.unioeste.sd.news.Assunto;
import br.unioeste.sd.news.Noticia;
import br.unioeste.sd.news.Noticias;
import br.unioeste.sd.error.Error;

public class Main {

	private Noticias noticias = new Noticias();
	private BufferedReader br = null;
	private Client client = null;
	
	public Main(){
		client = new Client("192.168.25.6", 7777);
		client.connect();
	}
	
	public void loop() throws NumberFormatException, IOException{
		br = new BufferedReader(new InputStreamReader(System.in));
		Error err = null;
		Command msg = new Command();
		boolean connected = true;
		
		while(connected){
			switch(cmd()){
				case 1: getSubjects(); break;
				case 2: getNews(); break;
				case 3: insertSubjects(); break;
				case 4: insertNews(); break;
				case 5: updateNews(); break;
				case 6: removeNews(); break;
				case 7: showSubjects(); break;
				case 8: showNews(); break;
				case 9: 
					err = client.close(msg);
					
					System.out.println(msg.getType());
					System.out.println(msg.getCmdString());
					connected = false;
					break;
			}
		}
	}
	
	private int cmd() throws NumberFormatException, IOException{
		System.out.println("----------------------------------------------------------------");
		System.out.println("(1) Requisitar assuntos.");
		System.out.println("(2) Requisitar noticias.");
		System.out.println("(3) Inserir novo assunto.");
		System.out.println("(4) Inserir nova noticia.");
		System.out.println("(5) Atualizar uma noticia.");
		System.out.println("(6) Remover uma noticia.");
		System.out.println("(7) Mostrar assuntos.");
		System.out.println("(8) Mostrar noticias.");
		System.out.println("(9) Fechar.");
		System.out.println(">> ");
		int i = Integer.parseInt(br.readLine());
		
		return i;
	}
	
	private void getSubjects() throws IOException{
		List<Assunto> assuntos = null;
		Error err = null;
		System.out.println("----------------------------------------------------------------");
		System.out.print("Pegar assuntos do servidor (s/n): ");
		String res = br.readLine();
		System.out.println();
		
		if(res.equals("s")){
			assuntos = new ArrayList<Assunto>();
			err = client.getSubjectsFromServer(assuntos);
			
			if(err != null){
				System.out.println(err.getTitle());
				System.out.println(err.getMsg());
				return;
			}
			
			System.out.println("Assuntos: ");
			for(int i = 0; i < assuntos.size(); i++)
				System.out.println(assuntos.get(i).getType());
			
			noticias.setAssuntos(assuntos);
		}
		else if(res.equals("n")){
			System.out.println("Verificando assuntos locais...");
			if(noticias.getAssuntos() != null)
				for(int i = 0; i < noticias.getAssuntos().size(); i++)
					System.out.println(noticias.getAssuntos().get(i).getType());
			else System.out.println("Não existe assuntos locais!");
		}
		else{
			System.out.println("Opção inválida!");
		}
		System.out.println("----------------------------------------------------------------");
	}
	
	private void getNews() throws IOException{
		List<Noticia> noticia = null;
		Error err = null;
		System.out.println("----------------------------------------------------------------");
		if(noticias.getAssuntos() == null){
			System.out.println("Não é possivel pegar noticias se não existe um assunto local!");
			return;
		}
		
		System.out.print("Digite o assunto: ");
		String assunto = br.readLine();
		System.out.println();
		
		System.out.print("Buscar no servidor (s/n): ");
		String res = br.readLine();
		System.out.println();
		
		if(res.equals("s")){
			if(hasAssunto(noticias.getAssuntos(), assunto)){
				noticia = new ArrayList<Noticia>();
				
				err = client.getNewsFromServer(assunto, noticia);
				
				if(err != null){
					System.out.println(err.getTitle());
					System.out.println(err.getMsg());
					return;
				}
				else{
					System.out.println("Noticias sobre " + assunto);
					for(int i = 0; i < noticia.size(); i++){
						System.out.println(noticia.get(i).getId());
						System.out.println(noticia.get(i).getTitle());
						System.out.println(noticia.get(i).getText());
					}
					
					noticias.getAssuntos().get(indexOfAssunto(noticias.getAssuntos(), assunto)).setNoticias(noticia);;
				}
			}
			else{
				System.out.println("O assunto não existe local!");
			}
		}
		else if(res.equals("n")){
			if(hasAssunto(noticias.getAssuntos(), assunto)){
				noticia = noticias.getAssuntos().get(indexOfAssunto(noticias.getAssuntos(), assunto)).getNoticias();
				
				System.out.println("Noticias sobre " + assunto);
				for(int i = 0; i < noticia.size(); i++){
					System.out.println(noticia.get(i).getId());
					System.out.println(noticia.get(i).getTitle());
					System.out.println(noticia.get(i).getText());
				}
			}
			else{
				System.out.println("O assunto não existe local!");
			}
		}
		else{
			System.out.println("Opção inválida!");
		}
		System.out.println("----------------------------------------------------------------");
	}
	
	private boolean hasAssunto(List<Assunto> assuntos, String assunto){
		
		for(int i = 0; i < assuntos.size(); i++){
			if(assuntos.get(i).getType().equals(assunto)) return true;
		}
		
		return false;
	}
	
	private int indexOfAssunto(List<Assunto> assuntos, String assunto){
		
		for(int i = 0; i < assuntos.size(); i++)
			if(assuntos.get(i).getType().equals(assunto)) return i;
		
		return -1;
	}
	
	private void insertSubjects() throws IOException{
		System.out.println("----------------------------------------------------------------");
		System.out.print("Digite o nome assunto: ");
		String type = br.readLine();
		System.out.println();
		
		Assunto assunto = new Assunto();
		Command msg = new Command();
		
		Error err = client.insertSubject(type, assunto, msg);
		
		if(err != null){
			System.out.println(err.getTitle());
			System.out.println(err.getMsg());
		}
		else{
			System.out.println(msg.getType());
			System.out.println(msg.getCmdString());
			noticias.getAssuntos().add(assunto);
		}
		System.out.println("----------------------------------------------------------------");
	}
	
	private void insertNews() throws IOException{
		System.out.println("----------------------------------------------------------------");
		
		if(noticias.getAssuntos() == null){
			System.out.println("Não existe assunto local!");
			return;
		}
		
		System.out.print("Digite o assunto: ");
		String assunto = br.readLine();
		System.out.println();
		
		Noticia noticia = null;
		Command msg = new Command();
		Error err = null;
		
		if(hasAssunto(noticias.getAssuntos(), assunto)){
			noticia = new Noticia();
			msg = new Command();
			
			System.out.print("Digite o ID: ");
			noticia.setId(Integer.parseInt(br.readLine()));
			System.out.println();
			
			System.out.print("Digite o título: ");
			noticia.setTitle(br.readLine());
			System.out.println();
			
			System.out.print("Digite o texto: ");
			noticia.setText(br.readLine());
			
			err = client.insertNews(assunto, noticia, msg);
			
			if(err != null){
				System.out.println(err.getTitle());
				System.out.println(err.getMsg());
			}
			else{
				System.out.println(msg.getType());
				System.out.println(msg.getCmdString());
				List<Noticia> lista = new ArrayList<Noticia>();
				lista.add(noticia);
				if(noticias.getAssuntos().get(indexOfAssunto(noticias.getAssuntos(), assunto)).getNoticias() == null)
					noticias.getAssuntos().get(indexOfAssunto(noticias.getAssuntos(), assunto)).setNoticias(lista);
				else
					noticias.getAssuntos().get(indexOfAssunto(noticias.getAssuntos(), assunto)).getNoticias().add(noticia);
			}
		}
		else{
			System.out.println("O assunto não existe local!");
		}
		
		System.out.println("----------------------------------------------------------------");
	}

	private void updateNews() throws IOException{
		System.out.println("----------------------------------------------------------------");
		
		if(noticias.getAssuntos() == null){
			System.out.println("Não existe assuntos local!");
			return;
		}
		
		System.out.print("Digite o assunto: ");
		String assunto = br.readLine();
		System.out.println();
		
		System.out.print("Digite o ID da noticia: ");
		int id = Integer.parseInt(br.readLine());
		System.out.println();
		
		Noticia noticia = null;
		Command msg = new Command();
		Error err = null;
		
		if(hasAssunto(noticias.getAssuntos(), assunto)){
			noticia = hasNoticia(noticias.getAssuntos().get(indexOfAssunto(noticias.getAssuntos(), assunto)).getNoticias(), id);
			
			if(noticia == null){
				System.out.println("Não foi encontrado a noticia nesse assunto: " + assunto);
				return;
			}
			
			System.out.println(noticia.getId());
			System.out.println(noticia.getTitle());
			System.out.println(noticia.getText());
			
			System.out.println("Insira os novos dados... ");
			System.out.print("Digite o titulo: ");
			noticia.setTitle(br.readLine());
			System.out.println();
			
			System.out.print("Digite o texto: ");
			noticia.setText(br.readLine());
			System.out.println();
			
			err = client.updateNews(assunto, noticia, msg);
			
			if(err != null){
				System.out.println(err.getTitle());
				System.out.println(err.getMsg());
			}
			else{
				System.out.println(msg.getType());
				System.out.println(msg.getCmdString());
			}
		}
		else{
			System.out.println("O assunto não existe local!");
		}
	}
	
	private Noticia hasNoticia(List<Noticia> noticia, int id){
		
		for(int i = 0; i < noticia.size(); i++)
			if(noticia.get(i).getId() == id) return noticia.get(i);
		
		return null;
	}
	
	private void removeNews() throws IOException{
		System.out.println("----------------------------------------------------------------");
		
		if(noticias.getAssuntos() == null){
			System.out.println("Não existe assuntos local!");
			return;
		}
		
		System.out.print("Digite o assunto: ");
		String assunto = br.readLine();
		System.out.println();
		
		System.out.print("Digite o ID da noticia: ");
		int id = Integer.parseInt(br.readLine());
		System.out.println();
		
		Noticia noticia = null;
		Command msg = new Command();
		Error err = null;
		
		if(hasAssunto(noticias.getAssuntos(), assunto)){
			noticia = hasNoticia(noticias.getAssuntos().get(indexOfAssunto(noticias.getAssuntos(), assunto)).getNoticias(), id);
			
			if(noticia == null){
				System.out.println("Não foi encontrado a noticia nesse assunto: " + assunto);
				return;
			}
			
			err = client.removeNews(assunto, id, msg);
			
			if(err != null){
				System.out.println(err.getTitle());
				System.out.println(err.getMsg());
			}
			else{
				System.out.println(msg.getType());
				System.out.println(msg.getCmdString());
				noticias.getAssuntos().get(indexOfAssunto(noticias.getAssuntos(), assunto)).getNoticias().remove(noticia);
			}
		}
		else{
			System.out.println("O assunto não existe local!");
		}
		
	}
	
	private void showSubjects(){
		System.out.println("----------------------------------------------------------------");
		
		if(noticias.getAssuntos() == null){
			System.out.println("Não existe assuntos local!");
			return;
		}
		else{
			for(int i = 0; i < noticias.getAssuntos().size(); i++)
				System.out.println(noticias.getAssuntos().get(i).getType());
		}
		System.out.println("----------------------------------------------------------------");
	}
	
	private void showNews() throws IOException{
		System.out.println("----------------------------------------------------------------");
		
		if(noticias.getAssuntos() == null){
			System.out.println("Não existe assuntos local!");
			return;
		}
		else{
			List<Noticia> lista = null;
			System.out.print("Digite o assunto: ");
			String assunto = br.readLine();
			System.out.println();
			
			if(hasAssunto(noticias.getAssuntos(), assunto)){
				lista = noticias.getAssuntos().get(indexOfAssunto(noticias.getAssuntos(), assunto)).getNoticias();
				
				System.out.println("Lista de noticias do assunto: " + assunto);
				for(int i = 0; i < lista.size(); i++){
					System.out.println(lista.get(i).getId());
					System.out.println(lista.get(i).getTitle());
					System.out.println(lista.get(i).getText());
				}
			}
			
		}
		System.out.println("----------------------------------------------------------------");
	}
	
	public static void main(String[] args) throws NumberFormatException, IOException {
		Main m = new Main();
		m.loop();
	}

}

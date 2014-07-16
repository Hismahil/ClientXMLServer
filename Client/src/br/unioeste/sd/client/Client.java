package br.unioeste.sd.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import br.unioeste.sd.xml.dao.XmlDao;
import br.unioeste.sd.xml.factory.XmlConnectionFactory;
import br.unioeste.sd.xml.util.Util;

public class Client {

	private Socket socket = null;
	private DataInputStream in = null;
	private DataOutputStream out = null;
	private String ip;
	private int port;
	private Scanner sc = null;
	
	private Document news;
	private Document cmd;
	private Document error;
	private XmlDao xmlDao;
	
	private boolean isConnected = true;
	
	public Client(String ip, int port){
		this.ip = ip;
		this.port = port;
		xmlDao = new XmlDao();
	}
	
	public void connect(){
		
		try{
			socket = new Socket(ip, port);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			sc = new Scanner(System.in);
			
			while(isConnected){
				showOptions();
				
				switch(sc.nextInt()){
					case 1: getSubjects(); break;
					case 2: showSubjects(); break;
					case 3: getNews(); break;
					case 4: showNews(); break;
					case 5: insertNews(); break;
					case 6: close(); break;
					default: break;
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(socket != null)
				try{
					socket.close();
				}catch(IOException e){
					e.printStackTrace();
				}
		}
	}
	
	private void showOptions(){
		System.out.println("------------------------------------------------------------");
		System.out.println("(1) Requisitar assuntos.");
		System.out.println("(2) Visualizar assuntos.");
		System.out.println("(3) Requisitar noticias.");
		System.out.println("(4) Visualizar noticias.");
		System.out.println("(5) Inserir noticia em um assunto.");
		System.out.println("(6) Fechar.");
		System.out.println(">> ");
	}
	
	private void insertNews(){
		System.out.println("------------------------------------------------------------");
		System.out.print("Qual o assunto: ");
		String type = sc.nextLine();
		//se nao for nenhum assunto
		if(!type.equals("futebol") || !type.equals("politica") || !type.equals("economia")){
			System.out.println("Assunto inexistente!");
			return;
		}
		
		System.out.println();
		
		System.out.print("ID da noticia: ");
		int id = sc.nextInt();
		System.out.println();
		
		System.out.println("Titulo da noticia: ");
		String title = sc.nextLine();
		System.out.println();
		
		System.out.println("Comentario: ");
		String comment = sc.nextLine();
		System.out.println("------------------------------------------------------------");
		
		String xpath = "/noticias/assunto[@type='" + type + "']";
		//insere local
		xmlDao.insert(xpath, news, id, title, comment);
		//transforma para string
		String temp = Util.toString(news);
		//cria novo doc
		Document outDoc = Util.toXml(temp, "noticias.xsd");
		//remove todas as noticias
		xmlDao.remove("/noticias/assunto[@type='futebol']//noticia", outDoc);
		xmlDao.remove("/noticias/assunto[@type='politica']//noticia", outDoc);
		xmlDao.remove("/noticias/assunto[@type='economia']//noticia", outDoc);
		//insere apenas a nova noticia
		xmlDao.insert(xpath, outDoc, id, title, comment);
		
		cmd = XmlConnectionFactory.getDocument("cmd.xml", "cmd.xsd");
		xmlDao.update("/cmd/type", cmd, "INSERT");
		xmlDao.update("/cmd/cmdString", cmd, xpath);
		
		try {
			out.writeUTF(Util.toString(cmd));
			out.writeUTF(Util.toString(outDoc)); //envia a nova noticia
		} catch (IOException e) { e.printStackTrace(); }
		
		System.out.println("Assunto inserido com sucesso!");
	}
	
	private void close(){
		String temp = null;
		
		cmd = XmlConnectionFactory.getDocument("cmd.xml", "cmd.xsd");
		xmlDao.update("/cmd/type", cmd, "CLOSE REQUEST");
		xmlDao.update("/cmd/cmdString", cmd, "CLOSE CONNECTION");
		
		try {
			out.writeUTF(Util.toString(cmd));
			temp = in.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Document reply = Util.toXml(temp, "cmd.xsd");
		System.out.println(reply.getElementsByTagName("type").item(0).getTextContent());
		System.out.println(reply.getElementsByTagName("cmdString").item(0).getTextContent());
		isConnected = false;
	}
	
	private void getSubjects(){
		String temp = null;
		
		cmd = XmlConnectionFactory.getDocument("cmd.xml", "cmd.xsd");
		xmlDao.update("/cmd/type", cmd, "SELECT");
		xmlDao.update("/cmd/cmdString", cmd, "/noticias/assunto");
	
		try {
			out.writeUTF(Util.toString(cmd));
			temp = in.readUTF();
			news = Util.toXml(temp, "noticias.xsd");
			
		} catch (IOException e) { e.printStackTrace(); }
		
		if(temp.contains("<assunto>")) news = Util.toXml(temp, "noticias.xsd");
		
		if(temp.contains("<error>")) showError(temp);
	}
	
	private void showSubjects(){
		NodeList list = xmlDao.select("/noticias//assunto", news);
		
		System.out.println("------------------------------------------------------------");
		for(int i = 0; i < list.getLength(); i++){
			Element attrib = (Element) list.item(i);
			System.out.println(attrib.getAttribute("type"));
		}
		System.out.println("------------------------------------------------------------");
	}
	
	private void getNews(){
		String temp = null;
		System.out.println("------------------------------------------------------------");
		System.out.print("Noticias de qual assunto: ");
		temp = sc.nextLine();
		System.out.println("------------------------------------------------------------");
		
		temp = "futebol";
		
		String xpathIn = "/noticias/assunto[@type='"+temp+"']//noticia";
		String xpathOut = "/noticias/assunto[@type='" + temp + "']";
		
		cmd = XmlConnectionFactory.getDocument("cmd.xml", "cmd.xsd");
		xmlDao.update("/cmd/type", cmd, "SELECT");
		xmlDao.update("/cmd/cmdString", cmd, xpathIn);
		
		try {
			out.writeUTF(Util.toString(cmd));
			temp = in.readUTF();
			System.out.println(temp);
		} catch (IOException e) { e.printStackTrace(); }
		
		if(temp.contains("noticia")){
			Document reply = Util.toXml(temp, "noticias.xsd");
			Util.merge(xpathIn, xpathOut, reply, news, xmlDao);
		}
		
		if(temp.contains("<error>")) showError(temp);
	}
	
	private void showNews(){
		String temp = null;
		System.out.println("------------------------------------------------------------");
		System.out.print("Noticias de qual assunto: ");
		temp = sc.nextLine();
		System.out.println("------------------------------------------------------------");
		
		temp = "futebol";
		
		String xpath = "/noticias/assunto[@type='"+temp+"']";
		
		NodeList list = xmlDao.select(xpath, news);
		
		/*for(int i = 0; i < list.getLength(); i++){
			Element node = (Element) list.item(i);
			
			System.out.println("ID: " + node.getAttribute("id"));
			System.out.println("Titulo: " + node.getElementsByTagName("title").item(0).getTextContent());
			System.out.println("Comentario: " + node.getElementsByTagName("comment").item(0).getTextContent());
			System.out.println("------------------------------------------------------------");
		}*/
		System.out.println(Util.toString(news));
	}
	
	private void showError(String temp){
		error = Util.toXml(temp, "error.xsd");
		System.out.println("------------------------------------------------------------");
		System.out.println(error.getElementsByTagName("title").item(0).getTextContent());
		System.out.println(error.getElementsByTagName("msg").item(0).getTextContent());
		System.out.println("------------------------------------------------------------");
	}
}

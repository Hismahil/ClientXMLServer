package br.unioeste.sd.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import br.unioeste.sd.news.News;
import br.unioeste.sd.xml.dao.XmlDao;
import br.unioeste.sd.xml.factory.XmlConnectionFactory;
import br.unioeste.sd.xml.util.Util;

public class ServerThread extends Thread{

	private Socket client;
	private DataInputStream in;
	private DataOutputStream out;
	private int clientID;
	private boolean conectado = true;
	
	private Document cmd;
	private Document error;
	private XmlDao xmlDao;
	
	public ServerThread(Socket client, int clientID){
		this.client = client;
		this.clientID = clientID;
		
		try {
			in = new DataInputStream(client.getInputStream());
			out = new DataOutputStream(client.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public synchronized void run() {
		try{
			xmlDao = new XmlDao();
			
			while(conectado){
				
				switch(command()){
					case 1: selectSubjects(); break;
					case 2: selectNews(); break;
					case 3: insertNews(); break;
					case 4: insertSubject(); break;
					case 5: updateNews(); break;
					case 6: removeNews(); break;
					case 7: close(); break;
				}
			}
		} finally{
			if(client != null)
				try{
					client.close();
				}catch(IOException e){
					e.printStackTrace();
				}
		}
	}
	
	private int command(){
		String temp = null;
		int op = 0;
		
		try {
			temp = in.readUTF();
				
			cmd = Util.toXml(temp, "cmd.xsd");
			
			
			if(cmd.getElementsByTagName("type").item(0).getTextContent().equals("SELECT") &&
			   cmd.getElementsByTagName("cmdString").item(0).getTextContent().equals("/noticias/assunto")) op = 1;
			
			if(cmd.getElementsByTagName("type").item(0).getTextContent().equals("SELECT") &&
			   cmd.getElementsByTagName("cmdString").item(0).getTextContent().contains("//noticia")) op = 2;
			
			if(cmd.getElementsByTagName("type").item(0).getTextContent().equals("INSERT") &&
			   cmd.getElementsByTagName("cmdString").item(0).getTextContent().contains("/noticias/assunto[@type")) op = 3;
			
			if(cmd.getElementsByTagName("type").item(0).getTextContent().equals("INSERT") &&
			   cmd.getElementsByTagName("cmdString").item(0).getTextContent().equals("/noticias")) op = 4;
			
			if(cmd.getElementsByTagName("type").item(0).getTextContent().equals("UPDATE")) op = 5;
			
			
			if(cmd.getElementsByTagName("type").item(0).getTextContent().equals("REMOVE")) op = 6;
			
			if(cmd.getElementsByTagName("type").item(0).getTextContent().equals("CLOSE REQUEST") &&
			   cmd.getElementsByTagName("cmdString").item(0).getTextContent().equals("CLOSE CONNECTION")) op = 7;
			
		} catch (IOException e) { e.printStackTrace(); }
		
		return op;
	}

	private void selectSubjects(){
		String temp = Util.toString(News.news);
		
		Document assuntos = Util.toXml(temp, "noticias.xsd");
		
		List<String> attrib = Util.getAttribs("/noticias//assunto", "type", News.news, xmlDao);
		
		for(int i = 0; i < attrib.size(); i++){
			xmlDao.remove("/noticias/assunto[@type='" + attrib.get(i) + "']//noticia", assuntos);
		}
		
		System.out.println("Cliente: " + clientID + 
				" IP: " + client.getInetAddress().getHostAddress() +
				" CMD-Type: " + cmd.getElementsByTagName("type").item(0).getTextContent() +
				" CMD-String: " + cmd.getElementsByTagName("cmdString").item(0).getTextContent());
		
		try {
			out.writeUTF(Util.toString(assuntos));
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	private void selectNews(){
		String temp = Util.toString(News.news);
		
		Document noticias = Util.toXml(temp, "noticias.xsd");
		
		List<String> attrib = Util.getAttribs("/noticias//assunto", "type", News.news, xmlDao);
		
		for(int i = 0; i < attrib.size(); i++){
			xmlDao.remove("/noticias/assunto[@type='" + attrib.get(i) + "']//noticia", noticias);
		}
		
		temp = cmd.getElementsByTagName("cmdString").item(0).getTextContent();
		
		Util.merge(temp, temp.substring(0, temp.indexOf("]") + 1), News.news, noticias, xmlDao);
		
		System.out.println("Cliente: " + clientID + 
				" IP: " + client.getInetAddress().getHostAddress() +
				" CMD-Type: " + cmd.getElementsByTagName("type").item(0).getTextContent() +
				" CMD-String: " + cmd.getElementsByTagName("cmdString").item(0).getTextContent());
		
		try {
			out.writeUTF(Util.toString(noticias));
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	private void insertNews(){
		String temp = null;
		
		try {
			temp = in.readUTF();
		} catch (IOException e) { e.printStackTrace(); }
		
		Document noticia = Util.toXml(temp, "noticias.xsd");
		
		Util.merge(cmd.getElementsByTagName("cmdString").item(0).getTextContent() + "//noticia", 
				cmd.getElementsByTagName("cmdString").item(0).getTextContent(), 
				noticia, News.news, xmlDao);
		
		System.out.println("Cliente: " + clientID + 
				" IP: " + client.getInetAddress().getHostAddress() +
				" CMD-Type: " + cmd.getElementsByTagName("type").item(0).getTextContent() +
				" CMD-String: " + cmd.getElementsByTagName("cmdString").item(0).getTextContent());
		
		reply("INSERT", "Noticia inserida com sucesso!");
	}
	
	private void insertSubject(){
		String temp = null;
		
		try {
			temp = in.readUTF();
		} catch (IOException e) { e.printStackTrace(); }
		
		Document assunto = Util.toXml(temp, "noticias.xsd");
		
		Element newAssunto = (Element) xmlDao.select(cmd.getElementsByTagName("cmdString").item(0).getTextContent() + "//assunto", 
				assunto, XPathConstants.NODE);
		
		xmlDao.insert(News.news, newAssunto.getAttribute("type"));
		
		System.out.println("Cliente: " + clientID + 
				" IP: " + client.getInetAddress().getHostAddress() +
				" CMD-Type: " + cmd.getElementsByTagName("type").item(0).getTextContent() +
				" CMD-String: " + cmd.getElementsByTagName("cmdString").item(0).getTextContent());
		
		reply("INSERT", "Novo assunto inserido com sucesso!");
	}
	
	private void close(){
		
		System.out.println("Cliente: " + clientID + 
				" IP: " + client.getInetAddress().getHostAddress() +
				" CMD-Type: " + cmd.getElementsByTagName("type").item(0).getTextContent() +
				" CMD-String: " + cmd.getElementsByTagName("cmdString").item(0).getTextContent());
		
		reply("CLOSE ACCEPT", "DISCONNECTED");
		
		conectado = false;
	}
	
	private void updateNews(){
		String temp = null, xpath = cmd.getElementsByTagName("cmdString").item(0).getTextContent();
		Document doc = null;
		
		try {
			temp = in.readUTF();
			doc = Util.toXml(temp, "noticias.xsd");
			
		} catch (IOException e) { e.printStackTrace(); }
		
		Element no = (Element) xmlDao.select(xpath, doc, XPathConstants.NODE);
		
		xmlDao.update(xpath + "/title", News.news, no.getElementsByTagName("title").item(0).getTextContent());
		xmlDao.update(xpath + "/text", News.news, no.getElementsByTagName("text").item(0).getTextContent());
		
		System.out.println("Cliente: " + clientID + 
				" IP: " + client.getInetAddress().getHostAddress() +
				" CMD-Type: " + cmd.getElementsByTagName("type").item(0).getTextContent() +
				" CMD-String: " + cmd.getElementsByTagName("cmdString").item(0).getTextContent());
		
		reply("UPDATE", "Dados atualizados com sucesso!");
		
	}
	
	private void removeNews(){
		String[] xpath = cmd.getElementsByTagName("cmdString").item(0).getTextContent().split(",");
		boolean contains = false;
		
		List<String> attrib = Util.getAttribs(xpath[0], "id", News.news, xmlDao);
		
		for(int i = 0; i < attrib.size(); i++){
			if(xpath[1].contains(attrib.get(i))) contains = true;
		}
		
		if(contains){
			xmlDao.remove(xpath[0], xpath[1], News.news);
			
			System.out.println("Cliente: " + clientID + 
					" IP: " + client.getInetAddress().getHostAddress() +
					" CMD-Type: " + cmd.getElementsByTagName("type").item(0).getTextContent() +
					" CMD-String: " + cmd.getElementsByTagName("cmdString").item(0).getTextContent());
			
			reply("REMOVE", "Noticia removida com sucesso!");
		}
		else{
			System.out.println("Cliente: " + clientID + 
					" IP: " + client.getInetAddress().getHostAddress() +
					" CMD-Type: " + cmd.getElementsByTagName("type").item(0).getTextContent() +
					" CMD-String: " + cmd.getElementsByTagName("cmdString").item(0).getTextContent() +
					" ERROR: Tentativa de remover noticia que nao existe");
			
			reply("REMOVE", "Noticia já foi removida!");
		}
	}
	
	private void reply(String type, String msg){
		
		cmd = XmlConnectionFactory.getDocument("cmd.xml", "cmd.xsd");
		xmlDao.update("/cmd/type", cmd, type);
		xmlDao.update("/cmd/cmdString", cmd, msg);
		
		try {
			out.writeUTF(Util.toString(cmd));
		} catch (IOException e) { e.printStackTrace(); }
	}

	private void sendError(String title, String msg){
		error = XmlConnectionFactory.getDocument("error.xml", "error.xsd");
		xmlDao.update("/error/title", error, title);
		xmlDao.update("/error/msg", error, msg);
		
		try {
			out.writeUTF(Util.toString(error));
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	public int getClientID() {
		return clientID;
	}

	public void setClientID(int clientID) {
		this.clientID = clientID;
	}
	
	
}

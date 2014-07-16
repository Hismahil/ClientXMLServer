package br.unioeste.sd.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.w3c.dom.Document;

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
		
		String temp;
		
		try{
			xmlDao = new XmlDao();
			
			while(conectado){
				
				switch(command()){
					case 1: 
						Document assuntos = XmlConnectionFactory.getDocument("noticias.xml", "noticias.xsd");
						xmlDao.remove("/noticias/assunto[@type='futebol']//noticia", assuntos);
						xmlDao.remove("/noticias/assunto[@type='economia']//noticia", assuntos);
						xmlDao.remove("/noticias/assunto[@type='politica']//noticia", assuntos);
						out.writeUTF(Util.toString(assuntos));
						break;
					case 2: 
						
						Document noticias = XmlConnectionFactory.getDocument("noticias.xml", "noticias.xsd");
						xmlDao.remove("/noticias/assunto[@type='futebol']//noticia", noticias);
						xmlDao.remove("/noticias/assunto[@type='economia']//noticia", noticias);
						xmlDao.remove("/noticias/assunto[@type='politica']//noticia", noticias);
						temp = cmd.getElementsByTagName("cmdString").item(0).getTextContent();
						
						Util.merge(temp, temp.substring(0, temp.indexOf("]") + 1), News.news, noticias, xmlDao);
						out.writeUTF(Util.toString(noticias));
						break;
					case 3:
						temp = in.readUTF();
						Document noticia = Util.toXml(temp, "noticias.xsd");
						Util.merge(cmd.getLastChild().getTextContent() + "//noticia", 
								cmd.getLastChild().getTextContent(), 
								noticia, News.news, xmlDao);
						break;
					case 4:
						cmd = XmlConnectionFactory.getDocument("cmd.xml", "cmd.xsd");
						xmlDao.update("/cmd/type", cmd, "CLOSE ACCEPT");
						xmlDao.update("/cmd/cmdString", cmd, "DISCONNECTED");
						out.writeUTF(Util.toString(cmd));
						conectado = false;
						break;
					default: 
						error = XmlConnectionFactory.getDocument("error.xml", "error.xsd");
						xmlDao.update("/error/title", error, "Command error");
						xmlDao.update("/error/msg", error, cmd.getElementsByTagName("cmdString").item(0).getTextContent());
						out.writeUTF(Util.toString(error));
						break;
				}
			}
		} catch(IOException e){
			e.printStackTrace();
		}finally{
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
			
			if(cmd.getElementsByTagName("type").item(0).getTextContent().equals("INSERT")) op = 3;
			
			if(cmd.getElementsByTagName("type").item(0).getTextContent().equals("CLOSE REQUEST") &&
			   cmd.getElementsByTagName("cmdString").item(0).getTextContent().equals("CLOSE CONNECTION")) op = 4;
			
		} catch (IOException e) { e.printStackTrace(); }
		
		return op;
	}

	public int getClientID() {
		return clientID;
	}

	public void setClientID(int clientID) {
		this.clientID = clientID;
	}
	
	
}

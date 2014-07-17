package br.unioeste.sd.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import br.unioeste.sd.msg.Command;
import br.unioeste.sd.news.Assunto;
import br.unioeste.sd.news.Noticia;
import br.unioeste.sd.error.Error;
import br.unioeste.sd.xml.dao.XmlDao;
import br.unioeste.sd.xml.factory.XmlConnectionFactory;
import br.unioeste.sd.xml.util.Util;

public class Client {

	private Socket socket = null;
	private DataInputStream in = null;
	private DataOutputStream out = null;
	private String ip;
	private int port;
	
	private Document news;
	private Document cmd;
	private Document error;
	private XmlDao xmlDao;
	
	
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
		}catch(IOException e){ e.printStackTrace(); }
	}
	
	public Command insertNews(String type, Noticia noticia){
		
		String xpath = "/noticias/assunto[@type='" + type + "']";
		//insere local
		xmlDao.insert(xpath, news, noticia.getId(), noticia.getTitle(), noticia.getText());
		
		//transforma para string
		String temp = Util.toString(news);
		
		//cria novo doc
		Document outDoc = Util.toXml(temp, "noticias.xsd");
		
		//remove todas as noticias
		xmlDao.remove("/noticias/assunto[@type='futebol']//noticia", outDoc);
		xmlDao.remove("/noticias/assunto[@type='politica']//noticia", outDoc);
		xmlDao.remove("/noticias/assunto[@type='economia']//noticia", outDoc);
		
		//insere apenas a nova noticia
		xmlDao.insert(xpath, outDoc, noticia.getId(), noticia.getTitle(), noticia.getText());
		
		cmd = XmlConnectionFactory.getDocument("cmd.xml", "cmd.xsd");
		xmlDao.update("/cmd/type", cmd, "INSERT");
		xmlDao.update("/cmd/cmdString", cmd, xpath);
		
		try {
			out.writeUTF(Util.toString(cmd));
			out.writeUTF(Util.toString(outDoc)); //envia a nova noticia
		} catch (IOException e) { e.printStackTrace(); }
		
		return new Command("Sucesso", "Noticia inserida com sucesso!");
	}
	
	public Assunto insertSubject(String type){
		Assunto assunto = null;
		
		xmlDao.insert(news, type);
		
		Document outDoc = XmlConnectionFactory.getDocument("noticias.xml", "noticias.xsd");
		xmlDao.insert(outDoc, type);
		
		cmd = XmlConnectionFactory.getDocument("cmd.xml", "cmd.xsd");
		xmlDao.update("/cmd/type", cmd, "INSERT");
		xmlDao.update("/cmd/cmdString", cmd, "/noticias");
		
		try {
			out.writeUTF(Util.toString(cmd));
			out.writeUTF(Util.toString(outDoc));
			
			assunto = new Assunto();
			assunto.setType(type);
		} catch (IOException e) { e.printStackTrace(); }
		
		return assunto;
	}
	
	public Command close(){
		Command command;
		String temp = null;
		
		cmd = XmlConnectionFactory.getDocument("cmd.xml", "cmd.xsd");
		xmlDao.update("/cmd/type", cmd, "CLOSE REQUEST");
		xmlDao.update("/cmd/cmdString", cmd, "CLOSE CONNECTION");
		
		try {
			out.writeUTF(Util.toString(cmd));
			temp = in.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if( socket != null)
				try{ socket.close(); } catch(IOException ex) { ex.printStackTrace(); }
		}
		
		Document reply = Util.toXml(temp, "cmd.xsd");
		command = new Command(reply.getElementsByTagName("type").item(0).getTextContent(),
				reply.getElementsByTagName("cmdString").item(0).getTextContent());
		
		return command;
	}
	
	public Error getSubjectsFromServer(List<Assunto> assuntos){
		Error err = null;
		List<String> attrib;
		Assunto assunto;
		String temp = null, xpath = "/noticias//assunto";
		
		cmd = XmlConnectionFactory.getDocument("cmd.xml", "cmd.xsd");
		xmlDao.update("/cmd/type", cmd, "SELECT");
		xmlDao.update("/cmd/cmdString", cmd, "/noticias/assunto");
	
		try {
			out.writeUTF(Util.toString(cmd));
			temp = in.readUTF();
			
		} catch (IOException e) { e.printStackTrace(); }
		
		if(temp.contains("assunto")) {
			if(news == null) news = Util.toXml(temp, "noticias.xsd");
			else{
				Document reply = Util.toXml(temp, "noticias.xsd");
				Util.cmp(xpath, "type", reply, news, xmlDao);
			}
			
			attrib = Util.getAttribs(xpath, "type", news, xmlDao);
			
			for(int i = 0; i < attrib.size(); i++){
				assunto = new Assunto();
				assunto.setType(attrib.get(i));
				assuntos.add(assunto);
			}
		}
		
		if(temp.contains("<error>")) err = showError(temp);
		
		return err;
	}

	public List<Assunto> getSubjectsFromDocument(){
		List<Assunto> assuntos = null;
		List<String> attrib;
		Assunto assunto;
		String xpath = "/noticias//assunto";
		
		attrib = Util.getAttribs(xpath, "type", news, xmlDao);
		assuntos = new ArrayList<Assunto>();
			
		for(int i = 0; i < attrib.size(); i++){
			assunto = new Assunto();
			assunto.setType(attrib.get(i));
			assuntos.add(assunto);
		}
		
		return assuntos;
	}
	
	public List<String> getSubjectsAttributes(){
		String xpath = "/noticias//assunto";
		return Util.getAttribs(xpath, "type", news, xmlDao);
	}
	
	public Error getNewsFromServer(String assunto, List<Noticia> noticias){
		String temp = null;
		Error err = null;
		
		String xpathIn = "/noticias/assunto[@type='"+assunto+"']//noticia";
		String xpathOut = "/noticias/assunto[@type='" + assunto + "']";
		
		cmd = XmlConnectionFactory.getDocument("cmd.xml", "cmd.xsd");
		xmlDao.update("/cmd/type", cmd, "SELECT");
		xmlDao.update("/cmd/cmdString", cmd, xpathIn);
		
		try {
			out.writeUTF(Util.toString(cmd));
			temp = in.readUTF();
		} catch (IOException e) { e.printStackTrace(); }
		
		if(temp.contains("noticia")){
			Document reply = Util.toXml(temp, "noticias.xsd");
			
			//se o assunto esta vasio
			if(xmlDao.select(xpathIn, news).getLength() == 0) 
				Util.merge(xpathIn, xpathOut, reply, news, xmlDao);
			else //se não compara e adiciona o que não tem
				Util.cmp(xpathIn, "id", reply, news, xmlDao);
			
			NodeList list = xmlDao.select(xpathIn, news);
			
			Element no = null;
			Noticia noticia = null;
			
			for(int i = 0; i < list.getLength(); i++){
				no = (Element) list.item(i);
				noticia = new Noticia(Integer.parseInt(no.getAttribute("id")), 
						no.getElementsByTagName("title").item(0).getTextContent(), 
						no.getElementsByTagName("text").item(0).getTextContent());
				
				noticias.add(noticia);
			}
		}
		
		if(temp.contains("<error>")) err = showError(temp);
		
		return err;
	}
	
	public List<Noticia> getNewsFromDocument(String assunto){
		List<Noticia> noticias = new ArrayList<Noticia>();
		
		String xpath = "/noticias/assunto[@type='"+assunto+"']//noticia";
		
		NodeList list = xmlDao.select(xpath, news);
		Element no = null;
		Noticia noticia = null;
		
		for(int i = 0; i < list.getLength(); i++){
			no = (Element) list.item(i);
			noticia = new Noticia(Integer.parseInt(no.getAttribute("id")), 
					no.getElementsByTagName("title").item(0).getTextContent(), 
					no.getElementsByTagName("text").item(0).getTextContent());
			
			noticias.add(noticia);
		}
		
		return noticias;
	}
	
	private Error showError(String temp){
		error = Util.toXml(temp, "error.xsd");
		Error err = new Error(error.getElementsByTagName("title").item(0).getTextContent(),
				error.getElementsByTagName("msg").item(0).getTextContent());
		return err;
	}
}

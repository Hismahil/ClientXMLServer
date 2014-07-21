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
	
	/**
	 * <h3><b>Insere nova noticia em um assunto</b></h3><br/>
	 * @param type <code>Assunto</code><br/>
	 * @param noticia <code>Nova noticia</code><br/>
	 * @param msg <code>Mensagem do servidor</code><br/>
	 * @return <code>Mensagem de erro se houver</code><br/>
	 */
	public Error insertNews(String type, Noticia noticia, Command msg){
		
		String xpath = "/noticias/assunto[@type='" + type + "']";
		//insere local
		xmlDao.insert(xpath, news, noticia.getId(), noticia.getTitle(), noticia.getText());
		
		//transforma para string
		String temp = Util.toString(news);
		
		//cria novo doc
		Document outDoc = Util.toXml(temp, "noticias.xsd");
		
		List<String> attrib = Util.getAttribs("/noticias//assunto", "type", news, xmlDao);
		
		//remove todas as noticias
		for(int i = 0; i < attrib.size(); i++){
			xmlDao.remove("/noticias/assunto[@type='" + attrib.get(i) + "']//noticia", outDoc);
		}
		
		//insere apenas a nova noticia
		xmlDao.insert(xpath, outDoc, noticia.getId(), noticia.getTitle(), noticia.getText());
		
		cmd = XmlConnectionFactory.getDocument("cmd.xml", "cmd.xsd");
		
		xmlDao.update("/cmd/type", cmd, "INSERT");
		xmlDao.update("/cmd/cmdString", cmd, xpath);
		
		try {
			out.writeUTF(Util.toString(cmd));
			out.writeUTF(Util.toString(outDoc)); //envia a nova noticia
			
			temp = in.readUTF(); //mensagem do servidor
			
		} catch (IOException e) { e.printStackTrace(); }
		
		if(temp.contains("<cmd>")) reply(temp, msg);
		
		if(temp.contains("<error>")) return showError(temp);
		
		return null;
	}
	
	/**
	 * <h3><b>Insere novo assunto local e no servidor</b></h3><br/>
	 * @param type <code>Novo assunto</code><br/>
	 * @param assunto <code>Instancia do assunto onde sera adicionado o novo assunto</code><br/>
	 * @param msg <code>Mensagem vinda do servidor</code><br/>
	 * @return <code>Novo assunto</code><br/>
	 */
	public Error insertSubject(String type, Assunto assunto, Command msg){
		String temp = null;
		
		xmlDao.insert(news, type);
		
		Document outDoc = XmlConnectionFactory.getDocument("noticias.xml", "noticias.xsd");
		
		xmlDao.insert(outDoc, type);
		
		cmd = XmlConnectionFactory.getDocument("cmd.xml", "cmd.xsd");
		
		xmlDao.update("/cmd/type", cmd, "INSERT");
		xmlDao.update("/cmd/cmdString", cmd, "/noticias");
		
		try {
			out.writeUTF(Util.toString(cmd));
			out.writeUTF(Util.toString(outDoc));
			
			temp = in.readUTF(); //mensagem do servidor
			
			assunto.setType(type);
		} catch (IOException e) { e.printStackTrace(); }
		
		if(temp.contains("<cmd>")) reply(temp, msg);
		
		if(temp.contains("<error>")) return showError(temp);
		
		return null;
	}
	
	/**
	 * <h3><b>Fecha o cliente</b></h3><br/>
	 * @param msg <code>Mensagem de resposta do servidor</code><br/> 
	 * @return <code>Mensagem do servidor</code><br/>
	 */
	public Error close(Command msg){
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
		
		if(temp.contains("<cmd>")) reply(temp, msg);
		
		if(temp.contains("<error>")) return showError(temp);
		
		return null;
	}
	
	/**
	 * <h3><b>Retorna os assuntos do servidor</b></h3><br/>
	 * @param assuntos <code>Lista onde sera adicionado os assuntos</code><br/>
	 * @return <code>Mensagem de erro</code><br/>
	 */
	public Error getSubjectsFromServer(List<Assunto> assuntos){
		List<String> attrib;
		Assunto assunto;
		String temp = null, xpath = "/noticias//assunto";
		
		cmd = XmlConnectionFactory.getDocument("cmd.xml", "cmd.xsd");
		xmlDao.update("/cmd/type", cmd, "SELECT");
		xmlDao.update("/cmd/cmdString", cmd, xpath);
	
		try {
			out.writeUTF(Util.toString(cmd));
			temp = in.readUTF();
			
		} catch (IOException e) { e.printStackTrace(); }
		
		if(temp.contains("assunto")) {
			if(news == null) {
				news = Util.toXml(temp, "noticias.xsd");
			}
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
		
		if(temp.contains("<error>")) return showError(temp);
		
		return null;
	}

	/**
	 * <h3><b>Retorna os assuntos do documento local</b></h3><br/>
	 * @return <code>Lista de assuntos</code><br/>
	 */
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
	
	/**
	 * <h3><b>Retorna os nomes dos assuntos</b></h3><br/>
	 * @return <code>Lista de assuntos</code><br/>
	 */
	public List<String> getSubjectsAttributes(){
		String xpath = "/noticias//assunto";
		return Util.getAttribs(xpath, "type", news, xmlDao);
	}
	
	/**
	 * <h3><b>Retorna noticias vindas do servidor</b></h3><br/>
	 * @param assunto <code>Atributo do Assunto</code><br/>
	 * @param noticias <code>Lista onde sera adicionado as noticias</code><br/>
	 * @return <code>Mensagem de erro</code><br/>
	 */
	public Error getNewsFromServer(String assunto, List<Noticia> noticias){
		String temp = null;
		
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
		
		if(temp.contains("<error>")) return showError(temp);
		
		return null;
	}
	
	/**
	 * <h3><b>Retorna noticia de um assunto no documento local</b></h3><br/>
	 * @param assunto <code>Atributo do Assunto</code><br/>
	 * @return <code>Lista de noticias</code><br/>
	 */
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
	
	/**
	 * <h3><b>Atualiza uma noticia de um assunto</b></h3><br/>
	 * @param assunto <code>Atributo do Assunto</code><br/>
	 * @param noticia <code>Dados atualizados da noticia</code><br/>
	 * @param msg <code>Mensagem do servidor</code><br/>
	 * @return <code>Mensagem de erro</code><br/>
	 */
	public Error updateNews(String assunto, Noticia noticia, Command msg){
		String xpath = "/noticias/assunto[@type='" + assunto + "']/noticia[@id='" + noticia.getId() + "']";
		String temp = null;
		
		xmlDao.update(xpath + "/title", news, noticia.getTitle());
		xmlDao.update(xpath + "/text", news, noticia.getText());
		
		Document outDoc = XmlConnectionFactory.getDocument("noticias.xml", "noticias.xsd");
		
		xmlDao.insert(outDoc, assunto);
		xmlDao.insert("/noticias/assunto[@type='" + assunto + "']", outDoc, noticia.getId(), noticia.getTitle(), noticia.getText());
		
		cmd = XmlConnectionFactory.getDocument("cmd.xml", "cmd.xsd");
		
		xmlDao.update("/cmd/type", cmd, "UPDATE");
		xmlDao.update("/cmd/cmdString", cmd, xpath);
		
		try {
			out.writeUTF(Util.toString(cmd));
			out.writeUTF(Util.toString(outDoc));
			
			temp = in.readUTF(); //mensagem do servidor
		} catch (IOException e) { e.printStackTrace(); }
		
		if(temp.contains("<cmd>")) reply(temp, msg);
		
		if(temp.contains("<error>")) return showError(temp);
		
		return null;
	}
	/**
	 * <h3><b>Remove uma noticia de um assunto</b></h3><br/>
	 * @param assunto <code>Atributo do Assunto</code><br/>
	 * @param id <code>ID da noticia</code><br/>
	 * @param msg <code>Mensagem de sucesso</code><br/>
	 * @return <code>Erro</code><br/>
	 */
	public Error removeNews(String assunto, int id, Command msg){
		String xpathRoot = "/noticias/assunto[@type='" + assunto + "']//noticia", xpathChild = "/noticias/assunto[@type='" + assunto + "']/noticia[@id='" + id + "']";
		boolean contains = false;
		String temp = null;
		
		List<String> attrib = Util.getAttribs(xpathRoot, "id", news, xmlDao);
		
		for(int i = 0; i < attrib.size(); i++){
			if(xpathChild.contains(attrib.get(i))) contains = true;
		}
		
		if(contains){
			xmlDao.remove(xpathRoot, xpathChild, news);
			
			cmd = XmlConnectionFactory.getDocument("cmd.xml", "cmd.xsd");
			
			xmlDao.update("/cmd/type", cmd, "REMOVE");
			xmlDao.update("/cmd/cmdString", cmd, xpathRoot + "," + xpathChild);
			
			
			
			try {
				out.writeUTF(Util.toString(cmd));
				temp = in.readUTF();
			} catch (IOException e) { e.printStackTrace(); }
			
			
			if(temp.contains("<cmd>")) reply(temp, msg);
			
			if(temp.contains("<error>")) return showError(temp);
		} 
		else {
			msg.setType("REMOVE");
			msg.setCmdString("A noticia não existe para ser removida");
			
			return new Error("Erro", "Não é possivel remover o que não existe!");
		}
		
		return null;
	}
	
	private Error showError(String temp){
		Error err = new Error(error.getElementsByTagName("title").item(0).getTextContent(),
				error.getElementsByTagName("msg").item(0).getTextContent());
		return err;
	}
	
	/**
	 * <h3><b>Replica de mensagem do servidor</b></h3><br/> 
	 * @param temp <code>XML vindo do servidor com a mensagem</code><br/>
	 * @param msg <code>Mensagem</code><br/>
	 */
	private void reply(String temp, Command msg){
		Document reply = Util.toXml(temp, "cmd.xsd");
		
		msg.setType(reply.getElementsByTagName("type").item(0).getTextContent());
		msg.setCmdString(reply.getElementsByTagName("cmdString").item(0).getTextContent());
	}
}

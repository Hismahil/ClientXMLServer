package br.unioeste.sd.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.w3c.dom.Document;

import br.unioeste.sd.xml.dao.XmlDao;
import br.unioeste.sd.xml.factory.XmlConnectionFactory;
import br.unioeste.sd.xml.util.Xml2String;

public class Client {

	private Socket socket = null;
	private DataInputStream in = null;
	private DataOutputStream out = null;
	private String ip;
	private int port;
	
	private Document noticias;
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
			
			while(true){
				cmd = XmlConnectionFactory.getDocument("cmd.xml", "cmd.xsd");			
				
				xmlDao.update("/cmd/cmdString", cmd, "Hello world");
				
				out.writeUTF(Xml2String.toString(cmd));
				
				String str = in.readUTF();
				System.out.println();
				System.out.println("Recebido: " + str);
				
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
	
}

package br.unioeste.sd.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.w3c.dom.Document;

import br.unioeste.sd.xml.factory.XmlConnectionFactory;
import br.unioeste.sd.xml.util.Xml2String;

public class ServerThread extends Thread{

	private Socket client;
	private DataInputStream in;
	private DataOutputStream out;
	private int clientID;
	private boolean conectado = true;
	private String recebido;
	private String response;
	
	private Document noticias;
	private Document cmd;
	private Document error;
	
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
	
	public void run() {
		
		try{
			while(conectado){
				recebido = in.readUTF();
				
				noticias = XmlConnectionFactory.getDocument("noticias.xml", "noticias.xsd");
				
				System.out.println("Recebido: " + recebido);
				
				out.writeUTF(Xml2String.toString(noticias));
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
}

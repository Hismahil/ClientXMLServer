package br.unioeste.sd.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import br.unioeste.sd.news.News;
import br.unioeste.sd.xml.factory.XmlConnectionFactory;

public class Server {

	private ServerSocket server = null;
	private Socket clientSocket = null;
	private ServerThread st = null;
	private int id = 0;
	private int port;
	
	public Server(int port){
		this.port = port;
		News.news = XmlConnectionFactory.getDocument("noticias.xml", "noticias.xsd");
	}
	
	public void listen(){
		try{
			server = new ServerSocket(port);
			
			while(true){
				try{
					clientSocket = server.accept();
					st = new ServerThread(clientSocket, id);
					st.run();
					
					id++;
				}catch(IOException e){
					e.printStackTrace();
				}finally{
					if(clientSocket != null)
						try{
							clientSocket.close();
						}catch(IOException e){
							e.printStackTrace();
						}
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(server != null)
				try{ server.close(); }
				catch(IOException e){ e.printStackTrace(); }
		}
	}
}

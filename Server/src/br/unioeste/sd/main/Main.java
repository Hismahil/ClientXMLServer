package br.unioeste.sd.main;

import java.net.InetAddress;
import java.net.UnknownHostException;

import br.unioeste.sd.server.Server;

public class Main {

	public static void main(String[] args) throws UnknownHostException {
		Server server = new Server(7777, InetAddress.getByName("192.168.25.6"));
		server.listen();
		
	}

}

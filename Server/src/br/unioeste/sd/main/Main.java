package br.unioeste.sd.main;

import br.unioeste.sd.server.Server;

public class Main {

	public static void main(String[] args) {
		Server server = new Server(1234);
		server.listen();
		
	}

}

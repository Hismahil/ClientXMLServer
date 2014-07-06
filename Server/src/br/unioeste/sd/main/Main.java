package br.unioeste.sd.main;

import br.unioeste.sd.server.Server;

public class Main {

	public static void main(String[] args) {
		Server server = new Server(7777);
		server.listen();

	}

}

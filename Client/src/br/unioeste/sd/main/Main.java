package br.unioeste.sd.main;

import br.unioeste.sd.client.Client;

public class Main {

	public static void main(String[] args) {
		Client client = new Client("127.0.0.1", 7777);
		client.connect();
	}

}

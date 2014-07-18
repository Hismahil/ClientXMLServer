package br.unioeste.sd.xml.error;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XmlErrorHandle implements ErrorHandler{
	
	public void error(SAXParseException saxex) throws SAXException {
		System.out.println("Erro: " + saxex.getMessage());
	}

	public void fatalError(SAXParseException saxex) throws SAXException {
		System.out.println("Erro Fatal: "+ saxex.getMessage());
		
	}

	public void warning(SAXParseException saxex) throws SAXException {
		System.out.println("Perigo: " + saxex.getMessage());
		
	}

}

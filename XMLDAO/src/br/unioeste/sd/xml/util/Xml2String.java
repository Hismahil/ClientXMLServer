package br.unioeste.sd.xml.util;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class Xml2String {

	public static String toString(Document doc){
		Transformer transformer;
		StreamResult result;
		DOMSource source;
		String temp = null;
		
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

	        result = new StreamResult(new StringWriter());
	        source = new DOMSource(doc);
	        
	        transformer.transform(source, result);
			
	        temp = result.getWriter().toString();
		} catch(TransformerException e){
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return temp;
	}
}

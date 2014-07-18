package br.unioeste.sd.xml.factory;

import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;

import br.unioeste.sd.xml.error.XmlErrorHandle;


public class XmlConnectionFactory {

	/**
	 * <h3><b>Cria intancia de um documento xml</b></h3><br>
	 * @param xml <code>Local e nome do documento xml</code><br/>
	 * @param xsd <code>Local e nome do documento xsd</code><br/>
	 * @return <code>Document</code><br/>
	 */
	public static Document getDocument(String xml, String xsd){
		File schemaFile = new File(xsd);
        File xmlFile = new File(xml);
        String constant = XMLConstants.W3C_XML_SCHEMA_NS_URI;
        Document doc = null;
        DocumentBuilderFactory factory = null;
        SchemaFactory xsdFactory = null;
        Schema schema = null;
        DocumentBuilder db = null;
        
        try{
	        factory = DocumentBuilderFactory.newInstance();
	        xsdFactory = SchemaFactory.newInstance(constant);
	        schema = xsdFactory.newSchema(schemaFile);
	
	        factory.setSchema(schema);
	
	        db = factory.newDocumentBuilder();
	
	        db.setErrorHandler(new XmlErrorHandle());
	
	        doc = db.parse(xmlFile);
	        
        }catch(Exception e){
        	System.out.println("Erro ao criar arquivo: " + e.getMessage());
        }
        
        return doc;
	}
}

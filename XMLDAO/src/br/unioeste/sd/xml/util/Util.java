package br.unioeste.sd.xml.util;

import java.io.File;
import java.io.StringBufferInputStream;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import br.unioeste.sd.xml.dao.XmlDao;
import br.unioeste.sd.xml.error.XmlErrorHandle;

public class Util {

	/**
	 * <h3><b>Converte uma String para um Documento XML</b></h3><br/>
	 * @param xml <code>String contendo o XML</code><br/>
	 * @param xmlSchema <code>Arquivo schema para validacao</code><br/>
	 * @return <code>Documento XML</code><br/>
	 */
	public static Document toXml(String xml, String xmlSchema){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
        DocumentBuilder builder;
        SchemaFactory xsdFactory = null;
        Schema schema = null;
        
        Document doc = null;
        try 
        {  
        	xsdFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        	schema = xsdFactory.newSchema(new File(xmlSchema));
        	factory.setSchema(schema);
            builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new XmlErrorHandle());
            doc = builder.parse(new StringBufferInputStream(xml)); 
        } catch (Exception e) {  
            e.printStackTrace();  
        } 
        return doc;
	}
	
	/**
	 * <h3><b>Converte um Documento XML para String</b></h3><br/>
	 * @param doc <code>Documento</code><br/>
	 * @return <code>String</code><br/>
	 */
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
	
	/**
	 * <h3><b>Adiciona o conteudo de um Doc em outro Doc</b></h3><br/>
	 * @param query <code>XPath do assunto</code><br/>
	 * @param in <code>Documento de entrada</code><br/>
	 * @param out <code>Documento de saida</code><br/>
	 * @param xml <code>XMLDAO</code><br/>
	 */
	public static void merge(String queryIn, String queryOut, Document in, Document out, XmlDao xml){
		NodeList listIn = xml.select(queryIn, in);
		
		for(int i = 0; i < listIn.getLength(); i++){
			
			Element item = (Element) listIn.item(i).getChildNodes();
			
			xml.insert(queryOut, out, 
					Integer.parseInt(listIn.item(i).getAttributes().item(0).getTextContent()),
					item.getElementsByTagName("title").item(0).getTextContent(), 
					item.getElementsByTagName("text").item(0).getTextContent());
		}
	}
	
}

package br.unioeste.sd.xml.util;

import java.io.File;
import java.io.StringBufferInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

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

	/**
	 * <h3><b>Compara assuntos ou noticias de um assunto e adiciona se houver algo diferente</b></h3><br/>
	 * @param query <code>XPath dos assuntos ou noticias de um assunto</code><br/>
	 * @param attrib <code>Atributo do assunto ou da noticia</code><br/>
	 * @param in <code>Documento contendo a diferença</code><br/>
	 * @param out <code>Documento que falta conteudo</code><br/>
	 * @param xml <code>XMLDao</code><br/>
	 */
	public static void cmp(String query, String attrib, Document in, Document out, XmlDao xml){
		NodeList listIn = xml.select(query, in);
		NodeList listOut = xml.select(query, out);
		boolean isSubject = (attrib.equals("type") ? true : false);
		
		int j = 0;
		
		for(int i = 0; i < listIn.getLength(); i++){
			Element n1 = (Element) listIn.item(i); //assuntos vindos do servidor
			for(j = 0; j < listOut.getLength(); j++){
				Element n2 = (Element) listOut.item(j); //assunto local
				//se for igual os atributos nem continua
				if(n1.getAttribute(attrib).equals(n2.getAttribute(attrib))) break;
			}
			//se for diferente o atributo adiciona
			if(j == listOut.getLength()){
				
				if(isSubject) //se for novo assunto
					xml.insert(out, n1.getAttribute(attrib));
				else //se for nova noticia
					xml.insert(query.substring(0, query.indexOf("]") + 1), 
							out, 
							Integer.parseInt(n1.getAttribute(attrib)), 
							n1.getElementsByTagName("title").item(0).getTextContent(), 
							n1.getElementsByTagName("text").item(0).getTextContent());
			}
		}
	}
	
	/**
	 * <h3><b>Adiciona o conteudo de um Doc em outro Doc</b></h3><br/>
	 * @param query <code>XPath dos assuntos ou das noticias</code><br/>
	 * @param attrib <code>Atributo da tag que se quer pegar</code><br/>
	 * @param doc <code>Documento XML</code><br/>
	 * @param xml <code>XMLDao para selecionar os nos</code><br/>
	 * @return <code>Lista de String com os atributos</code><br/>
	 */
	public static List<String> getAttribs(String query, String attrib, Document doc, XmlDao xml){
		List<String> attr = new ArrayList<String>();
		NodeList list = xml.select(query, doc);
		Element no = null;
		
		for(int i = 0; i < list.getLength(); i++){
			no = (Element) list.item(i);
			attr.add(new String(no.getAttribute(attrib)));
		}
		
		return attr;
	}
}

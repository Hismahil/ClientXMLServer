package br.unioeste.sd.xml.util;

import java.io.StringBufferInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class String2Xml {
	
	public static Document toXml(String xml){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
        DocumentBuilder builder;
        Document doc = null;
        try 
        {  
            builder = factory.newDocumentBuilder();  
            doc = builder.parse(new StringBufferInputStream(xml)); 
        } catch (Exception e) {  
            e.printStackTrace();  
        } 
        return doc;
	}
}

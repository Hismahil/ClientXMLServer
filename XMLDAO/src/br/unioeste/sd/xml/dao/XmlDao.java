package br.unioeste.sd.xml.dao;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlDao {
	
	private XPath xpath;
	
	public XmlDao() { }
	
	/**
	 * <h3><b>Insere nova noticia em um assunto no XML</b></h3><br/>
	 * @param query <code>String XPath selecionando um assunto</code><br/>
	 * @param doc <code>Instancia do documento XML</code><br/>
	 * @param id <code>ID da noticia</code><br/>
	 * @param title <code>Titulo da noticia</code><br/>
	 * @param comment <code>Texto da noticia</code><br/>
	 * @return <code>Document</code>
	 */
	public Document insert(String query, Document doc, int id, String title, String comment){
		Element listaNoticia = (Element) select(query, doc, XPathConstants.NODE);
		Element noticia = doc.createElement("noticia");
		noticia.setAttribute("id", String.valueOf(id));
		
		Element _title = doc.createElement("title");
		_title.appendChild(doc.createTextNode(title));
		
		Element text = doc.createElement("text");
		text.appendChild(doc.createTextNode(comment));
		
		noticia.appendChild(_title);
		noticia.appendChild(text);
		listaNoticia.appendChild(noticia);
		
		return doc;
	}

	/**
	 * <h3><b>Insere novo assunto no XML</b></h3><br>
	 * @param doc <code>Instancia do documento XML</code><br/>
	 * @param type <code>Atributo do novo assunto</code><br/>
	 * @return <code>Document</code>
	 */
	public Document insert(Document doc, String type){
		Element root = doc.getDocumentElement();
		
		Element assunto = doc.createElement("assunto");
		assunto.setAttribute("type", type);
		
		root.appendChild(assunto);
		
		return doc;
	}
	
	public NodeList select(String query, Document doc){
		xpath = XPathFactory.newInstance().newXPath();
		NodeList list = null;
		
		try {
			list = (NodeList) xpath.compile(query).evaluate(doc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			System.out.println("Erro: Não foi possivel achar o nó pesquisado!");
		}
		
		return list;
	}
	
	public Node select(String query, Document doc, QName xpathConstant){
		xpath = XPathFactory.newInstance().newXPath();
		Node no = null;
		
		try {
			no = (Node) xpath.compile(query).evaluate(doc, xpathConstant);
		} catch (XPathExpressionException e) {
			System.out.println("Erro: Não foi possivel achar o nó pesquisado!");
		}
		
		return no;
	}
	
	public Document update(String query, Document doc, String newValue){
		Node no = select(query, doc, XPathConstants.NODE);
		no.setTextContent(newValue);
		return doc;
	}
	
	/**
	 * <h3><b>Removo um nó de uma tag</b></h3><br>
	 * @param root <code>XPath do nó raiz</code><br/>
	 * @param child <code>XPath do elemento que sera removido</code><br/>
	 * @param doc <code>Documento</code><br/>
	 * @return <code>Documento</code><br/>
	 */
	public Document remove(String root, String child, Document doc){
		
		NodeList assunto = select(root, doc);
		Element noticia = (Element) select(child, doc, XPathConstants.NODE);
		Element title1 = (Element) noticia.getElementsByTagName("title").item(0);
		
		for(int i = 0; i < assunto.getLength(); i++){
			Element node = (Element)assunto.item(i);
			Element title2 = (Element) node.getElementsByTagName("title").item(0);
			
			if(title1.getTextContent().equals(title2.getTextContent()))
				node.getParentNode().removeChild(node);
		}
		
		return doc;
	}
	
	/**
	 * <h3><b>Removo todos os elementos de uma tag</b></h3><br>
	 * @param root <code>XPath da tag base</code><br/>
	 * @param doc <code>Documento XML</code><br/>
	 * @return <code>Documento XML</code><br/>
	 */
	public Document remove(String root, Document doc){
		
		NodeList assunto = select(root, doc);
		
		for(int i = 0; i < assunto.getLength(); i++){
			Element node = (Element)assunto.item(i);
			node.getParentNode().removeChild(node);
		}
		
		return doc;
	}
}

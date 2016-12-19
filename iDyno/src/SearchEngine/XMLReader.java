package SearchEngine;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLReader {

	//public static void main(String[] args) throws TransformerException {
		public static void updateParameter(Map map) throws TransformerException {

		try {
			String filepath = Constants.XML_PATH;

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(filepath);
			

	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			StreamResult result = new StreamResult(new File(filepath));

			// String muMax;
			NodeList company = doc.getElementsByTagName("param");
			boolean bulk=false,in=false,pulse=false;

			for (int i = 0; i < company.getLength(); i++) {

				Node node = company.item(i);
				Element eElement = (Element) node;
				//System.out.println(".........>>.........>>."+eElement.getAttribute("name"));
/*
				if (eElement.getAttribute("name").equals("randomSeed")) {
					String seed =(String) map.get("seed");
					eElement.setTextContent(seed);}*/
				
				if (eElement.getAttribute("name").equals("Sbulk")&& in==false ) {
					in=true;
					String Sbulk =(String) map.get("seed");
					System.out.println(eElement.getAttribute("name").equals("Sbulk"));
					eElement.setTextContent(Sbulk);}
		
				
				//String Spulse =(String) map.get("Sbulk");
				//eElement.setTextContent(Spulse);
				DOMSource source = new DOMSource(doc);
				transformer.transform(source, result);
				
				
				/*if (eElement.getAttribute("name").equals("Spulse") && pulse ==false) {
					pulse=true;
					String Spulse =(String) map.get("Sbulk");
					eElement.setTextContent(Spulse);
					DOMSource source = new DOMSource(doc);
					transformer.transform(source, result);*/
				
			
				
			}
				/*if (eElement.getAttribute("name").equals("pulseRate")) {
					//String muMaxstr =Double.toString( (double) map.get("muMax"));
					System.out.println(eElement.getAttribute("name").equals("pulseRate"));
					eElement.setTextContent("9902");
				if (eElement.getAttribute("name").equals("Sin")) {
					System.out.println(eElement.getAttribute("name"));
					//String muMaxstr =Double.toString( (double) map.get("muMax"));
					System.out.println(eElement.getAttribute("name").equals("Sin"));
					eElement.setTextContent("9902");
				}
				if (eElement.getAttribute("name").equals("birthday")) {
				System.out.println("hello----------->");

					//System.out.println(eElement.getAttribute("name"));
					//String muMaxstr =Double.toString( (double) map.get("muMax"));
					System.out.println(eElement.getAttribute("name"));
					eElement.setTextContent("9902");
				}
				DOMSource source = new DOMSource(doc);
				transformer.transform(source, result);
				break;
			}}
			
			//init area
			
					
                        */
                      /*  NodeList company2 = doc.getElementsByTagName("tightJunction");
						String cstrength2 = Double.toString((double) map.get("tightJunctionstiffness"));
                        for (int i = 0; i < company2.getLength(); i++) {
							Node node = company2.item(i);
							Element eElement = (Element) node;
								 
								eElement.setAttribute("stiffness", cstrength2);
							//System.out.println("..");
								DOMSource source = new DOMSource(doc);
								transformer.transform(source, result);
							//}

						}*/
			
			
                        

			
			
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SAXException sae) {
			sae.printStackTrace();
		}
	}
}

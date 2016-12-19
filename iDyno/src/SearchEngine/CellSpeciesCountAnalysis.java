package SearchEngine;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CellSpeciesCountAnalysis {
	public static void main(String args[]) throws ParserConfigurationException, SAXException, IOException, TransformerException
	
	{
		String filepath=null;
		String filepath1 = "C:\\Users\\Honey\\Downloads\\700-100-0.35\\900-100-0.35\\agent_Sum";
		
		//C:\Users\Honey\Desktop\NewSoutes\agit1(20161106_1505)\

		File folder = new File(filepath1);
		File[] listOfFiles = folder.listFiles();
	Arrays.sort(listOfFiles);
		for (File file : listOfFiles) {
		    if (file.isFile()) {
		    	filepath=filepath1+"\\"+file.getName();
		//System.out.println(file.getName());
		
	DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	Document doc = docBuilder.parse(filepath);
		

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StreamResult result = new StreamResult(new File(filepath));

		// String muMax;
		NodeList company = doc.getElementsByTagName("species");
		boolean bulk=false,in=false,pulse=false;
String ac=null;
String m=null;
String d=null;
		for (int i = 0; i < company.getLength(); i++) {

			Node node = company.item(i);
			Element eElement = (Element) node;
			//System.out.println(".........>>.........>>."+eElement.getAttribute("name"));

			/*if (eElement.getAttribute("name").equals("Acidogen")) {
				
				String[] a= eElement.getTextContent().split(",");
				ac=a[0];
			//System.out.println(a[0]);
			}
			if  (eElement.getAttribute("name").equals("Methanogen")) {
				
				String[] a1= eElement.getTextContent().split(",");
			//System.out.println(a1[0]);
			m=a1[0];
			System.out.println(ac.trim()+"	"+m.trim());
			}*/
	if  (eElement.getAttribute("name").equals("Methanogen")) {
				
				String[] a1= eElement.getTextContent().split(",");
			//System.out.println(a1[0]);
			m=a1[0];
			System.out.println(m.trim());
			}
	
			
			
		}}}   }
}
			
		
		
		



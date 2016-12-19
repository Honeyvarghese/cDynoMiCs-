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
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CellRatioAnalysis {
public static void main(String args[]) throws ParserConfigurationException, SAXException, IOException, TransformerException
	
	{
		
		String filepath = "C:\\Users\\Honey\\Downloads\\agent_State(800).xml";

		
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

Double aa1=0.0;
Double m1=0.0;
Double d1=0.0;
Double aa2=0.0;
Double m2=0.0;
Double d2=0.0;
Double aa3=0.0;
Double m3=0.0;
Double d3=0.0;
Double aa4=0.0;
Double m4=0.0;
Double d4=0.0;
Double aa5=0.0;
Double m5=0.0;
Double d5=0.0;
Double aa6=0.0;



		for (int i = 0; i < company.getLength(); i++) {

			Node node = company.item(i);
			Element eElement = (Element) node;
			System.out.println(".........>>.........>>."+eElement.getAttribute("name"));

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
	if  (eElement.getAttribute("name").equals("GDyingA")) {
				
				String[] a1= eElement.getTextContent().split(";");
				for(int j=0;j<a1.length-1;j++)
				{
					String[] a2=a1[j].split(",");
					String x=a2[10].trim();
					String y=a2[11].trim();
					String biomass=a2[5].trim();
					//String z=a2[12].trim();
					//System.out.println(x+":"+y+":"+z);
					double x1=Double.valueOf(x);
					double y1=Double.valueOf(y);
					double r=0;
					//System.out.println(biomass);
					if(x1<500 && y1>500)
					{
						
						r=Math.sqrt((500-x1)*(500-x1)+(500-y1)*(500-y1));
						//System.out.println(r);
						if(r>0 && r<90)
						{
							
							aa1=aa1+Double.parseDouble(a2[5].trim());
						}
						if(r>90 && r<180)
						{
							aa2=aa2+Double.parseDouble(a2[5].trim());
						}
						if(r>180 && r<270)
						{
							aa3=aa3+Double.parseDouble(a2[5].trim());
						}
						if(r>270 && r<360)
						{
							aa4=aa4+Double.parseDouble(a2[5].trim());
						}
						//if(r>224 && r<280)
							if(r>360&& r<450)
						{
								aa5=aa5+Double.parseDouble(a2[5].trim());
						}
							if(r>450)
							{
									aa6=aa6+Double.parseDouble(a2[5].trim());
							}
					}
					
					
					System.out.println(aa1);
					System.out.println(aa2);
					System.out.println(aa3);
					System.out.println(aa4);
					System.out.println(aa5);
					System.out.println(aa6);

					
				}
				
				
				
			
			}
	
	/*
	if  (eElement.getAttribute("name").equals("Methanogen")) {
		
		String[] a1= eElement.getTextContent().split(";");
		for(int j=0;j<a1.length;j++)
		{
			String[] a2=a1[j].split(",");
			String x=a2[10].trim();
			String y=a2[11].trim();
			//String z=a2[12].trim();
			//System.out.println(x+":"+y+":"+z);
			double x1=Double.valueOf(x);
			double y1=Double.valueOf(y);
			double r=0;
			//System.out.println(x1+":"+y1);
			if(x1>252 && y1>252)
			{
				
				r=Math.sqrt((252-x1)*(252-x1)+(252-y1)*(252-y1));
				if(r>0 && r<56)
				{
					m1++;
				}
				if(r>0 && r<56)
				{
					m2++;
				}
				if(r>0 && r<56)
				{
					m3++;
				}
				if(r>0 && r<56)
				{
					m4++;
				}
				if(r>0 && r<56)
				{
					m5++;
				}
			}
			
			
			
		}
		*/
		
		
	
	}
	/*if  (eElement.getAttribute("name").equals("GDyingA")) {
		
		String[] a1= eElement.getTextContent().split(";");
		for(int j=0;j<a1.length;j++)
		{
			String[] a2=a1[j].split(",");
			String x=a2[10].trim();
			String y=a2[11].trim();
			//String z=a2[12].trim();
			//System.out.println(x+":"+y+":"+z);
			double x1=Double.valueOf(x);
			double y1=Double.valueOf(y);
			double r=0;
			//System.out.println(x1+":"+y1);
			if(x1>252 && y1>252)
			{
				
				r=Math.sqrt((252-x1)*(252-x1)+(252-y1)*(252-y1));
				if(r>0 && r<56)
				{
					d1++;
				}
				if(r>0 && r<56)
				{
					d2++;
				}
				if(r>0 && r<56)
				{
					d3++;
				}
				if(r>0 && r<56)
				{
					d4++;
				}
				if(r>0 && r<56)
				{
					d5++;
				}
			}
			
			
			
		}
		
		
		
	
	}

	System.out.println(aa1);
	System.out.println(aa2);
	System.out.println(aa3);
	System.out.println(aa4);
	System.out.println(aa5);
	*/
			
		}}


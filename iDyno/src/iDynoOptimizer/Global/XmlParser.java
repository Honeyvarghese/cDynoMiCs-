package iDynoOptimizer.Global;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * Created by Chris on 11/20/2014.
 */
public class XmlParser {

    public static Document parseXML(String path) {
        File fXmlFile = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            return dBuilder.parse(fXmlFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;

    }


    private static NodeList getElements(Document d, String tagName) {
        return d.getElementsByTagName(tagName);
    }


    public static NodeList getElements(String path, String tagName) {
        Document d = parseXML(path);

        return getElements(d, tagName);
    }


}

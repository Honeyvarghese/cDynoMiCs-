package iDynoOptimizer.Protocol.ProtocolFile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

/**
 * Created by Chris on 11/19/2014.
 */
public class ProtocolWriter {


    public static void write(String path) {
        ProtocolFile rep = ProtocolFile.getClassRep();

        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document d = docBuilder.newDocument();

            Element e = d.createElement(rep.getRoot().getTagName().toString());
            writeTagAttributes(rep.getRoot(), e);
            writeTags(rep.getRoot(), e, d);

            d.appendChild(e);


            // write the content into xml file


            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(d);
            File f = new File(path);
            if (!f.exists()) {
                f.createNewFile();
                f = new File(path);
            }

            StreamResult result = new StreamResult(f);
            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void writeTags(Tag tag, Element e, Document d) {

        for (Tag t : tag.getAllChildren()) {
            Element newElement = d.createElement(t.getTagName().toString());

            e.appendChild(newElement);
            writeTagAttributes(t, newElement);
            String value = t.getStringValue();

            if (value != null && !value.isEmpty()) {
                Text textNode = d.createTextNode(value);

                newElement.appendChild(textNode);
                e.appendChild(newElement);

            }
            writeTags(t, newElement, d);

        }


    }

    private static void writeTagAttributes(Tag tag, Element e) {


        for (String pn : tag.getStringAttributes().keySet()) {

            e.setAttribute(pn, tag.getStringAttributes().get(pn).getValue());
        }

        for (String pn : tag.getDoubleAttributes().keySet()) {

            e.setAttribute(pn, String.valueOf(tag.getDoubleAttributes().get(pn).toString()));
        }

    }


}

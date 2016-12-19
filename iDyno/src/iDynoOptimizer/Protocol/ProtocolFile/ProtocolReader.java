package iDynoOptimizer.Protocol.ProtocolFile;

import de.schlichtherle.io.File;
import iDynoOptimizer.Global.MyPrinter;
import iDynoOptimizer.Global.XmlParser;
import org.w3c.dom.*;


/**
 * Created by Chris on 11/19/2014.
 */
public class ProtocolReader {


    public static ProtocolFile read(String path) {


        MyPrinter.Printer().printTier1ln("Reading protocol file " + path);

        ProtocolFile rep = ProtocolFile.getClassRep();


        Document d = XmlParser.parseXML(path);

        rep.setProtocolFileName(d.getDocumentURI().replace(File.separator, ""));


        Element rootElement = d.getDocumentElement();

        rep.setRoot(new Tag(rootElement.getNodeName()));

        addChildTag(rootElement, rep.getRoot());


        rep.getChangingParamsRecursively();

        return rep;
    }


    private static void addChildTag(Element element, Tag parent) {


        NodeList children = element.getChildNodes();
        int count = children.getLength();

        Element current = null;

        for (int i = 0; i < count; i++) {
            Node c = children.item(i);

            if (c.getNodeType() == Node.ELEMENT_NODE) {
                current = (Element) c;
                Tag child = new Tag(current.getNodeName());

                AddAttributes(current, child);
                parent.addChild(child);

                if (current.hasChildNodes()) {
                    addChildTag(current, child);
                }


            } else if (c.getNodeType() == Node.TEXT_NODE) {
                String value = c.getNodeValue();

                parent.setValue(value);

            }
        }


    }


    private static void AddAttributes(Element element, Tag tag) {
        NamedNodeMap attributes = element.getAttributes();

        int numAttrs = attributes.getLength();

        for (int i = 0; i < numAttrs; i++) {
            Attr attr = (Attr) attributes.item(i);

            tag.addAttrs(attr.getName(), attr.getValue());
        }
    }


}

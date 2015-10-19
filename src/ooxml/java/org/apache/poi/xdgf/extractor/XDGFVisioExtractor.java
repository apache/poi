package org.apache.poi.xdgf.extractor;

import java.io.IOException;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xdgf.usermodel.XDGFPage;
import org.apache.poi.xdgf.usermodel.XmlVisioDocument;
import org.apache.poi.xdgf.usermodel.shape.ShapeTextVisitor;

/**
 * Helper class to extract text from an OOXML Visio File
 */
public class XDGFVisioExtractor extends POIXMLTextExtractor {

    protected final XmlVisioDocument document;
    
    public XDGFVisioExtractor(XmlVisioDocument document) {
        super(document);
        this.document = document;
    }

    public XDGFVisioExtractor(OPCPackage openPackage) throws IOException {
        this(new XmlVisioDocument(openPackage));
    }

    public String getText() {
        ShapeTextVisitor visitor = new ShapeTextVisitor();
        
        for (XDGFPage page: document.getPages()) {
            page.getContent().visitShapes(visitor);
        }
        
        return visitor.getText().toString();
    }
    
    public static void main(String [] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Use:");
            System.err.println("  XDGFVisioExtractor <filename.vsdx>");
            System.exit(1);
        }
        POIXMLTextExtractor extractor =
                new XDGFVisioExtractor(POIXMLDocument.openPackage(
                        args[0]
                ));
        System.out.println(extractor.getText());
        extractor.close();
    }
}

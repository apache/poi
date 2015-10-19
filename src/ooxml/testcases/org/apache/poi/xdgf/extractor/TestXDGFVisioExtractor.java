package org.apache.poi.xdgf.extractor;

import java.io.IOException;

import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xdgf.usermodel.XmlVisioDocument;

import junit.framework.TestCase;

public class TestXDGFVisioExtractor extends TestCase {

    private POIDataSamples diagrams;
    private OPCPackage pkg;
    private XmlVisioDocument xml;

    protected void setUp() throws Exception {
        diagrams = POIDataSamples.getDiagramInstance();
        
        pkg = OPCPackage.open(diagrams.openResourceAsStream("test_text_extraction.vsdx"));
        xml = new XmlVisioDocument(pkg);
    }

    public void testGetSimpleText() throws IOException {
        new XDGFVisioExtractor(xml).close();
        new XDGFVisioExtractor(pkg).close();
        
        XDGFVisioExtractor extractor = new XDGFVisioExtractor(xml);
        extractor.getText();
        
        String text = extractor.getText();
        assertTrue(text.length() > 0);
        
        assertEquals("Text here\nText there\nText, text, everywhere!\nRouter here\n",
                     text);
        
        extractor.close();
    }
}

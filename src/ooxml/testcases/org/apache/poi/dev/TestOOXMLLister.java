package org.apache.poi.dev;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.util.NullOutputStream;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.Test;

import java.io.File;
import java.io.PrintStream;

public class TestOOXMLLister {
    @Test
    public void testMain() throws Exception {
        File file = XSSFTestDataSamples.getSampleFile("Formatting.xlsx");
        OOXMLLister.main(new String[] {file.getAbsolutePath()});
    }

    @Test
    public void testWithPrintStream() throws Exception {
        File file = XSSFTestDataSamples.getSampleFile("Formatting.xlsx");
        OOXMLLister lister = new OOXMLLister(OPCPackage.open(file.getAbsolutePath(), PackageAccess.READ), new PrintStream(new NullOutputStream()));
        lister.displayParts();
        lister.displayRelations();
        lister.close();
    }
}

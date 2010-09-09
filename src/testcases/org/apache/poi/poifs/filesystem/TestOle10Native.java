package org.apache.poi.poifs.filesystem;

import junit.framework.TestCase;
import org.apache.poi.POIDataSamples;

import java.io.IOException;

public class TestOle10Native extends TestCase {
    private static final POIDataSamples dataSamples = POIDataSamples.getPOIFSInstance();

    public void testOleNative() throws IOException, Ole10NativeException {
        POIFSFileSystem fs = new POIFSFileSystem(dataSamples.openResourceAsStream("oleObject1.bin"));

        Ole10Native ole = Ole10Native.createFromEmbeddedOleObject(fs);

        assertEquals("File1.svg", ole.getLabel());
        assertEquals("D:\\Documents and Settings\\rsc\\My Documents\\file1.svg", ole.getCommand());
    }
}

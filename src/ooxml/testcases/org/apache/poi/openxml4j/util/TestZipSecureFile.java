package org.apache.poi.openxml4j.util;

import org.apache.poi.openxml4j.opc.internal.ZipHelper;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;

import static org.junit.Assert.assertTrue;

public class TestZipSecureFile {
    @Test
    public void testThresholdInputStream() throws Exception {
        // This fails in Java 10 because our reflection injection of the ThresholdInputStream causes a
        // ClassCastException in ZipFile now
        ZipSecureFile.ThresholdInputStream zis = ZipHelper.openZipStream(new FileInputStream(XSSFTestDataSamples.getSampleFile("template.xlsx")));
        ZipInputStreamZipEntrySource thresholdInputStream = new ZipInputStreamZipEntrySource(zis);

        ZipSecureFile secureFile = new ZipSecureFile(XSSFTestDataSamples.getSampleFile("template.xlsx"));

        Enumeration<? extends ZipEntry> entries = thresholdInputStream.getEntries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();

            InputStream inputStream = secureFile.getInputStream(entry);
            assertTrue(inputStream.available() > 0);
        }
    }
}
package org.apache.poi.openxml4j.opc;

import org.apache.poi.openxml4j.OpenXML4JTestDataSamples;
import org.apache.poi.openxml4j.opc.internal.TempFilePackagePart;
import org.apache.poi.util.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTempFilePackagePart {
    @Test
    void testRoundTrip() throws Exception {
        String text = UUID.randomUUID().toString();
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        String filepath =  OpenXML4JTestDataSamples.getSampleFileName("sample.docx");

        try (OPCPackage p = OPCPackage.open(filepath, PackageAccess.READ)) {
            PackagePartName name = new PackagePartName("/test.txt", true);
            TempFilePackagePart part = new TempFilePackagePart(p, name, "text/plain");
            try (OutputStream os = part.getOutputStream()) {
                os.write(text.getBytes(StandardCharsets.UTF_8));
            }
            assertEquals(bytes.length, part.getSize());
            try (InputStream is = part.getInputStream()) {
                assertEquals(text, new String(IOUtils.toByteArray(is), StandardCharsets.UTF_8));
            }
        }
    }
}

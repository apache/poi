package org.apache.poi.xssf.streaming;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class OpcZipTest {
    @Test
    void compareOutput() throws Exception {
        Map<String, String> contents = createContents();
        try (
                UnsynchronizedByteArrayOutputStream bos1 = new UnsynchronizedByteArrayOutputStream();
                UnsynchronizedByteArrayOutputStream bos2 = new UnsynchronizedByteArrayOutputStream()
        ) {
            try (OpcOutputStream zip = new OpcOutputStream(bos1)) {
                for (Map.Entry<String, String> entry : contents.entrySet()) {
                    zip.putNextEntry(entry.getKey());
                    PrintStream printer = new PrintStream(zip, true, StandardCharsets.UTF_8.name());
                    printer.print(entry.getValue());
                    printer.flush();
                    zip.closeEntry();
                }
            }
            try (com.github.rzymek.opczip.OpcOutputStream zip = new com.github.rzymek.opczip.OpcOutputStream(bos2)) {
                for (Map.Entry<String, String> entry : contents.entrySet()) {
                    zip.putNextEntry(new ZipEntry(entry.getKey()));
                    PrintStream printer = new PrintStream(zip, true, StandardCharsets.UTF_8.name());
                    printer.print(entry.getValue());
                    printer.flush();
                    zip.closeEntry();
                }
            }
            assertArrayEquals(bos1.toByteArray(), bos2.toByteArray());
        }
    }

    private static Map<String, String> createContents() {
        Map<String, String> contents = new LinkedHashMap<>();
        for (int i = 0; i < 3; i++) {
            String name = String.format(Locale.US, "dir%s/file%s.txt", i % 3, i);
            contents.put(name, "this is the contents");
        }
        return contents;
    }
}

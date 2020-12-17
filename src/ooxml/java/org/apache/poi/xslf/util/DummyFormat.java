package org.apache.poi.xslf.util;

import java.awt.Graphics2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class DummyFormat implements OutputFormat {

    private final ByteArrayOutputStream bos;
    private final DummyGraphics2d dummy2d;

    public DummyFormat() {
        try {
            bos = new ByteArrayOutputStream();
            dummy2d = new DummyGraphics2d(new PrintStream(bos, true, StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Graphics2D addSlide(double width, double height) throws IOException {
        bos.reset();
        return dummy2d;
    }

    @Override
    public void writeSlide(MFProxy proxy, File outFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            bos.writeTo(fos);
            bos.reset();
        }
    }

    @Override
    public void writeDocument(MFProxy proxy, File outFile) throws IOException {

    }

    @Override
    public void close() throws IOException {
        bos.reset();
    }
}

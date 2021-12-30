package org.apache.poi.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.hssf.record.common.FormatRun;
import org.junit.jupiter.api.Test;

class TestLittleEndianInputStream {

    @Test
    void formatRun() throws IOException {
        FormatRun fr = new FormatRun((short)4, (short)0x15c);
        assertEquals(4, fr.getCharacterPos());
        assertEquals(0x15c, fr.getFontIndex());

        UnsynchronizedByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream();
        LittleEndianOutputStream out = new LittleEndianOutputStream(baos);

        fr.serialize(out);

        byte[] b = baos.toByteArray();
        assertEquals(4, b.length);
        assertEquals(4, b[0]);
        assertEquals(0, b[1]);
        assertEquals(0x5c, b[2]);
        assertEquals(0x01, b[3]);

        LittleEndianInputStream inp = new LittleEndianInputStream(new ByteArrayInputStream(b));
        fr = new FormatRun(inp);
        assertEquals(4, fr.getCharacterPos());
        assertEquals(0x15c, fr.getFontIndex());

        assertEquals(4, inp.getReadIndex());

        byte[] arr = new byte[1024];
        assertEquals(-1, inp.read(arr, 0, 1024));
        assertEquals(4, inp.getReadIndex());
    }

    @Test
    void empty() throws IOException {
        byte[] b = new byte[0];
        LittleEndianInputStream inp = new LittleEndianInputStream(new ByteArrayInputStream(b));
        assertEquals(0, inp.getReadIndex());

        byte[] arr = new byte[1024];
        assertEquals(-1, inp.read(arr, 0, 1024));
        assertEquals(0, inp.getReadIndex());
    }

}
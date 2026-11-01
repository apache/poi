package org.apache.poi.hemf.record.emfplus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.commons.io.input.NullInputStream;
import org.apache.poi.util.LittleEndianInputStream;
import org.junit.jupiter.api.Test;

class TestHemfPlusMisc {
    @Test
    void testInit() throws IOException {
        HemfPlusMisc.EmfPlusFlagOnly rec = new HemfPlusMisc.EmfPlusSetPixelOffsetMode();
        rec.init(new LittleEndianInputStream(NullInputStream.nullInputStream()), 0, 0, 3);

        assertEquals(3, rec.getFlags());
    }

    @Test
    void testInitInvalidDataSize() throws IOException {
        HemfPlusMisc.EmfPlusFlagOnly rec = new HemfPlusMisc.EmfPlusSetPixelOffsetMode();
        rec.init(new LittleEndianInputStream(NullInputStream.nullInputStream()), 1, 0, 3);

        assertEquals(3, rec.getFlags());
    }
}
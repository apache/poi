package org.apache.poi.ddf;

import java.io.ByteArrayInputStream;

import org.junit.Test;

public class TestEscherDump {
    @Test
    public void testSimple() throws Exception {
        // simple test to at least cover some parts of the class
        EscherDump.main(new String[] {});
        
        new EscherDump().dump(0, new byte[] {}, System.out);
        new EscherDump().dump(new byte[] {}, 0, 0, System.out);
        new EscherDump().dumpOld(0, new ByteArrayInputStream(new byte[] {}), System.out);
    }

    @Test
    public void testWithData() throws Exception {
        new EscherDump().dumpOld(8, new ByteArrayInputStream(new byte[] { 00, 00, 00, 00, 00, 00, 00, 00 }), System.out);
    }
}

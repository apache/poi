package org.apache.poi.hwpf.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class PlfLfoTest {
    @Test
    public void testAdd() {
        PlfLfo p = new PlfLfo(new byte[] {0, 0, 0, 0}, 0, 0);
        assertEquals(0, p.getLfoMac());
        p.add(new LFO(new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, 0), new LFOData());
        assertEquals(1, p.getLfoMac());
        assertNotNull(p.getLfo(1));
        assertNotNull(p.getLfoData(1));
    }

    @Test
    public void testEquals() {
        PlfLfo p = new PlfLfo(new byte[] {0, 0, 0, 0}, 0, 0);
        PlfLfo p2 = new PlfLfo(new byte[] {0, 0, 0, 0}, 0, 0);
        assertEquals(0, p.getLfoMac());
        assertEquals(0, p2.getLfoMac());

        assertTrue(p.equals(p2));
        //noinspection ObjectEqualsNull
        assertFalse(p.equals(null));

        p.add(new LFO(new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, 0), new LFOData());
        assertEquals(1, p.getLfoMac());

        assertFalse(p.equals(p2));

        p2.add(new LFO(new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, 0), new LFOData());
        assertEquals(1, p2.getLfoMac());
        assertTrue(p.equals(p2));
    }
}
package org.apache.poi.ddf;

import junit.framework.TestCase;

public class TestEscherBoolProperty extends TestCase
{
    public void testToString() throws Exception
    {
        EscherBoolProperty p = new EscherBoolProperty((short)1, 1);
        assertEquals("propNum: 1, propName: unknown, complex: false, blipId: false, value: 1 (0x00000001)", p.toString());
    }

}

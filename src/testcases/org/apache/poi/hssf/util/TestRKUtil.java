package org.apache.poi.hssf.util;

import junit.framework.TestCase;

public class TestRKUtil
        extends TestCase
{
    public TestRKUtil(String s)
    {
        super(s);
    }

    public void testDecode()
            throws Exception
    {
        assertEquals(3.0, RKUtil.decodeNumber(1074266112), 0.0000001);
        assertEquals(3.3, RKUtil.decodeNumber(1081384961), 0.0000001);
        assertEquals(3.33, RKUtil.decodeNumber(1081397249), 0.0000001);
    }
}

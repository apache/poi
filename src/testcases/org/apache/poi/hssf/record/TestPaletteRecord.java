/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.poi.hssf.record;

import java.util.Iterator;
import java.util.Map;
import junit.framework.TestCase;
import org.apache.poi.hssf.util.HSSFColor;

/**
 * Verifies that custom palette editing works correctly
 *
 * @author Brian Sanders (bsanders at risklabs dot com)
 */
public class TestPaletteRecord extends TestCase
{
    public TestPaletteRecord(String name)
    {
        super(name);
    }
    
    /**
     * Tests that the default palette matches the constants of HSSFColor
     */
    public void testDefaultPalette()
    {
        PaletteRecord palette = new PaletteRecord(PaletteRecord.sid);
        
        //make sure all the HSSFColor constants match
        Map colors = HSSFColor.getIndexHash();
        Iterator indexes = colors.keySet().iterator();
        while (indexes.hasNext())
        {
            Integer index = (Integer) indexes.next();
            HSSFColor c = (HSSFColor) colors.get(index);
            short[] rgbTriplet = c.getTriplet();
            byte[] paletteTriplet = palette.getColor(index.shortValue());
            String msg = "Expected HSSFColor constant to match PaletteRecord at index 0x"
                + Integer.toHexString(c.getIndex());
            assertEquals(msg, rgbTriplet[0], paletteTriplet[0] & 0xff);
            assertEquals(msg, rgbTriplet[1], paletteTriplet[1] & 0xff);
            assertEquals(msg, rgbTriplet[2], paletteTriplet[2] & 0xff);
        }
    }
}

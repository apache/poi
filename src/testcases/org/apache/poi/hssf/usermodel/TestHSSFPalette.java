/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
package org.apache.poi.hssf.usermodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import junit.framework.TestCase;
import org.apache.poi.hssf.record.PaletteRecord;
import org.apache.poi.hssf.util.HSSFColor;

/**
 * @author Brian Sanders (bsanders at risklabs dot com)
 */
public class TestHSSFPalette extends TestCase
{
    private PaletteRecord palette;
    private HSSFPalette hssfPalette;
    
    public TestHSSFPalette(String name)
    {
        super(name);
    }
    
    public void setUp()
    {
        palette = new PaletteRecord(PaletteRecord.sid);
        hssfPalette = new HSSFPalette(palette);
    }
    
    /**
     * Verifies that a custom palette can be created, saved, and reloaded
     */
    public void testCustomPalette() throws IOException
    {
        //reading sample xls
        String dir = System.getProperty("HSSF.testdata.path");
        File sample = new File(dir + "/Simple.xls");
        assertTrue("Simple.xls exists and is readable", sample.canRead());
        FileInputStream fis = new FileInputStream(sample);
        HSSFWorkbook book = new HSSFWorkbook(fis);
        fis.close();
        
        //creating custom palette
        HSSFPalette palette = book.getCustomPalette();
        palette.setColorAtIndex((short) 0x12, (byte) 101, (byte) 230, (byte) 100);
        palette.setColorAtIndex((short) 0x3b, (byte) 0, (byte) 255, (byte) 52);
        
        //writing to disk; reading in and verifying palette
        File temp = File.createTempFile("testCustomPalette", ".xls");
        FileOutputStream fos = new FileOutputStream(temp);
        book.write(fos);
        fos.close();
        
        fis = new FileInputStream(temp);
        book = new HSSFWorkbook(fis);
        fis.close();
        
        palette = book.getCustomPalette();
        HSSFColor color = palette.getColor(HSSFColor.CORAL.index);  //unmodified
        assertNotNull("Unexpected null in custom palette (unmodified index)", color);
        short[] expectedRGB = HSSFColor.CORAL.triplet;
        short[] actualRGB = color.getTriplet();
        String msg = "Expected palette position to remain unmodified";
        assertEquals(msg, expectedRGB[0], actualRGB[0]);
        assertEquals(msg, expectedRGB[1], actualRGB[1]);
        assertEquals(msg, expectedRGB[2], actualRGB[2]);
        
        color = palette.getColor((short) 0x12);
        assertNotNull("Unexpected null in custom palette (modified)", color);
        actualRGB = color.getTriplet();
        msg = "Expected palette modification to be preserved across save";
        assertEquals(msg, (short) 101, actualRGB[0]);
        assertEquals(msg, (short) 230, actualRGB[1]);
        assertEquals(msg, (short) 100, actualRGB[2]);
    }
    
    /**
     * Verifies that the generated gnumeric-format string values match the
     * hardcoded values in the HSSFColor default color palette
     */
    public void testGnumericStrings()
    {
        compareToDefaults(new ColorComparator() {
            public void compare(HSSFColor expected, HSSFColor palette)
            {
                assertEquals(expected.getHexString(), palette.getHexString());
            }
        });
    }
    
    /**
     * Verifies that the palette handles invalid palette indexes
     */
    public void testBadIndexes()
    {
        //too small
        hssfPalette.setColorAtIndex((short) 2, (byte) 255, (byte) 255, (byte) 255);
        //too large
        hssfPalette.setColorAtIndex((short) 0x45, (byte) 255, (byte) 255, (byte) 255);
        
        //should still match defaults; 
        compareToDefaults(new ColorComparator() {
            public void compare(HSSFColor expected, HSSFColor palette)
            {
                short[] s1 = expected.getTriplet();
                short[] s2 = palette.getTriplet();
                assertEquals(s1[0], s2[0]);
                assertEquals(s1[1], s2[1]);
                assertEquals(s1[2], s2[2]);
            }
        });
    }
    
    private void compareToDefaults(ColorComparator c)
    {
        Map colors = HSSFColor.getIndexHash();
        Iterator it = colors.keySet().iterator();
        while (it.hasNext())
        {
            Number index = (Number) it.next();
            HSSFColor expectedColor = (HSSFColor) colors.get(index);
            HSSFColor paletteColor = hssfPalette.getColor(index.shortValue());
            c.compare(expectedColor, paletteColor);
        }
    }
    
    private static interface ColorComparator
    {
        void compare(HSSFColor expected, HSSFColor palette);
    }
}

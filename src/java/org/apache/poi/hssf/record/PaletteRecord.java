
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

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.util.LittleEndian;

/**
 * PaletteRecord - Supports custom palettes.
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class PaletteRecord
    extends Record
{
    public final static short sid = 0x92;

    private short field_1_numcolors;
    private List  field_2_colors;

    public PaletteRecord()
    {
    }

    /**
     * Constructs a PaletteRecord record and sets its fields appropriately.
     *
     * @param id     id must be 0x92 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public PaletteRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a PaletteRecord record and sets its fields appropriately.
     *
     * @param id     id must be 0x0A or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public PaletteRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT An Palette RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
       field_1_numcolors = LittleEndian.getShort(data,offset+0); 
       field_2_colors    = new ArrayList(field_1_numcolors);
       for (int k = 0; k < field_1_numcolors; k++) {
           field_2_colors.add(new PColor(
                                         data[2+ offset+(k * 4) +0],
                                         data[2+ offset+(k * 4) +1],
                                         data[2+ offset+(k * 4) +2]
                                        )
                              );
       } 
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[Palette]\n");
        buffer.append("  numcolors     = ").append(field_1_numcolors)
              .append('\n');
        for (int k = 0; k < field_1_numcolors; k++) {
        PColor c = (PColor) field_2_colors.get(k);
        buffer.append("* colornum      = ").append(k)
              .append('\n');
        buffer.append(c.toString());
        buffer.append("/*colornum      = ").append(k)
              .append('\n');
        }
        buffer.append("[/Palette]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        for (int k = 0; k < field_1_numcolors; k++) {
          PColor c = (PColor)field_2_colors.get(k);
          c.serialize(data, (2+offset+(k*4)));
        }

        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 2 + (field_1_numcolors * 4);
    }

    public short getSid()
    {
        return this.sid;
    }

}

/**
 * PColor - element in the list of colors - consider it a "struct"
 */
class PColor {
  public byte red;
  public byte green;
  public byte blue;
  public PColor(byte red, byte green, byte blue) {
    this.red=red;
    this.green=green;
    this.blue=blue;
  }

  public void serialize(byte[] data, int offset) {
     data[offset + 0] = red;
     data[offset + 1] = green;
     data[offset + 2] = blue;
     data[offset + 3] = 0;
  }

  public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("  red           = ").append(red).append('\n');
        buffer.append("  green         = ").append(green).append('\n');
        buffer.append("  blue          = ").append(blue).append('\n');
        return buffer.toString();
  }
}

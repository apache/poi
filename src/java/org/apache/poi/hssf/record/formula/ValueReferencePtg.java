
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

/*
 * ValueReferencePtg.java
 *
 * Created on November 21, 2001, 5:27 PM
 */
package org.apache.poi.hssf.record.formula;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.BitField;

/**
 *
 * @author  andy
 */

public class ValueReferencePtg
    extends Ptg
{
    private final static int SIZE = 5;
    public final static byte sid  = 0x44;
    private short            field_1_row;
    private short            field_2_col;
    private BitField         rowRelative = new BitField(0x8000);
    private BitField         colRelative = new BitField(0x4000);

    /** Creates new ValueReferencePtg */

    public ValueReferencePtg()
    {
    }

    /** Creates new ValueReferencePtg */

    public ValueReferencePtg(byte [] data, int offset)
    {
        offset++;   // adjust for ptg
        field_1_row = LittleEndian.getShort(data, offset + 0);
        field_2_col = LittleEndian.getShort(data, offset + 2);
        System.out.println(toString());
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer("[ValueReferencePtg]\n");

        buffer.append("row = ").append(getRow()).append("\n");
        buffer.append("col = ").append(getColumnRaw()).append("\n");
        buffer.append("rowrelative = ").append(isRowRelative()).append("\n");
        buffer.append("colrelative = ").append(isColRelative()).append("\n");
        return buffer.toString();
    }

    public void writeBytes(byte [] array, int offset)
    {
    }

    public void setRow(short row)
    {
        field_1_row = row;
    }

    public short getRow()
    {
        return field_1_row;
    }

    public boolean isRowRelative()
    {
        return rowRelative.isSet(field_2_col);
    }

    public boolean isColRelative()
    {
        return rowRelative.isSet(field_2_col);
    }

    public void setColumnRaw(short col)
    {
        field_2_col = col;
    }

    public short getColumnRaw()
    {
        return field_2_col;
    }

    public void setColumn(short col)
    {
        field_2_col = col;   // fix this
    }

    public short getColumn()
    {
        return field_2_col;   // fix this
    }

    public int getSize()
    {
        return SIZE;
    }

    public String toFormulaString()
    {
        return "NO IDEA YET VALUE REF";
    }
}

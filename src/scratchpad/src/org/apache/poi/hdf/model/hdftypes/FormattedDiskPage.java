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

package org.apache.poi.hwpf.model.hdftypes;

import org.apache.poi.util.LittleEndian;

/**
 * Represents an FKP data structure. This data structure is used to store the
 * grpprls of the paragraph and character properties of the document. A grpprl
 * is a list of sprms(decompression operations) to perform on a parent style.
 *
 * The style properties for paragraph and character runs
 * are stored in fkps. There are PAP fkps for paragraph properties and CHP fkps
 * for character run properties. The first part of the fkp for both CHP and PAP
 * fkps consists of an array of 4 byte int offsets in the main stream for that
 * Paragraph's or Character run's text. The ending offset is the next
 * value in the array. For example, if an fkp has X number of Paragraph's
 * stored in it then there are (x + 1) 4 byte ints in the beginning array. The
 * number X is determined by the last byte in a 512 byte fkp.
 *
 * CHP and PAP fkps also store the compressed styles(grpprl) that correspond to
 * the offsets on the front of the fkp. The offset of the grpprls is determined
 * differently for CHP fkps and PAP fkps.
 *
 * @author Ryan Ackley
 */
public abstract class FormattedDiskPage
{
    protected byte[] _fkp;
    protected int _crun;
    protected int _offset;


    public FormattedDiskPage()
    {

    }

    /**
     * Uses a 512-byte array to create a FKP
     */
    public FormattedDiskPage(byte[] documentStream, int offset)
    {
        _crun = LittleEndian.getUnsignedByte(documentStream, offset + 511);
        _fkp = documentStream;
        _offset = offset;
    }
    /**
     * Used to get a text offset corresponding to a grpprl in this fkp.
     * @param index The index of the property in this FKP
     * @return an int representing an offset in the "WordDocument" stream
     */
    protected int getStart(int index)
    {
        return LittleEndian.getInt(_fkp, _offset + (index * 4));
    }
    /**
     * Used to get the end of the text corresponding to a grpprl in this fkp.
     * @param index The index of the property in this fkp.
     * @return an int representing an offset in the "WordDocument" stream
     */
    protected int getEnd(int index)
    {
        return LittleEndian.getInt(_fkp, _offset + ((index + 1) * 4));
    }
    /**
     * Used to get the total number of grrprl's stored int this FKP
     * @return The number of grpprls in this FKP
     */
    public int size()
    {
        return _crun;
    }

    protected abstract byte[] getGrpprl(int index);
}

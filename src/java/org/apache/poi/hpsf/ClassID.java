/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2000 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Apache" and "Apache Software Foundation" must
 *  not be used to endorse or promote products derived from this
 *  software without prior written permission. For written
 *  permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  nor may "Apache" appear in their name, without prior written
 *  permission of the Apache Software Foundation.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 *
 */
package org.apache.poi.hpsf;

import java.io.*;

/**
 *  REWRITE ME
 *  <p>
 *  Represents a class ID (16 bytes). Unlike other little-endian type the {@link
 *  ClassID} is not just 16 bytes stored in the wrong order. Instead, it is a
 *  double word (4 bytes) followed by two words (2 bytes each) followed by 8
 *  bytes.</p>
 *
 *@author     Rainer Klute (klute@rainer-klute.de)
 *@created    May 10, 2002
 *@see        LittleEndian
 *@version    $Id$
 *@since      2002-02-09
 */
public class ClassID {

    /**
     *  <p>
     *
     *  Creates a {@link ClassID} and reads its value from a byte array.</p>
     *
     *@param  src     The byte array to read from.
     *@param  offset  The offset of the first byte to read.
     */
    public ClassID(final byte[] src, final int offset) {
        super(src, offset);
    }



    public final static int LENGTH = 16;

    public int length() {
        return LENGTH;
    }


    /**
     *  Description of the Method - REWRITE ME REWRITE ME REWRITE ME
     *  ISNT += offset a bug?  -- doesn't the order of operations evaluate that
     * last?
     *
     *@param  src     Description of the Parameter
     *@param  offset  Description of the Parameter
     *@return         Description of the Return Value
     */
    public byte[] read(byte[] src, int offset) {
        byte[] retval = new byte[24];

        throw new RuntimeException("This fucntion must be rewritten");
/*
        Object[] b = new Object[11]; 
        b[0] = new Long(LittleEndian.getUInt(src, offset));
        b[1] = new Integer(LittleEndian.getInt(src, offset += LittleEndian.INT_SIZE));
        b[2] = new Integer(LittleEndian.getInt(src, offset += LittleEndian.INT_SIZE));
        b[3] = new Byte(LittleEndian.getUnsignedByte(src, offset += LittleEndian.INT_SIZE);  
        b[4] = new Byte(LittleEndian.getUnsignedByte(src, offset += 1));
        b[5] = new Byte(LittleEndian.getUnsignedByte(src, offset += 1));
        b[6] = new Byte(LittleEndian.getUnsignedByte(src, offset += 1));
        b[7] = new Byte(LittleEndian.getUnsignedByte(src, offset += 1));
        b[8] = new Byte(LittleEndian.getUnsignedByte(src, offset += 1));
        b[9] = new Byte(LittleEndian.getUnsignedByte(src, offset += 1));
        b[10] = new Byte(LittleEndian.getUnsignedByte(src, offset += 1));
        int capacity = 24;
//        for (int i = 0; i < b.length; i++) {
//            capacity += b[i].getBytes().length;
//        }
        bytes = new byte[capacity];
        int pos = 0;
        
        for (int i = 0; i < b.length; i++) {
            byte[] s = b[i].getBytes();
            for (int j = 0; j < s.length; j++) {
                bytes[pos++] = s[j];
            }
        }
        return bytes;*/
    }

}

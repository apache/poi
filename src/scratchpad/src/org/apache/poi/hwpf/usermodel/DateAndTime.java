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

package org.apache.poi.hwpf.usermodel;

import org.apache.poi.util.BitField;
import org.apache.poi.util.LittleEndian;

public class DateAndTime
  implements Cloneable
{
  public static final int SIZE = 4;
  private short _info;
    private static final BitField _minutes = new BitField(0x3f);
    private static final BitField _hours = new BitField(0x7c0);
    private static final BitField _dom = new BitField(0xf800);
  private short _info2;
    private static final BitField _months = new BitField(0xf);
    private static final BitField _years = new BitField(0x1ff0);
    private static final BitField _weekday = new BitField(0xe000);

  public DateAndTime()
  {
  }

  public DateAndTime(byte[] buf, int offset)
  {
    _info = LittleEndian.getShort(buf, offset);
    _info2 = LittleEndian.getShort(buf, offset + LittleEndian.SHORT_SIZE);
  }

  public void serialize(byte[] buf, int offset)
  {
    LittleEndian.putShort(buf, offset, _info);
    LittleEndian.putShort(buf, offset + LittleEndian.SHORT_SIZE, _info2);
  }

  public boolean equals(Object o)
  {
    DateAndTime dttm = (DateAndTime)o;
    return _info == dttm._info && _info2 == dttm._info2;
  }

  public Object clone()
    throws CloneNotSupportedException
  {
    return super.clone();
  }
}

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



/**
 * Represents a lightweight node in the Trees used to store content
 * properties.
 *
 * @author Ryan Ackley
 */
public class PropertyNode implements Comparable
{
  private byte[] _buf;
  private int _cpStart;
  private int _cpEnd;

  /**
   * @param fcStart The start of the text for this property.
   * @param fcEnd The end of the text for this property.
   * @param grpprl The property description in compressed form.
   */
  public PropertyNode(int fcStart, int fcEnd, byte[] buf)
  {
      _cpStart = fcStart;
      _cpEnd = fcEnd;
      _buf = buf;
  }
  /**
   * @return The offset of this property's text.
   */
  public int getStart()
  {
      return _cpStart;
  }
  /**
   * @retrun The offset of the end of this property's text.
   */
  public int getEnd()
  {
    return _cpEnd;
  }
  /**
   * @return This property's property in copmpressed form.
   */
  public byte[] getBuf()
  {
    return _buf;
  }

  public boolean equals(Object o)
  {
    if (((PropertyNode)o).getStart() == _cpStart &&
        ((PropertyNode)o).getEnd() == _cpEnd)
    {
      byte[] testBuf = ((PropertyNode)o).getBuf();

      if (testBuf.length == _buf.length)
      {
        for (int x = 0; x < _buf.length; x++)
        {
          if (testBuf[x] != _buf[x])
          {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }
  /**
   * Used for sorting in collections.
   */
  public int compareTo(Object o)
  {
      int cpEnd = ((PropertyNode)o).getEnd();
      if(_cpEnd == cpEnd)
      {
        return 0;
      }
      else if(_cpEnd < cpEnd)
      {
        return -1;
      }
      else
      {
        return 1;
      }
  }
}

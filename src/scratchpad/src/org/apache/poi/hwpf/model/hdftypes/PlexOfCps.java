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

import java.util.ArrayList;

import org.apache.poi.util.LittleEndian;



/**
 * common data structure in a Word file. Contains an array of 4 byte ints in
 * the front that relate to an array of abitrary data structures in the back.
 *
 *
 * @author Ryan Ackley
 */
public class PlexOfCps
{
  private int _count;
  private int _offset;
  private int _sizeOfStruct;
  private ArrayList _props;

  /**
   * Constructor
   *
   * @param size The size in bytes of this PlexOfCps
   * @param sizeOfStruct The size of the data structure type stored in
   *        this PlexOfCps.
   */
  public PlexOfCps(byte[] buf, int start, int size, int sizeOfStruct)
  {
    _count = (size - 4)/(4 + sizeOfStruct);
    _sizeOfStruct = sizeOfStruct;
    _props = new ArrayList(_count);

    for (int x = 0; x < _count; x++)
    {
      _props.add(getProperty(x, buf, start));
    }
  }

  public PropertyNode getProperty(int index)
  {
    return (PropertyNode)_props.get(index);
  }

  protected PropertyNode getProperty(int index, byte[] buf, int offset)
  {
    int start = LittleEndian.getInt(buf, offset + getIntOffset(index));
    int end = LittleEndian.getInt(buf, offset + getIntOffset(index+1));

    byte[] struct = new byte[_sizeOfStruct];
    System.arraycopy(buf, offset + getStructOffset(index), struct, 0, _sizeOfStruct);

    return new PropertyNode(start, end, struct);
  }

  protected int getIntOffset(int index)
  {
    return index * 4;
  }

  /**
   * returns the number of data structures in this PlexOfCps.
   *
   * @return The number of data structures in this PlexOfCps
   */
  public int length()
  {
    return _count;
  }

  /**
   * Returns the offset, in bytes, from the beginning if this PlexOfCps to
   * the data structure at index.
   *
   * @param index The index of the data structure.
   *
   * @return The offset, in bytes, from the beginning if this PlexOfCps to
   *         the data structure at index.
   */
  protected int getStructOffset(int index)
  {
    return (4 * (_count + 1)) + (_sizeOfStruct * index);
  }
}

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


package org.apache.poi.hdf.extractor;

/**
 * Comment me
 *
 * @author Ryan Ackley 
 */

public class TC
{

  boolean _fFirstMerged;
  boolean _fMerged;
  boolean _fVertical;
  boolean _fBackward;
  boolean _fRotateFont;
  boolean _fVertMerge;
  boolean _fVertRestart;
  short _vertAlign;
  short[] _brcTop = new short[2];
  short[] _brcLeft = new short[2];
  short[] _brcBottom = new short[2];
  short[] _brcRight = new short [2];

  public TC()
  {
  }
  static TC convertBytesToTC(byte[] array, int offset)
  {
    TC tc = new TC();
    int rgf = Utils.convertBytesToShort(array, offset);
    tc._fFirstMerged = (rgf & 0x0001) > 0;
    tc._fMerged = (rgf & 0x0002) > 0;
    tc._fVertical = (rgf & 0x0004) > 0;
    tc._fBackward = (rgf & 0x0008) > 0;
    tc._fRotateFont = (rgf & 0x0010) > 0;
    tc._fVertMerge = (rgf & 0x0020) > 0;
    tc._fVertRestart = (rgf & 0x0040) > 0;
    tc._vertAlign = (short)((rgf & 0x0180) >> 7);

    tc._brcTop[0] = Utils.convertBytesToShort(array, offset + 4);
    tc._brcTop[1] = Utils.convertBytesToShort(array, offset + 6);

    tc._brcLeft[0] = Utils.convertBytesToShort(array, offset + 8);
    tc._brcLeft[1] = Utils.convertBytesToShort(array, offset + 10);

    tc._brcBottom[0] = Utils.convertBytesToShort(array, offset + 12);
    tc._brcBottom[1] = Utils.convertBytesToShort(array, offset + 14);

    tc._brcRight[0] = Utils.convertBytesToShort(array, offset + 16);
    tc._brcRight[1] = Utils.convertBytesToShort(array, offset + 18);

    return tc;
  }

}
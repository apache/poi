/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 *  4. The names "Apache" and "Apache Software Foundation" and
 *  "Apache POI" must not be used to endorse or promote products
 *  derived from this software without prior written permission. For
 *  written permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  "Apache POI", nor may "Apache" appear in their name, without
 *  prior written permission of the Apache Software Foundation.
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
 */


package org.apache.poi.hwpf.model.hdftypes;


import java.io.UnsupportedEncodingException;

public class TextPieceTable
{
  StringBuffer _text = new StringBuffer();

  public TextPieceTable(byte[] documentStream, byte[] tableStream, int offset, int size)
    throws UnsupportedEncodingException
  {
    PlexOfCps pieceTable = new PlexOfCps(tableStream, offset, size, PieceDescriptor.getSizeInBytes());

    int multiple = 2;
    int length = pieceTable.length();
    PieceDescriptor[] pieces = new PieceDescriptor[length];

    for (int x = 0; x < length; x++)
    {
      PropertyNode node = pieceTable.getProperty(x);
      pieces[x] = new PieceDescriptor(node.getBuf(), 0);

      if (!pieces[x].isUnicode())
      {
        multiple = 1;
      }

    }

    for (int x = 0; x < pieces.length; x++)
    {
      int start = pieces[x].getFilePosition();
      PropertyNode node = pieceTable.getProperty(x);
      int textSize = node.getEnd() - node.getStart();

      boolean unicode = pieces[x].isUnicode();
      String toStr = null;
      if (unicode)
      {
        toStr = new String(documentStream, start, length * multiple, "UTF-16LE");
      }
      else
      {
        toStr = new String(documentStream, start, length, "ISO-8859-1");
      }
      _text.append(toStr);
    }

  }

}

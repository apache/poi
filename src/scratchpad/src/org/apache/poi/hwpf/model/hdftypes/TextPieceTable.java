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
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

import org.apache.poi.poifs.common.POIFSConstants;

import org.apache.poi.hwpf.model.io.*;


public class TextPieceTable
{
  ArrayList _textPieces = new ArrayList();
  //int _multiple;
  int _cpMin;

  public TextPieceTable(byte[] documentStream, byte[] tableStream, int offset,
                        int size, int fcMin)
    throws UnsupportedEncodingException
  {
    // get our plex of PieceDescriptors
    PlexOfCps pieceTable = new PlexOfCps(tableStream, offset, size, PieceDescriptor.getSizeInBytes());

    //_multiple = 2;
    int length = pieceTable.length();
    PieceDescriptor[] pieces = new PieceDescriptor[length];

    // iterate through piece descriptors raw bytes and create
    // PieceDescriptor objects
    for (int x = 0; x < length; x++)
    {
      PropertyNode node = pieceTable.getProperty(x);
      pieces[x] = new PieceDescriptor(node.getBuf(), 0);

//      if (!pieces[x].isUnicode())
//      {
//        _multiple = 1;
//      }
    }

    _cpMin = pieces[0].getFilePosition() - fcMin;
    // if a piece is unicode the actual offset may be bumped because of the
    // doubling of the needed size.
    int bump = 0;

    // using the PieceDescriptors, build our list of TextPieces.
    for (int x = 0; x < pieces.length; x++)
    {
      int start = pieces[x].getFilePosition();
      PropertyNode node = pieceTable.getProperty(x);
      int nodeStart = node.getStart();

      // multiple will be 2 if there is only one piece and its unicode. Some
      // type of optimization.
      boolean unicode = pieces[x].isUnicode();

      int multiple = 1;
      if (unicode)
      {
        multiple = 2;
      }
      int nodeEnd = ((node.getEnd() - nodeStart) * multiple) + nodeStart;
      int textSize = nodeEnd - nodeStart;


      byte[] buf = new byte[textSize];
      System.arraycopy(documentStream, start, buf, 0, textSize);
      _textPieces.add(new TextPiece(nodeStart + bump, nodeEnd + bump, buf, pieces[x]));

      if (unicode)
      {
        bump += (node.getEnd() - nodeStart);
      }
    }
  }

  public int getCpMin()
  {
    return _cpMin;
  }

  public List getTextPieces()
  {
    return _textPieces;
  }

  public byte[] writeTo(HWPFOutputStream docStream)
    throws IOException
  {

    PlexOfCps textPlex = new PlexOfCps(PieceDescriptor.getSizeInBytes());
    //int fcMin = docStream.getOffset();

    int size = _textPieces.size();
    int bumpDown = 0;
    for (int x = 0; x < size; x++)
    {
      TextPiece next = (TextPiece)_textPieces.get(x);
      PieceDescriptor pd = next.getPieceDescriptor();

      int offset = docStream.getOffset();
      int mod = (offset % POIFSConstants.BIG_BLOCK_SIZE);
      if (mod != 0)
      {
        mod = POIFSConstants.BIG_BLOCK_SIZE - mod;
        byte[] buf = new byte[mod];
        docStream.write(buf);
      }


      // set the text piece position to the current docStream offset.
      pd.setFilePosition(docStream.getOffset());

      // write the text to the docstream and save the piece descriptor to the
      // plex which will be written later to the tableStream.
      //if (_multiple == 1 && pd.isUnicode() &&
      docStream.write(next.getBuf());

      int nodeStart = next.getStart();
      int multiple = 1;
      if (pd.isUnicode())
      {
        multiple = 2;
      }
      textPlex.addProperty(new PropertyNode(nodeStart - bumpDown,
        ((next.getEnd() - nodeStart)/multiple + nodeStart) - bumpDown,
        pd.toByteArray()));

      if (pd.isUnicode())
      {
        bumpDown += ((next.getEnd() - nodeStart)/multiple);
      }


    }

    return textPlex.toByteArray();

  }


  public int adjustForInsert(int listIndex, int length)
  {
    int size = _textPieces.size();

    TextPiece tp = (TextPiece)_textPieces.get(listIndex);
    length = length * (tp.usesUnicode() ? 2 : 1);
    tp.setEnd(tp.getEnd() + length);
    for (int x = listIndex + 1; x < size; x++)
    {
      tp = (TextPiece)_textPieces.get(x);
      tp.setStart(tp.getStart() + length);
      tp.setEnd(tp.getEnd() + length);
    }
    return length;
  }


  public boolean equals(Object o)
  {
    TextPieceTable tpt = (TextPieceTable)o;

    int size = tpt._textPieces.size();
    if (size == _textPieces.size())
    {
      for (int x = 0; x < size; x++)
      {
        if (!tpt._textPieces.get(x).equals(_textPieces.get(x)))
        {
          return false;
        }
      }
      return true;
    }
    return false;
  }

}

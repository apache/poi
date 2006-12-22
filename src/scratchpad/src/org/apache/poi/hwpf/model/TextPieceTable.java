/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */


package org.apache.poi.hwpf.model;


import org.apache.poi.hwpf.model.io.HWPFOutputStream;
import org.apache.poi.poifs.common.POIFSConstants;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ryan Ackley
 */
public class TextPieceTable
{
  protected ArrayList _textPieces = new ArrayList();
  //int _multiple;
  int _cpMin;

  public TextPieceTable()
  {
  }

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
      GenericPropertyNode node = pieceTable.getProperty(x);
      pieces[x] = new PieceDescriptor(node.getBytes(), 0);

//      if (!pieces[x].isUnicode())
//      {
//        _multiple = 1;
//      }
    }

    int firstPieceFilePosition = pieces[0].getFilePosition();
    _cpMin = firstPieceFilePosition - fcMin;

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

      int startFilePosition = start - firstPieceFilePosition;
      _textPieces.add(new TextPiece(startFilePosition, startFilePosition+textSize, buf, pieces[x], node.getStart()));
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
      docStream.write(next.getRawBytes());

      int nodeStart = next.getStart();
      int multiple = 1;
      if (pd.isUnicode())
      {
        multiple = 2;
      }
      textPlex.addProperty(new GenericPropertyNode(nodeStart - bumpDown,
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

    //The text piece stores the length on file.
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

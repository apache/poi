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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.poi.hwpf.util.DoubleByteUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;

@Internal
public class OldTextPieceTable extends TextPieceTable {

    public OldTextPieceTable() {
        super();
    }

    public OldTextPieceTable(byte[] documentStream, byte[] tableStream,
                             int offset, int size, int fcMin, Charset charset) {
        //super(documentStream, tableStream, offset, size, fcMin, charset);
        // get our plex of PieceDescriptors
        PlexOfCps pieceTable = new PlexOfCps(tableStream, offset, size,
                PieceDescriptor.getSizeInBytes());

        int length = pieceTable.length();
        PieceDescriptor[] pieces = new PieceDescriptor[length];

        // iterate through piece descriptors raw bytes and create
        // PieceDescriptor objects
        for (int x = 0; x < length; x++) {
            GenericPropertyNode node = pieceTable.getProperty(x);
            pieces[x] = new PieceDescriptor(node.getBytes(), 0, charset);
        }

        // Figure out the cp of the earliest text piece
        // Note that text pieces don't have to be stored in order!
        _cpMin = pieces[0].getFilePosition() - fcMin;
        for (PieceDescriptor piece : pieces) {
            int start = piece.getFilePosition() - fcMin;
            if (start < _cpMin) {
                _cpMin = start;
            }
        }

        // using the PieceDescriptors, build our list of TextPieces.
        for (int x = 0; x < pieces.length; x++) {
            int start = pieces[x].getFilePosition();
            GenericPropertyNode node = pieceTable.getProperty(x);

            // Grab the start and end, which are in characters
            int nodeStartChars = node.getStart();
            int nodeEndChars = node.getEnd();

            // What's the relationship between bytes and characters?
            boolean unicode = pieces[x].isUnicode();
            int multiple = 1;
            if (unicode ||
                    (charset != null && DoubleByteUtil.DOUBLE_BYTE_CHARSETS.contains(charset))) {
                multiple = 2;
            }

            // Figure out the length, in bytes and chars
            int textSizeChars = (nodeEndChars - nodeStartChars);
            int textSizeBytes = textSizeChars * multiple;

            // Grab the data that makes up the piece
            byte[] buf = IOUtils.safelyClone(documentStream, start, textSizeBytes, getMaxRecordLength());

            // And now build the piece
            final TextPiece newTextPiece = newTextPiece(nodeStartChars, nodeEndChars, buf, pieces[x]);

            _textPieces.add(newTextPiece);
        }

        // In the interest of our sanity, now sort the text pieces
        // into order, if they're not already
        Collections.sort(_textPieces);
        _textPiecesFCOrder = new ArrayList<>(_textPieces);
        _textPiecesFCOrder.sort(byFilePosition());

    }

    @Override
    protected TextPiece newTextPiece(int nodeStartChars, int nodeEndChars, byte[] buf, PieceDescriptor pd) {
        return new OldTextPiece(nodeStartChars, nodeEndChars, buf, pd);
    }

    @Override
    protected int getEncodingMultiplier(TextPiece textPiece) {
        Charset charset = textPiece.getPieceDescriptor().getCharset();
        if (charset != null && DoubleByteUtil.DOUBLE_BYTE_CHARSETS.contains(charset)) {
            return 2;
        }
        return 1;
    }
}

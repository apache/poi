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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.Internal;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * The piece table for matching up character positions to bits of text. This
 * mostly works in bytes, but the TextPieces themselves work in characters. This
 * does the icky convertion.
 *
 * @author Ryan Ackley
 */
@Internal
public class TextPieceTable implements CharIndexTranslator {
    private static final POILogger logger = POILogFactory
            .getLogger(TextPieceTable.class);

    // int _multiple;
    int _cpMin;
    protected ArrayList<TextPiece> _textPieces = new ArrayList<TextPiece>();
    protected ArrayList<TextPiece> _textPiecesFCOrder = new ArrayList<TextPiece>();

    public TextPieceTable() {
    }

    public TextPieceTable(byte[] documentStream, byte[] tableStream,
                          int offset, int size, int fcMin) {
        // get our plex of PieceDescriptors
        PlexOfCps pieceTable = new PlexOfCps(tableStream, offset, size,
                PieceDescriptor.getSizeInBytes());

        int length = pieceTable.length();
        PieceDescriptor[] pieces = new PieceDescriptor[length];

        // iterate through piece descriptors raw bytes and create
        // PieceDescriptor objects
        for (int x = 0; x < length; x++) {
            GenericPropertyNode node = pieceTable.getProperty(x);
            pieces[x] = new PieceDescriptor(node.getBytes(), 0);
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
            if (unicode) {
                multiple = 2;
            }

            // Figure out the length, in bytes and chars
            int textSizeChars = (nodeEndChars - nodeStartChars);
            int textSizeBytes = textSizeChars * multiple;

            // Grab the data that makes up the piece
            byte[] buf = new byte[textSizeBytes];
            System.arraycopy(documentStream, start, buf, 0, textSizeBytes);

            // And now build the piece
            final TextPiece newTextPiece = newTextPiece(nodeStartChars, nodeEndChars, buf,
                    pieces[x]);

            _textPieces.add(newTextPiece);
        }

        // In the interest of our sanity, now sort the text pieces
        // into order, if they're not already
        Collections.sort(_textPieces);
        _textPiecesFCOrder = new ArrayList<TextPiece>(_textPieces);
        Collections.sort(_textPiecesFCOrder, new FCComparator());
    }

    protected TextPiece newTextPiece(int nodeStartChars, int nodeEndChars, byte[] buf, PieceDescriptor pd) {
        return new TextPiece(nodeStartChars, nodeEndChars, buf, pd);
    }

    public void add(TextPiece piece) {
        _textPieces.add(piece);
        _textPiecesFCOrder.add(piece);
        Collections.sort(_textPieces);
        Collections.sort(_textPiecesFCOrder, new FCComparator());
    }

    /**
     * Adjust all the text piece after inserting some text into one of them
     *
     * @param listIndex The TextPiece that had characters inserted into
     * @param length    The number of characters inserted
     */
    public int adjustForInsert(int listIndex, int length) {
        int size = _textPieces.size();

        TextPiece tp = _textPieces.get(listIndex);

        // Update with the new end
        tp.setEnd(tp.getEnd() + length);

        // Now change all subsequent ones
        for (int x = listIndex + 1; x < size; x++) {
            tp = _textPieces.get(x);
            tp.setStart(tp.getStart() + length);
            tp.setEnd(tp.getEnd() + length);
        }

        // All done
        return length;
    }

    public boolean equals(Object o) {
        if (!(o instanceof TextPieceTable)) return false;
        TextPieceTable tpt = (TextPieceTable) o;

        int size = tpt._textPieces.size();
        if (size == _textPieces.size()) {
            for (int x = 0; x < size; x++) {
                if (!tpt._textPieces.get(x).equals(_textPieces.get(x))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public int getByteIndex(int charPos) {
        int byteCount = 0;
        for (TextPiece tp : _textPieces) {
            if (charPos >= tp.getEnd()) {
                byteCount = tp.getPieceDescriptor().getFilePosition()
                        + (tp.getEnd() - tp.getStart())
                        * (tp.isUnicode() ? 2 : 1);

                if (charPos == tp.getEnd())
                    break;

                continue;
            }
            if (charPos < tp.getEnd()) {
                int left = charPos - tp.getStart();
                byteCount = tp.getPieceDescriptor().getFilePosition() + left
                        * (tp.isUnicode() ? 2 : 1);
                break;
            }
        }
        return byteCount;
    }

    @Deprecated
    public int getCharIndex(int bytePos) {
        return getCharIndex(bytePos, 0);
    }

    @Deprecated
    public int getCharIndex(int startBytePos, int startCP) {
        int charCount = 0;

        int bytePos = lookIndexForward(startBytePos);

        for (TextPiece tp : _textPieces) {
            int pieceStart = tp.getPieceDescriptor().getFilePosition();

            int bytesLength = tp.bytesLength();
            int pieceEnd = pieceStart + bytesLength;

            int toAdd;

            if (bytePos < pieceStart || bytePos > pieceEnd) {
                toAdd = bytesLength;
            } else if (bytePos > pieceStart && bytePos < pieceEnd) {
                toAdd = (bytePos - pieceStart);
            } else {
                toAdd = bytesLength - (pieceEnd - bytePos);
            }

            if (tp.isUnicode()) {
                charCount += toAdd / 2;
            } else {
                charCount += toAdd;
            }

            if (bytePos >= pieceStart && bytePos <= pieceEnd
                    && charCount >= startCP) {
                break;
            }
        }

        return charCount;
    }

    @Override
    public int[][] getCharIndexRanges(int startBytePosInclusive,
                                      int endBytePosExclusive) {
        List<int[]> result = new LinkedList<int[]>();
        for (TextPiece textPiece : _textPiecesFCOrder) {
            final int tpStart = textPiece.getPieceDescriptor()
                    .getFilePosition();
            final int tpEnd = textPiece.getPieceDescriptor().getFilePosition()
                    + textPiece.bytesLength();
            if (startBytePosInclusive > tpEnd)
                continue;
            if (endBytePosExclusive <= tpStart)
                break;

            final int rangeStartBytes = Math.max(tpStart,
                    startBytePosInclusive);
            final int rangeEndBytes = Math.min(tpEnd, endBytePosExclusive);
            final int rangeLengthBytes = rangeEndBytes - rangeStartBytes;

            if (rangeStartBytes > rangeEndBytes)
                continue;

            final int encodingMultiplier = getEncodingMultiplier(textPiece);

            final int rangeStartCp = textPiece.getStart()
                    + (rangeStartBytes - tpStart) / encodingMultiplier;
            final int rangeEndCp = rangeStartCp + rangeLengthBytes
                    / encodingMultiplier;

            result.add(new int[]{rangeStartCp, rangeEndCp});
        }

        return result.toArray(new int[result.size()][]);
    }

    protected int getEncodingMultiplier(TextPiece textPiece) {
        return textPiece.isUnicode() ? 2 : 1;
    }

    public int getCpMin() {
        return _cpMin;
    }

    public StringBuilder getText() {
        final long start = System.currentTimeMillis();

        // rebuild document paragraphs structure
        StringBuilder docText = new StringBuilder();
        for (TextPiece textPiece : _textPieces) {
            String toAppend = textPiece.getStringBuilder().toString();
            int toAppendLength = toAppend.length();

            if (toAppendLength != textPiece.getEnd() - textPiece.getStart()) {
                logger.log(
                        POILogger.WARN,
                        "Text piece has boundaries [",
                        Integer.valueOf(textPiece.getStart()),
                        "; ",
                        Integer.valueOf(textPiece.getEnd()),
                        ") but length ",
                        Integer.valueOf(textPiece.getEnd()
                                - textPiece.getStart()));
            }

            docText.replace(textPiece.getStart(), textPiece.getStart()
                    + toAppendLength, toAppend);
        }

        logger.log(POILogger.DEBUG, "Document text were rebuilded in ",
                Long.valueOf(System.currentTimeMillis() - start), " ms (",
                Integer.valueOf(docText.length()), " chars)");

        return docText;
    }

    public List<TextPiece> getTextPieces() {
        return _textPieces;
    }

    @Override
    public int hashCode() {
        return _textPieces.size();
    }

    public boolean isIndexInTable(int bytePos) {
        for (TextPiece tp : _textPiecesFCOrder) {
            int pieceStart = tp.getPieceDescriptor().getFilePosition();

            if (bytePos > pieceStart + tp.bytesLength()) {
                continue;
            }

            if (pieceStart > bytePos) {
                return false;
            }

            return true;
        }

        return false;
    }

    boolean isIndexInTable(int startBytePos, int endBytePos) {
        for (TextPiece tp : _textPiecesFCOrder) {
            int pieceStart = tp.getPieceDescriptor().getFilePosition();

            if (startBytePos >= pieceStart + tp.bytesLength()) {
                continue;
            }

            int left = Math.max(startBytePos, pieceStart);
            int right = Math.min(endBytePos, pieceStart + tp.bytesLength());

            if (left >= right)
                return false;

            return true;
        }

        return false;
    }

    public int lookIndexBackward(final int startBytePos) {
        int bytePos = startBytePos;
        int lastEnd = 0;

        for (TextPiece tp : _textPiecesFCOrder) {
            int pieceStart = tp.getPieceDescriptor().getFilePosition();

            if (bytePos > pieceStart + tp.bytesLength()) {
                lastEnd = pieceStart + tp.bytesLength();
                continue;
            }

            if (pieceStart > bytePos) {
                bytePos = lastEnd;
            }

            break;
        }

        return bytePos;
    }

    public int lookIndexForward(final int startBytePos) {
        if (_textPiecesFCOrder.isEmpty())
            throw new IllegalStateException("Text pieces table is empty");

        if (_textPiecesFCOrder.get(0).getPieceDescriptor().getFilePosition() > startBytePos)
            return _textPiecesFCOrder.get(0).getPieceDescriptor().getFilePosition();

        if (_textPiecesFCOrder.get(_textPiecesFCOrder.size() - 1)
                .getPieceDescriptor().getFilePosition() <= startBytePos)
            return startBytePos;

        int low = 0;
        int high = _textPiecesFCOrder.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            final TextPiece textPiece = _textPiecesFCOrder.get(mid);
            int midVal = textPiece.getPieceDescriptor().getFilePosition();

            if (midVal < startBytePos)
                low = mid + 1;
            else if (midVal > startBytePos)
                high = mid - 1;
            else
                // found piece with exact start
                return textPiece.getPieceDescriptor().getFilePosition();
        }
        assert low == high;
        assert _textPiecesFCOrder.get(low).getPieceDescriptor()
                .getFilePosition() < startBytePos;
        // last line can't be current, can it?
        assert _textPiecesFCOrder.get(low + 1).getPieceDescriptor()
                .getFilePosition() > startBytePos;

        // shifting to next piece start
        return _textPiecesFCOrder.get(low + 1).getPieceDescriptor().getFilePosition();
    }

    public byte[] writeTo(ByteArrayOutputStream docStream) throws IOException {
        PlexOfCps textPlex = new PlexOfCps(PieceDescriptor.getSizeInBytes());
        // int fcMin = docStream.getOffset();

        for (TextPiece next : _textPieces) {
            PieceDescriptor pd = next.getPieceDescriptor();

            int offset = docStream.size();
            int mod = (offset % POIFSConstants.SMALLER_BIG_BLOCK_SIZE);
            if (mod != 0) {
                mod = POIFSConstants.SMALLER_BIG_BLOCK_SIZE - mod;
                byte[] buf = new byte[mod];
                docStream.write(buf);
            }

            // set the text piece position to the current docStream offset.
            pd.setFilePosition(docStream.size());

            // write the text to the docstream and save the piece descriptor to
            // the
            // plex which will be written later to the tableStream.
            docStream.write(next.getRawBytes());

            // The TextPiece is already in characters, which
            // makes our life much easier
            int nodeStart = next.getStart();
            int nodeEnd = next.getEnd();
            textPlex.addProperty(new GenericPropertyNode(nodeStart, nodeEnd,
                    pd.toByteArray()));
        }

        return textPlex.toByteArray();
    }

    protected static class FCComparator implements Comparator<TextPiece>, Serializable {
        public int compare(TextPiece textPiece, TextPiece textPiece1) {
            if (textPiece.getPieceDescriptor().fc > textPiece1
                    .getPieceDescriptor().fc) {
                return 1;
            } else if (textPiece.getPieceDescriptor().fc < textPiece1
                    .getPieceDescriptor().fc) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}

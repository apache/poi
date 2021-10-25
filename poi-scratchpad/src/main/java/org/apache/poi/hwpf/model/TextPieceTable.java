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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;

import static java.lang.System.currentTimeMillis;
import static org.apache.logging.log4j.util.Unbox.box;

/**
 * The piece table for matching up character positions to bits of text. This
 * mostly works in bytes, but the TextPieces themselves work in characters. This
 * does the icky conversion.
 */
@Internal
public class TextPieceTable implements CharIndexTranslator {
    private static final Logger LOG = LogManager.getLogger(TextPieceTable.class);
    //arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_RECORD_LENGTH = 100_000_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    /**
     * @param length the max record length allowed for TextPieceTable
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for TextPieceTable
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }


    // int _multiple;
    int _cpMin;
    protected ArrayList<TextPiece> _textPieces = new ArrayList<>();
    protected ArrayList<TextPiece> _textPiecesFCOrder = new ArrayList<>();

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
            byte[] buf = IOUtils.safelyClone(documentStream, start, textSizeBytes, MAX_RECORD_LENGTH);

            // And now build the piece
            final TextPiece newTextPiece = newTextPiece(nodeStartChars, nodeEndChars, buf,
                    pieces[x]);

            _textPieces.add(newTextPiece);
        }

        // In the interest of our sanity, now sort the text pieces
        // into order, if they're not already
        Collections.sort(_textPieces);
        _textPiecesFCOrder = new ArrayList<>(_textPieces);
        _textPiecesFCOrder.sort(byFilePosition());
    }

    protected TextPiece newTextPiece(int nodeStartChars, int nodeEndChars, byte[] buf, PieceDescriptor pd) {
        return new TextPiece(nodeStartChars, nodeEndChars, buf, pd);
    }

    public void add(TextPiece piece) {
        _textPieces.add(piece);
        _textPiecesFCOrder.add(piece);
        Collections.sort(_textPieces);
        _textPiecesFCOrder.sort(byFilePosition());
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

    @Override
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
        List<int[]> result = new LinkedList<>();
        for (TextPiece textPiece : _textPiecesFCOrder) {
            final int tpStart = textPiece.getPieceDescriptor()
                    .getFilePosition();
            if (endBytePosExclusive <= tpStart)
                break;

            final int tpEnd = textPiece.getPieceDescriptor().getFilePosition()
                    + textPiece.bytesLength();
            if (startBytePosInclusive > tpEnd)
                continue;

            final int rangeStartBytes = Math.max(tpStart, startBytePosInclusive);
            final int rangeEndBytes = Math.min(tpEnd, endBytePosExclusive);

            if (rangeStartBytes > rangeEndBytes)
                continue;

            final int encodingMultiplier = getEncodingMultiplier(textPiece);

            final int rangeStartCp = textPiece.getStart()
                    + (rangeStartBytes - tpStart) / encodingMultiplier;
            final int rangeLengthBytes = rangeEndBytes - rangeStartBytes;
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
        final long start = currentTimeMillis();

        // rebuild document paragraphs structure
        StringBuilder docText = new StringBuilder();
        for (TextPiece textPiece : _textPieces) {
            String toAppend = textPiece.getStringBuilder().toString();
            int toAppendLength = toAppend.length();

            if (toAppendLength != textPiece.getEnd() - textPiece.getStart()) {
                LOG.atWarn().log("Text piece has boundaries [{}; {}) but length {}", box(textPiece.getStart()),box(textPiece.getEnd()),box(textPiece.getEnd() - textPiece.getStart()));
            }

            docText.replace(textPiece.getStart(), textPiece.getStart()
                    + toAppendLength, toAppend);
        }

        LOG.atDebug().log("Document text were rebuilt in {} ms ({} chars)", box(currentTimeMillis() - start),box(docText.length()));

        return docText;
    }

    public List<TextPiece> getTextPieces() {
        return _textPieces;
    }

    @Override
    public int hashCode() {
        return _textPieces.hashCode();
    }

    @Override
    public boolean isIndexInTable(int bytePos) {
        for (TextPiece tp : _textPiecesFCOrder) {
            int pieceStart = tp.getPieceDescriptor().getFilePosition();

            if (bytePos > pieceStart + tp.bytesLength()) {
                continue;
            }

            return pieceStart <= bytePos;
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

            return left < right;
        }

        return false;
    }

    @Override
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

    @Override
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
                byte[] buf = IOUtils.safelyAllocate(mod, MAX_RECORD_LENGTH);
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

    static Comparator<TextPiece> byFilePosition() {
        return Comparator.comparing(t -> t.getPieceDescriptor().getFilePosition());
    }
}

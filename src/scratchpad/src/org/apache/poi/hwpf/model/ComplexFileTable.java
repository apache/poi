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
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.hwpf.model.io.HWPFFileSystem;
import org.apache.poi.hwpf.sprm.SprmBuffer;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

@Internal
public class ComplexFileTable {

    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;

    private static final byte GRPPRL_TYPE = 1;
    private static final byte TEXT_PIECE_TABLE_TYPE = 2;

    protected TextPieceTable _tpt;

    private SprmBuffer[] _grpprls;

    public ComplexFileTable() {
        _tpt = new TextPieceTable();
    }

    protected ComplexFileTable(byte[] documentStream, byte[] tableStream, int offset, int fcMin,
                               Charset charset) throws IOException {
        //skips through the prms before we reach the piece table. These contain data
        //for actual fast saved files
        List<SprmBuffer> sprmBuffers = new LinkedList<>();
        while (tableStream[offset] == GRPPRL_TYPE) {
            offset++;
            int size = LittleEndian.getShort(tableStream, offset);
            offset += LittleEndian.SHORT_SIZE;
            byte[] bs = LittleEndian.getByteArray(tableStream, offset, size, MAX_RECORD_LENGTH);
            offset += size;

            SprmBuffer sprmBuffer = new SprmBuffer(bs, false, 0);
            sprmBuffers.add(sprmBuffer);
        }
        this._grpprls = sprmBuffers.toArray(new SprmBuffer[sprmBuffers.size()]);

        if (tableStream[offset] != TEXT_PIECE_TABLE_TYPE) {
            throw new IOException("The text piece table is corrupted");
        }
        int pieceTableSize = LittleEndian.getInt(tableStream, ++offset);
        offset += LittleEndian.INT_SIZE;
        _tpt = newTextPieceTable(documentStream, tableStream, offset, pieceTableSize, fcMin, charset);

    }

    public ComplexFileTable(byte[] documentStream, byte[] tableStream, int offset, int fcMin) throws IOException {
        this(documentStream, tableStream, offset, fcMin, StringUtil.WIN_1252);
    }

    public TextPieceTable getTextPieceTable() {
        return _tpt;
    }

    public SprmBuffer[] getGrpprls() {
        return _grpprls;
    }

    @Deprecated
    public void writeTo(HWPFFileSystem sys) throws IOException {
        ByteArrayOutputStream docStream = sys.getStream("WordDocument");
        ByteArrayOutputStream tableStream = sys.getStream("1Table");

        writeTo(docStream, tableStream);
    }

    public void writeTo(ByteArrayOutputStream wordDocumentStream,
                        ByteArrayOutputStream tableStream) throws IOException {
        tableStream.write(TEXT_PIECE_TABLE_TYPE);

        byte[] table = _tpt.writeTo(wordDocumentStream);

        byte[] numHolder = new byte[LittleEndian.INT_SIZE];
        LittleEndian.putInt(numHolder, 0, table.length);
        tableStream.write(numHolder);
        tableStream.write(table);
    }

    protected TextPieceTable newTextPieceTable(byte[] documentStream,
                                               byte[] tableStream, int offset, int pieceTableSize, int fcMin,
                                               Charset charset) {
        return new TextPieceTable(documentStream, tableStream, offset, pieceTableSize, fcMin);
    }


}

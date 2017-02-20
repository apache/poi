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
package org.apache.poi.hwpf;

import org.apache.poi.hwpf.model.*;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.LittleEndian;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Provides very simple support for old (Word 6 / Word 95)
 * files.
 */
public class HWPFOldDocument extends HWPFDocumentCore {
    private TextPieceTable tpt;

    private StringBuilder _text;

    public HWPFOldDocument(POIFSFileSystem fs) throws IOException {
        this(fs.getRoot());
    }

    @Deprecated
    public HWPFOldDocument(DirectoryNode directory, POIFSFileSystem fs)
            throws IOException {
        this(directory);
    }

    public HWPFOldDocument(DirectoryNode directory)
            throws IOException {
        super(directory);

        // Where are things?
        int sedTableOffset = LittleEndian.getInt(_mainStream, 0x88);
        int sedTableSize = LittleEndian.getInt(_mainStream, 0x8c);
        int chpTableOffset = LittleEndian.getInt(_mainStream, 0xb8);
        int chpTableSize = LittleEndian.getInt(_mainStream, 0xbc);
        int papTableOffset = LittleEndian.getInt(_mainStream, 0xc0);
        int papTableSize = LittleEndian.getInt(_mainStream, 0xc4);
        //int shfTableOffset = LittleEndian.getInt(_mainStream, 0x60);
        //int shfTableSize   = LittleEndian.getInt(_mainStream, 0x64);
        int complexTableOffset = LittleEndian.getInt(_mainStream, 0x160);

        // We need to get hold of the text that makes up the
        //  document, which might be regular or fast-saved
        ComplexFileTable cft = null;
        StringBuilder text = new StringBuilder();
        if (_fib.getFibBase().isFComplex()) {
            cft = new ComplexFileTable(
                    _mainStream, _mainStream,
                    complexTableOffset, _fib.getFibBase().getFcMin()
            );
            tpt = cft.getTextPieceTable();

            for (TextPiece tp : tpt.getTextPieces()) {
                text.append(tp.getStringBuilder());
            }
        } else {
            // TODO Discover if these older documents can ever hold Unicode Strings?
            //  (We think not, because they seem to lack a Piece table)
            // TODO Build the Piece Descriptor properly
            //  (We have to fake it, as they don't seem to have a proper Piece table)
            PieceDescriptor pd = new PieceDescriptor(new byte[]{0, 0, 0, 0, 0, 127, 0, 0}, 0);
            pd.setFilePosition(_fib.getFibBase().getFcMin());

            // Generate a single Text Piece Table, with a single Text Piece
            //  which covers all the (8 bit only) text in the file
            tpt = new TextPieceTable();
            byte[] textData = new byte[_fib.getFibBase().getFcMac() - _fib.getFibBase().getFcMin()];
            System.arraycopy(_mainStream, _fib.getFibBase().getFcMin(), textData, 0, textData.length);
            TextPiece tp = new TextPiece(
                    0, textData.length, textData, pd
            );
            tpt.add(tp);

            text.append(tp.getStringBuilder());
        }

        _text = tpt.getText();

        // Now we can fetch the character and paragraph properties
        _cbt = new OldCHPBinTable(
                _mainStream, chpTableOffset, chpTableSize,
                _fib.getFibBase().getFcMin(), tpt
        );
        _pbt = new OldPAPBinTable(
                _mainStream, papTableOffset, papTableSize,
                _fib.getFibBase().getFcMin(), tpt
        );
        _st = new OldSectionTable(
                _mainStream, sedTableOffset, sedTableSize,
                _fib.getFibBase().getFcMin(), tpt
        );

        /*
         * in this mode we preserving PAPX/CHPX structure from file, so text may
         * miss from output, and text order may be corrupted
         */
        boolean preserveBinTables = false;
        try {
            preserveBinTables = Boolean.parseBoolean(System
                    .getProperty(HWPFDocument.PROPERTY_PRESERVE_BIN_TABLES));
        } catch (Exception exc) {
            // ignore;
        }

        if (!preserveBinTables) {
            _cbt.rebuild(cft);
            _pbt.rebuild(_text, cft);
        }
    }

    public Range getOverallRange() {
        // Life is easy when we have no footers, headers or unicode!
        return new Range(0, _fib.getFibBase().getFcMac() - _fib.getFibBase().getFcMin(), this);
    }

    public Range getRange() {
        return getOverallRange();
    }

    public TextPieceTable getTextTable() {
        return tpt;
    }

    @Override
    public StringBuilder getText() {
        return _text;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        throw new IllegalStateException("Writing is not available for the older file formats");
    }
}

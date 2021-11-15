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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.common.usermodel.fonts.FontCharset;
import org.apache.poi.hwpf.model.ComplexFileTable;
import org.apache.poi.hwpf.model.FontTable;
import org.apache.poi.hwpf.model.OldCHPBinTable;
import org.apache.poi.hwpf.model.OldComplexFileTable;
import org.apache.poi.hwpf.model.OldFfn;
import org.apache.poi.hwpf.model.OldFontTable;
import org.apache.poi.hwpf.model.OldPAPBinTable;
import org.apache.poi.hwpf.model.OldSectionTable;
import org.apache.poi.hwpf.model.OldTextPieceTable;
import org.apache.poi.hwpf.model.PieceDescriptor;
import org.apache.poi.hwpf.model.TextPiece;
import org.apache.poi.hwpf.model.TextPieceTable;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.hwpf.util.DoubleByteUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.NotImplemented;
import org.apache.poi.util.StringUtil;

/**
 * Provides very simple support for old (Word 6 / Word 95) files.
 */
public class HWPFOldDocument extends HWPFDocumentCore {

    private static final Logger LOG = LogManager.getLogger(HWPFOldDocument.class);

    //arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_RECORD_LENGTH = 10_000_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    /**
     * @param length the max record length allowed for HWPFOldDocument
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for HWPFOldDocument
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    private static final Charset DEFAULT_CHARSET = StringUtil.WIN_1252;

    private OldTextPieceTable tpt;

    private StringBuilder _text;

    private final OldFontTable fontTable;
    private final Charset guessedCharset;

    public HWPFOldDocument(POIFSFileSystem fs) throws IOException {
        this(fs.getRoot());
    }

    public HWPFOldDocument(DirectoryNode directory)
            throws IOException {
        super(directory);

        // Where are things?
        int sedTableOffset = LittleEndian.getInt(_mainStream, 0x88);
        int sedTableSize   = LittleEndian.getInt(_mainStream, 0x8c);
        int chpTableOffset = LittleEndian.getInt(_mainStream, 0xb8);
        int chpTableSize   = LittleEndian.getInt(_mainStream, 0xbc);
        int papTableOffset = LittleEndian.getInt(_mainStream, 0xc0);
        int papTableSize   = LittleEndian.getInt(_mainStream, 0xc4);
        int fontTableOffset = LittleEndian.getInt(_mainStream, 0xd0);
        int fontTableSize = LittleEndian.getInt(_mainStream, 0xd4);

        fontTable = new OldFontTable(_mainStream, fontTableOffset, fontTableSize);
        //TODO: figure out how to map runs/text pieces to fonts
        //for now, if there's a non standard codepage in one of the fonts
        //assume that the doc is in that codepage.
        guessedCharset = guessCodePage(fontTable);

        int complexTableOffset = LittleEndian.getInt(_mainStream, 0x160);

        // We need to get hold of the text that makes up the
        //  document, which might be regular or fast-saved
        ComplexFileTable cft = null;
        if(_fib.getFibBase().isFComplex()) {
            cft = new OldComplexFileTable(
                    _mainStream, _mainStream,
                    complexTableOffset, _fib.getFibBase().getFcMin(), guessedCharset
            );
            tpt = (OldTextPieceTable)cft.getTextPieceTable();

        } else {
            // TODO Discover if these older documents can ever hold Unicode Strings?
            //  (We think not, because they seem to lack a Piece table)
            //
            //  What we have here is a wretched hack.  We need to figure out
            //  how to get the correct charset for the doc.
            TextPiece tp = null;
            try {
                tp = buildTextPiece(guessedCharset);
            } catch (IllegalStateException e) {
                //if there was a problem with the guessed charset and the length of the
                //textpiece, back off to win1252. This is effectively what we used to do.
                tp = buildTextPiece(StringUtil.WIN_1252);
                LOG.atWarn().log("Error with {}. Backing off to Windows-1252", guessedCharset);
            }
            tpt.add(tp);

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
        try
        {
            preserveBinTables = Boolean.parseBoolean( System
                    .getProperty( HWPFDocument.PROPERTY_PRESERVE_BIN_TABLES ) );
        }
        catch ( Exception exc )
        {
            // ignore;
        }

        if ( !preserveBinTables )
        {
            _cbt.rebuild( cft );
            _pbt.rebuild( _text, cft );
        }
    }

    /**
     *
     * @param guessedCharset charset that we think this is
     * @return a new text piece
     * @throws IllegalStateException if the length isn't correct
     */
    private TextPiece buildTextPiece(Charset guessedCharset) throws IllegalStateException {
        PieceDescriptor pd = new PieceDescriptor(new byte[] {0,0, 0,0,0,127, 0,0}, 0, guessedCharset);
        pd.setFilePosition(_fib.getFibBase().getFcMin());

        // Generate a single Text Piece Table, with a single Text Piece
        //  which covers all the (8 bit only) text in the file
        tpt = new OldTextPieceTable();

        byte[] textData = IOUtils.safelyClone(_mainStream, _fib.getFibBase().getFcMin(),
              _fib.getFibBase().getFcMac()-_fib.getFibBase().getFcMin(), MAX_RECORD_LENGTH);

        int numChars = textData.length;
        if (DoubleByteUtil.DOUBLE_BYTE_CHARSETS.contains(guessedCharset)) {
            numChars /= 2;
        }

        return new TextPiece(0, numChars, textData, pd);
    }


    /**
     * Try to get the code page from various areas of the document.
     * Start with the DocumentSummaryInformation, back off to the section info,
     * finally try the charset information from the font table.
     *
     * Consider throwing an exception if > 1 unique codepage that is not default, symbol or ansi
     * appears here.
     *
     * @param fontTable
     * @return The detected Charset from the old font table
     */
    private Charset guessCodePage(OldFontTable fontTable) {
        // pick the first non-default, non-symbol charset
        for (OldFfn oldFfn : fontTable.getFontNames()) {
            FontCharset wmfCharset = FontCharset.valueOf(oldFfn.getChs()& 0xff);
            if (wmfCharset != null &&
                    wmfCharset != FontCharset.ANSI &&
                    wmfCharset != FontCharset.DEFAULT &&
                    wmfCharset != FontCharset.SYMBOL ) {
                return wmfCharset.getCharset();
            }
        }
        LOG.atWarn().log("Couldn't find a defined charset; backing off to cp1252");
        //if all else fails
        return DEFAULT_CHARSET;
    }

    public Range getOverallRange()
    {
        // Life is easy when we have no footers, headers or unicode!
        return new Range( 0, _fib.getFibBase().getFcMac() - _fib.getFibBase().getFcMin(), this );
    }

    /**
     * Use {@link #getOldFontTable()} instead!!!
     * This always throws an {@link UnsupportedOperationException}.
     *
     * @return nothing
     * @throws UnsupportedOperationException Always.
     */
    @Override
    @NotImplemented
    public FontTable getFontTable() {
        throw new UnsupportedOperationException("Use getOldFontTable instead.");
    }

    public OldFontTable getOldFontTable() {
        return fontTable;
    }
    public Range getRange()
    {
        return getOverallRange();
    }

    public TextPieceTable getTextTable()
    {
      return tpt;
    }

    @Override
    public StringBuilder getText()
    {
        return _text;
    }

    @Override
    public void write() throws IOException {
        throw new IllegalStateException("Writing is not available for the older file formats");
    }
    @Override
    public void write(File out) throws IOException {
        throw new IllegalStateException("Writing is not available for the older file formats");
    }
    @Override
    public void write(OutputStream out) throws IOException {
        throw new IllegalStateException("Writing is not available for the older file formats");
    }

    /**
     * As a rough heuristic (total hack), read through the HPSF,
     * then read through the font table, and take the first
     * non-default, non-ansi, non-symbol
     * font's charset and return that.
     *
     * Once we figure out how to link a font to a text piece, we should
     * use the font information per text piece.
     *
     * @return charset
     */
    public Charset getGuessedCharset() {
        return guessedCharset;
    }

}

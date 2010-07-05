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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.hwpf.model.ComplexFileTable;
import org.apache.poi.hwpf.model.OldCHPBinTable;
import org.apache.poi.hwpf.model.OldPAPBinTable;
import org.apache.poi.hwpf.model.OldSectionTable;
import org.apache.poi.hwpf.model.PieceDescriptor;
import org.apache.poi.hwpf.model.TextPiece;
import org.apache.poi.hwpf.model.TextPieceTable;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.LittleEndian;

/**
 * Provides very simple support for old (Word 6 / Word 95)
 *  files.
 */
public class HWPFOldDocument extends HWPFDocumentCore {
    private TextPieceTable tpt;
    
    public HWPFOldDocument(POIFSFileSystem fs) throws IOException {
        this(fs.getRoot(), fs);
    }

    public HWPFOldDocument(DirectoryNode directory, POIFSFileSystem fs)
            throws IOException {
        super(directory, fs);
        
        // Where are things?
        int sedTableOffset = LittleEndian.getInt(_mainStream, 0x88);
        int sedTableSize   = LittleEndian.getInt(_mainStream, 0x8c);
        int chpTableOffset = LittleEndian.getInt(_mainStream, 0xb8);
        int chpTableSize   = LittleEndian.getInt(_mainStream, 0xbc);
        int papTableOffset = LittleEndian.getInt(_mainStream, 0xc0);
        int papTableSize   = LittleEndian.getInt(_mainStream, 0xc4);
        //int shfTableOffset = LittleEndian.getInt(_mainStream, 0x60);
        //int shfTableSize   = LittleEndian.getInt(_mainStream, 0x64);
        int complexTableOffset = LittleEndian.getInt(_mainStream, 0x160);
        
        // We need to get hold of the text that makes up the
        //  document, which might be regular or fast-saved
        StringBuffer text = new StringBuffer();
        if(_fib.isFComplex()) {
            ComplexFileTable cft = new ComplexFileTable(
                    _mainStream, _mainStream,
                    complexTableOffset, _fib.getFcMin()
            );
            tpt = cft.getTextPieceTable();
            
            for(TextPiece tp : tpt.getTextPieces()) {
                text.append( tp.getStringBuffer() );
            }
        } else {
            // TODO Discover if these older documents can ever hold Unicode Strings?
            //  (We think not, because they seem to lack a Piece table)
            // TODO Build the Piece Descriptor properly
            //  (We have to fake it, as they don't seem to have a proper Piece table)
            PieceDescriptor pd = new PieceDescriptor(new byte[] {0,0, 0,0,0,127, 0,0}, 0);
            pd.setFilePosition(_fib.getFcMin());

            // Generate a single Text Piece Table, with a single Text Piece
            //  which covers all the (8 bit only) text in the file
            tpt = new TextPieceTable();
            byte[] textData = new byte[_fib.getFcMac()-_fib.getFcMin()];
            System.arraycopy(_mainStream, _fib.getFcMin(), textData, 0, textData.length);
            TextPiece tp = new TextPiece(
                    0, textData.length, textData, pd, 0
            );
            tpt.add(tp);
            
            text.append(tp.getStringBuffer());
        }
        
        // Now we can fetch the character and paragraph properties
        _cbt = new OldCHPBinTable(
                _mainStream, chpTableOffset, chpTableSize,
                _fib.getFcMin(), tpt
        );
        _pbt = new OldPAPBinTable(
                _mainStream, papTableOffset, papTableSize,
                _fib.getFcMin(), tpt
        );
        _st = new OldSectionTable(
                _mainStream, sedTableOffset, sedTableSize,
                _fib.getFcMin(), tpt
        );
    }
    
    public Range getRange() {
        // Life is easy when we have no footers, headers or unicode!
        return new Range(
                0, _fib.getFcMac() - _fib.getFcMin(), this
        );
    }

    public TextPieceTable getTextTable()
    {
      return tpt;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        throw new IllegalStateException("Writing is not available for the older file formats");
    }
}

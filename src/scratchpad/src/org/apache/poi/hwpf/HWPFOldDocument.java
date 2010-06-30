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
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hwpf.model.CHPX;
import org.apache.poi.hwpf.model.ComplexFileTable;
import org.apache.poi.hwpf.model.OldCHPBinTable;
import org.apache.poi.hwpf.model.PieceDescriptor;
import org.apache.poi.hwpf.model.TextPiece;
import org.apache.poi.hwpf.model.TextPieceTable;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.LittleEndian;

/**
 * Provides very simple support for old (Word 6 / Word 95)
 *  files.
 * TODO Provide a way to get at the properties associated
 *  with each block of text
 */
public class HWPFOldDocument extends HWPFDocumentCore {
    private List<TextAndCHPX> contents = new ArrayList<TextAndCHPX>(); 
    
    public HWPFOldDocument(POIFSFileSystem fs) throws IOException {
        this(fs.getRoot(), fs);
    }

    public HWPFOldDocument(DirectoryNode directory, POIFSFileSystem fs)
            throws IOException {
        super(directory, fs);
        
        // Where are things?
        int chpTableOffset = LittleEndian.getInt(_mainStream, 0xb8);
        int chpTableSize = LittleEndian.getInt(_mainStream, 0xbc);
        int complexTableOffset = LittleEndian.getInt(_mainStream, 0x160);
        
        // We need to get hold of the text that makes up the
        //  document, which might be regular or fast-saved
        StringBuffer text = new StringBuffer();
        TextPieceTable tpt;
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
            // TODO Build the Piece Descriptor properly
            // TODO Can these old documents ever contain Unicode strings?
            PieceDescriptor pd = new PieceDescriptor(new byte[] {0,0, 0,0,0,127, 0,0}, 0);
            pd.setFilePosition(_fib.getFcMin());

            tpt = new TextPieceTable();
            byte[] textData = new byte[_fib.getFcMac()-_fib.getFcMin()];
            System.arraycopy(_mainStream, _fib.getFcMin(), textData, 0, textData.length);
            TextPiece tp = new TextPiece(
                    0, textData.length, textData, pd, 0
            );
            tpt.getTextPieces().add(tp);
            
            text.append(tp.getStringBuffer());
        }
        
        // Now we can fetch the character and paragraph properties
        OldCHPBinTable chpTable = new OldCHPBinTable(
                _mainStream, chpTableOffset, chpTableSize,
                _fib.getFcMin(), tpt
        );
        
        // Finally build up runs
        for(CHPX chpx : chpTable.getTextRuns()) {
            String str = text.substring(chpx.getStart(), chpx.getEnd());
            contents.add(new TextAndCHPX(str,chpx));
        }
    }

    @Override
    public void write(OutputStream out) throws IOException {
        throw new IllegalStateException("Writing is not available for the older file formats");
    }
    
    /**
     * Retrieves all our text, in order, along with the
     *  CHPX information on each bit.
     * Every entry has the same formatting, but as yet 
     *  we've no way to tell what the formatting is...
     * Warnings - this will change as soon as we support
     *  text formatting!
     */
    public List<TextAndCHPX> getContents() {
        return contents;
    }
    
    /**
     * Warnings - this will change as soon as we support
     *  text formatting!
     */
    public static class TextAndCHPX {
        private String text;
        private CHPX chpx;
        private TextAndCHPX(String text, CHPX chpx) {
            this.text = text;
            this.chpx = chpx;
        }
        public String getText() {
            return text;
        }
        public CHPX getChpx() {
            return chpx;
        }
    }
}

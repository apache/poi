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

package org.apache.poi.hdf.model;

import org.apache.poi.hdf.event.HDFLowLevelParsingListener;
import org.apache.poi.hdf.model.util.BTreeSet;

import org.apache.poi.hdf.model.hdftypes.ChpxNode;
import org.apache.poi.hdf.model.hdftypes.PapxNode;
import org.apache.poi.hdf.model.hdftypes.SepxNode;
import org.apache.poi.hdf.model.hdftypes.TextPiece;
import org.apache.poi.hdf.model.hdftypes.DocumentProperties;
import org.apache.poi.hdf.model.hdftypes.FontTable;
import org.apache.poi.hdf.model.hdftypes.ListTables;
import org.apache.poi.hdf.model.hdftypes.StyleSheet;


public final class HDFObjectModel implements HDFLowLevelParsingListener
{

    /** "WordDocument" from the POIFS */
    private byte[] _mainDocument;

    /** The DOP*/
    private DocumentProperties _dop;
    /**the StyleSheet*/
    private StyleSheet _styleSheet;
    /**list info */
    private ListTables _listTables;
    /** Font info */
    private FontTable _fonts;

    /** text offset in main stream */
    int _fcMin;

    /** text pieces */
    BTreeSet _text = new BTreeSet();
    /** document sections */
    BTreeSet _sections = new BTreeSet();
    /** document paragraphs */
    BTreeSet _paragraphs = new BTreeSet();
    /** document character runs */
    BTreeSet _characterRuns = new BTreeSet();

    public HDFObjectModel()
    {
    }
    public void mainDocument(byte[] mainDocument)
    {
      _mainDocument = mainDocument;
    }
    public void tableStream(byte[] tableStream)
    {
    }
    public void miscellaneous(int fcMin, int ccpText, int ccpFtn, int fcPlcfhdd, int lcbPlcfhdd)
    {
      _fcMin = fcMin;
    }
    public void document(DocumentProperties dop)
    {
      _dop = dop;
    }
    public void bodySection(SepxNode sepx)
    {
      _sections.add(sepx);
    }
    public void hdrSection(SepxNode sepx)
    {
      _sections.add(sepx);
    }
    public void endSections()
    {
    }
    public void paragraph(PapxNode papx)
    {
      _paragraphs.add(papx);
    }
    public void characterRun(ChpxNode chpx)
    {
      _characterRuns.add(chpx);
    }
    public void text(TextPiece t)
    {
      _text.add(t);
    }
    public void fonts(FontTable fontTbl)
    {
      _fonts = fontTbl;
    }
    public void lists(ListTables listTbl)
    {
      _listTables = listTbl;
    }
    public void styleSheet(StyleSheet stsh)
    {
      _styleSheet = stsh;
    }
}

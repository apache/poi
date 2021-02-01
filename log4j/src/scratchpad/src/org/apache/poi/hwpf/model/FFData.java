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

import static org.apache.poi.hwpf.model.types.FFDataBaseAbstractType.ITYPE_CHCK;
import static org.apache.poi.hwpf.model.types.FFDataBaseAbstractType.ITYPE_DROP;
import static org.apache.poi.hwpf.model.types.FFDataBaseAbstractType.ITYPE_TEXT;

import org.apache.poi.hwpf.model.types.FFDataBaseAbstractType;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * The FFData structure specifies form field data for a text box, check box, or
 * drop-down list box.
 * <p>
 * Class and fields descriptions are quoted from [MS-DOC] -- v20121003 Word
 * (.doc) Binary File Format; Copyright (c) 2012 Microsoft Corporation; Release:
 * October 8, 2012
 * <p>
 * This class is internal. It content or properties may change without notice
 * due to changes in our knowledge of internal Microsoft Word binary structures.
 *
 * @author Sergey Vladimirov; according to [MS-DOC] -- v20121003 Word (.doc)
 *         Binary File Format; Copyright (c) 2012 Microsoft Corporation;
 *         Release: October 8, 2012
 */
@Internal
public class FFData
{
    private FFDataBase _base;

    /**
     * An optional STTB that specifies the entries in the dropdown list box.
     * This MUST exist if and only if bits.iType is iTypeDrop (2). The entries
     * are Unicode strings and do not have extra data. This MUST NOT exceed 25
     * elements.
     */
    private Sttb _hsttbDropList;

    /**
     * An optional unsigned integer that specifies the default state of the
     * checkbox or dropdown list box. This value MUST exist if and only if
     * bits.iType is iTypeChck (1) or iTypeDrop (2). If bits.iType is iTypeChck
     * (1), wDef MUST be 0 or 1 and specify the default state of the checkbox as
     * unchecked or checked, respectively. If bits.iType is iTypeDrop (2), wDef
     * MUST be less than the number of items in the dropdown list box and
     * specify the default item selected (zero-based index).
     */
    private Integer _wDef;

    private Xstz _xstzEntryMcr;

    private Xstz _xstzExitMcr;

    private Xstz _xstzHelpText;

    /**
     * An Xstz that specifies the name of this form field. xstzName.cch MUST NOT
     * exceed 20.
     */
    private Xstz _xstzName;

    private Xstz _xstzStatText;

    /**
     * An optional Xstz that specifies the default text of this textbox. This
     * structure MUST exist if and only if bits.iType is iTypeTxt (0).
     * xstzTextDef.cch MUST NOT exceed 255. If bits.iTypeTxt is either
     * iTypeTxtCurDate (3) or iTypeTxtCurTime (4), xstzTextDef MUST be an empty
     * string. If bits.iTypeTxt is iTypeTxtCalc (5), xstzTextDef specifies an
     * expression to calculate.
     */
    private Xstz _xstzTextDef;

    private Xstz _xstzTextFormat;

    public FFData( byte[] std, int offset )
    {
        fillFields( std, offset );
    }

    public void fillFields( final byte[] std, final int startOffset ) // NOSONAR
    {
        int offset = startOffset;

        this._base = new FFDataBase( std, offset );
        offset += FFDataBaseAbstractType.getSize();

        this._xstzName = new Xstz( std, offset );
        offset += this._xstzName.getSize();

        if ( _base.getIType() == ITYPE_TEXT )
        {
            _xstzTextDef = new Xstz( std, offset );
            offset += this._xstzTextDef.getSize();
        }
        else
        {
            this._xstzTextDef = null;
        }

        if ( _base.getIType() == ITYPE_CHCK
                || _base.getIType() == ITYPE_DROP )
        {
            this._wDef = LittleEndian.getUShort(std, offset);
            offset += LittleEndianConsts.SHORT_SIZE;
        }
        else
        {
            this._wDef = null;
        }

        _xstzTextFormat = new Xstz( std, offset );
        offset += this._xstzTextFormat.getSize();

        _xstzHelpText = new Xstz( std, offset );
        offset += this._xstzHelpText.getSize();

        _xstzStatText = new Xstz( std, offset );
        offset += this._xstzStatText.getSize();

        _xstzEntryMcr = new Xstz( std, offset );
        offset += this._xstzEntryMcr.getSize();

        _xstzExitMcr = new Xstz( std, offset );
        offset += this._xstzExitMcr.getSize();

        if ( _base.getIType() == ITYPE_DROP ) {
            _hsttbDropList = new Sttb( std, offset );
        }
    }

    /**
     * specify the default item selected (zero-based index).
     */
    public int getDefaultDropDownItemIndex()
    {
        return _wDef;
    }

    public String[] getDropList()
    {
        return _hsttbDropList.getData();
    }

    public int getSize()
    {
        int size = FFDataBaseAbstractType.getSize();

        size += _xstzName.getSize();

        if ( _base.getIType() == ITYPE_TEXT )
        {
            size += _xstzTextDef.getSize();
        }

        if ( _base.getIType() == ITYPE_CHCK
                || _base.getIType() == ITYPE_DROP )
        {
            size += LittleEndianConsts.SHORT_SIZE;
        }

        size += _xstzTextFormat.getSize();
        size += _xstzHelpText.getSize();
        size += _xstzStatText.getSize();
        size += _xstzEntryMcr.getSize();
        size += _xstzExitMcr.getSize();

        if ( _base.getIType() == ITYPE_DROP )
        {
            size += _hsttbDropList.getSize();
        }

        return size;
    }

    public String getTextDef()
    {
        return _xstzTextDef.getAsJavaString();
    }

    public byte[] serialize()
    {
        byte[] buffer = new byte[getSize()];
        int offset = 0;

        _base.serialize( buffer, offset );
        offset += FFDataBaseAbstractType.getSize();

        offset += _xstzName.serialize( buffer, offset );

        if ( _base.getIType() == ITYPE_TEXT )
        {
            offset += _xstzTextDef.serialize( buffer, offset );
        }

        if ( _base.getIType() == ITYPE_CHCK
                || _base.getIType() == ITYPE_DROP )
        {
            LittleEndian.putUShort( buffer, offset, _wDef );
            offset += LittleEndianConsts.SHORT_SIZE;
        }

        offset += _xstzTextFormat.serialize( buffer, offset );
        offset += _xstzHelpText.serialize( buffer, offset );
        offset += _xstzStatText.serialize( buffer, offset );
        offset += _xstzEntryMcr.serialize( buffer, offset );
        offset += _xstzExitMcr.serialize( buffer, offset );

        if ( _base.getIType() == ITYPE_DROP ) {
            _hsttbDropList.serialize( buffer, offset );
        }

        return buffer;
    }
}

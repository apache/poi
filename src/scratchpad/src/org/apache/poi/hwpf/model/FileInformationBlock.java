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

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;

import org.apache.poi.hwpf.model.io.HWPFOutputStream;
import org.apache.poi.hwpf.model.types.FIBAbstractType;
import org.apache.poi.util.Internal;

/**
 * The File Information Block (FIB). Holds pointers
 *  to various bits of the file, and lots of flags which
 *  specify properties of the document.
 *
 * The parent class, {@link FIBAbstractType}, holds the
 *  first 32 bytes, which make up the FibBase.
 * The next part, the fibRgW / FibRgW97, is handled
 *  by {@link FIBShortHandler}.
 * The next part, the fibRgLw / The FibRgLw97, is
 *  handled by the {@link FIBLongHandler}.
 * Finally, the rest of the fields are handled by
 *  the {@link FIBFieldHandler}.
 *
 * @author  andy
 */
@Internal
public final class FileInformationBlock extends FIBAbstractType
  implements Cloneable
{

    FIBLongHandler _longHandler;
    FIBShortHandler _shortHandler;
    FIBFieldHandler _fieldHandler;

    /** Creates a new instance of FileInformationBlock */
    public FileInformationBlock(byte[] mainDocument)
    {
        fillFields(mainDocument, 0);
    }

    public void fillVariableFields( byte[] mainDocument, byte[] tableStream )
    {
        _shortHandler = new FIBShortHandler( mainDocument );
        _longHandler = new FIBLongHandler( mainDocument, FIBShortHandler.START
                + _shortHandler.sizeInBytes() );

        /*
         * Listed fields won't be treat as UnhandledDataStructure. For all other
         * fields FIBFieldHandler will load it content into
         * UnhandledDataStructure and save them on save.
         */
        HashSet<Integer> knownFieldSet = new HashSet<Integer>();
        knownFieldSet.add( Integer.valueOf( FIBFieldHandler.STSHF ) );
        knownFieldSet.add( Integer.valueOf( FIBFieldHandler.CLX ) );
        knownFieldSet.add( Integer.valueOf( FIBFieldHandler.DOP ) );
        knownFieldSet.add( Integer.valueOf( FIBFieldHandler.PLCFBTECHPX ) );
        knownFieldSet.add( Integer.valueOf( FIBFieldHandler.PLCFBTEPAPX ) );
        knownFieldSet.add( Integer.valueOf( FIBFieldHandler.PLCFSED ) );
        knownFieldSet.add( Integer.valueOf( FIBFieldHandler.PLCFLST ) );
        knownFieldSet.add( Integer.valueOf( FIBFieldHandler.PLFLFO ) );

        // field info
        for ( FieldsDocumentPart part : FieldsDocumentPart.values() )
            knownFieldSet.add( Integer.valueOf( part.getFibFieldsField() ) );

        // bookmarks
        knownFieldSet.add( Integer.valueOf( FIBFieldHandler.PLCFBKF ) );
        knownFieldSet.add( Integer.valueOf( FIBFieldHandler.PLCFBKL ) );
        knownFieldSet.add( Integer.valueOf( FIBFieldHandler.STTBFBKMK ) );

        // notes
        for ( NoteType noteType : NoteType.values() )
        {
            knownFieldSet.add( Integer.valueOf( noteType
                    .getFibDescriptorsFieldIndex() ) );
            knownFieldSet.add( Integer.valueOf( noteType
                    .getFibTextPositionsFieldIndex() ) );
        }

        knownFieldSet.add( Integer.valueOf( FIBFieldHandler.STTBFFFN ) );
        knownFieldSet.add( Integer.valueOf( FIBFieldHandler.STTBFRMARK ) );
        knownFieldSet.add( Integer.valueOf( FIBFieldHandler.STTBSAVEDBY ) );
        knownFieldSet.add( Integer.valueOf( FIBFieldHandler.MODIFIED ) );

        _fieldHandler = new FIBFieldHandler( mainDocument,
                FIBShortHandler.START + _shortHandler.sizeInBytes()
                        + _longHandler.sizeInBytes(), tableStream,
                knownFieldSet, true );
    }

    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder( super.toString() );
        stringBuilder.append( "[FIB2]\n" );
        stringBuilder.append( "\tSubdocuments info:\n" );
        for ( SubdocumentType type : SubdocumentType.values() )
        {
            stringBuilder.append( "\t\t" );
            stringBuilder.append( type );
            stringBuilder.append( " has length of " );
            stringBuilder.append( getSubdocumentTextStreamLength( type ) );
            stringBuilder.append( " char(s)\n" );
        }
        stringBuilder.append( "\tFields PLCF info:\n" );
        for ( FieldsDocumentPart part : FieldsDocumentPart.values() )
        {
            stringBuilder.append( "\t\t" );
            stringBuilder.append( part );
            stringBuilder.append( ": PLCF starts at " );
            stringBuilder.append( getFieldsPlcfOffset( part ) );
            stringBuilder.append( " and have length of " );
            stringBuilder.append( getFieldsPlcfLength( part ) );
            stringBuilder.append( "\n" );
        }
        stringBuilder.append( "\tNotes PLCF info:\n" );
        for ( NoteType noteType : NoteType.values() )
        {
            stringBuilder.append( "\t\t" );
            stringBuilder.append( noteType );
            stringBuilder.append( ": descriptions starts " );
            stringBuilder.append( getNotesDescriptorsOffset( noteType ) );
            stringBuilder.append( " and have length of " );
            stringBuilder.append( getNotesDescriptorsSize( noteType ) );
            stringBuilder.append( " bytes\n" );
            stringBuilder.append( "\t\t" );
            stringBuilder.append( noteType );
            stringBuilder.append( ": text positions starts " );
            stringBuilder.append( getNotesTextPositionsOffset( noteType ) );
            stringBuilder.append( " and have length of " );
            stringBuilder.append( getNotesTextPositionsSize( noteType ) );
            stringBuilder.append( " bytes\n" );
        }
        try
        {
            stringBuilder.append( "\tJava reflection info:\n" );
            for ( Method method : FileInformationBlock.class.getMethods() )
            {
                if ( !method.getName().startsWith( "get" )
                        || !Modifier.isPublic( method.getModifiers() )
                        || Modifier.isStatic( method.getModifiers() )
                        || method.getParameterTypes().length > 0 )
                    continue;
                stringBuilder.append( "\t\t" );
                stringBuilder.append( method.getName() );
                stringBuilder.append( " => " );
                stringBuilder.append( method.invoke( this ) );
                stringBuilder.append( "\n" );
            }
        }
        catch ( Exception exc )
        {
            stringBuilder.append( "(exc: " + exc.getMessage() + ")" );
        }
        stringBuilder.append( "[/FIB2]\n" );
        return stringBuilder.toString();
    }

    public int getFcDop()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.DOP);
    }

    public void setFcDop(int fcDop)
    {
      _fieldHandler.setFieldOffset(FIBFieldHandler.DOP, fcDop);
    }

    public int getLcbDop()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.DOP);
    }

    public void setLcbDop(int lcbDop)
    {
      _fieldHandler.setFieldSize(FIBFieldHandler.DOP, lcbDop);
    }

    public int getFcStshf()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.STSHF);
    }

    public int getLcbStshf()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.STSHF);
    }

    public void setFcStshf(int fcStshf)
    {
      _fieldHandler.setFieldOffset(FIBFieldHandler.STSHF, fcStshf);
    }

    public void setLcbStshf(int lcbStshf)
    {
      _fieldHandler.setFieldSize(FIBFieldHandler.STSHF, lcbStshf);
    }

    public int getFcClx()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.CLX);
    }

    public int getLcbClx()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.CLX);
    }

    public void setFcClx(int fcClx)
    {
      _fieldHandler.setFieldOffset(FIBFieldHandler.CLX, fcClx);
    }

    public void setLcbClx(int lcbClx)
    {
      _fieldHandler.setFieldSize(FIBFieldHandler.CLX, lcbClx);
    }

    public int getFcPlcfbteChpx()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.PLCFBTECHPX);
    }

    public int getLcbPlcfbteChpx()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.PLCFBTECHPX);
    }

    public void setFcPlcfbteChpx(int fcPlcfBteChpx)
    {
      _fieldHandler.setFieldOffset(FIBFieldHandler.PLCFBTECHPX, fcPlcfBteChpx);
    }

    public void setLcbPlcfbteChpx(int lcbPlcfBteChpx)
    {
      _fieldHandler.setFieldSize(FIBFieldHandler.PLCFBTECHPX, lcbPlcfBteChpx);
    }

    public int getFcPlcfbtePapx()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.PLCFBTEPAPX);
    }

    public int getLcbPlcfbtePapx()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.PLCFBTEPAPX);
    }

    public void setFcPlcfbtePapx(int fcPlcfBtePapx)
    {
      _fieldHandler.setFieldOffset(FIBFieldHandler.PLCFBTEPAPX, fcPlcfBtePapx);
    }

    public void setLcbPlcfbtePapx(int lcbPlcfBtePapx)
    {
      _fieldHandler.setFieldSize(FIBFieldHandler.PLCFBTEPAPX, lcbPlcfBtePapx);
    }

    public int getFcPlcfsed()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.PLCFSED);
    }

    public int getLcbPlcfsed()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.PLCFSED);
    }

    public void setFcPlcfsed(int fcPlcfSed)
    {
      _fieldHandler.setFieldOffset(FIBFieldHandler.PLCFSED, fcPlcfSed);
    }

    public void setLcbPlcfsed(int lcbPlcfSed)
    {
      _fieldHandler.setFieldSize(FIBFieldHandler.PLCFSED, lcbPlcfSed);
    }

    public int getFcPlcfLst()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.PLCFLST);
    }

    public int getLcbPlcfLst()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.PLCFLST);
    }

    public void setFcPlcfLst(int fcPlcfLst)
    {
      _fieldHandler.setFieldOffset(FIBFieldHandler.PLCFLST, fcPlcfLst);
    }

    public void setLcbPlcfLst(int lcbPlcfLst)
    {
      _fieldHandler.setFieldSize(FIBFieldHandler.PLCFLST, lcbPlcfLst);
    }

    public int getFcPlfLfo()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.PLFLFO);
    }

    public int getLcbPlfLfo()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.PLFLFO);
    }

    /**
     * @return Offset in table stream of the STTBF that records bookmark names
     *         in the main document
     */
    public int getFcSttbfbkmk()
    {
        return _fieldHandler.getFieldOffset( FIBFieldHandler.STTBFBKMK );
    }

    public void setFcSttbfbkmk( int offset )
    {
        _fieldHandler.setFieldOffset( FIBFieldHandler.STTBFBKMK, offset );
    }

    /**
     * @return Count of bytes in Sttbfbkmk
     */
    public int getLcbSttbfbkmk()
    {
        return _fieldHandler.getFieldSize( FIBFieldHandler.STTBFBKMK );
    }

    public void setLcbSttbfbkmk( int length )
    {
        _fieldHandler.setFieldSize( FIBFieldHandler.STTBFBKMK, length );
    }

    /**
     * @return Offset in table stream of the PLCF that records the beginning CP
     *         offsets of bookmarks in the main document. See BKF structure
     *         definition.
     */
    public int getFcPlcfbkf()
    {
        return _fieldHandler.getFieldOffset( FIBFieldHandler.PLCFBKF );
    }

    public void setFcPlcfbkf( int offset )
    {
        _fieldHandler.setFieldOffset( FIBFieldHandler.PLCFBKF, offset );
    }

    /**
     * @return Count of bytes in Plcfbkf
     */
    public int getLcbPlcfbkf()
    {
        return _fieldHandler.getFieldSize( FIBFieldHandler.PLCFBKF );
    }

    public void setLcbPlcfbkf( int length )
    {
        _fieldHandler.setFieldSize( FIBFieldHandler.PLCFBKF, length );
    }

    /**
     * @return Offset in table stream of the PLCF that records the ending CP
     *         offsets of bookmarks recorded in the main document. No structure
     *         is stored in this PLCF.
     */
    public int getFcPlcfbkl()
    {
        return _fieldHandler.getFieldOffset( FIBFieldHandler.PLCFBKL );
    }

    public void setFcPlcfbkl( int offset )
    {
        _fieldHandler.setFieldOffset( FIBFieldHandler.PLCFBKL, offset );
    }

    /**
     * @return Count of bytes in Plcfbkl
     */
    public int getLcbPlcfbkl()
    {
        return _fieldHandler.getFieldSize( FIBFieldHandler.PLCFBKL );
    }

    public void setLcbPlcfbkl( int length )
    {
        _fieldHandler.setFieldSize( FIBFieldHandler.PLCFBKL, length );
    }

    public void setFcPlfLfo(int fcPlfLfo)
    {
      _fieldHandler.setFieldOffset(FIBFieldHandler.PLFLFO, fcPlfLfo);
    }

    public void setLcbPlfLfo(int lcbPlfLfo)
    {
      _fieldHandler.setFieldSize(FIBFieldHandler.PLFLFO, lcbPlfLfo);
    }

    public int getFcSttbfffn()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.STTBFFFN);
    }

    public int getLcbSttbfffn()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.STTBFFFN);
    }

    public void setFcSttbfffn(int fcSttbFffn)
    {
      _fieldHandler.setFieldOffset(FIBFieldHandler.STTBFFFN, fcSttbFffn);
    }

    public void setLcbSttbfffn(int lcbSttbFffn)
    {
      _fieldHandler.setFieldSize(FIBFieldHandler.STTBFFFN, lcbSttbFffn);
    }
    
    public int getFcSttbfRMark()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.STTBFRMARK);
    }

    public int getLcbSttbfRMark()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.STTBFRMARK);
    }
    
    public void setFcSttbfRMark(int fcSttbfRMark)
    {
      _fieldHandler.setFieldOffset(FIBFieldHandler.STTBFRMARK, fcSttbfRMark);
    }

    public void setLcbSttbfRMark(int lcbSttbfRMark)
    {
      _fieldHandler.setFieldSize(FIBFieldHandler.STTBFRMARK, lcbSttbfRMark);
    }

    /**
     * Return the offset to the PlcfHdd, in the table stream,
     * i.e. fcPlcfHdd
     */
    public int getPlcfHddOffset() {
       return _fieldHandler.getFieldOffset(FIBFieldHandler.PLCFHDD);
    }
    /**
     * Return the size of the PlcfHdd, in the table stream,
     * i.e. lcbPlcfHdd
     */
    public int getPlcfHddSize() {
    	return _fieldHandler.getFieldSize(FIBFieldHandler.PLCFHDD);
    }
    public void setPlcfHddOffset(int fcPlcfHdd) {
    	_fieldHandler.setFieldOffset(FIBFieldHandler.PLCFHDD, fcPlcfHdd);
    }
    public void setPlcfHddSize(int lcbPlcfHdd) {
    	_fieldHandler.setFieldSize(FIBFieldHandler.PLCFHDD, lcbPlcfHdd);
    }

    public int getFcSttbSavedBy()
    {
        return _fieldHandler.getFieldOffset(FIBFieldHandler.STTBSAVEDBY);
    }

    public int getLcbSttbSavedBy()
    {
        return _fieldHandler.getFieldSize(FIBFieldHandler.STTBSAVEDBY);
    }

    public void setFcSttbSavedBy(int fcSttbSavedBy)
    {
      _fieldHandler.setFieldOffset(FIBFieldHandler.STTBSAVEDBY, fcSttbSavedBy);
    }

    public void setLcbSttbSavedBy(int fcSttbSavedBy)
    {
      _fieldHandler.setFieldSize(FIBFieldHandler.STTBSAVEDBY, fcSttbSavedBy);
    }

    public int getModifiedLow()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.PLFLFO);
    }

    public int getModifiedHigh()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.PLFLFO);
    }

    public void setModifiedLow(int modifiedLow)
    {
      _fieldHandler.setFieldOffset(FIBFieldHandler.PLFLFO, modifiedLow);
    }

    public void setModifiedHigh(int modifiedHigh)
    {
      _fieldHandler.setFieldSize(FIBFieldHandler.PLFLFO, modifiedHigh);
    }
    
    /**
     * How many bytes of the main stream contain real data.
     */
    public int getCbMac() {
       return _longHandler.getLong(FIBLongHandler.CBMAC);
    }

    /**
     * Updates the count of the number of bytes in the
     * main stream which contain real data
     */
    public void setCbMac(int cbMac) {
       _longHandler.setLong(FIBLongHandler.CBMAC, cbMac);
    }

    /**
     * @return length of specified subdocument text stream in characters
     */
    public int getSubdocumentTextStreamLength( SubdocumentType type )
    {
        return _longHandler.getLong( type.getFibLongFieldIndex() );
    }

    public void setSubdocumentTextStreamLength( SubdocumentType type, int length )
    {
        _longHandler.setLong( type.getFibLongFieldIndex(), length );
    }

    /**
     * The count of CPs in the main document
     */
    @Deprecated
    public int getCcpText() {
       return _longHandler.getLong(FIBLongHandler.CCPTEXT);
    }
    /**
     * Updates the count of CPs in the main document
     */
    @Deprecated
    public void setCcpText(int ccpText) {
       _longHandler.setLong(FIBLongHandler.CCPTEXT, ccpText);
    }

    /**
     * The count of CPs in the footnote subdocument
     */
    @Deprecated
    public int getCcpFtn() {
       return _longHandler.getLong(FIBLongHandler.CCPFTN);
    }
    /**
     * Updates the count of CPs in the footnote subdocument
     */
    @Deprecated
    public void setCcpFtn(int ccpFtn) {
       _longHandler.setLong(FIBLongHandler.CCPFTN, ccpFtn);
    }

    /**
     * The count of CPs in the header story subdocument
     */
    @Deprecated
    public int getCcpHdd() {
       return _longHandler.getLong(FIBLongHandler.CCPHDD);
    }
    /**
     * Updates the count of CPs in the header story subdocument
     */
    @Deprecated
    public void setCcpHdd(int ccpHdd) {
       _longHandler.setLong(FIBLongHandler.CCPHDD, ccpHdd);
    }

    /**
     * The count of CPs in the comments (atn) subdocument
     */
    @Deprecated
    public int getCcpAtn() {
       return _longHandler.getLong(FIBLongHandler.CCPATN);
    }

    @Deprecated
    public int getCcpCommentAtn() {
       return getCcpAtn();
    }
    /**
     * Updates the count of CPs in the comments (atn) story subdocument
     */
    @Deprecated
    public void setCcpAtn(int ccpAtn) {
       _longHandler.setLong(FIBLongHandler.CCPATN, ccpAtn);
    }

    /**
     * The count of CPs in the end note subdocument
     */
    @Deprecated
    public int getCcpEdn() {
       return _longHandler.getLong(FIBLongHandler.CCPEDN);
    }
    /**
     * Updates the count of CPs in the end note subdocument
     */
    @Deprecated
    public void setCcpEdn(int ccpEdn) {
       _longHandler.setLong(FIBLongHandler.CCPEDN, ccpEdn);
    }

    /**
     * The count of CPs in the main document textboxes
     */
    @Deprecated
    public int getCcpTxtBx() {
       return _longHandler.getLong(FIBLongHandler.CCPTXBX);
    }
    /**
     * Updates the count of CPs in the main document textboxes
     */
    @Deprecated
    public void setCcpTxtBx(int ccpTxtBx) {
       _longHandler.setLong(FIBLongHandler.CCPTXBX, ccpTxtBx);
    }

    /**
     * The count of CPs in the header textboxes
     */
    @Deprecated
    public int getCcpHdrTxtBx() {
       return _longHandler.getLong(FIBLongHandler.CCPHDRTXBX);
    }
    /**
     * Updates the count of CPs in the header textboxes
     */
    @Deprecated
    public void setCcpHdrTxtBx(int ccpTxtBx) {
       _longHandler.setLong(FIBLongHandler.CCPHDRTXBX, ccpTxtBx);
    }


    public void clearOffsetsSizes()
    {
      _fieldHandler.clearFields();
    }

    public int getFieldsPlcfOffset( FieldsDocumentPart part )
    {
        return _fieldHandler.getFieldOffset( part.getFibFieldsField() );
    }

    public int getFieldsPlcfLength( FieldsDocumentPart part )
    {
        return _fieldHandler.getFieldSize( part.getFibFieldsField() );
    }

    public void setFieldsPlcfOffset( FieldsDocumentPart part, int offset )
    {
        _fieldHandler.setFieldOffset( part.getFibFieldsField(), offset );
    }

    public void setFieldsPlcfLength( FieldsDocumentPart part, int length )
    {
        _fieldHandler.setFieldSize( part.getFibFieldsField(), length );
    }

    @Deprecated
    public int getFcPlcffldAtn()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.PLCFFLDATN);
    }

    @Deprecated
    public int getLcbPlcffldAtn()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.PLCFFLDATN);
    }

    @Deprecated
    public void setFcPlcffldAtn( int offset )
    {
        _fieldHandler.setFieldOffset( FIBFieldHandler.PLCFFLDATN, offset );
    }

    @Deprecated
    public void setLcbPlcffldAtn( int size )
    {
        _fieldHandler.setFieldSize( FIBFieldHandler.PLCFFLDATN, size );
    }

    @Deprecated
    public int getFcPlcffldEdn()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.PLCFFLDEDN);
    }

    @Deprecated
    public int getLcbPlcffldEdn()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.PLCFFLDEDN);
    }

    @Deprecated
    public void setFcPlcffldEdn( int offset )
    {
        _fieldHandler.setFieldOffset( FIBFieldHandler.PLCFFLDEDN, offset );
    }

    @Deprecated
    public void setLcbPlcffldEdn( int size )
    {
        _fieldHandler.setFieldSize( FIBFieldHandler.PLCFFLDEDN, size );
    }

    @Deprecated
    public int getFcPlcffldFtn()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.PLCFFLDFTN);
    }

    @Deprecated
    public int getLcbPlcffldFtn()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.PLCFFLDFTN);
    }

    @Deprecated
    public void setFcPlcffldFtn( int offset )
    {
        _fieldHandler.setFieldOffset( FIBFieldHandler.PLCFFLDFTN, offset );
    }

    @Deprecated
    public void setLcbPlcffldFtn( int size )
    {
        _fieldHandler.setFieldSize( FIBFieldHandler.PLCFFLDFTN, size );
    }

    @Deprecated
    public int getFcPlcffldHdr()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.PLCFFLDHDR);
    }

    @Deprecated
    public int getLcbPlcffldHdr()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.PLCFFLDHDR);
    }

    @Deprecated
    public void setFcPlcffldHdr( int offset )
    {
        _fieldHandler.setFieldOffset( FIBFieldHandler.PLCFFLDHDR, offset );
    }

    @Deprecated
    public void setLcbPlcffldHdr( int size )
    {
        _fieldHandler.setFieldSize( FIBFieldHandler.PLCFFLDHDR, size );
    }

    @Deprecated
    public int getFcPlcffldHdrtxbx()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.PLCFFLDHDRTXBX);
    }

    @Deprecated
    public int getLcbPlcffldHdrtxbx()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.PLCFFLDHDRTXBX);
    }

    @Deprecated
    public void setFcPlcffldHdrtxbx( int offset )
    {
        _fieldHandler.setFieldOffset( FIBFieldHandler.PLCFFLDHDRTXBX, offset );
    }

    @Deprecated
    public void setLcbPlcffldHdrtxbx( int size )
    {
        _fieldHandler.setFieldSize( FIBFieldHandler.PLCFFLDHDRTXBX, size );
    }

    @Deprecated
    public int getFcPlcffldMom()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.PLCFFLDMOM);
    }

    @Deprecated
    public int getLcbPlcffldMom()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.PLCFFLDMOM);
    }

    @Deprecated
    public void setFcPlcffldMom( int offset )
    {
        _fieldHandler.setFieldOffset( FIBFieldHandler.PLCFFLDMOM, offset );
    }

    @Deprecated
    public void setLcbPlcffldMom( int size )
    {
        _fieldHandler.setFieldSize( FIBFieldHandler.PLCFFLDMOM, size );
    }

    @Deprecated
    public int getFcPlcffldTxbx()
    {
      return _fieldHandler.getFieldOffset(FIBFieldHandler.PLCFFLDTXBX);
    }

    @Deprecated
    public int getLcbPlcffldTxbx()
    {
      return _fieldHandler.getFieldSize(FIBFieldHandler.PLCFFLDTXBX);
    }

    @Deprecated
    public void setFcPlcffldTxbx( int offset )
    {
        _fieldHandler.setFieldOffset( FIBFieldHandler.PLCFFLDTXBX, offset );
    }

    @Deprecated
    public void setLcbPlcffldTxbx( int size )
    {
        _fieldHandler.setFieldSize( FIBFieldHandler.PLCFFLDTXBX, size );
    }


    public int getFSPAPlcfOffset( FSPADocumentPart part )
    {
        return _fieldHandler.getFieldOffset( part.getFibFieldsField() );
    }

    public int getFSPAPlcfLength( FSPADocumentPart part )
    {
        return _fieldHandler.getFieldSize( part.getFibFieldsField() );
    }

    public void setFSPAPlcfOffset( FSPADocumentPart part, int offset )
    {
        _fieldHandler.setFieldOffset( part.getFibFieldsField(), offset );
    }

    public void setFSPAPlcfLength( FSPADocumentPart part, int length )
    {
        _fieldHandler.setFieldSize( part.getFibFieldsField(), length );
    }

    @Deprecated
    public int getFcPlcspaMom()
    {
        return _fieldHandler.getFieldOffset(FIBFieldHandler.PLCSPAMOM);
    }

    @Deprecated
    public int getLcbPlcspaMom()
    {
        return _fieldHandler.getFieldSize(FIBFieldHandler.PLCSPAMOM);
    }

    public int getFcDggInfo()
    {
        return _fieldHandler.getFieldOffset(FIBFieldHandler.DGGINFO);
    }

    public int getLcbDggInfo()
    {
        return _fieldHandler.getFieldSize(FIBFieldHandler.DGGINFO);
    }

    public int getNotesDescriptorsOffset( NoteType noteType )
    {
        return _fieldHandler.getFieldOffset( noteType
                .getFibDescriptorsFieldIndex() );
    }

    public void setNotesDescriptorsOffset( NoteType noteType, int offset )
    {
        _fieldHandler.setFieldOffset( noteType.getFibDescriptorsFieldIndex(),
                offset );
    }

    public int getNotesDescriptorsSize( NoteType noteType )
    {
        return _fieldHandler.getFieldSize( noteType
                .getFibDescriptorsFieldIndex() );
    }

    public void setNotesDescriptorsSize( NoteType noteType, int offset )
    {
        _fieldHandler.setFieldSize( noteType.getFibDescriptorsFieldIndex(),
                offset );
    }

    public int getNotesTextPositionsOffset( NoteType noteType )
    {
        return _fieldHandler.getFieldOffset( noteType
                .getFibTextPositionsFieldIndex() );
    }

    public void setNotesTextPositionsOffset( NoteType noteType, int offset )
    {
        _fieldHandler.setFieldOffset( noteType.getFibTextPositionsFieldIndex(),
                offset );
    }

    public int getNotesTextPositionsSize( NoteType noteType )
    {
        return _fieldHandler.getFieldSize( noteType
                .getFibTextPositionsFieldIndex() );
    }

    public void setNotesTextPositionsSize( NoteType noteType, int offset )
    {
        _fieldHandler.setFieldSize( noteType.getFibTextPositionsFieldIndex(),
                offset );
    }

    public void writeTo( byte[] mainStream, HWPFOutputStream tableStream)
      throws IOException
    {
      //HWPFOutputStream mainDocument = sys.getStream("WordDocument");
      //HWPFOutputStream tableStream = sys.getStream("1Table");

      super.serialize(mainStream, 0);

      int size = super.getSize();
      _shortHandler.serialize(mainStream);
      _longHandler.serialize(mainStream, size + _shortHandler.sizeInBytes());
      _fieldHandler.writeTo(mainStream,
        super.getSize() + _shortHandler.sizeInBytes() + _longHandler.sizeInBytes(), tableStream);

    }

    public int getSize()
    {
      return super.getSize() + _shortHandler.sizeInBytes() +
        _longHandler.sizeInBytes() + _fieldHandler.sizeInBytes();
    }
//    public Object clone()
//    {
//      try
//      {
//        return super.clone();
//      }
//      catch (CloneNotSupportedException e)
//      {
//        e.printStackTrace();
//        return null;
//      }
//    }

}


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

package org.apache.poi.hslf.record;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.poi.ddf.DefaultEscherRecordFactory;
import org.apache.poi.ddf.EscherBoolProperty;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherDgRecord;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.ddf.EscherPropertyTypes;
import org.apache.poi.ddf.EscherRGBProperty;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherRecordTypes;
import org.apache.poi.ddf.EscherSimpleProperty;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.ddf.EscherSpgrRecord;
import org.apache.poi.ddf.EscherTextboxRecord;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndian;

/**
 * These are actually wrappers onto Escher drawings. Make use of
 *  the DDF classes to do useful things with them.
 * For now, creates a tree of the Escher records, and then creates any
 *  PowerPoint (hslf) records found within the EscherTextboxRecord
 *  (msofbtClientTextbox) records.
 * Also provides easy access to the EscherTextboxRecords, so that their
 *  text may be extracted and used in Sheets.
 * <p>
 * {@code [MS-PPT] - v20210216} refers to this as a {@code DrawingContainer}.
 */

// For now, pretending to be an atom. Might not always be, but that
//  would require a wrapping class
public final class PPDrawing extends RecordAtom implements Iterable<EscherRecord> {

    private final byte[] _header;
    private long _type;

    private EscherTextboxWrapper[] textboxWrappers;

    private final EscherContainerRecord dgContainer = new EscherContainerRecord();

    //cached EscherDgRecord
    private EscherDgRecord dg;

    /**
     * Get access to the underlying Escher Records
     */
    @SuppressWarnings("WeakerAccess")
    public List<EscherRecord> getEscherRecords() { return Collections.singletonList(dgContainer); }

    @Override
    public Iterator<EscherRecord> iterator() {
        return getEscherRecords().iterator();
    }

    /**
     * @since POI 5.2.0
     */
    @Override
    public Spliterator<EscherRecord> spliterator() {
        return getEscherRecords().spliterator();
    }

    /**
     * Get access to the atoms inside Textboxes
     */
    public EscherTextboxWrapper[] getTextboxWrappers() { return textboxWrappers; }


    /* ******************** record stuff follows ********************** */

    /**
     * Creates a new, empty, PPDrawing (typically for use with a new Slide
     *  or Notes)
     */
    public PPDrawing() {
        _header = new byte[8];
        LittleEndian.putUShort(_header, 0, 15);
        LittleEndian.putUShort(_header, 2, RecordTypes.PPDrawing.typeID);
        LittleEndian.putInt(_header, 4, 0);

        textboxWrappers = new EscherTextboxWrapper[]{};
        create();
    }

    /**
     * Sets everything up, groks the escher etc
     */
    PPDrawing(byte[] source, int start, int len) {
        // Get the header
        _header = Arrays.copyOfRange(source, start, start+8);

        // Get the type
        _type = LittleEndian.getUShort(_header,2);

        // Build up a tree of Escher records contained within
        final DefaultEscherRecordFactory erf = new HSLFEscherRecordFactory();
        dgContainer.fillFields(source, start + 8, erf);
        if (dgContainer.getRecordId() != EscherRecordTypes.DG_CONTAINER.typeID) {
            throw new IllegalArgumentException("Unexpected record type: " + dgContainer.getRecordId());
        }
        dg = dgContainer.getChildById(EscherRecordTypes.DG.typeID);

        textboxWrappers = Stream.of(dgContainer).
            flatMap(findEscherContainer(EscherRecordTypes.SPGR_CONTAINER)).
            flatMap(findEscherContainer(EscherRecordTypes.SP_CONTAINER)).
            flatMap(PPDrawing::getTextboxHelper).
            toArray(EscherTextboxWrapper[]::new);
    }

    private static Stream<EscherTextboxWrapper> getTextboxHelper(EscherContainerRecord spContainer) {
        Optional<EscherTextboxRecord> oTB = firstEscherRecord(spContainer, EscherRecordTypes.CLIENT_TEXTBOX);
        if (!oTB.isPresent()) {
            return Stream.empty();
        }

        EscherTextboxWrapper tbw = new EscherTextboxWrapper(oTB.get());
        findInSpContainer(spContainer).ifPresent(tbw::setStyleTextProp9Atom);

        Optional<EscherSpRecord> oSP = firstEscherRecord(spContainer, EscherRecordTypes.SP);
        oSP.map(EscherSpRecord::getShapeId).ifPresent(tbw::setShapeId);

        return Stream.of(tbw);
    }

    private static Optional<StyleTextProp9Atom> findInSpContainer(final EscherContainerRecord spContainer) {
        Optional<HSLFEscherClientDataRecord> oCD = firstEscherRecord(spContainer, EscherRecordTypes.CLIENT_DATA);
        return oCD.map(HSLFEscherClientDataRecord::getHSLFChildRecords).map(List::stream).orElseGet(Stream::empty).
            filter(sameHSLF(RecordTypes.ProgTags)).
            flatMap(r -> Stream.of(r.getChildRecords())).
            filter(sameHSLF(RecordTypes.ProgBinaryTag)).
            flatMap(PPDrawing::findInProgBinaryTag).
            findFirst();
    }

    private static Stream<StyleTextProp9Atom> findInProgBinaryTag(org.apache.poi.hslf.record.Record r) {
        Record[] ch = r.getChildRecords();
        if (ch != null &&
            ch.length == 2 &&
            ch[0] instanceof CString &&
            ch[1] instanceof BinaryTagDataBlob &&
            "___PPT9".equals(((CString)ch[0]).getText())
        ) {
            BinaryTagDataBlob blob = (BinaryTagDataBlob) ch[1];
            StyleTextProp9Atom prop9 = (StyleTextProp9Atom) blob.findFirstOfType(RecordTypes.StyleTextProp9Atom.typeID);
            if (prop9 != null) {
                return Stream.of(prop9);
            }
        }
        return Stream.empty();
    }

    /**
     * We are type 1036
     */
    public long getRecordType() { return _type; }

    /**
     * We're pretending to be an atom, so return null
     */
    public org.apache.poi.hslf.record.Record[] getChildRecords() { return null; }

    /**
     * Write the contents of the record back, so it can be written
     *  to disk
     * Walks the escher layer to get the contents
     */
    public void writeOut(OutputStream out) throws IOException {
        // Ensure the escher layer reflects the text changes
        for (EscherTextboxWrapper w : textboxWrappers) {
            w.writeOut(null);
        }

        // Find the new size of the escher children;
        int newSize = 0;
        newSize += dgContainer.getRecordSize();

        // Update the size (header bytes 5-8)
        LittleEndian.putInt(_header,4,newSize);

        // Write out our header
        out.write(_header);

        // Now grab the children's data
        byte[] b = new byte[newSize];
        int done = 0;
        dgContainer.serialize(done, b);

        // Finally, write out the children
        out.write(b);
    }

    /**
     * Create the Escher records associated with a new PPDrawing
     */
    private void create(){
        dgContainer.setRecordId( EscherContainerRecord.DG_CONTAINER );
        dgContainer.setOptions((short)15);

        dg = new EscherDgRecord();
        dg.setOptions((short)16);
        dg.setNumShapes(1);
        dgContainer.addChildRecord(dg);

        EscherContainerRecord spgrContainer = new EscherContainerRecord();
        spgrContainer.setOptions((short)15);
        spgrContainer.setRecordId(EscherContainerRecord.SPGR_CONTAINER);

        EscherContainerRecord spContainer = new EscherContainerRecord();
        spContainer.setOptions((short)15);
        spContainer.setRecordId(EscherContainerRecord.SP_CONTAINER);

        EscherSpgrRecord spgr = new EscherSpgrRecord();
        spgr.setOptions((short)1);
        spContainer.addChildRecord(spgr);

        EscherSpRecord sp = new EscherSpRecord();
        sp.setOptions((short)((ShapeType.NOT_PRIMITIVE.nativeId << 4) + 2));
        sp.setFlags(EscherSpRecord.FLAG_PATRIARCH | EscherSpRecord.FLAG_GROUP);
        spContainer.addChildRecord(sp);
        spgrContainer.addChildRecord(spContainer);
        dgContainer.addChildRecord(spgrContainer);

        spContainer = new EscherContainerRecord();
        spContainer.setOptions((short)15);
        spContainer.setRecordId(EscherContainerRecord.SP_CONTAINER);
        sp = new EscherSpRecord();
        sp.setOptions((short)((ShapeType.RECT.nativeId << 4) + 2));
        sp.setFlags(EscherSpRecord.FLAG_BACKGROUND | EscherSpRecord.FLAG_HASSHAPETYPE);
        spContainer.addChildRecord(sp);

        EscherOptRecord opt = new EscherOptRecord();
        opt.setRecordId(EscherOptRecord.RECORD_ID);
        opt.addEscherProperty(new EscherRGBProperty(EscherPropertyTypes.FILL__FILLCOLOR, 134217728));
        opt.addEscherProperty(new EscherRGBProperty(EscherPropertyTypes.FILL__FILLBACKCOLOR, 134217733));
        opt.addEscherProperty(new EscherSimpleProperty(EscherPropertyTypes.FILL__RECTRIGHT, 10064750));
        opt.addEscherProperty(new EscherSimpleProperty(EscherPropertyTypes.FILL__RECTBOTTOM, 7778750));
        opt.addEscherProperty(new EscherBoolProperty(EscherPropertyTypes.FILL__NOFILLHITTEST, 1179666));
        opt.addEscherProperty(new EscherBoolProperty(EscherPropertyTypes.LINESTYLE__NOLINEDRAWDASH, 524288));
        opt.addEscherProperty(new EscherSimpleProperty(EscherPropertyTypes.SHAPE__BLACKANDWHITESETTINGS, 9));
        opt.addEscherProperty(new EscherSimpleProperty(EscherPropertyTypes.SHAPE__BACKGROUNDSHAPE, 65537));
        spContainer.addChildRecord(opt);

        dgContainer.addChildRecord(spContainer);
    }

    /**
     * Add a new EscherTextboxWrapper to this <code>PPDrawing</code>.
     */
    public void addTextboxWrapper(EscherTextboxWrapper txtbox){
        EscherTextboxWrapper[] tw = new EscherTextboxWrapper[textboxWrappers.length + 1];
        System.arraycopy(textboxWrappers, 0, tw, 0, textboxWrappers.length);

        tw[textboxWrappers.length] = txtbox;
        textboxWrappers = tw;
    }

    /**
     * @return the container record for drawings
     * @since POI 3.14-Beta2
     */
    public EscherContainerRecord getDgContainer() {
        return dgContainer;
    }

    /**
     * Return EscherDgRecord which keeps track of the number of shapes and shapeId in this drawing group
     *
     * @return EscherDgRecord
     */
    public EscherDgRecord getEscherDgRecord(){
        return dg;
    }

    public StyleTextProp9Atom[] getNumberedListInfo() {
        return Stream.of(dgContainer).
                    flatMap(findEscherContainer(EscherRecordTypes.SPGR_CONTAINER)).
                    flatMap(findEscherContainer(EscherRecordTypes.SP_CONTAINER)).
                    map(PPDrawing::findInSpContainer).
                    filter(Optional::isPresent).
                    map(Optional::get).
                    toArray(StyleTextProp9Atom[]::new);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("escherRecords", this::getEscherRecords);
    }

    private static Predicate<org.apache.poi.hslf.record.Record> sameHSLF(RecordTypes type) {
        return (p) -> p.getRecordType() == type.typeID;
    }

    private static Predicate<EscherRecord> sameEscher(EscherRecordTypes type) {
        return (p) -> p.getRecordId() == type.typeID;
    }

    @SuppressWarnings("unchecked")
    private static <T extends EscherRecord> Optional<T> firstEscherRecord(Iterable<EscherRecord> container, EscherRecordTypes type) {
        return StreamSupport.stream(container.spliterator(), false).filter(sameEscher(type)).map(o -> (T)o).findFirst();
    }

    private static Function<EscherContainerRecord,Stream<EscherContainerRecord>> findEscherContainer(EscherRecordTypes type) {
        return (r) -> r.getChildContainers().stream().filter(sameEscher(type));
    }

}

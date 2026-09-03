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

package org.apache.poi.hssf.record;

import static org.apache.poi.hssf.record.RecordInputStream.MAX_RECORD_DATA_SIZE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.ddf.DefaultEscherRecordFactory;
import org.apache.poi.ddf.EscherClientDataRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherDgRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherSerializationListener;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.ddf.EscherSpgrRecord;
import org.apache.poi.ddf.EscherTextboxRecord;
import org.apache.poi.util.GenericRecordXmlWriter;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.RecordFormatException;

/**
 * This class is used to aggregate the MSODRAWING and OBJ record
 * combinations.  This is necessary due to the bizare way in which
 * these records are serialized.  What happens is that you get a
 * combination of MSODRAWING -&gt; OBJ -&gt; MSODRAWING -&gt; OBJ records
 * but the escher records are serialized _across_ the MSODRAWING
 * records.
 * <p>
 * It gets even worse when you start looking at TXO records.
 * <p>
 * So what we do with this class is aggregate lazily.  That is
 * we don't aggregate the MSODRAWING -&gt; OBJ records unless we
 * need to modify them.
 * <p>
 * At first document contains 4 types of records which belong to drawing layer.
 * There are can be such sequence of record:
 * <p>
 * DrawingRecord
 * ContinueRecord
 * ...
 * ContinueRecord
 * ObjRecord | TextObjectRecord
 * .....
 * ContinueRecord
 * ...
 * ContinueRecord
 * ObjRecord | TextObjectRecord
 * NoteRecord
 * ...
 * NoteRecord
 * <p>
 * To work with shapes we have to read data from Drawing and Continue records into single array of bytes and
 * build escher(office art) records tree from this array.
 * Each shape in drawing layer matches corresponding ObjRecord
 * Each textbox matches corresponding TextObjectRecord
 * <p>
 * ObjRecord contains information about shape. Thus each ObjRecord corresponds EscherContainerRecord(SPGR)
 * <p>
 * EscherAggrefate contains also NoteRecords
 * NoteRecords must be serial
 */

public final class EscherAggregate extends AbstractEscherHolderRecord {
    // not a real sid - dummy value
    public static final short sid = 9876;
    //arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_RECORD_LENGTH = 100_000_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    /**
     * @param length the max record length allowed for EscherAggregate
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for EscherAggregate
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    /**
     * Maps shape container objects to their {@link TextObjectRecord} or {@link ObjRecord}
     */
    private final Map<EscherRecord, Record> shapeToObj = new HashMap<>();

    /**
     * list of "tail" records that need to be serialized after all drawing group records
     */
    private final Map<Integer, NoteRecord> tailRec = new LinkedHashMap<>();

    /**
     * create new EscherAggregate
     * @param createDefaultTree if true creates base tree of the escher records, see EscherAggregate.buildBaseTree()
     *                          else return empty escher aggregate
     */
    public EscherAggregate(boolean createDefaultTree) {
        if (createDefaultTree){
            buildBaseTree();
        }
    }

    public EscherAggregate(EscherAggregate other) {
        super(other);
        // shallow copy, because the aggregates doesn't own the records
        shapeToObj.putAll(other.shapeToObj);
        tailRec.putAll(other.tailRec);
    }

    /**
     * @return Returns the current sid.
     */
    @Override
    public short getSid() {
        return sid;
    }

    /**
     * Calculates the xml representation of this record.  This is
     * simply a dump of all the records.
     * @param tab - string which must be added before each line (used by default '\t')
     * @return xml representation of the all aggregated records
     */
    public String toXml(String tab) {
        return GenericRecordXmlWriter.marshal(this);
    }

    /**
     * Collapses the drawing records into an aggregate.
     * read Drawing, Obj, TxtObj, Note and Continue records into single byte array,
     * create Escher tree from byte array, create map &lt;EscherRecord, Record&gt;
     *
     * @param records - list of all records inside sheet
     * @param locFirstDrawingRecord - location of the first DrawingRecord inside sheet
     * @return new EscherAggregate create from all aggregated records which belong to drawing layer
     */
    public static EscherAggregate createAggregate(final List<RecordBase> records, final int locFirstDrawingRecord) {
        EscherAggregate agg = new EscherAggregate(false);

        ShapeCollector recordFactory = new ShapeCollector();
        List<Record> objectRecords = new ArrayList<>();

        int nextIdx = locFirstDrawingRecord;
        for (RecordBase rb : records.subList(locFirstDrawingRecord, records.size())) {
            nextIdx++;
            switch (sid(rb)) {
                case DrawingRecord.sid:
                    recordFactory.addBytes(((DrawingRecord)rb).getRecordData());
                    continue;
                case ContinueRecord.sid:
                    recordFactory.addBytes(((ContinueRecord)rb).getData());
                    continue;
                case ObjRecord.sid:
                case TextObjectRecord.sid:
                    objectRecords.add((org.apache.poi.hssf.record.Record)rb);
                    continue;
                case NoteRecord.sid:
                    // any NoteRecords that follow the drawing block must be aggregated and saved in the tailRec collection
                    NoteRecord r = (NoteRecord)rb;
                    agg.tailRec.put(r.getShapeId(), r);
                    continue;
                default:
                    nextIdx--;
                    break;
            }
            break;
        }

        // replace drawing block with the created EscherAggregate
        records.set(locFirstDrawingRecord, agg);
        if (locFirstDrawingRecord+1 <= nextIdx) {
            records.subList(locFirstDrawingRecord + 1, nextIdx).clear();
        }

        // Decode the shapes
        Iterator<EscherRecord> shapeIter = recordFactory.parse(agg).iterator();

        // Associate the object records with the shapes
        objectRecords.forEach(or -> agg.shapeToObj.put(shapeIter.next(), or));

        return agg;
    }

    private static class ShapeCollector extends DefaultEscherRecordFactory {
        final List<EscherRecord> objShapes = new ArrayList<>();
        final UnsynchronizedByteArrayOutputStream buffer = UnsynchronizedByteArrayOutputStream.builder().get();

        void addBytes(byte[] data) {
            buffer.write(data);
        }

        @Override
        public EscherRecord createRecord(byte[] data, int offset) {
            EscherRecord r = super.createRecord(data, offset);
            short rid = r.getRecordId();
            if (rid == EscherClientDataRecord.RECORD_ID || rid == EscherTextboxRecord.RECORD_ID) {
                objShapes.add(r);
            }
            return r;
        }

        List<EscherRecord> parse(EscherAggregate agg) {
            byte[] buf = buffer.toByteArray();
            int pos = 0;
            while (pos < buf.length) {
                EscherRecord r = createRecord(buf, pos);
                pos += r.fillFields(buf, pos, this);
                agg.addEscherRecord(r);
            }
            return objShapes;
        }
    }

    /**
     * Serializes this aggregate to a byte array.  Since this is an aggregate
     * record it will effectively serialize the aggregated records.
     *
     * @param offset The offset into the start of the array.
     * @param data   The byte array to serialize to.
     * @return The number of bytes serialized.
     */
    @Override
    public int serialize(final int offset, final byte[] data) {
        // Determine buffer size
        List <EscherRecord>records = getEscherRecords();
        int size = getEscherRecordSize(records);
        byte[] buffer = new byte[size];

        // Serialize escher records into one big data structure and keep note of ending offsets.
        final List <Integer>spEndingOffsets = new ArrayList<>();
        final List <EscherRecord> shapes = new ArrayList<>();
        int pos = 0;
        for (EscherRecord record : records) {
            pos += record.serialize(pos, buffer, new EscherSerializationListener() {
                @Override
                public void beforeRecordSerialize(int offset, short recordId, EscherRecord record) {
                }

                @Override
                public void afterRecordSerialize(int offset, short recordId, int size, EscherRecord record) {
                    if (recordId == EscherClientDataRecord.RECORD_ID || recordId == EscherTextboxRecord.RECORD_ID) {
                        spEndingOffsets.add(offset);
                        shapes.add(record);
                    }
                }
            });
        }
        shapes.add(0, null);
        spEndingOffsets.add(0, 0);

        // Split escher records into separate MSODRAWING and OBJ, TXO records.  (We don't break on
        // the first one because it's the patriach).
        pos = offset;
        int writtenEscherBytes = 0;
        boolean isFirst = true;
        int endOffset = 0;
        for (int i = 1; i < shapes.size(); i++) {
            int startOffset = endOffset;
            endOffset = spEndingOffsets.get(i);

            byte[] drawingData = Arrays.copyOfRange(buffer, startOffset, endOffset);
            pos += writeDataIntoDrawingRecord(drawingData, writtenEscherBytes, pos, data, isFirst);

            writtenEscherBytes += drawingData.length;

            // Write the matching OBJ record
            Record obj = shapeToObj.get(shapes.get(i));
            if (obj == null) {
                throw new IllegalStateException("Cannot serialize EscherAggregate with missing shape-object");
            }
            pos += obj.serialize(pos, data);

            isFirst = false;
        }

        if (endOffset < buffer.length - 1) {
            byte[] drawingData = Arrays.copyOfRange(buffer, endOffset, buffer.length);
            pos += writeDataIntoDrawingRecord(drawingData, writtenEscherBytes, pos, data, isFirst);
        }

        for (NoteRecord noteRecord : tailRec.values()) {
            pos += noteRecord.serialize(pos, data);
        }

        int bytesWritten = pos - offset;
        if (bytesWritten != getRecordSize()) {
            throw new RecordFormatException(bytesWritten + " bytes written but getRecordSize() reports " + getRecordSize());
        }
        return bytesWritten;
    }

    /**
     * @param drawingData - escher records saved into single byte array
     * @param writtenEscherBytes - count of bytes already saved into drawing records (we should know it to decide create
     *                           drawing or continue record)
     * @param pos current position of data array
     * @param data - array of bytes where drawing records must be serialized
     * @param isFirst - is it the first shape, saved into data array
     * @return offset of data array after serialization
     */
    private int writeDataIntoDrawingRecord(final byte[] drawingData, final int writtenEscherBytes, final int pos, final byte[] data, final boolean isFirst) {
        int temp = 0;
        //First record in drawing layer MUST be DrawingRecord
        boolean useDrawingRecord = isFirst || (writtenEscherBytes + drawingData.length) <= MAX_RECORD_DATA_SIZE;

        for (int j = 0; j < drawingData.length; j += MAX_RECORD_DATA_SIZE) {
            byte[] buf = Arrays.copyOfRange(drawingData, j, Math.min(j+MAX_RECORD_DATA_SIZE, drawingData.length));
            Record drawing = (useDrawingRecord) ? new DrawingRecord(buf) : new ContinueRecord(buf);
            temp += drawing.serialize(pos + temp, data);
            useDrawingRecord = false;
        }
        return temp;
    }

    /**
     * How many bytes do the raw escher records contain.
     *
     * @param records List of escher records
     * @return the number of bytes
     */
    private int getEscherRecordSize(List<EscherRecord> records) {
        int size = 0;
        for (EscherRecord record : records){
            size += record.getRecordSize();
        }
        return size;
    }

    /**
     * @return record size, including header size of obj, text, note, drawing, continue records
     */
    @Override
    public int getRecordSize() {
        // To determine size of aggregate record we have to know size of each DrawingRecord because if DrawingRecord
        // is split into several continue records we have to add header size to total EscherAggregate size
        int continueRecordsHeadersSize = 0;
        // Determine buffer size
        List<EscherRecord> records = getEscherRecords();
        int rawEscherSize = getEscherRecordSize(records);
        byte[] buffer = IOUtils.safelyAllocate(rawEscherSize, MAX_RECORD_LENGTH,
                "EscherAggregate.setMaxRecordLength()");
        final List<Integer> spEndingOffsets = new ArrayList<>();
        int pos = 0;
        for (EscherRecord e : records) {
            pos += e.serialize(pos, buffer, new EscherSerializationListener() {
                @Override
                public void beforeRecordSerialize(int offset, short recordId, EscherRecord record) {
                }

                @Override
                public void afterRecordSerialize(int offset, short recordId, int size, EscherRecord record) {
                    if (recordId == EscherClientDataRecord.RECORD_ID || recordId == EscherTextboxRecord.RECORD_ID) {
                        spEndingOffsets.add(offset);
                    }
                }
            });
        }
        spEndingOffsets.add(0, 0);

        for (int i = 1; i < spEndingOffsets.size(); i++) {
            if (i == spEndingOffsets.size() - 1 && spEndingOffsets.get(i) < pos) {
                continueRecordsHeadersSize += 4;
            }
            if (spEndingOffsets.get(i) - spEndingOffsets.get(i - 1) <= MAX_RECORD_DATA_SIZE) {
                continue;
            }
            continueRecordsHeadersSize += ((spEndingOffsets.get(i) - spEndingOffsets.get(i - 1)) / MAX_RECORD_DATA_SIZE) * 4;
        }

        int drawingRecordSize = rawEscherSize + (shapeToObj.size()) * 4;
        if (rawEscherSize != 0 && spEndingOffsets.size() == 1) {
            // EMPTY
            continueRecordsHeadersSize += 4;
        }
        int objRecordSize = 0;
        for (org.apache.poi.hssf.record.Record r : shapeToObj.values()) {
            objRecordSize += r.getRecordSize();
        }
        int tailRecordSize = 0;
        for (NoteRecord noteRecord : tailRec.values()) {
            tailRecordSize += noteRecord.getRecordSize();
        }
        return drawingRecordSize + objRecordSize + tailRecordSize + continueRecordsHeadersSize;
    }

    /**
     * Associates an escher record to an OBJ record or a TXO record.
     * @param r - ClientData or Textbox record
     * @param objRecord - Obj or TextObj record
     */
    public void associateShapeToObjRecord(EscherRecord r, Record objRecord) {
        shapeToObj.put(r, objRecord);
    }

    /**
     * Remove echerRecord and associated to it Obj or TextObj record
     * @param rec - clientData or textbox record to be removed
     */
    public void removeShapeToObjRecord(EscherRecord rec) {
        shapeToObj.remove(rec);
    }

    /**
     * @return "ESCHERAGGREGATE"
     */
    @Override
    protected String getRecordName() {
        return "ESCHERAGGREGATE";
    }

    // =============== Private methods ========================

    /**
     * create base tree with such structure:
     * EscherDgContainer
     * -EscherSpgrContainer
     * --EscherSpContainer
     * ---EscherSpRecord
     * ---EscherSpgrRecord
     * ---EscherSpRecord
     * -EscherDgRecord
     *
     * id of DgRecord and SpRecord are empty and must be set later by HSSFPatriarch
     */
    private void buildBaseTree() {
        EscherContainerRecord dgContainer = new EscherContainerRecord();
        EscherContainerRecord spgrContainer = new EscherContainerRecord();
        EscherContainerRecord spContainer1 = new EscherContainerRecord();
        EscherSpgrRecord spgr = new EscherSpgrRecord();
        EscherSpRecord sp1 = new EscherSpRecord();
        dgContainer.setRecordId(EscherContainerRecord.DG_CONTAINER);
        dgContainer.setOptions((short) 0x000F);
        EscherDgRecord dg = new EscherDgRecord();
        dg.setRecordId(EscherDgRecord.RECORD_ID);
        short dgId = 1;
        dg.setOptions((short) (dgId << 4));
        dg.setNumShapes(0);
        dg.setLastMSOSPID(1024);
        spgrContainer.setRecordId(EscherContainerRecord.SPGR_CONTAINER);
        spgrContainer.setOptions((short) 0x000F);
        spContainer1.setRecordId(EscherContainerRecord.SP_CONTAINER);
        spContainer1.setOptions((short) 0x000F);
        spgr.setRecordId(EscherSpgrRecord.RECORD_ID);
        spgr.setOptions((short) 0x0001);    // version
        spgr.setRectX1(0);
        spgr.setRectY1(0);
        spgr.setRectX2(1023);
        spgr.setRectY2(255);
        sp1.setRecordId(EscherSpRecord.RECORD_ID);

        sp1.setOptions((short) 0x0002);
        sp1.setVersion((short) 0x2);
        sp1.setShapeId(-1);
        sp1.setFlags(EscherSpRecord.FLAG_GROUP | EscherSpRecord.FLAG_PATRIARCH);
        dgContainer.addChildRecord(dg);
        dgContainer.addChildRecord(spgrContainer);
        spgrContainer.addChildRecord(spContainer1);
        spContainer1.addChildRecord(spgr);
        spContainer1.addChildRecord(sp1);
        addEscherRecord(dgContainer);
    }

    /**
     * EscherDgContainer
     * -EscherSpgrContainer
     * -EscherDgRecord - set id for this record
     * set id for DgRecord of DgContainer
     * @param dgId - id which must be set
     */
    public void setDgId(short dgId) {
        EscherContainerRecord dgContainer = getEscherContainer();
        EscherDgRecord dg = dgContainer.getChildById(EscherDgRecord.RECORD_ID);
        if (dg != null) {
            dg.setOptions((short) (dgId << 4));
        }
    }

    /**
     * EscherDgContainer
     * -EscherSpgrContainer
     * --EscherSpContainer
     * ---EscherSpRecord -set id for this record
     * ---***
     * --***
     * -EscherDgRecord
     * set id for the sp record of the first spContainer in main spgrConatiner
     * @param shapeId - id which must be set
     */
    public void setMainSpRecordId(int shapeId) {
        EscherContainerRecord dgContainer = getEscherContainer();
        EscherContainerRecord spgrContainer = dgContainer.getChildById(EscherContainerRecord.SPGR_CONTAINER);
        if (spgrContainer != null) {
            EscherContainerRecord spContainer = (EscherContainerRecord) spgrContainer.getChild(0);
            EscherSpRecord sp = spContainer.getChildById(EscherSpRecord.RECORD_ID);
            if (sp != null) {
                sp.setShapeId(shapeId);
            }
        }
    }

    /**
     * @param record the record to look into
     * @return sid of the record
     */
    private static short sid(RecordBase record) {
        // Aggregates don't have a sid
        // We could step into them, but for these needs we don't care
        return (record instanceof org.apache.poi.hssf.record.Record rec)
            ? rec.getSid()
            : -1;
    }

    /**
     * @return unmodifiable copy of the mapping  of {@link EscherClientDataRecord} and {@link EscherTextboxRecord}
     * to their {@link TextObjectRecord} or {@link ObjRecord} .
     * <p>
     * We need to access it outside of EscherAggregate when building shapes
     */
    public Map<EscherRecord, Record> getShapeToObjMapping() {
        return Collections.unmodifiableMap(shapeToObj);
    }

    /**
     * @return unmodifiable copy of tail records. We need to access them when building shapes.
     *         Every HSSFComment shape has a link to a NoteRecord from the tailRec collection.
     */
    public Map<Integer, NoteRecord> getTailRecords() {
        return Collections.unmodifiableMap(tailRec);
    }

    /**
     * @param obj - ObjRecord with id == NoteRecord.id
     * @return null if note record is not found else returns note record with id == obj.id
     */
    public NoteRecord getNoteRecordByObj(ObjRecord obj) {
        CommonObjectDataSubRecord cod = (CommonObjectDataSubRecord) obj.getSubRecords().get(0);
        return tailRec.get(cod.getObjectId());
    }

    /**
     * Add tail record to existing map
     * @param note to be added
     */
    public void addTailRecord(NoteRecord note) {
        tailRec.put(note.getShapeId(), note);
    }

    /**
     * Remove tail record from the existing map
     * @param note to be removed
     */
    public void removeTailRecord(NoteRecord note) {
        tailRec.remove(note.getShapeId());
    }

    @Override
    public EscherAggregate copy() {
        return new EscherAggregate(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.ESCHER_AGGREGATE;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return null;
    }
}

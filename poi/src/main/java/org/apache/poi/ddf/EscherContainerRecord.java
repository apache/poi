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

package org.apache.poi.ddf;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;

import static org.apache.logging.log4j.util.Unbox.box;

/**
 * Escher container records store other escher records as children.
 * The container records themselves never store any information beyond
 * the standard header used by all escher records.  This one record is
 * used to represent many different types of records.
 */
public final class EscherContainerRecord extends EscherRecord implements Iterable<EscherRecord> {
    public static final short DGG_CONTAINER    = EscherRecordTypes.DGG_CONTAINER.typeID;
    public static final short BSTORE_CONTAINER = EscherRecordTypes.BSTORE_CONTAINER.typeID;
    public static final short DG_CONTAINER     = EscherRecordTypes.DG_CONTAINER.typeID;
    public static final short SPGR_CONTAINER   = EscherRecordTypes.SPGR_CONTAINER.typeID;
    public static final short SP_CONTAINER     = EscherRecordTypes.SP_CONTAINER.typeID;
    public static final short SOLVER_CONTAINER = EscherRecordTypes.SOLVER_CONTAINER.typeID;

    private static final Logger LOGGER = LogManager.getLogger(EscherContainerRecord.class);

    /**
     * in case if document contains any charts we have such document structure:
     * BOF
     * ...
     * DrawingRecord
     * ...
     * ObjRecord|TxtObjRecord
     * ...
     * EOF
     * ...
     * BOF(Chart begin)
     * ...
     * DrawingRecord
     * ...
     * ObjRecord|TxtObjRecord
     * ...
     * EOF
     * So, when we call EscherAggregate.createAggregate() we have not all needed data.
     * When we got warning "WARNING: " + bytesRemaining + " bytes remaining but no space left"
     * we should save value of bytesRemaining
     * and add it to container size when we serialize it
     */
    private int _remainingLength;

    private final List<EscherRecord> _childRecords = new ArrayList<>();

    public EscherContainerRecord() {}

    public EscherContainerRecord(EscherContainerRecord other) {
        super(other);
        _remainingLength = other._remainingLength;
        other._childRecords.stream().map(EscherRecord::copy).forEach(_childRecords::add);
    }

    @Override
    public int fillFields(byte[] data, int pOffset, EscherRecordFactory recordFactory) {
        int bytesRemaining = readHeader(data, pOffset);
        int bytesWritten = 8;
        int offset = pOffset + 8;
        while (bytesRemaining > 0 && offset < data.length) {
            EscherRecord child = recordFactory.createRecord(data, offset);
            int childBytesWritten = child.fillFields(data, offset, recordFactory);
            bytesWritten += childBytesWritten;
            offset += childBytesWritten;
            bytesRemaining -= childBytesWritten;
            addChildRecord(child);
            if (offset >= data.length && bytesRemaining > 0) {
                _remainingLength = bytesRemaining;
                LOGGER.atWarn().log("Not enough Escher data: {} bytes remaining but no space left", box(bytesRemaining));
            }
        }
        return bytesWritten;
    }

    @Override
    public int serialize( int offset, byte[] data, EscherSerializationListener listener )
    {
        listener.beforeRecordSerialize( offset, getRecordId(), this );

        LittleEndian.putShort(data, offset, getOptions());
        LittleEndian.putShort(data, offset+2, getRecordId());
        int remainingBytes = 0;
        for (EscherRecord r : this) {
            remainingBytes += r.getRecordSize();
        }
        remainingBytes += _remainingLength;
        LittleEndian.putInt(data, offset+4, remainingBytes);
        int pos = offset+8;
        for (EscherRecord r : this) {
            pos += r.serialize(pos, data, listener );
        }

        listener.afterRecordSerialize( pos, getRecordId(), pos - offset, this );
        return pos - offset;
    }

    @Override
    public int getRecordSize() {
        int childRecordsSize = 0;
        for (EscherRecord r : this) {
            childRecordsSize += r.getRecordSize();
        }
        return 8 + childRecordsSize;
    }

    /**
     * Do any of our (top level) children have the given recordId?
     *
     * @param recordId the recordId of the child
     *
     * @return true, if any child has the given recordId
     */
    public boolean hasChildOfType(short recordId) {
        return _childRecords.stream().anyMatch(r -> r.getRecordId() == recordId);
    }

    @Override
    public EscherRecord getChild( int index ) {
        return _childRecords.get(index);
    }

    /**
     * @return a copy of the list of all the child records of the container.
     */
    @Override
    public List<EscherRecord> getChildRecords() {
        return new ArrayList<>(_childRecords);
    }

    public int getChildCount() {
        return _childRecords.size();
    }

    /**
     * @return an iterator over the child records
     */
    @Override
    public Iterator<EscherRecord> iterator() {
        return Collections.unmodifiableList(_childRecords).iterator();
    }

    /**
     * @return a spliterator over the child records
     *
     * @since POI 5.2.0
     */
    @Override
    public Spliterator<EscherRecord> spliterator() {
        return _childRecords.spliterator();
    }

    /**
     * replaces the internal child list with the contents of the supplied {@code childRecords}
     */
    @Override
    public void setChildRecords(List<EscherRecord> childRecords) {
        if (childRecords == _childRecords) {
            throw new IllegalStateException("Child records private data member has escaped");
        }
        _childRecords.clear();
        _childRecords.addAll(childRecords);
    }

    /**
     * Removes the given escher record from the child list
     *
     * @param toBeRemoved the escher record to be removed
     * @return true, if the record was found and removed
     */
    public boolean removeChildRecord(EscherRecord toBeRemoved) {
        return _childRecords.remove(toBeRemoved);
    }



    /**
     * Returns all of our children which are also
     * EscherContainers (may be 0, 1, or vary rarely 2 or 3)
     *
     * @return EscherContainer children
     */
    public List<EscherContainerRecord> getChildContainers() {
        List<EscherContainerRecord> containers = new ArrayList<>();
        for (EscherRecord r : this) {
            if(r instanceof EscherContainerRecord) {
                containers.add((EscherContainerRecord) r);
            }
        }
        return containers;
    }

    @Override
    public String getRecordName() {
        final short id = getRecordId();
        EscherRecordTypes t = EscherRecordTypes.forTypeID(id);
        return (t != EscherRecordTypes.UNKNOWN) ? t.recordName : "Container 0x" + HexDump.toHex(id);
    }

    @Override
    public void display(PrintWriter w, int indent) {
        super.display(w, indent);
        for (EscherRecord escherRecord : this) {
            escherRecord.display(w, indent + 1);
        }
    }

    /**
     * Append a child record
     *
     * @param record the record to be added
     */
    public void addChildRecord(EscherRecord record) {
        _childRecords.add(record);
    }

    /**
     * Add a child record before the record with given recordId
     *
     * @param record the record to be added
     * @param insertBeforeRecordId the recordId of the next sibling
     */
    public void addChildBefore(EscherRecord record, int insertBeforeRecordId) {
        int idx = 0;
        for (EscherRecord rec : this) {
            if(rec.getRecordId() == (short)insertBeforeRecordId) {
                break;
            }
            // TODO - keep looping? Do we expect multiple matches?
            idx++;
        }
        _childRecords.add(idx, record);
    }

    public <T extends EscherRecord> T getChildById( short recordId ) {
        for ( EscherRecord childRecord : this ) {
            if ( childRecord.getRecordId() == recordId ) {
                @SuppressWarnings( "unchecked" )
                final T result = (T) childRecord;
                return result;
            }
        }
        return null;
    }

    /**
     * Recursively find records with the specified record ID
     *
     * @param recordId the recordId to be searched for
     * @param out - list to store found records
     */
    public void getRecordsById(short recordId, List<EscherRecord> out){
        for (EscherRecord r : this) {
            if(r instanceof EscherContainerRecord) {
                EscherContainerRecord c = (EscherContainerRecord)r;
                c.getRecordsById(recordId, out );
            } else if (r.getRecordId() == recordId){
                out.add(r);
            }
        }
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "base", super::getGenericProperties,
            "isContainer", this::isContainerRecord
        );
    }

    @Override
    public Enum getGenericRecordType() {
        return EscherRecordTypes.forTypeID(getRecordId());
    }

    @Override
    public EscherContainerRecord copy() {
        return new EscherContainerRecord(this);
    }
}

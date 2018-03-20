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

import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Escher container records store other escher records as children.
 * The container records themselves never store any information beyond
 * the standard header used by all escher records.  This one record is
 * used to represent many different types of records.
 */
public final class EscherContainerRecord extends EscherRecord implements Iterable<EscherRecord> {
    public static final short DGG_CONTAINER    = (short)0xF000;
    public static final short BSTORE_CONTAINER = (short)0xF001;
    public static final short DG_CONTAINER     = (short)0xF002;
    public static final short SPGR_CONTAINER   = (short)0xF003;
    public static final short SP_CONTAINER     = (short)0xF004;
    public static final short SOLVER_CONTAINER = (short)0xF005;

    private static final POILogger log = POILogFactory.getLogger(EscherContainerRecord.class);

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
                if (log.check(POILogger.WARN)) {
                    log.log(POILogger.WARN, "Not enough Escher data: " + bytesRemaining + " bytes remaining but no space left");
                }
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
        for (EscherRecord r : this) {
            if(r.getRecordId() == recordId) {
                return true;
            }
        }
        return false;
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

    /**
     * @return an iterator over the child records
     */
    @Override
    public Iterator<EscherRecord> iterator() {
        return Collections.unmodifiableList(_childRecords).iterator();
    }

    /**
     * replaces the internal child list with the contents of the supplied <tt>childRecords</tt>
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
        switch (getRecordId()) {
            case DGG_CONTAINER:
                return "DggContainer";
            case BSTORE_CONTAINER:
                return "BStoreContainer";
            case DG_CONTAINER:
                return "DgContainer";
            case SPGR_CONTAINER:
                return "SpgrContainer";
            case SP_CONTAINER:
                return "SpContainer";
            case SOLVER_CONTAINER:
                return "SolverContainer";
            default:
                return "Container 0x" + HexDump.toHex(getRecordId());
        }
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
    protected Object[][] getAttributeMap() {
        List<Object> chList = new ArrayList<>(_childRecords.size() * 2 + 2);
        chList.add("children");
        chList.add(_childRecords.size());
        int count = 0;
        for ( EscherRecord record : this ) {
            chList.add("Child "+count);
            chList.add(record);
            count++;
        }
        return new Object[][] {
        	{ "isContainer", isContainerRecord() },
            chList.toArray()
        };
    }
}

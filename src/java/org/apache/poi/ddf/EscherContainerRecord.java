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

import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.io.PrintWriter;

/**
 * Escher container records store other escher records as children.
 * The container records themselves never store any information beyond
 * the standard header used by all escher records.  This one record is
 * used to represent many different types of records.
 *
 * @author Glen Stampoultzis
 */
public final class EscherContainerRecord extends EscherRecord {
    public static final short DGG_CONTAINER    = (short)0xF000;
    public static final short BSTORE_CONTAINER = (short)0xF001;
    public static final short DG_CONTAINER     = (short)0xF002;
    public static final short SPGR_CONTAINER   = (short)0xF003;
    public static final short SP_CONTAINER     = (short)0xF004;
    public static final short SOLVER_CONTAINER = (short)0xF005;

    private final List<EscherRecord> _childRecords = new ArrayList<EscherRecord>();

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
                System.out.println("WARNING: " + bytesRemaining + " bytes remaining but no space left");
            }
        }
        return bytesWritten;
    }

    public int serialize( int offset, byte[] data, EscherSerializationListener listener )
    {
        listener.beforeRecordSerialize( offset, getRecordId(), this );

        LittleEndian.putShort(data, offset, getOptions());
        LittleEndian.putShort(data, offset+2, getRecordId());
        int remainingBytes = 0;
        Iterator<EscherRecord> iterator = _childRecords.iterator();
        while (iterator.hasNext()) {
            EscherRecord r = iterator.next();
            remainingBytes += r.getRecordSize();
        }
        LittleEndian.putInt(data, offset+4, remainingBytes);
        int pos = offset+8;
        iterator = _childRecords.iterator();
        while (iterator.hasNext()) {
            EscherRecord r = iterator.next();
            pos += r.serialize(pos, data, listener );
        }

        listener.afterRecordSerialize( pos, getRecordId(), pos - offset, this );
        return pos - offset;
    }

    public int getRecordSize() {
        int childRecordsSize = 0;
        Iterator<EscherRecord> iterator = _childRecords.iterator();
        while (iterator.hasNext()) {
            EscherRecord r = iterator.next();
            childRecordsSize += r.getRecordSize();
        }
        return 8 + childRecordsSize;
    }

    /**
     * Do any of our (top level) children have the
     *  given recordId?
     */
    public boolean hasChildOfType(short recordId) {
        Iterator<EscherRecord> iterator = _childRecords.iterator();
        while (iterator.hasNext()) {
            EscherRecord r = iterator.next();
            if(r.getRecordId() == recordId) {
                return true;
            }
        }
        return false;
    }
    public EscherRecord getChild( int index ) {
        return _childRecords.get(index);
    }

    /**
     * @return a copy of the list of all the child records of the container.
     */
    public List<EscherRecord> getChildRecords() {
        return new ArrayList<EscherRecord>(_childRecords);
    }

    public Iterator<EscherRecord> getChildIterator() {
        return new ReadOnlyIterator(_childRecords);
    }
    private static final class ReadOnlyIterator implements Iterator<EscherRecord> {
        private final List<EscherRecord> _list;
        private int _index;

        public ReadOnlyIterator(List<EscherRecord> list) {
            _list = list;
            _index = 0;
        }

        public boolean hasNext() {
            return _index < _list.size();
        }
        public EscherRecord next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return _list.get(_index++);
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    /**
     * replaces the internal child list with the contents of the supplied <tt>childRecords</tt>
     */
    public void setChildRecords(List<EscherRecord> childRecords) {
        if (childRecords == _childRecords) {
            throw new IllegalStateException("Child records private data member has escaped");
        }
        _childRecords.clear();
        _childRecords.addAll(childRecords);
    }

    public boolean removeChildRecord(EscherRecord toBeRemoved) {
        return _childRecords.remove(toBeRemoved);
    }



    /**
     * Returns all of our children which are also
     *  EscherContainers (may be 0, 1, or vary rarely
     *   2 or 3)
     */
    public List<EscherContainerRecord> getChildContainers() {
        List<EscherContainerRecord> containers = new ArrayList<EscherContainerRecord>();
        Iterator<EscherRecord> iterator = _childRecords.iterator();
        while (iterator.hasNext()) {
            EscherRecord r = iterator.next();
            if(r instanceof EscherContainerRecord) {
                containers.add((EscherContainerRecord) r);
            }
        }
        return containers;
    }

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

    public void display(PrintWriter w, int indent) {
        super.display(w, indent);
        for (Iterator<EscherRecord> iterator = _childRecords.iterator(); iterator.hasNext();)
        {
            EscherRecord escherRecord = iterator.next();
            escherRecord.display(w, indent + 1);
        }
    }

    public void addChildRecord(EscherRecord record) {
        _childRecords.add(record);
    }

    public void addChildBefore(EscherRecord record, int insertBeforeRecordId) {
        for (int i = 0; i < _childRecords.size(); i++) {
            EscherRecord rec = _childRecords.get(i);
            if(rec.getRecordId() == insertBeforeRecordId){
                _childRecords.add(i++, record);
                // TODO - keep looping? Do we expect multiple matches?
            }
        }
    }

    public String toString()
    {
        return toString("");
    }
    public String toString(String indent)
    {
        String nl = System.getProperty( "line.separator" );

        StringBuffer children = new StringBuffer();
        if (_childRecords.size() > 0) {
            children.append( "  children: " + nl );

            int count = 0;
            for ( Iterator<EscherRecord> iterator = _childRecords.iterator(); iterator.hasNext(); )
            {
                String newIndent = indent + "   ";

                EscherRecord record = iterator.next();
                children.append(newIndent + "Child " + count + ":" + nl);

                if(record instanceof EscherContainerRecord) {
                    EscherContainerRecord ecr = (EscherContainerRecord)record;
                    children.append( ecr.toString(newIndent));
                } else {
                    children.append( record.toString() );
                }
                count++;
            }
        }

        return
            indent + getClass().getName() + " (" + getRecordName() + "):" + nl +
            indent + "  isContainer: " + isContainerRecord() + nl +
            indent + "  options: 0x" + HexDump.toHex( getOptions() ) + nl +
            indent + "  recordId: 0x" + HexDump.toHex( getRecordId() ) + nl +
            indent + "  numchildren: " + _childRecords.size() + nl +
            indent + children.toString();

    }

    public EscherSpRecord getChildById(short recordId) {
        Iterator<EscherRecord> iterator = _childRecords.iterator();
        while (iterator.hasNext()) {
            EscherRecord r = iterator.next();
            if (r.getRecordId() == recordId)
                return (EscherSpRecord) r;
        }
        return null;
    }

    /**
     * Recursively find records with the specified record ID
     *
     * @param out - list to store found records
     */
    public void getRecordsById(short recordId, List<EscherRecord> out){
        Iterator<EscherRecord> iterator = _childRecords.iterator();
        while (iterator.hasNext()) {
            EscherRecord r = iterator.next();
            if(r instanceof EscherContainerRecord) {
                EscherContainerRecord c = (EscherContainerRecord)r;
                c.getRecordsById(recordId, out );
            } else if (r.getRecordId() == recordId){
                out.add(r);
            }
        }
    }
}

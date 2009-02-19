
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
import java.io.PrintWriter;

/**
 * Escher container records store other escher records as children.
 * The container records themselves never store any information beyond
 * the standard header used by all escher records.  This one record is
 * used to represent many different types of records.
 *
 * @author Glen Stampoultzis
 */
public class EscherContainerRecord extends EscherRecord
{
    public static final short DGG_CONTAINER    = (short)0xF000;
    public static final short BSTORE_CONTAINER = (short)0xF001;
    public static final short DG_CONTAINER     = (short)0xF002;
    public static final short SPGR_CONTAINER   = (short)0xF003;
    public static final short SP_CONTAINER     = (short)0xF004;
    public static final short SOLVER_CONTAINER = (short)0xF005;

    private final List<EscherRecord> _childRecords = new ArrayList<EscherRecord>();

    public int fillFields( byte[] data, int offset, EscherRecordFactory recordFactory )
    {
        int bytesRemaining = readHeader( data, offset );
        int bytesWritten = 8;
        offset += 8;
        while ( bytesRemaining > 0 && offset < data.length )
        {
            EscherRecord child = recordFactory.createRecord(data, offset);
            int childBytesWritten = child.fillFields( data, offset, recordFactory );
            bytesWritten += childBytesWritten;
            offset += childBytesWritten;
            bytesRemaining -= childBytesWritten;
            getChildRecords().add( child );
            if (offset >= data.length && bytesRemaining > 0)
            {
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
        for ( Iterator iterator = getChildRecords().iterator(); iterator.hasNext(); )
        {
            EscherRecord r = (EscherRecord) iterator.next();
            remainingBytes += r.getRecordSize();
        }
        LittleEndian.putInt(data, offset+4, remainingBytes);
        int pos = offset+8;
        for ( Iterator iterator = getChildRecords().iterator(); iterator.hasNext(); )
        {
            EscherRecord r = (EscherRecord) iterator.next();
            pos += r.serialize(pos, data, listener );
        }

        listener.afterRecordSerialize( pos, getRecordId(), pos - offset, this );
        return pos - offset;
    }

    public int getRecordSize()
    {
        int childRecordsSize = 0;
        for ( Iterator iterator = getChildRecords().iterator(); iterator.hasNext(); )
        {
            EscherRecord r = (EscherRecord) iterator.next();
            childRecordsSize += r.getRecordSize();
        }
        return 8 + childRecordsSize;
    }
    
    /**
     * Do any of our (top level) children have the
     *  given recordId?
     */
    public boolean hasChildOfType(short recordId) {
        for ( Iterator iterator = getChildRecords().iterator(); iterator.hasNext(); )
        {
            EscherRecord r = (EscherRecord) iterator.next();
            if(r.getRecordId() == recordId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of all the child (escher) records
     *  of the container.
     */
    public List<EscherRecord> getChildRecords() {
        return _childRecords;
    }
    
    /**
     * Returns all of our children which are also
     *  EscherContainers (may be 0, 1, or vary rarely
     *   2 or 3)
     */
    public List getChildContainers() {
        List containers = new ArrayList();
        for ( Iterator iterator = getChildRecords().iterator(); iterator.hasNext(); )
        {
            EscherRecord r = (EscherRecord) iterator.next();
            if(r instanceof EscherContainerRecord) {
            	containers.add(r);
            }
        }
        return containers;
    }

    public void setChildRecords(List<EscherRecord> childRecords) {
        _childRecords.clear();
        _childRecords.addAll(childRecords);
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

    public void display( PrintWriter w, int indent )
    {
        super.display( w, indent );
        for (Iterator iterator = _childRecords.iterator(); iterator.hasNext();)
        {
            EscherRecord escherRecord = (EscherRecord) iterator.next();
            escherRecord.display( w, indent + 1 );
        }
    }

    public void addChildRecord(EscherRecord record) {
        _childRecords.add( record );
    }

    public String toString()
    {
    	return toString("");
    }
    public String toString(String indent)
    {
        String nl = System.getProperty( "line.separator" );

        StringBuffer children = new StringBuffer();
        if ( getChildRecords().size() > 0 )
        {
            children.append( "  children: " + nl );
            
            int count = 0;
            for ( Iterator iterator = getChildRecords().iterator(); iterator.hasNext(); )
            {
            	String newIndent = indent + "   ";
            	
                EscherRecord record = (EscherRecord) iterator.next();
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
            indent + "  numchildren: " + getChildRecords().size() + nl +
            indent + children.toString();

    }

    public EscherSpRecord getChildById( short recordId )
    {
        for ( Iterator iterator = _childRecords.iterator(); iterator.hasNext(); )
        {
            EscherRecord escherRecord = (EscherRecord) iterator.next();
            if (escherRecord.getRecordId() == recordId)
                return (EscherSpRecord) escherRecord;
        }
        return null;
    }

    /**
     * Recursively find records with the specified record ID
     *
     * @param out - list to store found records
     */
    public void getRecordsById(short recordId, List out){
        for(Iterator it = _childRecords.iterator(); it.hasNext();) {
            Object er = it.next();
            EscherRecord r = (EscherRecord)er;
            if(r instanceof EscherContainerRecord) {
                EscherContainerRecord c = (EscherContainerRecord)r;
                c.getRecordsById(recordId, out );
            } else if (r.getRecordId() == recordId){
                out.add(er);
            }
        }
    }

}

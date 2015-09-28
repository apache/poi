
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

import org.apache.poi.util.LittleEndianOutput;

/**
 * Title: Object Protect Record<P>
 * Description: Protect embedded object with the lamest "security" ever invented.  
 * This record tells  "I want to protect my objects" with lame security.  It 
 * appears in conjunction with the PASSWORD and PROTECT records as well as its 
 * scenario protect cousin.<P>
 * REFERENCE:  PG 368 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */

public final class ObjectProtectRecord extends StandardRecord implements Cloneable {
    public final static short sid = 0x63;
    private short             field_1_protect;

    public ObjectProtectRecord()
    {
    }

    public ObjectProtectRecord(RecordInputStream in)
    {
        field_1_protect = in.readShort();
    }

    /**
     * set whether the sheet is protected or not
     * @param protect whether to protect the sheet or not
     */

    public void setProtect(boolean protect)
    {
        if (protect)
        {
            field_1_protect = 1;
        }
        else
        {
            field_1_protect = 0;
        }
    }

    /**
     * get whether the sheet is protected or not
     * @return whether to protect the sheet or not
     */

    public boolean getProtect()
    {
        return (field_1_protect == 1);
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[SCENARIOPROTECT]\n");
	    buffer.append("    .protect         = ").append(getProtect())
            .append("\n");
        buffer.append("[/SCENARIOPROTECT]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_protect);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public ObjectProtectRecord clone() {
        ObjectProtectRecord rec = new ObjectProtectRecord();
        rec.field_1_protect = field_1_protect;
        return rec;
    }
}

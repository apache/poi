
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
 * Title:        Protection Revision 4 Record<P>
 * Description:  describes whether this is a protected shared/tracked workbook<P>
 *  ( HSSF does not support encryption because we don't feel like going to jail ) <P>
 * REFERENCE:  PG 373 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public final class ProtectionRev4Record
    extends StandardRecord
{
    public final static short sid = 0x1af;
    private short             field_1_protect;

    public ProtectionRev4Record()
    {
    }

    public ProtectionRev4Record(RecordInputStream in)
    {
        field_1_protect = in.readShort();
    }

    /**
     * set whether the this is protected shared/tracked workbook or not
     * @param protect  whether to protect the workbook or not
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
     * get whether the this is protected shared/tracked workbook or not
     * @return whether to protect the workbook or not
     */

	public boolean getProtect()
	{
		return (field_1_protect == 1);
	}

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[PROT4REV]\n");
	    buffer.append("    .protect         = ").append(getProtect())
            .append("\n");
        buffer.append("[/PROT4REV]\n");
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
}

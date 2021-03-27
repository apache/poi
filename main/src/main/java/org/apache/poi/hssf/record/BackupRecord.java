
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

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Boolean specifying whether the GUI should store a backup of the file.
 *
 * @version 2.0-pre
 */

public final class BackupRecord extends StandardRecord {
    public static final short sid = 0x40;

    private short field_1_backup;

    public BackupRecord() {}

    public BackupRecord(BackupRecord other) {
        super(other);
        field_1_backup = other.field_1_backup;
    }

    public BackupRecord(RecordInputStream in) {
        field_1_backup = in.readShort();
    }

    /**
     * set the backup flag (0,1)
     *
     * @param backup    backup flag
     */

    public void setBackup(short backup)
    {
        field_1_backup = backup;
    }

    /**
     * get the backup flag
     *
     * @return short 0/1 (off/on)
     */

    public short getBackup()
    {
        return field_1_backup;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(getBackup());
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public BackupRecord copy() {
        return new BackupRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.BACKUP;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "backup", this::getBackup
        );
    }
}

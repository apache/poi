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

package org.apache.poi.hsmf.datatypes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.util.LittleEndian;

/**
 * A {@link PropertiesChunk} for a Storage Properties, such as Attachments and
 * Recipients. This only has a 8 byte header
 */
public class StoragePropertiesChunk extends PropertiesChunk {
    public StoragePropertiesChunk(ChunkGroup parentGroup) {
        super(parentGroup);
    }

    @Override
    public void readValue(InputStream stream) throws IOException {
        // 8 bytes of reserved zeros
        LittleEndian.readLong(stream);

        // Now properties
        readProperties(stream);
    }

    @Override
    public void writeValue(OutputStream out) throws IOException {
        // 8 bytes of reserved zeros
        out.write(new byte[8]);

        // Now properties
        writeProperties(out);
    }
}
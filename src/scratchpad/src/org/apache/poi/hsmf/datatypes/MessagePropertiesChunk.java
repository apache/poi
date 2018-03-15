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
import java.util.List;

import org.apache.poi.util.LittleEndian;

/**
 * A {@link PropertiesChunk} for a Message or Embedded-Message. This has a 32
 * byte header
 */
public class MessagePropertiesChunk extends PropertiesChunk {
    private boolean isEmbedded;
    private long nextRecipientId;
    private long nextAttachmentId;
    private long recipientCount;
    private long attachmentCount;

    public MessagePropertiesChunk(ChunkGroup parentGroup) {
        super(parentGroup);
    }

    public MessagePropertiesChunk(ChunkGroup parentGroup, boolean isEmbedded) {
      super(parentGroup);
      this.isEmbedded = isEmbedded;
    }

    public long getNextRecipientId() {
        return nextRecipientId;
    }

    public long getNextAttachmentId() {
        return nextAttachmentId;
    }

    public long getRecipientCount() {
        return recipientCount;
    }

    public long getAttachmentCount() {
        return attachmentCount;
    }
    
    public void setNextRecipientId(long nextRecipientId) {
      this.nextRecipientId = nextRecipientId;
    }
    
    public void setNextAttachmentId(long nextAttachmentId) {
      this.nextAttachmentId = nextAttachmentId;
    }

    public void setRecipientCount(long recipientCount) {
      this.recipientCount = recipientCount;
    }

    public void setAttachmentCount(long attachmentCount) {
      this.attachmentCount = attachmentCount;
    }

    @Override
    public void readValue(InputStream stream) throws IOException {
        // 8 bytes of reserved zeros
        LittleEndian.readLong(stream);

        // Nexts and counts
        nextRecipientId = LittleEndian.readUInt(stream);
        nextAttachmentId = LittleEndian.readUInt(stream);
        recipientCount = LittleEndian.readUInt(stream);
        attachmentCount = LittleEndian.readUInt(stream);

        if (!isEmbedded) {
          // 8 bytes of reserved zeros (top level properties stream only)
          LittleEndian.readLong(stream);
        }

        // Now properties
        readProperties(stream);
    }

    @Override
    protected List<PropertyValue> writeHeaderData(OutputStream out) throws IOException
    {
      // 8 bytes of reserved zeros
      out.write(new byte[8]);
      // Nexts and counts
      LittleEndian.putUInt(nextRecipientId, out);
      LittleEndian.putUInt(nextAttachmentId, out);
      LittleEndian.putUInt(recipientCount, out);
      LittleEndian.putUInt(attachmentCount, out);
      // 8 bytes of reserved zeros
      if (!isEmbedded) {
        out.write(new byte[8]);
      }
      // Now properties.
      return super.writeHeaderData(out);
    }

    @Override
    public void writeValue(OutputStream out) throws IOException {
    }
}

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

package org.apache.poi.hwpf.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

import org.apache.poi.hwpf.model.io.HWPFOutputStream;

/**
 * String table containing the history of the last few revisions ("saves") of the document.
 * Read-only for the time being.
 *
 * @author Daniel Noll
 */
public final class SavedByTable
{
  /**
   * A value that I don't know what it does, but is maintained for accuracy.
   */
  private short unknownValue = -1;

  /**
   * Array of entries.
   */
  private SavedByEntry[] entries;

  /**
   * Constructor to read the table from the table stream.
   *
   * @param tableStream the table stream.
   * @param offset the offset into the byte array.
   * @param size the size of the table in the byte array.
   */
  public SavedByTable(byte[] tableStream, int offset, int size)
  {
    // Read the value that I don't know what it does. :-)
    unknownValue = LittleEndian.getShort(tableStream, offset);
    offset += 2;

    // The stored int is the number of strings, and there are two strings per entry.
    int numEntries = LittleEndian.getInt(tableStream, offset) / 2;
    offset += 4;

    entries = new SavedByEntry[numEntries];
    for (int i = 0; i < numEntries; i++)
    {
      int len = LittleEndian.getShort(tableStream, offset);
      offset += 2;
      String userName = StringUtil.getFromUnicodeLE(tableStream, offset, len);
      offset += len * 2;
      len = LittleEndian.getShort(tableStream, offset);
      offset += 2;
      String saveLocation = StringUtil.getFromUnicodeLE(tableStream, offset, len);
      offset += len * 2;

      entries[i] = new SavedByEntry(userName, saveLocation);
    }
  }

  /**
   * Gets the entries.  The returned list cannot be modified.
   *
   * @return the list of entries.
   */
  public List getEntries()
  {
    return Collections.unmodifiableList(Arrays.asList(entries));
  }

  /**
   * Writes this table to the table stream.
   *
   * @param tableStream the table stream to write to.
   * @throws IOException if an error occurs while writing.
   */
  public void writeTo(HWPFOutputStream tableStream)
    throws IOException
  {
    byte[] header = new byte[6];
    LittleEndian.putShort(header, 0, unknownValue);
    LittleEndian.putInt(header, 2, entries.length * 2);
    tableStream.write(header);

    for (int i = 0; i < entries.length; i++)
    {
      writeStringValue(tableStream, entries[i].getUserName());
      writeStringValue(tableStream, entries[i].getSaveLocation());
    }
  }

  private void writeStringValue(HWPFOutputStream tableStream, String value)
    throws IOException
  {
    byte[] buf = new byte[value.length() * 2 + 2];
    LittleEndian.putShort(buf, 0, (short) value.length());
    StringUtil.putUnicodeLE(value, buf, 2);
    tableStream.write(buf);
  }
}


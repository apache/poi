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

import java.text.MessageFormat;

import org.apache.poi.util.HexDump;

/**
 * Class for the FLD structure.
 * 
 * @author Cedric Bosdonnat <cbosdonnat@novell.com>
 *
 */
public final class FieldDescriptor
{
  public static final int FIELD_BEGIN_MARK = 0x13;
  public static final int FIELD_SEPARATOR_MARK = 0x14;
  public static final int FIELD_END_MARK = 0x15;

  private static final short BOUNDARY_MASK = 0x1F;
  private static final short TYPE_MASK = 0xFF;
  private static final short ZOMBIE_EMBED_MASK = 0x02;
  private static final short RESULT_DIRTY_MASK = 0x04;
  private static final short RESULT_EDITED_MASK = 0x08;
  private static final short LOCKED_MASK = 0x10;
  private static final short PRIVATE_RESULT_MASK = 0x20;
  private static final short NESTED_MASK = 0x40;
  private static final short HAS_SEP_MASK = 0x80;

  byte _fieldBoundaryType;
  byte _info;

  private int fieldType;
  private boolean zombieEmbed;
  private boolean resultDirty;
  private boolean resultEdited;
  private boolean locked;
  private boolean privateResult;
  private boolean nested;
  private boolean hasSep;

  public FieldDescriptor(byte[] data)
  {
    _fieldBoundaryType = (byte) (data[0] & BOUNDARY_MASK);
    _info = data[1];

    if (_fieldBoundaryType == FIELD_BEGIN_MARK)
    {
      fieldType = _info & TYPE_MASK;
    } else if (_fieldBoundaryType == FIELD_END_MARK)
    {
      zombieEmbed = ((_info & ZOMBIE_EMBED_MASK) == 1);
      resultDirty = ((_info & RESULT_DIRTY_MASK) == 1);
      resultEdited = ((_info & RESULT_EDITED_MASK) == 1);
      locked = ((_info & LOCKED_MASK) == 1);
      privateResult = ((_info & PRIVATE_RESULT_MASK) == 1);
      nested = ((_info & NESTED_MASK) == 1);
      hasSep = ((_info & HAS_SEP_MASK) == 1);
    }
  }

  public int getBoundaryType()
  {
    return _fieldBoundaryType;
  }

  public int getFieldType()
  {
    if (_fieldBoundaryType != FIELD_BEGIN_MARK)
      throw new UnsupportedOperationException(
          "This field is only defined for begin marks.");
    return fieldType;
  }

  public boolean isZombieEmbed()
  {
    if (_fieldBoundaryType != FIELD_END_MARK)
      throw new UnsupportedOperationException(
          "This field is only defined for end marks.");
    return zombieEmbed;
  }

  public boolean isResultDirty()
  {
    if (_fieldBoundaryType != FIELD_END_MARK)
      throw new UnsupportedOperationException(
          "This field is only defined for end marks.");
    return resultDirty;
  }

  public boolean isResultEdited()
  {
    if (_fieldBoundaryType != FIELD_END_MARK)
      throw new UnsupportedOperationException(
          "This field is only defined for end marks.");
    return resultEdited;
  }

  public boolean isLocked()
  {
    if (_fieldBoundaryType != FIELD_END_MARK)
      throw new UnsupportedOperationException(
          "This field is only defined for end marks.");
    return locked;
  }

  public boolean isPrivateResult()
  {
    if (_fieldBoundaryType != FIELD_END_MARK)
      throw new UnsupportedOperationException(
          "This field is only defined for end marks.");
    return privateResult;
  }

  public boolean isNested()
  {
    if (_fieldBoundaryType != FIELD_END_MARK)
      throw new UnsupportedOperationException(
          "This field is only defined for end marks.");
    return nested;
  }

  public boolean isHasSep()
  {
    if (_fieldBoundaryType != FIELD_END_MARK)
      throw new UnsupportedOperationException(
          "This field is only defined for end marks.");
    return hasSep;
  }

  public String toString()
  {
    String details = new String();
    if (_fieldBoundaryType == FIELD_BEGIN_MARK)
    {
      details = " type: " + fieldType;
    }
    else if (_fieldBoundaryType == FIELD_END_MARK)
    {
      details = " flags: 0x" + HexDump.toHex(_info);
    }

    return MessageFormat.format("FLD - 0x{0}{1}", HexDump
        .toHex((byte) _fieldBoundaryType), details);
  }
}

package org.apache.poi.hwpf.model;

import org.apache.poi.util.BitField;

public class FieldDescriptor
{
  byte _fieldBoundaryType;
  byte _info;
    private final static BitField fZombieEmbed = new BitField(0x02);
    private final static BitField fResultDiry = new BitField(0x04);
    private final static BitField fResultEdited = new BitField(0x08);
    private final static BitField fLocked = new BitField(0x10);
    private final static BitField fPrivateResult = new BitField(0x20);
    private final static BitField fNested = new BitField(0x40);
    private final static BitField fHasSep = new BitField(0x80);


  public FieldDescriptor()
  {
  }
}
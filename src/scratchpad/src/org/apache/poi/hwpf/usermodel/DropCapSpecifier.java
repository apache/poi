package org.apache.poi.hwpf.usermodel;

import org.apache.poi.util.BitField;
import org.apache.poi.util.LittleEndian;

public class DropCapSpecifier
{
  private short _info;
    private static BitField _type = new BitField(0x07);
    private static BitField _lines = new BitField(0xf8);

  public DropCapSpecifier(byte[] buf, int offset)
  {
    this(LittleEndian.getShort(buf, offset));
  }

  public DropCapSpecifier(short info)
  {
    _info = info;
  }

  public short toShort()
  {
    return _info;
  }
}

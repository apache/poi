package org.apache.poi.hwpf.model;

import java.io.IOException;

import org.apache.poi.util.LittleEndian;

import org.apache.poi.hwpf.model.io.HWPFOutputStream;

public class FIBLongHandler
{
  public static final int CBMAC = 0;
  public static final int PRODUCTCREATED = 1;
  public static final int PRODUCTREVISED = 2;
  public static final int CCPTEXT = 3;
  public static final int CCPFTN = 4;
  public static final int CCPHDD = 5;
  public static final int CCPMCR = 6;
  public static final int CCPATN = 7;
  public static final int CCPEDN = 8;
  public static final int CCPTXBX = 9;
  public static final int CCPHDRTXBX = 10;
  public static final int PNFBPCHPFIRST = 11;
  public static final int PNCHPFIRST = 12;
  public static final int CPNBTECHP = 13;
  public static final int PNFBPPAPFIRST = 14;
  public static final int PNPAPFIRST = 15;
  public static final int CPNBTEPAP = 16;
  public static final int PNFBPLVCFIRST = 17;
  public static final int PNLVCFIRST = 18;
  public static final int CPNBTELVC = 19;
  public static final int FCISLANDFIRST = 20;
  public static final int FCISLANDLIM = 21;

  int[] _longs;

  public FIBLongHandler(byte[] mainStream, int offset)
  {
    int longCount = LittleEndian.getShort(mainStream, offset);
    offset += LittleEndian.SHORT_SIZE;
    _longs = new int[longCount];

    for (int x = 0; x < longCount; x++)
    {
      _longs[x] = LittleEndian.getInt(mainStream, offset + (x * LittleEndian.INT_SIZE));
    }
  }

  /**
   * Refers to a 32 bit windows "long" same as a Java int
   * @param longCode
   * @return
   */
  public int getLong(int longCode)
  {
    return _longs[longCode];
  }

  public void setLong(int longCode, int value)
  {
    _longs[longCode] = value;
  }

  void serialize(byte[] mainStream, int offset)
    throws IOException
  {
    LittleEndian.putShort(mainStream, offset, (short)_longs.length);
    offset += LittleEndian.SHORT_SIZE;

    for (int x = 0; x < _longs.length; x++)
    {
      LittleEndian.putInt(mainStream, offset, _longs[x]);
      offset += LittleEndian.INT_SIZE;
    }
  }

  int sizeInBytes()
  {
    return (_longs.length * LittleEndian.INT_SIZE) + LittleEndian.SHORT_SIZE;
  }


}

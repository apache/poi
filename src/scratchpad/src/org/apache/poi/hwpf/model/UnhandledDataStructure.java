package org.apache.poi.hwpf.model;

public class UnhandledDataStructure
{
  byte[] _buf;

  public UnhandledDataStructure(byte[] buf, int offset, int length)
  {
    _buf = new byte[length];
    System.arraycopy(buf, offset, _buf, 0, length);
  }

  byte[] getBuf()
  {
    return _buf;
  }
}

package org.apache.poi.hwpf.model.io;

import java.io.ByteArrayOutputStream;

public class HWPFOutputStream extends ByteArrayOutputStream
{

  int _offset;

  public HWPFOutputStream()
  {
    super();
  }

  public int getOffset()
  {
    return _offset;
  }

  public void reset()
  {
    super.reset();
    _offset = 0;
  }

  public void write(byte[] buf, int off, int len)
  {
    super.write(buf, off, len);
    _offset += len;
  }

  public void write(int b)
  {
    super.write(b);
    _offset++;
  }
}

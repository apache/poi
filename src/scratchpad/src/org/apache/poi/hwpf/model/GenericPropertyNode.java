package org.apache.poi.hwpf.model;

public class GenericPropertyNode
  extends PropertyNode
{
  public GenericPropertyNode(int start, int end, byte[] buf)
  {
    super(start, end, buf);
  }

  public byte[] getBytes()
  {
    return (byte[])_buf;
  }


}

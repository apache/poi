package org.apache.poi.hwpf.model.io;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */


import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class HWPFFileSystem
{
  HashMap _streams = new HashMap();

  public HWPFFileSystem()
  {
    _streams.put("WordDocument", new HWPFOutputStream());
    _streams.put("1Table", new HWPFOutputStream());
  }

  public HWPFOutputStream getStream(String name)
  {
    return (HWPFOutputStream)_streams.get(name);
  }

}

package org.apache.poi.hwpf.model;

import org.apache.poi.hwpf.sprm.SprmBuffer;

import java.lang.ref.SoftReference;

public class CachedPropertyNode
  extends PropertyNode
{
  protected SoftReference _propCache;

  public CachedPropertyNode(int start, int end, SprmBuffer buf)
  {
    super(start, end, buf);
  }

  protected void fillCache(Object ref)
  {
    _propCache = new SoftReference(ref);
  }

  protected Object getCacheContents()
  {
    return _propCache == null ? null : _propCache.get();
  }

  /**
   * @return This property's property in compressed form.
   */
  public SprmBuffer getSprmBuf()
  {
    return (SprmBuffer)_buf;
  }


}

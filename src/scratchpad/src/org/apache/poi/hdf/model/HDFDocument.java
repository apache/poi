package org.apache.poi.hdf.model;

import java.io.InputStream;
import java.io.IOException;

import org.apache.poi.hdf.event.HDFParsingListener;
import org.apache.poi.hdf.event.EventBridge;

public class HDFDocument
{

  HDFObjectModel _model;


  public HDFDocument(InputStream in, HDFParsingListener listener) throws IOException
  {
    EventBridge eb = new EventBridge(listener);
    HDFObjectFactory factory = new HDFObjectFactory(in, eb);
  }
  public HDFDocument(InputStream in) throws IOException
  {
    _model = new HDFObjectModel();
    HDFObjectFactory factory = new HDFObjectFactory(in, _model);
  }
}
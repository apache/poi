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

package org.apache.poi.hdf.model;

import java.io.InputStream;
import java.io.IOException;

import org.apache.poi.hdf.event.HDFParsingListener;
import org.apache.poi.hdf.event.EventBridge;

public final class HDFDocument
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


/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        
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
    _streams.put("Data", new HWPFOutputStream());
  }

  public HWPFOutputStream getStream(String name)
  {
    return (HWPFOutputStream)_streams.get(name);
  }

}

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

package org.apache.poi.hwpf.model.io;

import java.io.ByteArrayOutputStream;

public final class HWPFOutputStream extends ByteArrayOutputStream
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

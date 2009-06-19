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

package org.apache.poi.hwpf.model;

import org.apache.poi.hwpf.sprm.SectionSprmCompressor;
import org.apache.poi.hwpf.sprm.SectionSprmUncompressor;
import org.apache.poi.hwpf.usermodel.SectionProperties;

/**
 */
public final class SEPX extends BytePropertyNode
{

  SectionDescriptor _sed;

  public SEPX(SectionDescriptor sed, int start, int end, CharIndexTranslator translator, byte[] grpprl)
  {
    super(start, end, translator, SectionSprmUncompressor.uncompressSEP(grpprl, 0));
    _sed = sed;
  }

  public byte[] getGrpprl()
  {
    return SectionSprmCompressor.compressSectionProperty((SectionProperties)_buf);
  }

  public SectionDescriptor getSectionDescriptor()
  {
    return _sed;
  }

  public SectionProperties getSectionProperties()
  {
    return (SectionProperties)_buf;
  }

  public boolean equals(Object o)
  {
    SEPX sepx = (SEPX)o;
    if (super.equals(o))
    {
      return sepx._sed.equals(_sed);
    }
    return false;
  }
}

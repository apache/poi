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

package org.apache.poi.hdf.event;

import org.apache.poi.hdf.model.hdftypes.SectionProperties;
import org.apache.poi.hdf.model.hdftypes.CharacterProperties;
import org.apache.poi.hdf.model.hdftypes.ParagraphProperties;
import org.apache.poi.hdf.model.hdftypes.TableProperties;
import org.apache.poi.hdf.model.hdftypes.DocumentProperties;

public interface HDFParsingListener
{
  public void document(DocumentProperties dop);
  public void section(SectionProperties sep, int start, int end);
  public void paragraph(ParagraphProperties pap, int start, int end);
  public void listEntry(String bulletText, CharacterProperties bulletProperties, ParagraphProperties pap, int start, int end);
  public void paragraphInTableRow(ParagraphProperties pap, int start, int end);
  public void characterRun(CharacterProperties chp, String text, int start, int end);
  public void tableRowEnd(TableProperties tap, int start, int end);
  public void header(int sectionIndex, int type);
  public void footer(int sectionIndex, int type);
}

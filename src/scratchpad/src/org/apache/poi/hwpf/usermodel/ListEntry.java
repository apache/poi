package org.apache.poi.hwpf.usermodel;

import org.apache.poi.hwpf.model.ListFormatOverride;
import org.apache.poi.hwpf.model.ListFormatOverrideLevel;
import org.apache.poi.hwpf.model.ListLevel;
import org.apache.poi.hwpf.model.ListTables;

import org.apache.poi.hwpf.sprm.SprmBuffer;

public class ListEntry
  extends Paragraph
{
  ListLevel _level;
  ListFormatOverrideLevel _overrideLevel;

  ListEntry(int start, int end, ListTables tables,
                   ParagraphProperties pap, SprmBuffer sprmBuf, Range parent)
  {
    super(start, end, pap, sprmBuf, parent);
    ListFormatOverride override = tables.getOverride(pap.getIlfo());
    _overrideLevel = override.getOverrideLevel(pap.getIlvl());
    _level = tables.getLevel(override.getLsid(), pap.getIlvl());
  }
}

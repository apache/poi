package org.apache.poi.hwpf.usermodel;

import org.apache.poi.hwpf.model.ListFormatOverride;
import org.apache.poi.hwpf.model.ListFormatOverrideLevel;
import org.apache.poi.hwpf.model.ListLevel;
import org.apache.poi.hwpf.model.ListTables;
import org.apache.poi.hwpf.model.PAPX;

import org.apache.poi.hwpf.sprm.SprmBuffer;

public class ListEntry
  extends Paragraph
{
  ListLevel _level;
  ListFormatOverrideLevel _overrideLevel;

  ListEntry(PAPX papx, Range parent, ListTables tables)
  {
    super(papx, parent);
    ListFormatOverride override = tables.getOverride(_props.getIlfo());
    _overrideLevel = override.getOverrideLevel(_props.getIlvl());
    _level = tables.getLevel(override.getLsid(), _props.getIlvl());
  }

  public int type()
  {
    return TYPE_LISTENTRY;
  }
}

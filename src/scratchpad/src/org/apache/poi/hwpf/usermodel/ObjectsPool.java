package org.apache.poi.hwpf.usermodel;

import org.apache.poi.poifs.filesystem.Entry;

public interface ObjectsPool
{
    public Entry getObjectById( String objId );
}

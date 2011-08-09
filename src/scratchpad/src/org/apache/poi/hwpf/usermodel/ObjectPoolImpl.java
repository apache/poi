package org.apache.poi.hwpf.usermodel;

import java.io.FileNotFoundException;

import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.util.Internal;

@Internal
public class ObjectPoolImpl implements ObjectsPool
{
    private DirectoryEntry _objectPool;

    public ObjectPoolImpl( DirectoryEntry _objectPool )
    {
        super();
        this._objectPool = _objectPool;
    }

    public Entry getObjectById( String objId )
    {
        if ( _objectPool == null )
            return null;

        try
        {
            return _objectPool.getEntry( objId );
        }
        catch ( FileNotFoundException exc )
        {
            return null;
        }
    }
}

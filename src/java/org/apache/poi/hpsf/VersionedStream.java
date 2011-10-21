package org.apache.poi.hpsf;

import org.apache.poi.util.Internal;

@Internal
class VersionedStream
{
    private GUID _versionGuid;
    private IndirectPropertyName _streamName;

    VersionedStream( byte[] data, int offset )
    {
        _versionGuid = new GUID( data, offset );
        _streamName = new IndirectPropertyName( data, offset + GUID.SIZE );
    }

    int getSize()
    {
        return GUID.SIZE + _streamName.getSize();
    }
}

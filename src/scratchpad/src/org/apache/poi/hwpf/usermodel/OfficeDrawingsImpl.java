package org.apache.poi.hwpf.usermodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.poi.hwpf.model.EscherRecordHolder;
import org.apache.poi.hwpf.model.FSPA;
import org.apache.poi.hwpf.model.FSPATable;

public class OfficeDrawingsImpl implements OfficeDrawings
{
    private final EscherRecordHolder _escherRecordHolder;
    private final FSPATable _fspaTable;

    public OfficeDrawingsImpl( FSPATable fspaTable,
            EscherRecordHolder escherRecordHolder )
    {
        this._fspaTable = fspaTable;
        this._escherRecordHolder = escherRecordHolder;
    }

    private OfficeDrawing getOfficeDrawing( final FSPA fspa )
    {
        return new OfficeDrawing()
        {
            public int getRectangleBottom()
            {
                return fspa.getYaBottom();
            }

            public int getRectangleLeft()
            {
                return fspa.getXaLeft();
            }

            public int getRectangleRight()
            {
                return fspa.getXaRight();
            }

            public int getRectangleTop()
            {
                return fspa.getYaTop();
            }

            public int getShapeId()
            {
                return fspa.getSpid();
            }

            @Override
            public String toString()
            {
                return "OfficeDrawingImpl: " + fspa.toString();
            }
        };
    }

    public OfficeDrawing getOfficeDrawingAt( int characterPosition )
    {
        final FSPA fspa = _fspaTable.getFspaFromCp( characterPosition );
        if ( fspa == null )
            return null;

        return getOfficeDrawing( fspa );
    }

    public Collection<OfficeDrawing> getOfficeDrawings()
    {
        List<OfficeDrawing> result = new ArrayList<OfficeDrawing>();
        for ( FSPA fspa : _fspaTable.getShapes() )
        {
            result.add( getOfficeDrawing( fspa ) );
        }
        return Collections.unmodifiableList( result );
    }
}

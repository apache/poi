package org.apache.poi.util;

import java.util.List;
import java.util.ArrayList;

/**
 * Provides an interface for interacting with 2d arrays of objects.  This
 * implementation will return null for items not yet allocated and automatically
 * increase the array size for set operations.  You never get an index out of
 * bounds.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 * @version $Id$
 */
public class List2d
{
    // Implemented using a List of List's.
    List rows = new ArrayList();

    public Object get(int col, int row)
    {
        if (row >= rows.size())
        {
            return null;
        }
        else
        {
            List cols = (List) rows.get(row);
            if (col >= cols.size())
                return null;
            else
                return cols.get( col );
        }
    }

    public void set(int col, int row, Object value)
    {
        resizeRows(row);
        resizeCols(row,col);
        List cols = (List) rows.get( row );
        cols.set( col, value );
    }

    private void resizeRows( int row )
    {
        while (rows.size() <= row)
            rows.add( new ArrayList() );
    }

    private void resizeCols( int row, int col )
    {
        List cols = (List) rows.get( row );
        while (cols.size() <= col)
            cols.add(null);
    }


}

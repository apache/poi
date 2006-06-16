package org.apache.poi.util;

import java.util.List;
import java.util.ArrayList;

/**
 * Provides an interface for interacting with 2d arrays of integers.  This
 * implementation will return 0 for items not yet allocated and automatically
 * increase the array size for set operations.  You never get an index out of
 * bounds.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 * @version $Id$
 */
public class IntList2d
{
    // Implemented using a List of IntList's.
    List rows = new ArrayList();

    public int get(int col, int row)
    {
        if (row >= rows.size())
        {
            return 0;
        }
        else
        {
            IntList cols = (IntList) rows.get(row);
            if (col >= cols.size())
                return 0;
            else
                return cols.get( col );
        }
    }

    public void set(int col, int row, int value)
    {
        resizeRows(row);
        resizeCols(row,col);
        IntList cols = (IntList) rows.get( row );
        cols.set( col, value );
    }

    private void resizeRows( int row )
    {
        while (rows.size() <= row)
            rows.add( new IntList() );
    }

    private void resizeCols( int row, int col )
    {
        IntList cols = (IntList) rows.get( row );
        while (cols.size() <= col)
            cols.add(0);
    }

    public boolean isAllocated( int col, int row )
    {
        if (row < rows.size())
        {
            IntList cols = (IntList) rows.get( row );
            return ( col < cols.size() );
        }
        else
        {
            return false;
        }
    }



}

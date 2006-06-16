package org.apache.poi.util;

import java.util.List;
import java.util.ArrayList;

/**
 * Provides an interface for interacting with 2d arrays of doubles.  This
 * implementation will return 0 for items not yet allocated and automatically
 * increase the array size for set operations.  You never get an index out of
 * bounds.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 * @version $Id$
 */
public class DoubleList2d
{
    // Implemented using a List of DoubleList's.
    List rows = new ArrayList();

    public double get(int col, int row)
    {
        if (row >= rows.size())
        {
            return 0;
        }
        else
        {
            DoubleList cols = (DoubleList) rows.get(row);
            if (col >= cols.size())
                return 0;
            else
                return cols.get( col );
        }
    }

    public void set(int col, int row, double value)
    {
        resizeRows(row);
        resizeCols(row,col);
        DoubleList cols = (DoubleList) rows.get( row );
        cols.set( col, value );
    }

    private void resizeRows( int row )
    {
        while (rows.size() <= row)
            rows.add( new DoubleList() );
    }

    private void resizeCols( int row, int col )
    {
        DoubleList cols = (DoubleList) rows.get( row );
        while (cols.size() <= col)
            cols.add(0);
    }


}

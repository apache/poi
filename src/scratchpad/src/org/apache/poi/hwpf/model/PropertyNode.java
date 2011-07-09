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

package org.apache.poi.hwpf.model;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Represents a lightweight node in the Trees used to store content
 *  properties.
 * This only ever works in characters. For the few odd cases when
 *  the start and end aren't in characters (eg PAPX and CHPX), use
 *  {@link BytePropertyNode} between you and this.
 *
 * @author Ryan Ackley
 */
public abstract class PropertyNode<T extends PropertyNode<T>>  implements Comparable<T>, Cloneable
{

    static final class StartComparator implements Comparator<PropertyNode<?>>
    {
        static StartComparator instance = new StartComparator();

        public int compare( PropertyNode<?> o1, PropertyNode<?> o2 )
        {
            int thisVal = o1.getStart();
            int anotherVal = o2.getStart();
            return ( thisVal < anotherVal ? -1 : ( thisVal == anotherVal ? 0
                    : 1 ) );
        }
    }

  private final static POILogger _logger = POILogFactory.getLogger(PropertyNode.class);
  protected Object _buf;
  /** The start, in characters */
  private int _cpStart;
  /** The end, in characters */
  private int _cpEnd;


  /**
   * @param fcStart The start of the text for this property, in characters.
   * @param fcEnd The end of the text for this property, in characters.
   * @param buf FIXME: Old documentation is: "grpprl The property description in compressed form."
   */
  protected PropertyNode(int fcStart, int fcEnd, Object buf)
  {
      _cpStart = fcStart;
      _cpEnd = fcEnd;
      _buf = buf;

      if(_cpStart < 0) {
    	  _logger.log(POILogger.WARN, "A property claimed to start before zero, at " + _cpStart + "! Resetting it to zero, and hoping for the best");
    	  _cpStart = 0;
      }

        if ( _cpEnd < _cpStart )
        {
            _logger.log( POILogger.WARN, "A property claimed to end (" + _cpEnd
                    + ") before start! "
                    + "Resetting end to start, and hoping for the best" );
            _cpEnd = _cpStart;
        }
    }

  /**
   * @return The start offset of this property's text.
   */
  public int getStart()
  {
      return _cpStart;
  }

  public void setStart(int start)
  {
    _cpStart = start;
  }

  /**
   * @return The offset of the end of this property's text.
   */
  public int getEnd()
  {
    return _cpEnd;
  }

  public void setEnd(int end)
  {
    _cpEnd = end;
  }

  /**
   * Adjust for a deletion that can span multiple PropertyNodes.
   * @param start
   * @param length
   */
  public void adjustForDelete(int start, int length)
  {
    int end = start + length;

    if (_cpEnd > start) {
        // The start of the change is before we end

        if (_cpStart < end) {
            // The delete was somewhere in the middle of us
            _cpEnd = end >= _cpEnd ? start : _cpEnd - length;
            _cpStart = Math.min(start, _cpStart);
        } else {
            // The delete was before us
            _cpEnd -= length;
            _cpStart -= length;
        }
    }
  }

  protected boolean limitsAreEqual(Object o)
  {
    return ((PropertyNode<?>)o).getStart() == _cpStart &&
           ((PropertyNode<?>)o).getEnd() == _cpEnd;

  }

    @Override
    public int hashCode()
    {
        return this._cpStart * 31 + this._buf.hashCode();
    }

  public boolean equals(Object o)
  {
    if (limitsAreEqual(o))
    {
      Object testBuf = ((PropertyNode<?>)o)._buf;
      if (testBuf instanceof byte[] && _buf instanceof byte[])
      {
        return Arrays.equals((byte[])testBuf, (byte[])_buf);
      }
      return _buf.equals(testBuf);
    }
    return false;
  }

  public T clone()
    throws CloneNotSupportedException
  {
    return (T) super.clone();
  }

  /**
   * Used for sorting in collections.
   */
  public int compareTo(T o)
  {
      int cpEnd = o.getEnd();
      if(_cpEnd == cpEnd)
      {
        return 0;
      }
      else if(_cpEnd < cpEnd)
      {
        return -1;
      }
      else
      {
        return 1;
      }
  }
}

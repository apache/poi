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

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * This class holds all of the character formatting
 *  properties from Old (Word 6 / Word 95) documents.
 * Unlike with Word 97+, it all gets held in the
 *  same stream.
 * In common with the rest of the old support, it
 *  is read only
 */
@Internal
public final class OldCHPBinTable extends CHPBinTable
{
  /**
   * Constructor used to read an old-style binTable
   *  in from a Word document.
   *
   * @param documentStream
   * @param offset
   * @param size
   * @param fcMin
   */
  public OldCHPBinTable(byte[] documentStream, int offset,
                     int size, int fcMin, OldTextPieceTable tpt)
  {
    PlexOfCps binTable = new PlexOfCps(documentStream, offset, size, 2);

    int length = binTable.length();
    for (int x = 0; x < length; x++)
    {
      GenericPropertyNode node = binTable.getProperty(x);

      int pageNum = LittleEndian.getUShort(node.getBytes());
      int pageOffset = POIFSConstants.SMALLER_BIG_BLOCK_SIZE * pageNum;

      CHPFormattedDiskPage cfkp = new CHPFormattedDiskPage(documentStream,
        pageOffset, tpt);

            for ( CHPX chpx : cfkp.getCHPXs() )
            {
                if ( chpx != null )
                    _textRuns.add( chpx );
            }
    }
    _textRuns.sort(PropertyNode.StartComparator);
  }
}


/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndianConsts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Used to calculate the record sizes for a particular record.  This kind of
 * sucks because it's similar to the SST serialization code.  In general
 * the SST serialization code needs to be rewritten.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Jason Height (jheight at apache.org)
 */
class SSTRecordSizeCalculator
{
    private UnicodeString unistr = null;
    private int stringReminant = 0;
    private int unipos = 0;
    /** Is there any more string to be written? */
    private boolean isRemainingString = false;
    private int totalBytesWritten = 0;
    private boolean finished = false;
    private boolean firstRecord = true;
    private int totalWritten = 0;
    private int recordSize = 0;
    private List recordLengths = new ArrayList();
    private int pos = 0;
    private Map strings;

    public SSTRecordSizeCalculator(Map strings)
    {
        this.strings = strings;
    }

    private boolean canFitStringInRecord(int recordLength) {
      return (recordLength+SSTRecord.STRING_MINIMAL_OVERHEAD) < SSTRecord.MAX_RECORD_SIZE;
                    }

    public int getRecordSize() {
       //Indicates how much of the current base or continue record has
       //been written
       int continueSize = SSTRecord.SST_RECORD_OVERHEAD;
       int recordSize = 0;
        for (int i=0; i < strings.size(); i++ )
        {
          Integer intunipos = new Integer(i);    
          UnicodeString unistr = ( (UnicodeString) strings.get(intunipos));
          final int stringLength = unistr.getRecordSize();
          if ((continueSize + stringLength) <= SSTRecord.MAX_RECORD_SIZE) {
            //String can fit within the bounds of the current record (SST or Continue)
            continueSize += stringLength;
            
            if ((i < (strings.size()-1)) && !canFitStringInRecord(continueSize)) {
              //Start new continueRecord if there is another string              
              recordLengths.add(new Integer(continueSize));
              recordSize += continueSize;
              //Minimum ammount of space for a new continue record.
              continueSize = 4;   
            }
          } else {
            int stringRemainder = stringLength;
            while (stringRemainder != 0) {              
              if ( (continueSize + stringRemainder) > SSTRecord.MAX_RECORD_SIZE) {
                //Determine number of bytes that can be written in the space
                //available
                int bytesWritten = Math.min((SSTRecord.MAX_RECORD_SIZE - continueSize), stringRemainder);

                //Ensure that the Unicode String writes both the high and low
                //byte in the one action. Since the string overhead is 3 bytes
                //if the bytes that can be written is even, then we need to 
                //write one less byte to capture both the high and low bytes.
                bytesWritten = unistr.maxBrokenLength(bytesWritten);
                continueSize += bytesWritten;
                stringRemainder -= bytesWritten;
                recordLengths.add(new Integer(continueSize));
                recordSize += continueSize;
                //Minimum ammount of space for a new continue record.
                continueSize = 4;
                //Add one to the size of the string that is remaining, since the
                //first byte for the next continue record will be compressed unicode indicator
                stringRemainder++;
              } else {
                //Remainder of string can fit within the bounds of the current
                //continue record
                continueSize += stringRemainder;
                stringRemainder = 0;
                if ((i < (strings.size()-1)) && !canFitStringInRecord(continueSize)) {
                  //Start new continueRecord if there is another string
                  recordLengths.add(new Integer(continueSize));
                  recordSize += continueSize;
                  //Minimum ammount of space for a new continue record.
                  continueSize = 4;          
        }
    }
        }
            }
        }
        recordLengths.add(new Integer(continueSize));
        recordSize += continueSize;        
        return recordSize;
    }

    public List getRecordLengths()
    {
        return recordLengths;
    }
}

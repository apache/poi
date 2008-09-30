
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
        

package org.apache.poi.hssf.record;

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.HexDump;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Title: Unicode String<P>
 * Description:  Unicode String record.  We implement these as a record, although
 *               they are really just standard fields that are in several records.
 *               It is considered more desirable then repeating it in all of them.<P>
 * REFERENCE:  PG 264 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author  Andrew C. Oliver
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Glen Stampoultzis (glens at apache.org)
 * @version 2.0-pre
 */

public class UnicodeString
    implements Comparable
{
    public final static short sid = 0xFFF;
    private short             field_1_charCount;     // = 0;
    private byte              field_2_optionflags;   // = 0;
    private String            field_3_string;        // = null;
    private List field_4_format_runs;
    private byte[] field_5_ext_rst;
    private  static final BitField   highByte  = BitFieldFactory.getInstance(0x1);
    private  static final BitField   extBit    = BitFieldFactory.getInstance(0x4);
    private  static final BitField   richText  = BitFieldFactory.getInstance(0x8);

    public static class FormatRun implements Comparable {
      private short character;
      private short fontIndex;

      public FormatRun(short character, short fontIndex) {
        this.character = character;
        this.fontIndex = fontIndex;
      }

      public short getCharacterPos() {
        return character;
      }

      public short getFontIndex() {
        return fontIndex;
      }

      public boolean equals(Object o) {
        if ((o == null) || (o.getClass() != this.getClass()))
    {
            return false;
        }
        FormatRun other = ( FormatRun ) o;

        return ((character == other.character) && (fontIndex == other.fontIndex));
      }

      public int compareTo(Object obj) {
        FormatRun r = (FormatRun)obj;
        if ((character == r.character) && (fontIndex == r.fontIndex))
          return 0;
        if (character == r.character)
          return fontIndex - r.fontIndex;
        else return character - r.character;
      }

      public String toString() {
        return "character="+character+",fontIndex="+fontIndex;
      }
    }

    private UnicodeString() {
     //Used for clone method.
    }

    public UnicodeString(String str)
    {
      setString(str);
    }

    /**
     * construct a unicode string record and fill its fields, ID is ignored
     * @param in the RecordInputstream to read the record from
     */

    public UnicodeString(RecordInputStream in)
    {
      fillFields(in); // TODO - inline
    }


    public int hashCode()
    {
        int stringHash = 0;
        if (field_3_string != null)
            stringHash = field_3_string.hashCode();
        return field_1_charCount + stringHash;
    }

    /**
     * Our handling of equals is inconsistent with compareTo.  The trouble is because we don't truely understand
     * rich text fields yet it's difficult to make a sound comparison.
     *
     * @param o     The object to compare.
     * @return      true if the object is actually equal.
     */
    public boolean equals(Object o)
    {
        if ((o == null) || (o.getClass() != this.getClass()))
        {
            return false;
        }
        UnicodeString other = ( UnicodeString ) o;

        //Ok lets do this in stages to return a quickly, first check the actual string
        boolean eq = ((field_1_charCount == other.field_1_charCount)
                && (field_2_optionflags == other.field_2_optionflags)
                && field_3_string.equals(other.field_3_string));
        if (!eq) return false;

        //Ok string appears to be equal but now lets compare formatting runs
        if ((field_4_format_runs == null) && (other.field_4_format_runs == null))
          //Strings are equal, and there are not formtting runs.
          return true;
        if (((field_4_format_runs == null) && (other.field_4_format_runs != null)) ||
             (field_4_format_runs != null) && (other.field_4_format_runs == null))
           //Strings are equal, but one or the other has formatting runs
           return false;

        //Strings are equal, so now compare formatting runs.
        int size = field_4_format_runs.size();
        if (size != other.field_4_format_runs.size())
          return false;

        for (int i=0;i<size;i++) {
          FormatRun run1 = (FormatRun)field_4_format_runs.get(i);
          FormatRun run2 = (FormatRun)other.field_4_format_runs.get(i);

          if (!run1.equals(run2))
            return false;
    }

        //Well the format runs are equal as well!, better check the ExtRst data
        //Which by the way we dont know how to decode!
        if ((field_5_ext_rst == null) && (other.field_5_ext_rst == null))
          return true;
        if (((field_5_ext_rst == null) && (other.field_5_ext_rst != null)) ||
            ((field_5_ext_rst != null) && (other.field_5_ext_rst == null)))
          return false;
        size = field_5_ext_rst.length;
        if (size != field_5_ext_rst.length)
          return false;

        //Check individual bytes!
        for (int i=0;i<size;i++) {
          if (field_5_ext_rst[i] != other.field_5_ext_rst[i])
            return false;
        }
        //Phew!! After all of that we have finally worked out that the strings
        //are identical.
        return true;
    }

    /**
     * @param in the RecordInputstream to read the record from
     */
    protected void fillFields(RecordInputStream in)
        {
        field_1_charCount   = in.readShort();
        field_2_optionflags = in.readByte();

        int runCount = 0;
        int extensionLength = 0;
        //Read the number of rich runs if rich text.
        if ( isRichText() )
        {
            runCount = in.readShort();
        }
        //Read the size of extended data if present.
        if ( isExtendedText() )
        {
            extensionLength = in.readInt();
        }

        //Now need to get the string data.
        //Turn off autocontinuation so that we can catch the continue boundary
        in.setAutoContinue(false);
        StringBuffer tmpString = new StringBuffer(field_1_charCount);
        int stringCharCount = field_1_charCount;
        boolean isCompressed = ((field_2_optionflags & 1) == 0);
        while (stringCharCount != 0) {
          if (in.remaining() == 0) {
            if (in.isContinueNext()) {
              in.nextRecord();
              //Check if we are now reading, compressed or uncompressed unicode.
              byte optionflags = in.readByte();
              isCompressed = ((optionflags & 1) == 0);
            } else
              throw new RecordFormatException("Expected continue record.");
          }
          if (isCompressed) {
            //Typecast direct to char from byte with high bit set causes all ones
            //in the high byte of the char (which is of course incorrect)
            char ch = (char)( (short)0xff & (short)in.readByte() );
            tmpString.append(ch);
          } else {
            char ch = (char) in.readShort();
            tmpString.append(ch);
          }
          stringCharCount --;
        }
        field_3_string = tmpString.toString();
        //Turn back on autocontinuation
        in.setAutoContinue(true);


        if (isRichText() && (runCount > 0)) {
          field_4_format_runs = new ArrayList(runCount);
          for (int i=0;i<runCount;i++) {
            field_4_format_runs.add(new FormatRun(in.readShort(), in.readShort()));
            //read reserved
            //in.readInt();
            }
        }

        if (isExtendedText() && (extensionLength > 0)) {
          field_5_ext_rst = new byte[extensionLength];
          for (int i=0;i<extensionLength;i++) {
            field_5_ext_rst[i] = in.readByte();
            }
        }
    }



    /**
     * get the number of characters in the string
     *
     *
     * @return number of characters
     *
     */

    public short getCharCount()
    {
        return field_1_charCount;
    }

    /**
     * set the number of characters in the string
     * @param cc - number of characters
     */

    public void setCharCount(short cc)
    {
        field_1_charCount = cc;
    }

    /**
     * get the option flags which among other things return if this is a 16-bit or
     * 8 bit string
     *
     * @return optionflags bitmask
     *
     */

    public byte getOptionFlags()
    {
        return field_2_optionflags;
    }

    /**
     * set the option flags which among other things return if this is a 16-bit or
     * 8 bit string
     *
     * @param of  optionflags bitmask
     *
     */

    public void setOptionFlags(byte of)
    {
        field_2_optionflags = of;
    }

    /**
     * get the actual string this contains as a java String object
     *
     *
     * @return String
     *
     */

    public String getString()
    {
        return field_3_string;
    }

    /**
     * set the actual string this contains
     * @param string  the text
     */

    public void setString(String string)
    {
        field_3_string = string;
        setCharCount((short)field_3_string.length());
        // scan for characters greater than 255 ... if any are
        // present, we have to use 16-bit encoding. Otherwise, we
        // can use 8-bit encoding
        boolean useUTF16 = false;
        int strlen = string.length();

        for ( int j = 0; j < strlen; j++ )
        {
            if ( string.charAt( j ) > 255 )
        {
                useUTF16 = true;
                break;
            }
        }
        if (useUTF16)
          //Set the uncomressed bit
          field_2_optionflags = highByte.setByte(field_2_optionflags);
        else field_2_optionflags = highByte.clearByte(field_2_optionflags);
    }

    public int getFormatRunCount() {
      if (field_4_format_runs == null)
        return 0;
      return field_4_format_runs.size();
    }

    public FormatRun getFormatRun(int index) {
      if (field_4_format_runs == null)
        return null;
      if ((index < 0) || (index >= field_4_format_runs.size()))
        return null;
      return (FormatRun)field_4_format_runs.get(index);
    }

    private int findFormatRunAt(int characterPos) {
      int size = field_4_format_runs.size();
      for (int i=0;i<size;i++) {
        FormatRun r = (FormatRun)field_4_format_runs.get(i);
        if (r.character == characterPos)
          return i;
        else if (r.character > characterPos)
          return -1;
      }
      return -1;
    }

    /** Adds a font run to the formatted string.
     *
     *  If a font run exists at the current charcter location, then it is
     *  replaced with the font run to be added.
     */
    public void addFormatRun(FormatRun r) {
      if (field_4_format_runs == null)
        field_4_format_runs = new ArrayList();

      int index = findFormatRunAt(r.character);
      if (index != -1)
         field_4_format_runs.remove(index);

      field_4_format_runs.add(r);
      //Need to sort the font runs to ensure that the font runs appear in
      //character order
      Collections.sort(field_4_format_runs);

      //Make sure that we now say that we are a rich string
      field_2_optionflags = richText.setByte(field_2_optionflags);
        }

    public Iterator formatIterator() {
      if (field_4_format_runs != null)
        return field_4_format_runs.iterator();
      return null;
    }

    public void removeFormatRun(FormatRun r) {
      field_4_format_runs.remove(r);
      if (field_4_format_runs.size() == 0) {
        field_4_format_runs = null;
        field_2_optionflags = richText.clearByte(field_2_optionflags);
      }
    }

    public void clearFormatting() {
      field_4_format_runs = null;
      field_2_optionflags = richText.clearByte(field_2_optionflags);
    }

    public byte[] getExtendedRst() {
       return this.field_5_ext_rst;
    }

    public void setExtendedRst(byte[] ext_rst) {
      if (ext_rst != null)
        field_2_optionflags = extBit.setByte(field_2_optionflags);
      else field_2_optionflags = extBit.clearByte(field_2_optionflags);
      this.field_5_ext_rst = ext_rst;
    }


    /**
     * Swaps all use in the string of one font index 
     *  for use of a different font index.
     * Normally only called when fonts have been
     *  removed / re-ordered
     */
    public void swapFontUse(short oldFontIndex, short newFontIndex) {
    	Iterator i = field_4_format_runs.iterator();
    	while(i.hasNext()) {
    		FormatRun run = (FormatRun)i.next();
    		if(run.fontIndex == oldFontIndex) {
    			run.fontIndex = newFontIndex;
    		}
    	}
    }
    
    /**
     * unlike the real records we return the same as "getString()" rather than debug info
     * @see #getDebugInfo()
     * @return String value of the record
     */

    public String toString()
    {
        return getString();
    }

    /**
     * return a character representation of the fields of this record
     *
     *
     * @return String of output for biffviewer etc.
     *
     */

    public String getDebugInfo()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[UNICODESTRING]\n");
        buffer.append("    .charcount       = ")
            .append(Integer.toHexString(getCharCount())).append("\n");
        buffer.append("    .optionflags     = ")
            .append(Integer.toHexString(getOptionFlags())).append("\n");
        buffer.append("    .string          = ").append(getString()).append("\n");
        if (field_4_format_runs != null) {
          for (int i = 0; i < field_4_format_runs.size();i++) {
            FormatRun r = (FormatRun)field_4_format_runs.get(i);
            buffer.append("      .format_run"+i+"          = ").append(r.toString()).append("\n");
          }
        }
        if (field_5_ext_rst != null) {
          buffer.append("    .field_5_ext_rst          = ").append("\n").append(HexDump.toHex(field_5_ext_rst)).append("\n");
        }
        buffer.append("[/UNICODESTRING]\n");
        return buffer.toString();
    }

    private int writeContinueIfRequired(UnicodeRecordStats stats, final int requiredSize, int offset, byte[] data) {
      //Basic string overhead
      if (stats.remainingSize < requiredSize) {
        //Check if be are already in a continue record, if so make sure that
        //we go back and write out our length
        if (stats.lastLengthPos != -1) {
          short lastRecordLength = (short)(offset - stats.lastLengthPos - 2);
          if (lastRecordLength > 8224)
            throw new InternalError();
          LittleEndian.putShort(data, stats.lastLengthPos, lastRecordLength);
        }

        LittleEndian.putShort(data, offset, ContinueRecord.sid);
        offset+=2;
        //Record the location of the last continue legnth position, but dont write
        //anything there yet (since we dont know what it will be!)
        stats.lastLengthPos = offset;
        offset += 2;

        stats.recordSize += 4;
        stats.remainingSize = SSTRecord.MAX_RECORD_SIZE-4;
      }
      return offset;
        }

    public int serialize(UnicodeRecordStats stats, final int offset, byte [] data)
    {
      int pos = offset;

      //Basic string overhead
      pos = writeContinueIfRequired(stats, 3, pos, data);
        // byte[] retval = new byte[ 3 + (getString().length() * charsize)];
      LittleEndian.putShort(data, pos, getCharCount());
      pos += 2;
      data[ pos ] = getOptionFlags();
      pos += 1;
      stats.recordSize += 3;
      stats.remainingSize-= 3;

      if (isRichText()) {
        if (field_4_format_runs != null) {
          pos = writeContinueIfRequired(stats, 2, pos, data);

          LittleEndian.putShort(data, pos, (short) field_4_format_runs.size());
          pos += 2;
          stats.recordSize += 2;
          stats.remainingSize -= 2;
        }
      }
      if ( isExtendedText() )
      {
        if (this.field_5_ext_rst != null) {
          pos = writeContinueIfRequired(stats, 4, pos, data);

          LittleEndian.putInt(data, pos, field_5_ext_rst.length);
          pos += 4;
          stats.recordSize += 4;
          stats.remainingSize -= 4;
        }
      }

      int charsize = isUncompressedUnicode() ? 2 : 1;
      int strSize = (getString().length() * charsize);

      byte[] strBytes = null;
        try {
            String unicodeString = getString();
              if (!isUncompressedUnicode())
            {
                strBytes = unicodeString.getBytes("ISO-8859-1");
            }
            else
            {
                  strBytes = unicodeString.getBytes("UTF-16LE");
            }
        }
        catch (Exception e) {
              throw new InternalError();
        }
          if (strSize != strBytes.length)
            throw new InternalError("That shouldnt have happened!");

      //Check to see if the offset occurs mid string, if so then we need to add
      //the byte to start with that represents the first byte of the continue record.
      if (strSize > stats.remainingSize) {
        //Ok the offset occurs half way through the string, that means that
        //we need an extra byte after the continue record ie we didnt finish
        //writing out the string the 1st time through

        //But hang on, how many continue records did we span? What if this is
        //a REALLY long string. We need to work this all out.
        int ammountThatCantFit = strSize;
        int strPos = 0;
        while (ammountThatCantFit > 0) {
          int ammountWritten = Math.min(stats.remainingSize, ammountThatCantFit);
          //Make sure that the ammount that cant fit takes into account
          //whether we are writing double byte unicode
          if (isUncompressedUnicode()) {
            //We have the '-1' here because whether this is the first record or
            //subsequent continue records, there is always the case that the
            //number of bytes in a string on doube byte boundaries is actually odd.
            if ( ( (ammountWritten ) % 2) == 1)
              ammountWritten--;
          }
          System.arraycopy(strBytes, strPos, data, pos, ammountWritten);
          pos += ammountWritten;
          strPos += ammountWritten;
          stats.recordSize += ammountWritten;
          stats.remainingSize -= ammountWritten;

          //Ok lets subtract what we can write
          ammountThatCantFit -= ammountWritten;

          //Each iteration of this while loop is another continue record, unless
          //everything  now fits.
          if (ammountThatCantFit > 0) {
            //We know that a continue WILL be requied, but use this common method
            pos = writeContinueIfRequired(stats, ammountThatCantFit, pos, data);

            //The first byte after a continue mid string is the extra byte to
            //indicate if this run is compressed or not.
            data[pos] = (byte) (isUncompressedUnicode() ? 0x1 : 0x0);
            pos++;
            stats.recordSize++;
            stats.remainingSize --;
          }
        }
      } else {
        if (strSize > (data.length-pos))
          System.out.println("Hmm shouldnt happen");
        //Ok the string fits nicely in the remaining size
        System.arraycopy(strBytes, 0, data, pos, strSize);
        pos += strSize;
        stats.recordSize += strSize;
        stats.remainingSize -= strSize;
      }


      if (isRichText() && (field_4_format_runs != null)) {
        int count = field_4_format_runs.size();

        //This will ensure that a run does not split a continue
        for (int i=0;i<count;i++) {
          pos = writeContinueIfRequired(stats, 4, pos, data);
          FormatRun r = (FormatRun)field_4_format_runs.get(i);
          LittleEndian.putShort(data, pos, r.character);
          pos += 2;
          LittleEndian.putShort(data, pos, r.fontIndex);
          pos += 2;

          //Each run count is four bytes
          stats.recordSize += 4;
          stats.remainingSize -=4;
        }
      }

      if (isExtendedText() && (field_5_ext_rst != null)) {
        //Ok ExtRst is actually not documented, so i am going to hope
        //that we can actually continue on byte boundaries
        int ammountThatCantFit = field_5_ext_rst.length - stats.remainingSize;
        int extPos = 0;
        if (ammountThatCantFit > 0) {
          while (ammountThatCantFit > 0) {
            //So for this record we have already written
            int ammountWritten = Math.min(stats.remainingSize, ammountThatCantFit);
            System.arraycopy(field_5_ext_rst, extPos, data, pos, ammountWritten);
            pos += ammountWritten;
            extPos += ammountWritten;
            stats.recordSize += ammountWritten;
            stats.remainingSize -= ammountWritten;

            //Ok lets subtract what we can write
            ammountThatCantFit -= ammountWritten;
            if (ammountThatCantFit > 0) {
              pos = writeContinueIfRequired(stats, 1, pos, data);
            }
          }
        } else {
          //We can fit wholey in what remains.
          System.arraycopy(field_5_ext_rst, 0, data, pos, field_5_ext_rst.length);
          pos +=  field_5_ext_rst.length;
          stats.remainingSize -= field_5_ext_rst.length;
          stats.recordSize += field_5_ext_rst.length;
        }
      }

        return pos - offset;
    }


    public void setCompressedUnicode() {
      field_2_optionflags = highByte.setByte(field_2_optionflags);
    }

    public void setUncompressedUnicode() {
      field_2_optionflags = highByte.clearByte(field_2_optionflags);
    }

    private boolean isUncompressedUnicode()
    {
        return highByte.isSet(getOptionFlags());
    }

    /** Returns the size of this record, given the ammount of record space
     * remaining, it will also include the size of writing a continue record.
     */

    public static class UnicodeRecordStats {
      public int recordSize;
      public int remainingSize = SSTRecord.MAX_RECORD_SIZE;
      public int lastLengthPos = -1;
    }
    public void getRecordSize(UnicodeRecordStats stats) {
      //Basic string overhead
      if (stats.remainingSize < 3) {
        //Needs a continue
         stats.recordSize += 4;
         stats.remainingSize = SSTRecord.MAX_RECORD_SIZE-4;
      }
      stats.recordSize += 3;
      stats.remainingSize-= 3;

      //Read the number of rich runs if rich text.
      if ( isRichText() )
    {
          //Run count
          if (stats.remainingSize < 2) {
            //Needs a continue
            //Reset the available space.
            stats.remainingSize = SSTRecord.MAX_RECORD_SIZE-4;
            //continue record overhead
            stats.recordSize+=4;
    }

          stats.recordSize += 2;
          stats.remainingSize -=2;
      }
      //Read the size of extended data if present.
      if ( isExtendedText() )
    {
        //Needs a continue
          //extension length
          if (stats.remainingSize < 4) {
            //Reset the available space.
            stats.remainingSize = SSTRecord.MAX_RECORD_SIZE-4;
            //continue record overhead
            stats.recordSize+=4;
          }

          stats.recordSize += 4;
          stats.remainingSize -=4;
      }

      int charsize = isUncompressedUnicode() ? 2 : 1;
      int strSize = (getString().length() * charsize);
      //Check to see if the offset occurs mid string, if so then we need to add
      //the byte to start with that represents the first byte of the continue record.
      if (strSize > stats.remainingSize) {
        //Ok the offset occurs half way through the string, that means that
        //we need an extra byte after the continue record ie we didnt finish
        //writing out the string the 1st time through

        //But hang on, how many continue records did we span? What if this is
        //a REALLY long string. We need to work this all out.
        int ammountThatCantFit = strSize;
        while (ammountThatCantFit > 0) {
          int ammountWritten = Math.min(stats.remainingSize, ammountThatCantFit);
          //Make sure that the ammount that cant fit takes into account
          //whether we are writing double byte unicode
          if (isUncompressedUnicode()) {
            //We have the '-1' here because whether this is the first record or
            //subsequent continue records, there is always the case that the
            //number of bytes in a string on doube byte boundaries is actually odd.
            if ( ( (ammountWritten) % 2) == 1)
              ammountWritten--;
          }
          stats.recordSize += ammountWritten;
          stats.remainingSize -= ammountWritten;

          //Ok lets subtract what we can write
          ammountThatCantFit -= ammountWritten;

          //Each iteration of this while loop is another continue record, unless
          //everything  now fits.
          if (ammountThatCantFit > 0) {
            //Reset the available space.
            stats.remainingSize = SSTRecord.MAX_RECORD_SIZE-4;
            //continue record overhead
            stats.recordSize+=4;

            //The first byte after a continue mid string is the extra byte to
            //indicate if this run is compressed or not.
            stats.recordSize++;
            stats.remainingSize --;
          }
        }
      } else {
        //Ok the string fits nicely in the remaining size
        stats.recordSize += strSize;
        stats.remainingSize -= strSize;
      }

      if (isRichText() && (field_4_format_runs != null)) {
        int count = field_4_format_runs.size();

        //This will ensure that a run does not split a continue
        for (int i=0;i<count;i++) {
          if (stats.remainingSize < 4) {
            //Reset the available space.
            stats.remainingSize = SSTRecord.MAX_RECORD_SIZE-4;
            //continue record overhead
            stats.recordSize+=4;
          }

          //Each run count is four bytes
          stats.recordSize += 4;
          stats.remainingSize -=4;
        }
      }

      if (isExtendedText() && (field_5_ext_rst != null)) {
        //Ok ExtRst is actually not documented, so i am going to hope
        //that we can actually continue on byte boundaries
        int ammountThatCantFit = field_5_ext_rst.length - stats.remainingSize;
        if (ammountThatCantFit > 0) {
          while (ammountThatCantFit > 0) {
            //So for this record we have already written
            int ammountWritten = Math.min(stats.remainingSize, ammountThatCantFit);
            stats.recordSize += ammountWritten;
            stats.remainingSize -= ammountWritten;

            //Ok lets subtract what we can write
            ammountThatCantFit -= ammountWritten;
            if (ammountThatCantFit > 0) {
              //Each iteration of this while loop is another continue record.

              //Reset the available space.
              stats.remainingSize = SSTRecord.MAX_RECORD_SIZE-4;
              //continue record overhead
              stats.recordSize += 4;
            }
          }
        } else {
          //We can fit wholey in what remains.
          stats.remainingSize -= field_5_ext_rst.length;
          stats.recordSize += field_5_ext_rst.length;
        }
      }
    }



    public short getSid()
    {
        return sid;
    }

    public int compareTo(Object obj)
    {
        UnicodeString str = ( UnicodeString ) obj;

        int result = getString().compareTo(str.getString());

        //As per the equals method lets do this in stages
        if (result != 0)
          return result;

        //Ok string appears to be equal but now lets compare formatting runs
        if ((field_4_format_runs == null) && (str.field_4_format_runs == null))
          //Strings are equal, and there are no formtting runs.
          return 0;

        if ((field_4_format_runs == null) && (str.field_4_format_runs != null))
          //Strings are equal, but one or the other has formatting runs
          return 1;
        if ((field_4_format_runs != null) && (str.field_4_format_runs == null))
          //Strings are equal, but one or the other has formatting runs
          return -1;

        //Strings are equal, so now compare formatting runs.
        int size = field_4_format_runs.size();
        if (size != str.field_4_format_runs.size())
          return size - str.field_4_format_runs.size();

        for (int i=0;i<size;i++) {
          FormatRun run1 = (FormatRun)field_4_format_runs.get(i);
          FormatRun run2 = (FormatRun)str.field_4_format_runs.get(i);

          result = run1.compareTo(run2);
          if (result != 0)
            return result;
        }

        //Well the format runs are equal as well!, better check the ExtRst data
        //Which by the way we dont know how to decode!
        if ((field_5_ext_rst == null) && (str.field_5_ext_rst == null))
          return 0;
        if ((field_5_ext_rst == null) && (str.field_5_ext_rst != null))
         return 1;
        if ((field_5_ext_rst != null) && (str.field_5_ext_rst == null))
          return -1;

        size = field_5_ext_rst.length;
        if (size != field_5_ext_rst.length)
          return size - field_5_ext_rst.length;

        //Check individual bytes!
        for (int i=0;i<size;i++) {
          if (field_5_ext_rst[i] != str.field_5_ext_rst[i])
            return field_5_ext_rst[i] - str.field_5_ext_rst[i];
        }
        //Phew!! After all of that we have finally worked out that the strings
        //are identical.
        return 0;
    }

    public boolean isRichText()
    {
      return richText.isSet(getOptionFlags());
    }

    public boolean isExtendedText()
        {
        return extBit.isSet(getOptionFlags());
    }

    public Object clone() {
        UnicodeString str = new UnicodeString();
        str.field_1_charCount = field_1_charCount;
        str.field_2_optionflags = field_2_optionflags;
        str.field_3_string = field_3_string;
        if (field_4_format_runs != null) {
          str.field_4_format_runs = new ArrayList();
          int size = field_4_format_runs.size();
          for (int i = 0; i < size; i++) {
            FormatRun r = (FormatRun) field_4_format_runs.get(i);
            str.field_4_format_runs.add(new FormatRun(r.character, r.fontIndex));
            }
        }
        if (field_5_ext_rst != null) {
          str.field_5_ext_rst = new byte[field_5_ext_rst.length];
          System.arraycopy(field_5_ext_rst, 0, str.field_5_ext_rst, 0,
                           field_5_ext_rst.length);
    }

        return str;
    }


}

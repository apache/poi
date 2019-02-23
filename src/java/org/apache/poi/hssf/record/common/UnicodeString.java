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

package org.apache.poi.hssf.record.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.cont.ContinuableRecordInput;
import org.apache.poi.hssf.record.cont.ContinuableRecordOutput;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.StringUtil;

/**
 * Title: Unicode String<p>
 * Description:  Unicode String - just standard fields that are in several records.
 *               It is considered more desirable then repeating it in all of them.<p>
 *               This is often called a XLUnicodeRichExtendedString in MS documentation.<p>
 * REFERENCE:  PG 264 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<p>
 * REFERENCE:  PG 951 Excel Binary File Format (.xls) Structure Specification v20091214 
 */
public class UnicodeString implements Comparable<UnicodeString> {
    private static final POILogger _logger = POILogFactory.getLogger(UnicodeString.class);

    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;


    private short             field_1_charCount;
    private byte              field_2_optionflags;
    private String            field_3_string;
    private List<FormatRun>   field_4_format_runs;
    private ExtRst            field_5_ext_rst;
    private static final BitField   highByte  = BitFieldFactory.getInstance(0x1);
    // 0x2 is reserved
    private static final BitField   extBit    = BitFieldFactory.getInstance(0x4);
    private static final BitField   richText  = BitFieldFactory.getInstance(0x8);

    public static class FormatRun implements Comparable<FormatRun> {
        final short _character;
        short _fontIndex;

        public FormatRun(short character, short fontIndex) {
            this._character = character;
            this._fontIndex = fontIndex;
        }

        public FormatRun(LittleEndianInput in) {
            this(in.readShort(), in.readShort());
        }

        public short getCharacterPos() {
            return _character;
        }

        public short getFontIndex() {
            return _fontIndex;
        }

        public boolean equals(Object o) {
            if (!(o instanceof FormatRun)) {
                return false;
            }
            FormatRun other = ( FormatRun ) o;

            return _character == other._character && _fontIndex == other._fontIndex;
        }

        public int compareTo(FormatRun r) {
            if (_character == r._character && _fontIndex == r._fontIndex) {
                return 0;
            }
            if (_character == r._character) {
                return _fontIndex - r._fontIndex;
            }
            return _character - r._character;
        }

        @Override
        public int hashCode() {
            assert false : "hashCode not designed";
            return 42; // any arbitrary constant will do
        }

        public String toString() {
            return "character="+_character+",fontIndex="+_fontIndex;
        }

        public void serialize(LittleEndianOutput out) {
            out.writeShort(_character);
            out.writeShort(_fontIndex);
        }
    }
    
    // See page 681
    public static class ExtRst implements Comparable<ExtRst> {
       private short reserved;
       
       // This is a Phs (see page 881)
       private short formattingFontIndex;
       private short formattingOptions;
       
       // This is a RPHSSub (see page 894)
       private int numberOfRuns;
       private String phoneticText;
       
       // This is an array of PhRuns (see page 881)
       private PhRun[] phRuns;
       // Sometimes there's some cruft at the end
       private byte[] extraData;

       private void populateEmpty() {
          reserved = 1;
          phoneticText = "";
          phRuns = new PhRun[0];
          extraData = new byte[0];
       }
       
       protected ExtRst() {
          populateEmpty();
       }
       protected ExtRst(LittleEndianInput in, int expectedLength) {
          reserved = in.readShort();
          
          // Old style detection (Reserved = 0xFF)
          if(reserved == -1) {
             populateEmpty();
             return;
          }
          
          // Spot corrupt records
          if(reserved != 1) {
             _logger.log(POILogger.WARN, "Warning - ExtRst has wrong magic marker, expecting 1 but found " + reserved + " - ignoring");
             // Grab all the remaining data, and ignore it
             for(int i=0; i<expectedLength-2; i++) {
                in.readByte();
             }
             // And make us be empty
             populateEmpty();
             return;
          }

          // Carry on reading in as normal
          short stringDataSize = in.readShort();
          
          formattingFontIndex = in.readShort();
          formattingOptions   = in.readShort();
          
          // RPHSSub
          numberOfRuns = in.readUShort();
          short length1 = in.readShort();
          // No really. Someone clearly forgot to read
          //  the docs on their datastructure...
          short length2 = in.readShort();
          // And sometimes they write out garbage :(
          if(length1 == 0 && length2 > 0) {
             length2 = 0;
          }
          if(length1 != length2) {
             throw new IllegalStateException(
                   "The two length fields of the Phonetic Text don't agree! " +
                   length1 + " vs " + length2
             );
          }
          phoneticText = StringUtil.readUnicodeLE(in, length1);
          
          int runData = stringDataSize - 4 - 6 - (2*phoneticText.length());
          int numRuns = (runData / 6);
          phRuns = new PhRun[numRuns];
          for(int i=0; i<phRuns.length; i++) {
             phRuns[i] = new PhRun(in);
          }

          int extraDataLength = runData - (numRuns*6);
          if(extraDataLength < 0) {
        	 _logger.log( POILogger.WARN, "Warning - ExtRst overran by " + (0-extraDataLength) + " bytes");
             extraDataLength = 0;
          }
          extraData = IOUtils.safelyAllocate(extraDataLength, MAX_RECORD_LENGTH);
          for(int i=0; i<extraData.length; i++) {
             extraData[i] = in.readByte();
          }
       }
       /**
        * Returns our size, excluding our 
        *  4 byte header
        */
       protected int getDataSize() {
          return 4 + 6 + (2*phoneticText.length()) + 
             (6*phRuns.length) + extraData.length;
       }
       protected void serialize(ContinuableRecordOutput out) {
          int dataSize = getDataSize();
          
          out.writeContinueIfRequired(8);
          out.writeShort(reserved);
          out.writeShort(dataSize);
          out.writeShort(formattingFontIndex);
          out.writeShort(formattingOptions);
          
          out.writeContinueIfRequired(6);
          out.writeShort(numberOfRuns);
          out.writeShort(phoneticText.length());
          out.writeShort(phoneticText.length());
          
          out.writeContinueIfRequired(phoneticText.length()*2);
          StringUtil.putUnicodeLE(phoneticText, out);
          
          for(int i=0; i<phRuns.length; i++) {
             phRuns[i].serialize(out);
          }
          
          out.write(extraData);
       }

       public boolean equals(Object obj) {
          if(! (obj instanceof ExtRst)) {
             return false;
          }
          ExtRst other = (ExtRst)obj;
          return (compareTo(other) == 0);
       }
       public int compareTo(ExtRst o) {
          int result;
          
          result = reserved - o.reserved;
          if (result != 0) {
              return result;
          }
          result = formattingFontIndex - o.formattingFontIndex;
          if (result != 0) {
              return result;
          }
          result = formattingOptions - o.formattingOptions;
          if (result != 0) {
              return result;
          }
          result = numberOfRuns - o.numberOfRuns;
          if (result != 0) {
              return result;
          }
          
          result = phoneticText.compareTo(o.phoneticText);
          if (result != 0) {
              return result;
          }
          
          result = phRuns.length - o.phRuns.length;
          if (result != 0) {
              return result;
          }
          for(int i=0; i<phRuns.length; i++) {
             result = phRuns[i].phoneticTextFirstCharacterOffset - o.phRuns[i].phoneticTextFirstCharacterOffset;
             if (result != 0) {
                 return result;
             }
             result = phRuns[i].realTextFirstCharacterOffset - o.phRuns[i].realTextFirstCharacterOffset;
             if (result != 0) {
                 return result;
             }
             result = phRuns[i].realTextLength - o.phRuns[i].realTextLength;
             if (result != 0) {
                 return result;
             }
          }
          
          result = Arrays.hashCode(extraData)-Arrays.hashCode(o.extraData);
          
          return result;
       }

       @Override
       public int hashCode() {
           int hash = reserved;
           hash = 31*hash+formattingFontIndex;
           hash = 31*hash+formattingOptions;
           hash = 31*hash+numberOfRuns;
           hash = 31*hash+phoneticText.hashCode();

           if (phRuns != null) {
               for (PhRun ph : phRuns) {
                   hash = 31*hash+ph.phoneticTextFirstCharacterOffset;
                   hash = 31*hash+ph.realTextFirstCharacterOffset;
                   hash = 31*hash+ph.realTextLength;
               }
           }
           return hash;
       }

       protected ExtRst clone() {
          ExtRst ext = new ExtRst();
          ext.reserved = reserved;
          ext.formattingFontIndex = formattingFontIndex;
          ext.formattingOptions = formattingOptions;
          ext.numberOfRuns = numberOfRuns;
          ext.phoneticText = phoneticText;
          ext.phRuns = new PhRun[phRuns.length];
          for(int i=0; i<ext.phRuns.length; i++) {
             ext.phRuns[i] = new PhRun(
                   phRuns[i].phoneticTextFirstCharacterOffset,
                   phRuns[i].realTextFirstCharacterOffset,
                   phRuns[i].realTextLength
             );
          }
          return ext;
       }
       
       public short getFormattingFontIndex() {
         return formattingFontIndex;
       }
       public short getFormattingOptions() {
         return formattingOptions;
       }
       public int getNumberOfRuns() {
         return numberOfRuns;
       }
       public String getPhoneticText() {
         return phoneticText;
       }
       public PhRun[] getPhRuns() {
         return phRuns;
       }
    }
    public static class PhRun {
       private int phoneticTextFirstCharacterOffset;
       private int realTextFirstCharacterOffset;
       private int realTextLength;
       
       public PhRun(int phoneticTextFirstCharacterOffset,
            int realTextFirstCharacterOffset, int realTextLength) {
         this.phoneticTextFirstCharacterOffset = phoneticTextFirstCharacterOffset;
         this.realTextFirstCharacterOffset = realTextFirstCharacterOffset;
         this.realTextLength = realTextLength;
      }
      private PhRun(LittleEndianInput in) {
          phoneticTextFirstCharacterOffset = in.readUShort();
          realTextFirstCharacterOffset = in.readUShort();
          realTextLength = in.readUShort();
       }
       private void serialize(ContinuableRecordOutput out) {
          out.writeContinueIfRequired(6);
          out.writeShort(phoneticTextFirstCharacterOffset);
          out.writeShort(realTextFirstCharacterOffset);
          out.writeShort(realTextLength);
       }
    }

    private UnicodeString() {
     //Used for clone method.
    }

    public UnicodeString(String str)
    {
      setString(str);
    }



    public int hashCode()
    {
        int stringHash = 0;
        if (field_3_string != null) {
            stringHash = field_3_string.hashCode();
        }
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
        if (!(o instanceof UnicodeString)) {
            return false;
        }
        UnicodeString other = (UnicodeString) o;

        //OK lets do this in stages to return a quickly, first check the actual string
        if (field_1_charCount != other.field_1_charCount
            || field_2_optionflags != other.field_2_optionflags
            || !field_3_string.equals(other.field_3_string)) {
            return false;
        }

        //OK string appears to be equal but now lets compare formatting runs
        if (field_4_format_runs == null) {
            // Strings are equal, and there are not formatting runs.
            return (other.field_4_format_runs == null);
        } else if (other.field_4_format_runs == null) {
            // Strings are equal, but one or the other has formatting runs
            return false;
        }

        //Strings are equal, so now compare formatting runs.
        int size = field_4_format_runs.size();
        if (size != other.field_4_format_runs.size()) {
          return false;
        }

        for (int i=0;i<size;i++) {
          FormatRun run1 = field_4_format_runs.get(i);
          FormatRun run2 = other.field_4_format_runs.get(i);

          if (!run1.equals(run2)) {
            return false;
          }
        }

        // Well the format runs are equal as well!, better check the ExtRst data
        if (field_5_ext_rst == null) {
            return (other.field_5_ext_rst == null);
        } else if (other.field_5_ext_rst == null) {
            return false;
        }
            
       return field_5_ext_rst.equals(other.field_5_ext_rst);
    }

    /**
     * construct a unicode string record and fill its fields, ID is ignored
     * @param in the RecordInputstream to read the record from
     */
    public UnicodeString(RecordInputStream in) {
        field_1_charCount   = in.readShort();
        field_2_optionflags = in.readByte();

        int runCount = 0;
        int extensionLength = 0;
        //Read the number of rich runs if rich text.
        if (isRichText()) {
            runCount = in.readShort();
        }
        //Read the size of extended data if present.
        if (isExtendedText()) {
            extensionLength = in.readInt();
        }

        boolean isCompressed = ((field_2_optionflags & 1) == 0);
        int cc = getCharCount();
        field_3_string = (isCompressed) ? in.readCompressedUnicode(cc) : in.readUnicodeLEString(cc);

        if (isRichText() && (runCount > 0)) {
          field_4_format_runs = new ArrayList<>(runCount);
          for (int i=0;i<runCount;i++) {
            field_4_format_runs.add(new FormatRun(in));
          }
        }

        if (isExtendedText() && (extensionLength > 0)) {
          field_5_ext_rst = new ExtRst(new ContinuableRecordInput(in), extensionLength);
          if(field_5_ext_rst.getDataSize()+4 != extensionLength) {
             _logger.log(POILogger.WARN, "ExtRst was supposed to be " + extensionLength + " bytes long, but seems to actually be " + (field_5_ext_rst.getDataSize() + 4));
          }
        }
    }



    /**
     * get the number of characters in the string,
     *  as an un-wrapped int
     *
     * @return number of characters
     */
    public int getCharCount() {
    	if(field_1_charCount < 0) {
    		return field_1_charCount + 65536;
    	}
        return field_1_charCount;
    }

    /**
     * get the number of characters in the string,
     * wrapped as needed to fit within a short
     *
     * @return number of characters
     */
    public short getCharCountShort() {
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
     * @return the actual string this contains as a java String object
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

        for ( int j = 0; j < strlen; j++ ) {
            if ( string.charAt( j ) > 255 ) {
                useUTF16 = true;
                break;
            }
        }
        if (useUTF16) {
          //Set the uncompressed bit
          field_2_optionflags = highByte.setByte(field_2_optionflags);
        } else {
          field_2_optionflags = highByte.clearByte(field_2_optionflags);
        }
    }

    public int getFormatRunCount() {
        return (field_4_format_runs == null) ? 0 : field_4_format_runs.size();
    }

    public FormatRun getFormatRun(int index) {
      if (field_4_format_runs == null) {
		return null;
	  }
      if (index < 0 || index >= field_4_format_runs.size()) {
		return null;
	  }
      return field_4_format_runs.get(index);
    }

    private int findFormatRunAt(int characterPos) {
      int size = field_4_format_runs.size();
      for (int i=0;i<size;i++) {
        FormatRun r = field_4_format_runs.get(i);
        if (r._character == characterPos) {
          return i;
        } else if (r._character > characterPos) {
          return -1;
        }
      }
      return -1;
    }

    /** Adds a font run to the formatted string.
     *
     *  If a font run exists at the current charcter location, then it is
     *  replaced with the font run to be added.
     */
    public void addFormatRun(FormatRun r) {
      if (field_4_format_runs == null) {
		field_4_format_runs = new ArrayList<>();
	  }

      int index = findFormatRunAt(r._character);
      if (index != -1) {
         field_4_format_runs.remove(index);
      }

      field_4_format_runs.add(r);
      //Need to sort the font runs to ensure that the font runs appear in
      //character order
      Collections.sort(field_4_format_runs);

      //Make sure that we now say that we are a rich string
      field_2_optionflags = richText.setByte(field_2_optionflags);
    }

    public Iterator<FormatRun> formatIterator() {
      if (field_4_format_runs != null) {
        return field_4_format_runs.iterator();
      }
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


    public ExtRst getExtendedRst() {
       return this.field_5_ext_rst;
    }
    void setExtendedRst(ExtRst ext_rst) {
      if (ext_rst != null) {
         field_2_optionflags = extBit.setByte(field_2_optionflags);
      } else {
         field_2_optionflags = extBit.clearByte(field_2_optionflags);
      }
      this.field_5_ext_rst = ext_rst;
    }


    /**
     * Swaps all use in the string of one font index
     *  for use of a different font index.
     * Normally only called when fonts have been
     *  removed / re-ordered
     */
    public void swapFontUse(short oldFontIndex, short newFontIndex) {
        if (field_4_format_runs != null) {
            for (FormatRun run : field_4_format_runs) {
                if(run._fontIndex == oldFontIndex) {
                    run._fontIndex = newFontIndex;
                }
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
        StringBuilder buffer = new StringBuilder();

        buffer.append("[UNICODESTRING]\n");
        buffer.append("    .charcount       = ")
            .append(Integer.toHexString(getCharCount())).append("\n");
        buffer.append("    .optionflags     = ")
            .append(Integer.toHexString(getOptionFlags())).append("\n");
        buffer.append("    .string          = ").append(getString()).append("\n");
        if (field_4_format_runs != null) {
          for (int i = 0; i < field_4_format_runs.size();i++) {
            FormatRun r = field_4_format_runs.get(i);
            buffer.append("      .format_run").append(i).append("          = ").append(r).append("\n");
          }
        }
        if (field_5_ext_rst != null) {
          buffer.append("    .field_5_ext_rst          = ").append("\n");
          buffer.append(field_5_ext_rst).append("\n");
        }
        buffer.append("[/UNICODESTRING]\n");
        return buffer.toString();
    }

    /**
     * Serialises out the String. There are special rules
     *  about where we can and can't split onto
     *  Continue records.
     */
    public void serialize(ContinuableRecordOutput out) {
        int numberOfRichTextRuns = 0;
        int extendedDataSize = 0;
        if (isRichText() && field_4_format_runs != null) {
            numberOfRichTextRuns = field_4_format_runs.size();
        }
        if (isExtendedText() && field_5_ext_rst != null) {
            extendedDataSize = 4 + field_5_ext_rst.getDataSize();
        }
       
        // Serialise the bulk of the String
        // The writeString handles tricky continue stuff for us
        out.writeString(field_3_string, numberOfRichTextRuns, extendedDataSize);

        if (numberOfRichTextRuns > 0) {

          //This will ensure that a run does not split a continue
          for (int i=0;i<numberOfRichTextRuns;i++) {
              if (out.getAvailableSpace() < 4) {
                  out.writeContinue();
              }
              FormatRun r = field_4_format_runs.get(i);
              r.serialize(out);
          }
        }

        if (extendedDataSize > 0 && field_5_ext_rst != null) {
           field_5_ext_rst.serialize(out);
        }
    }

    public int compareTo(UnicodeString str) {

        int result = getString().compareTo(str.getString());

        //As per the equals method lets do this in stages
        if (result != 0) {
          return result;
        }

        //OK string appears to be equal but now lets compare formatting runs
        if (field_4_format_runs == null) {
            //Strings are equal, and there are no formatting runs. -> 0
            //Strings are equal, but one or the other has formatting runs -> 1
            return (str.field_4_format_runs == null) ? 0 : 1;
        } else if (str.field_4_format_runs == null) {
            //Strings are equal, but one or the other has formatting runs
            return -1;
        }

        //Strings are equal, so now compare formatting runs.
        int size = field_4_format_runs.size();
        if (size != str.field_4_format_runs.size()) {
          return size - str.field_4_format_runs.size();
        }

        for (int i=0;i<size;i++) {
          FormatRun run1 = field_4_format_runs.get(i);
          FormatRun run2 = str.field_4_format_runs.get(i);

          result = run1.compareTo(run2);
          if (result != 0) {
            return result;
          }
        }

        //Well the format runs are equal as well!, better check the ExtRst data
        if (field_5_ext_rst == null) {
            return (str.field_5_ext_rst == null) ? 0 : 1;
        } else if (str.field_5_ext_rst == null) {
            return -1;
        } else {
            return field_5_ext_rst.compareTo(str.field_5_ext_rst);
        }
    }

    private boolean isRichText() {
      return richText.isSet(getOptionFlags());
    }

    private boolean isExtendedText() {
        return extBit.isSet(getOptionFlags());
    }

    public Object clone() {
        UnicodeString str = new UnicodeString();
        str.field_1_charCount = field_1_charCount;
        str.field_2_optionflags = field_2_optionflags;
        str.field_3_string = field_3_string;
        if (field_4_format_runs != null) {
          str.field_4_format_runs = new ArrayList<>();
          for (FormatRun r : field_4_format_runs) {
            str.field_4_format_runs.add(new FormatRun(r._character, r._fontIndex));
          }
        }
        if (field_5_ext_rst != null) {
           str.field_5_ext_rst = field_5_ext_rst.clone();
        }

        return str;
    }
}

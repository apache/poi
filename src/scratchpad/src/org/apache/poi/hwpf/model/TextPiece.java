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


import java.io.UnsupportedEncodingException;
/**
 * Lightweight representation of a text piece.
 * Works in the character domain, not the byte domain, so you
 *  need to have turned byte references into character
 *  references before getting here.
 *
 * @author Ryan Ackley
 */

public final class TextPiece extends PropertyNode implements Comparable
{
  private boolean _usesUnicode;

  private PieceDescriptor _pd;

  /**
   * @param start Beginning offset in main document stream, in characters.
   * @param end Ending offset in main document stream, in characters.
   * @param text The raw bytes of our text
   */
  public TextPiece(int start, int end, byte[] text, PieceDescriptor pd, int cpStart) {
	  super(start, end, buildInitSB(text, pd));
	  _usesUnicode = pd.isUnicode();
	  _pd = pd;

	  // Validate
	  int textLength = ((StringBuffer)_buf).length();
	  if(end-start != textLength) {
		  throw new IllegalStateException("Told we're for characters " + start + " -> " + end + ", but actually covers " + textLength + " characters!");
	  }
	  if(end < start) {
		  throw new IllegalStateException("Told we're of negative size! start="+start + " end="+end);
	  }
  }

  /**
   * Create the StringBuffer from the text and unicode flag
   */
  private static StringBuffer buildInitSB(byte[] text, PieceDescriptor pd) {
	  String str;
	  try {
		  if(pd.isUnicode()) {
			  str = new String(text, "UTF-16LE");
		  } else {
			  str = new String(text, "Cp1252");
		  }
	  } catch(UnsupportedEncodingException e) {
		  throw new RuntimeException("Your Java is broken! It doesn't know about basic, required character encodings!");
	  }
	  return new StringBuffer(str);
  }

  /**
   * @return If this text piece is unicode
   */
   public boolean isUnicode()
   {
      return _usesUnicode;
   }

   public PieceDescriptor getPieceDescriptor()
   {
     return _pd;
   }

   public StringBuffer getStringBuffer()
   {
     return (StringBuffer)_buf;
   }

   public byte[] getRawBytes()
   {
     try {
       return ((StringBuffer)_buf).toString().getBytes(_usesUnicode ?
           "UTF-16LE" : "Cp1252");
     } catch (UnsupportedEncodingException ignore) {
		  throw new RuntimeException("Your Java is broken! It doesn't know about basic, required character encodings!");
     }
   }

   /**
    * Returns part of the string.
    * Works only in characters, not in bytes!
    * @param start Local start position, in characters
    * @param end Local end position, in characters
    */
   public String substring(int start, int end)
   {
	   StringBuffer buf = (StringBuffer)_buf;

	   // Validate
	   if(start < 0) {
		   throw new StringIndexOutOfBoundsException("Can't request a substring before 0 - asked for " + start);
	   }
	   if(end > buf.length()) {
		   throw new StringIndexOutOfBoundsException("Index " + end + " out of range 0 -> " + buf.length());
	   }
	   if(end < start) {
		   throw new StringIndexOutOfBoundsException("Asked for text from " + start + " to " + end + ", which has an end before the start!");
	   }
	   return buf.substring(start, end);
   }

   /**
    * Adjusts the internal string for deletinging
    *  some characters within this.
    * @param start The start position for the delete, in characters
    * @param length The number of characters to delete
    */
   public void adjustForDelete(int start, int length) {
	   int numChars = length;

	   int myStart = getStart();
	   int myEnd = getEnd();
	   int end = start + numChars;

	   /* do we have to delete from this text piece? */
	   if (start <= myEnd && end >= myStart) {

		   /* find where the deleted area overlaps with this text piece */
		   int overlapStart = Math.max(myStart, start);
		   int overlapEnd = Math.min(myEnd, end);
		   ((StringBuffer)_buf).delete(overlapStart, overlapEnd);
	   }

	   // We need to invoke this even if text from this piece is not being
	   // deleted because the adjustment must propagate to all subsequent
	   // text pieces i.e., if text from tp[n] is being deleted, then
	   // tp[n + 1], tp[n + 2], etc. will need to be adjusted.
	   // The superclass is expected to use a separate sentry for this.
	   super.adjustForDelete(start, length);
   }

   /**
    * Returns the length, in characters
    */
   public int characterLength()
   {
     return (getEnd() - getStart());
   }
   /**
    * Returns the length, in bytes
    */
   public int bytesLength() {
	   return (getEnd() - getStart()) * (_usesUnicode ? 2 : 1);
   }

   public boolean equals(Object o)
   {
     if (limitsAreEqual(o))
     {
       TextPiece tp = (TextPiece)o;
       return getStringBuffer().toString().equals(tp.getStringBuffer().toString()) &&
              tp._usesUnicode == _usesUnicode && _pd.equals(tp._pd);
     }
     return false;
   }


   /**
    * Returns the character position we start at.
    */
   public int getCP()
   {
     return getStart();
   }
}

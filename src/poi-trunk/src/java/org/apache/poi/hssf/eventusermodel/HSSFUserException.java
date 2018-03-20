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


package org.apache.poi.hssf.eventusermodel;

/**
 * <p>This exception is provided as a way for API users to throw 
 * exceptions from their event handling code. By doing so they
 * abort file processing by the HSSFEventFactory and by
 * catching it from outside the HSSFEventFactory.processEvents 
 * method they can diagnose the cause for the abort.</p>
 *
 * <p>The HSSFUserException supports a nested "reason"
 * throwable, i.e. an exception that caused this one to be thrown.</p>
 *
 * <p>The HSSF package does not itself throw any of these 
 * exceptions.</p>
 *
 * @version HSSFUserException.java,v 1.0
 * @since 2002-04-19
 */
public class HSSFUserException extends Exception
{

    private Throwable reason;



    /**
     * <p>Creates a new {@link HSSFUserException}.</p>
     */
    public HSSFUserException()
    {
        super();
    }



    /**
     * <p>Creates a new {@link HSSFUserException} with a message
     * string.</p>
     * 
     * @param msg the error message
     */
    public HSSFUserException(final String msg)
    {
        super(msg);
    }



    /**
     * <p>Creates a new {@link HSSFUserException} with a reason.</p>
     * 
     * @param reason the causing exception
     */
    public HSSFUserException(final Throwable reason)
    {
        super();
        this.reason = reason;
    }



    /**
     * <p>Creates a new {@link HSSFUserException} with a message string
     * and a reason.</p>
     * 
     * @param msg the error message
     * @param reason the causing exception
     */
    public HSSFUserException(final String msg, final Throwable reason)
    {
        super(msg);
        this.reason = reason;
    }



    /**
     * <p>Returns the {@link Throwable} that caused this exception to
     * be thrown or <code>null</code> if there was no such {@link
     * Throwable}.</p>
     * 
     * @return the reason
     */
    public Throwable getReason()
    {
        return reason;
    }

}

/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2000 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Apache" and "Apache Software Foundation" must
 *  not be used to endorse or promote products derived from this
 *  software without prior written permission. For written
 *  permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  nor may "Apache" appear in their name, without prior written
 *  permission of the Apache Software Foundation.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 */
package org.apache.poi.hpsf;

/**
 *  <p>
 *
 *  This exception is the superclass of all other unchecked exceptions thrown in
 *  this package. It supports a nested "reason" throwable, i.e. an exception
 *  that caused this one to be thrown.</p>
 *
 *@author     Rainer Klute (klute@rainer-klute.de)
 *@version    $Id: HPSFRuntimeException.java,v 1.3 2002/05/01 09:31:52 klute Exp
 *      $
 *@since      2002-02-09
 */
public class HPSFRuntimeException extends RuntimeException {

    private Throwable reason;



    /**
     *  <p>
     *
     *  Creates a new {@link HPSFRuntimeException}.</p>
     */
    public HPSFRuntimeException() {
        super();
    }



    /**
     *  <p>
     *
     *  Creates a new {@link HPSFRuntimeException} with a message string.</p>
     *
     *@param  msg  Description of the Parameter
     */
    public HPSFRuntimeException(final String msg) {
        super(msg);
    }



    /**
     *  <p>
     *
     *  Creates a new {@link HPSFRuntimeException} with a reason.</p>
     *
     *@param  reason  Description of the Parameter
     */
    public HPSFRuntimeException(final Throwable reason) {
        super();
        this.reason = reason;
    }



    /**
     *  <p>
     *
     *  Creates a new {@link HPSFRuntimeException} with a message string and a
     *  reason.</p>
     *
     *@param  msg     Description of the Parameter
     *@param  reason  Description of the Parameter
     */
    public HPSFRuntimeException(final String msg, final Throwable reason) {
        super(msg);
        this.reason = reason;
    }



    /**
     *  <p>
     *
     *  Returns the {@link Throwable} that caused this exception to be thrown or
     *  <code>null</code> if there was no such {@link Throwable}.</p>
     *
     *@return    The reason value
     */
    public Throwable getReason() {
        return reason;
    }

}

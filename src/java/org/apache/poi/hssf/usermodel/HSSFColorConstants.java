
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/*
 * HSSFColorConstants.java
 *
 * Created on December 16, 2001, 8:05 AM
 */
package org.apache.poi.hssf.usermodel;

/**
 * contains constants representing colors in the file.
 *
 * NOTE WILL MOST LIKELY BE DEPRECATED BY THE END OF THE 2.0 CYCLE IN FAVOR
 * OF nsph.util.HSSFColor...
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @deprecated use org.apache.poi.hssf.util.HSSFColor instead
 * @see org.apache.poi.hssf.util.HSSFColor
 */

public interface HSSFColorConstants
{
//    public final static short AUTOMATIC       = 0x7fff;
    public final static short AUTOMATIC       = 0x40;
//    public final static short BLACK           = 0x0;    // 0 0 0
    public final static short BLACK           = 0x8;    // 0 0 0
    public final static short BROWN           = 0x3c;   // 153, 51,  0
    public final static short OLIVE_GREEN     = 0x3b;   // 51, 51,  0
    public final static short DARK_GREEN      = 0x3a;   // 0, 51,  0
    public final static short DARK_TEAL       = 0x38;   // 0, 51,102
    public final static short DARK_BLUE       = 0x12;   // 0,  0,128
    public final static short INDIGO          = 0x3e;   // 51, 51,153
    public final static short GREY_80_PERCENT = 0x3f;   // 51, 51, 51
    public final static short DARK_RED        = 0x10;   // 128,  0,  0
    public final static short ORANGE          = 0x35;   // 255,102,  0
    public final static short DARK_YELLOW     = 0x13;   // 128,128,  0
    public final static short GREEN           = 0x11;   // 0,128,  0
    public final static short TEAL            = 0x15;   // 0.128,128
    public final static short BLUE            = 0xc;    // 0,  0,255
    public final static short BLUE_GREY       = 0x36;   // 102,102,153
    public final static short GREY_50_PERCENT = 0x17;   // 128,128,128
    public final static short RED             = 0xa;    // 255,  0,  0
    public final static short LIGHT_ORANGE    = 0x34;   // 255,153,  0
    public final static short LIME            = 0x32;   // 153,204,  0
    public final static short SEA_GREEN       = 0x39;   // 51,153,102
    public final static short AQUA            = 0x31;   // 51,204,204
    public final static short LIGHT_BLUE      = 0x30;   // 51,102,255
    public final static short VIOLET          = 0x14;   // 128,  0,128
    public final static short GREY_40_PERCENT = 0x37;   // 150,150,150
    public final static short PINK            = 0xe;    // 255,  0,255
    public final static short GOLD            = 0x33;   // 255,204,  0
    public final static short YELLOW          = 0xd;    // 255,255,  0
    public final static short BRIGHT_GREEN    = 0xb;    // 0,255,  0
    public final static short TURQUOISE       = 0xf;    // 0,255,255
    public final static short SKY_BLUE        = 0x28;   // 0,204,255
    public final static short PLUM            = 0x3d;   // 153, 51,102
    public final static short GREY_25_PERCENT = 0x16;   // 192,192,192
    public final static short ROSE            = 0x2d;   // 255,153,204
    public final static short TAN             = 0x2f;   // 255,204,153
    public final static short LIGHT_YELLOW    = 0x2b;   // 255,255,153
    public final static short LIGHT_GREEN     = 0x2a;   // 204,255,204
    public final static short LIGHT_TURQUOISE = 0x29;   // 204,255,255
    public final static short PALE_BLUE       = 0x2c;   // 153,204,255
    public final static short LAVENDER        = 0x2e;   // 204,153,255
    public final static short WHITE           = 0x9;    // 255,255,255
}

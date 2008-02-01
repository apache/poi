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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

/**
 * The <code>HyperlinkRecord</code> wraps an HLINK-record 
 *  from the Excel-97 format.
 * Supports only external links for now (eg http://) 
 *
 * @author      Mark Hissink Muller <a href="mailto:mark@hissinkmuller.nl >mark&064;hissinkmuller.nl</a>
 */
public class HyperlinkRecord extends Record implements CellValueRecordInterface
{
    /** Indicates the URL in the Record */
    private static byte[] GUID_OF_URL_MONIKER =
    { -32, -55, -22, 121, -7, -70, -50, 17, -116, -126, 0, -86, 0, 75, -87, 11 };

    /** Indicates the STD_LINK in the Record */
    // MHM: to be added when necessary
    private static byte[] GUID_OF_STD_LINK =  {};

    /** Logger */
    public static final Log log = LogFactory.getLog(HyperlinkRecord.class);

    // quick and dirty
    private static final boolean _DEBUG_ = true;

    public final static short sid = 0x1b8;

    private short field_1_unknown;
    private int field_2_row;
    private short field_3_column;
    private short field_4_xf_index;
    private byte[] field_5_unknown;
    private int field_6_label_opts;
    private int field_7_url_len;
    private int field_8_label_len;
    private String field_9_label;
    private byte[] field_10_unknown;
    private int field_11_url_opts;
    private String field_12_url;

    /** Blank Constructor */
    public HyperlinkRecord()
    {
    }

    /** Real Constructor */
    public HyperlinkRecord(RecordInputStream in)
    {
        super(in);
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.record.CellValueRecordInterface#getColumn()
     */
    public short getColumn()
    {
        return field_3_column;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.record.CellValueRecordInterface#getRow()
     */
    public int getRow()
    {
        return field_2_row;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.record.CellValueRecordInterface#getXFIndex()
     */
    public short getXFIndex()
    {
        return field_4_xf_index;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.record.CellValueRecordInterface#isAfter(org.apache.poi.hssf.record.CellValueRecordInterface)
     */
    public boolean isAfter(CellValueRecordInterface i)
    {
        if (this.getRow() < i.getRow())
        {
            return false;
        }
        if ((this.getRow() == i.getRow()) && (this.getColumn() < i.getColumn()))
        {
            return false;
        }
        if ((this.getRow() == i.getRow()) && (this.getColumn() == i.getColumn()))
        {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.record.CellValueRecordInterface#isBefore(org.apache.poi.hssf.record.CellValueRecordInterface)
     */
    public boolean isBefore(CellValueRecordInterface i)
    {
        if (this.getRow() > i.getRow())
        {
            return false;
        }
        if ((this.getRow() == i.getRow()) && (this.getColumn() > i.getColumn()))
        {
            return false;
        }
        if ((this.getRow() == i.getRow()) && (this.getColumn() == i.getColumn()))
        {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.record.CellValueRecordInterface#isEqual(org.apache.poi.hssf.record.CellValueRecordInterface)
     */
    public boolean isEqual(CellValueRecordInterface i)
    {
        return ((this.getRow() == i.getRow()) && (this.getColumn() == i.getColumn()));
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.record.CellValueRecordInterface#setColumn(short)
     */
    public void setColumn(short col)
    {
        this.field_3_column = col;

    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.record.CellValueRecordInterface#setRow(int)
     */
    public void setRow(int row)
    {
        this.field_2_row = row;

    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.record.CellValueRecordInterface#setXFIndex(short)
     */
    public void setXFIndex(short xf)
    {
        this.field_4_xf_index = xf;

    }

    /**
     * @param in the RecordInputstream to read the record from
     */
    protected void fillFields(RecordInputStream in)
    {
//    	System.err.println(in.currentSid);
//    	System.err.println(in.currentLength);
//    	for(int i=0; i<300; i++) {
//    		System.err.println(in.readByte());
//    	}
//    	if(1==1)
//    		throw new IllegalArgumentException("");
    	
        field_1_unknown = in.readShort();
        field_2_row = in.readUShort(); 
        field_3_column = in.readShort();
        field_4_xf_index = in.readShort();
        
        // Next up is 16 bytes we don't get
        field_5_unknown = new byte[16];
        try {
        in.read(field_5_unknown);
        } catch(IOException e) { throw new IllegalStateException(e); }
        
        // Some sort of opts
        field_6_label_opts = in.readInt();
        
        // Now for lengths, in characters
        field_7_url_len = in.readInt();
        field_8_label_len = in.readInt();
        
        // Now we have the label, as little endian unicode,
        //  with a trailing \0
        field_9_label = in.readUnicodeLEString(field_8_label_len);
        
        // Next up is some more data we can't make sense of
        field_10_unknown = new byte[16];
        try {
        in.read(field_10_unknown);
        } catch(IOException e) { throw new IllegalStateException(e); }
        
        // Might need to nudge the length by one byte
        // This is an empirical hack!
        field_11_url_opts = in.readInt();
        if(field_11_url_opts == 44) {
        	field_7_url_len--;
        }
        
        // Finally it's the URL
        field_12_url = in.readUnicodeLEString(field_7_url_len);
    }
    
    /* (non-Javadoc)
     * @see org.apache.poi.hssf.record.Record#getSid()
     */
    public short getSid()
    {
        return HyperlinkRecord.sid;
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A HYPERLINKRECORD!");
        }
    }

    public int serialize(int offset, byte[] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              ( short )(getRecordSize()-4));
        LittleEndian.putShort(data, 4 + offset, field_1_unknown);
        LittleEndian.putUShort(data, 6 + offset, field_2_row);
        LittleEndian.putShort(data, 8 + offset, field_3_column);
        LittleEndian.putShort(data, 10 + offset, field_4_xf_index);
        
        offset += 12;
        for(int i=0; i<field_5_unknown.length; i++) {
        	data[offset] = field_5_unknown[i];
        	offset++;
        }
        
        LittleEndian.putInt(data, offset, field_6_label_opts);
        offset += 4;
        LittleEndian.putInt(data, offset, field_7_url_len);
        offset += 4;
        LittleEndian.putInt(data, offset, field_8_label_len);
        offset += 4;
        StringUtil.putUnicodeLE(field_9_label, data, offset);
        offset += field_9_label.length()*2;

        for(int i=0; i<field_10_unknown.length; i++) {
        	data[offset] = field_10_unknown[i];
        	offset++;
        }
    	
        LittleEndian.putInt(data, offset, field_11_url_opts);
        offset += 4;
        StringUtil.putUnicodeLE(field_12_url, data, offset);
        
    	return getRecordSize();
    }

    public int getRecordSize()
    {
    	// We have:
    	// 4 shorts
    	// junk
    	// 3 ints
    	// label
    	// junk
    	// int
    	// url
    	return 4 + 4*2 + field_5_unknown.length +
    		3*4 + field_9_label.length()*2 +
    		field_10_unknown.length + 4 +
    		field_12_url.length()*2;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[HYPERLINK RECORD]\n");
        buffer.append("    .row            = ").append(Integer.toHexString(getRow())).append("\n");
        buffer.append("    .column         = ").append(Integer.toHexString(getColumn())).append("\n");
        buffer.append("    .xfindex        = ").append(Integer.toHexString(getXFIndex())).append("\n");
        buffer.append("    .label          = ").append(field_9_label).append("\n");
        buffer.append("    .url            = ").append(field_12_url).append("\n");
        buffer.append("[/HYPERLINK RECORD]\n");
        return buffer.toString();
    }

    /**
     * @return Returns the label.
     */
    public String getLabel()
    {
    	if(field_9_label.length() == 0) {
    		return "";
    	} else {
    		// Trim off \0
            return field_9_label.substring(0, field_9_label.length() - 1);
    	}
    }

    /**
     * @param label The label to set.
     */
    public void setLabel(String label)
    {
        this.field_9_label = label + '\u0000';
        this.field_8_label_len = field_9_label.length();
    }

    /**
     * @return Returns the Url.
     */
    public URL getUrl() throws MalformedURLException
    {
        return new URL(getUrlString());
    }
    public String getUrlString()
    {
    	if(field_12_url.length() == 0) {
    		return "";
    	} else {
    		// Trim off \0
            return field_12_url.substring(0, field_12_url.length() - 1);
    	}
    }

    /**
     * @param url The url to set.
     */
    public void setUrl(URL url)
    {
    	setUrl(url.toString());
    }
    /**
     * @param url The url to set.
     */
    public void setUrl(String url)
    {
        this.field_12_url = url + '\u0000';
        this.field_7_url_len = field_12_url.length();
    }
}

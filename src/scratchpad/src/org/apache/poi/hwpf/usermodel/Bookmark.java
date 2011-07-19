package org.apache.poi.hwpf.usermodel;

public interface Bookmark
{
    public int getEnd();

    public String getName();

    public int getStart();

    public void setName( String name );
}

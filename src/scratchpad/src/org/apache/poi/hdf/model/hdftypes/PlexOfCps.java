package org.apache.poi.hdf.model.hdftypes;


/**
 * common data structure in a Word file. Contains an array of 4 byte ints in
 * the front that relate to an array of abitrary data structures in the back.
 */
public class PlexOfCps
{
    private int _count;
    private int _offset;
    private int _sizeOfStruct;



    public PlexOfCps(int offset, int size, int sizeOfStruct)
    {
        _count = (size - 4)/(4 + sizeOfStruct);
        _offset = offset;
        _sizeOfStruct = sizeOfStruct;
    }
    public int size()
    {
        return _count;
    }
    public int getStructOffset(int index)
    {
        return (4 * (_count + 1)) + (_sizeOfStruct * index);
    }
}
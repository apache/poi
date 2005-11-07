package org.apache.poi.hslf.record;

/**
 * Interface to define how a record can indicate it cares about what its
 *  parent is, and how it wants to be told which record is its parent.
 * 
 * @author Nick Burch (nick at torchbox dot com)
 */
public interface ParentAwareRecord {
	public RecordContainer getParentRecord();
	public void setParentRecord(RecordContainer parentRecord);
}

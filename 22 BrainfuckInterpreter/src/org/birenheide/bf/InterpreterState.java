package org.birenheide.bf;

public interface InterpreterState {

	public int instructionPointer();
	public int dataPointer();
	/**
	 * Retrieves a snapshot of the data storage.
	 * For large storages (see {@link #getDataSize()}) it is inadvisable
	 * to fetch the entire data as the provider may choke on the request. 
	 * @param start the start index of the snapshot. Must be greater than 0, and 
	 * smaller than <code>end</code> 
	 * @param end the last index of the snapshot, exclusive. Must be smaller or equal than {@link #getDataSize()}.
	 * @return a copy of the region requested.
	 */
	public byte[] dataSnapShot(int start, int end);
	
	/**
	 * The length of the data storage.
	 * @return the length.
	 */
	public int getDataSize();
}

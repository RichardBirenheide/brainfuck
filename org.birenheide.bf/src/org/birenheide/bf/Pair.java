package org.birenheide.bf;

public class Pair<F, S> {
	/**
	 * First value of the tuple. Can be <code>null</code>.
	 */
	public final F fst;
	/**
	 * Second value of the tuple. Can be <code>null</code>.
	 */
	public final S scnd;
	
	/**
	 * Creates a pair from the values given.
	 * <code>null</code> values are permissible.
	 * @param first first value.
	 * @param second second value.
	 */
	public Pair(F first, S second) {
		this.fst = first;
		this.scnd = second;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.fst == null) ? 0 : this.fst.hashCode());
		result = prime * result
				+ ((this.scnd == null) ? 0 : this.scnd.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		Pair other = (Pair) obj;
		if (this.fst == null) {
			if (other.fst != null) {
				return false;
			}
		} else if (!this.fst.equals(other.fst)) {
			return false;
		}
		if (this.scnd == null) {
			if (other.scnd != null) {
				return false;
			}
		} else if (!this.scnd.equals(other.scnd)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Pair [first=" + this.fst + ", second=" + this.scnd + "]";
	}
}

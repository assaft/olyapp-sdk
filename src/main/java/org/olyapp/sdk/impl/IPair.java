package org.olyapp.sdk.impl;

import org.olyapp.sdk.Pair;


public class IPair<T> implements Pair<T> {
	private final T first;
	private final T second;

	public IPair(T first, T second) {
		super();
		this.first = first;
		this.second = second;
	}
	
	public T getFirst() {
		return first;
	}

	public T getSecond() {
		return second;
	}
	
}

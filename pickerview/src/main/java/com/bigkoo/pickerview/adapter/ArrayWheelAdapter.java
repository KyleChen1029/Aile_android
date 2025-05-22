package com.bigkoo.pickerview.adapter;

import java.util.ArrayList;

/**
 * The simple Array wheel adapter
 * @param <T> the element type
 */
public class ArrayWheelAdapter<T> implements WheelAdapter {
	
	/** The default items length */
	public static final int DEFAULT_LENGTH = 4;
	
	// items
	private final ArrayList<T> items;

	/**
	 * Contructor
	 * @param items the items
	 */
	public ArrayWheelAdapter(ArrayList<T> items) {
        this.items = items;
	}

	@Override
	public Object getItem(int index) {
		if (index >= 0 && index < items.size()) {
			return items.get(index);
		}
		return "";
	}

	@Override
	public int getItemsCount() {
		return items.size();
	}

	@Override
	public int indexOf(Object o){
		return items.indexOf(o);
	}

}

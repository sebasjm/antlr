package org.antlr.misc;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/** A hash table that maps a key to a list of elements not just a single. */
public class MultiMap<K, V> extends HashMap<K, List<V>> {
	public void map(K key, V value) {
		List<V> elementsForKey = get(key);
		if ( elementsForKey==null ) {
			elementsForKey = new ArrayList<V>();
			super.put(key, elementsForKey);
		}
		elementsForKey.add(value);
	}
}

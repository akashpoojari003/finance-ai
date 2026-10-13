package com.example.ai_finance_manager.ai_finance.ai;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class LruCache<K, V> {

    private final Map<K, V> map;

    public LruCache(int maxSize) {
        this.map = Collections.synchronizedMap(new LinkedHashMap<K, V>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxSize;
            }
        });
    }

    public V get(K key) {
        return map.get(key);
    }

    public void put(K key, V value) {
        map.put(key, value);
    }
}

package io.lionweb.client.utils;

import java.util.*;

public class IdentityMultimap<K, V> {
  private final Map<K, List<V>> map = new IdentityHashMap<>();

  public void put(K key, V value) {
    map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
  }

  public List<V> get(K key) {
    return map.getOrDefault(key, Collections.emptyList());
  }

  public boolean containsKey(K key) {
    return map.containsKey(key);
  }

  public Set<K> keySet() {
    return map.keySet();
  }

  public Collection<List<V>> values() {
    return map.values();
  }

  public void remove(K key, V value) {
    List<V> values = map.get(key);
    if (values != null) {
      values.remove(value);
      if (values.isEmpty()) {
        map.remove(key);
      }
    }
  }

  public void removeAll(K key) {
    map.remove(key);
  }

  public void clear() {
    map.clear();
  }
}

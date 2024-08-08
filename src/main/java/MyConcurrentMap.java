import java.util.*;

public class MyConcurrentMap<K, V> implements Map<K, V> {
    private static class Entry<K, V> {
        final K key;
        V value;
        Entry<K, V> next;

        Entry(K key, V value, Entry<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private static final int DEFAULT_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private Entry<K, V>[] table;
    private int size;
    private int threshold;
    private final Object[] locks;

    @SuppressWarnings("unchecked")
    public MyConcurrentMap() {
        table = new Entry[DEFAULT_CAPACITY];
        threshold = (int) (DEFAULT_CAPACITY * DEFAULT_LOAD_FACTOR);
        locks = new Object[DEFAULT_CAPACITY];
        for (int i = 0; i < DEFAULT_CAPACITY; i++) {
            locks[i] = new Object();
        }
    }

    private int hash(Object key) {
        return Objects.hashCode(key) & (table.length - 1);
    }

    @Override
    public V put(K key, V value) {
        int hash = hash(key);
        synchronized (locks[hash % locks.length]) {
            for (Entry<K, V> e = table[hash]; e != null; e = e.next) {
                if (e.key.equals(key)) {
                    V oldValue = e.value;
                    e.value = value;
                    return oldValue;
                }
            }

            table[hash] = new Entry<>(key, value, table[hash]);

            if (++size >= threshold) {
                resize();
            }

            return null;
        }
    }

    @Override
    public V get(Object key) {
        int hash = hash(key);
        synchronized (locks[hash % locks.length]) {
            for (Entry<K, V> e = table[hash]; e != null; e = e.next) {
                if (e.key.equals(key)) {
                    return e.value;
                }
            }
            return null;
        }
    }

    @Override
    public V remove(Object key) {
        int hash = hash(key);
        synchronized (locks[hash % locks.length]) {
            Entry<K, V> previous = null;
            for (Entry<K, V> e = table[hash]; e != null; e = e.next) {
                if (e.key.equals(key)) {
                    if (previous == null) {
                        table[hash] = e.next;
                    } else {
                        previous.next = e.next;
                    }
                    size--;
                    return e.value;
                }
                previous = e;
            }
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Entry<K, V>[] oldTable = table;
        table = new Entry[oldTable.length * 2];
        threshold = (int) (table.length * DEFAULT_LOAD_FACTOR);
        size = 0;

        for (Entry<K, V> e : oldTable) {
            while (e != null) {
                put(e.key, e.value);
                e = e.next;
            }
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        synchronized (this) {
            for (Entry<K, V> entry : table) {
                for (Entry<K, V> e = entry; e != null; e = e.next) {
                    if (e.value.equals(value)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        synchronized (this) {
            table = new Entry[DEFAULT_CAPACITY];
            size = 0;
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        synchronized (this) {
            for (Entry<K, V> entry : table) {
                for (Entry<K, V> e = entry; e != null; e = e.next) {
                    keys.add(e.key);
                }
            }
        }
        return keys;
    }

    @Override
    public Collection<V> values() {
        Collection<V> values = new ArrayList<>();
        synchronized (this) {
            for (Entry<K, V> entry : table) {
                for (Entry<K, V> e = entry; e != null; e = e.next) {
                    values.add(e.value);
                }
            }
        }
        return values;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> entrySet = new HashSet<>();
        synchronized (this) {
            for (Entry<K, V> entry : table) {
                for (Entry<K, V> e = entry; e != null; e = e.next) {
                    entrySet.add(new AbstractMap.SimpleEntry<>(e.key, e.value));
                }
            }
        }
        return entrySet;
    }
}

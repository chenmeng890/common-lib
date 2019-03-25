package yooso.analyzer;

/** 主要参考HashMap, key的类型为char,value的的类型为CharDictInfo */
public class CharDictMap {
  static final int DEFAULT_INITIAL_CAPACITY = 16;
  static final int MAXIMUM_CAPACITY = 1 << 30;
  static final float DEFAULT_LOAD_FACTOR = 0.75f;

  transient Entry[] table;
  transient int size;
  int threshold;
  final float loadFactor;
  transient volatile int modCount;
  int indexFor_length;

  public CharDictMap() {
    this.loadFactor = DEFAULT_LOAD_FACTOR;
    threshold = (int) (DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
    table = new Entry[DEFAULT_INITIAL_CAPACITY];
    indexFor_length = table.length -1;
    //init();
  }

  static int hash(int h) {
      h ^= (h >>> 20) ^ (h >>> 12);
      return h ^ (h >>> 7) ^ (h >>> 4);
  }

  static int indexFor(int h, int length) {
      return h & (length-1);
  }

  public final CharDictInfo get(char key) {
      int hash = hash(key);
      int i = indexFor(hash, table.length);
      Entry e = table[i];
      while (e != null) {
          if (e.hash == hash && e.key == key)
              return e.value;
          e = e.next;
      }
      return null;
  }

  public final boolean containsKey(char key) {
    int hash = hash(key);
    int i = indexFor(hash, table.length);
    Entry e = table[i];
    while (e != null) {
        if (e.hash == hash && e.key == key)
            return true;
        e = e.next;
    }
    return false;
  }

  public Iterator getIterator() {
    return new Iterator();
  }
  public int size() {
      return size;
  }

  void addEntry(int hash, char key, CharDictInfo value, int bucketIndex) {
      table[bucketIndex] = new Entry(hash, key, value, table[bucketIndex]);
      if (size++ >= threshold)
          resize(2 * table.length);
  }

  void resize(int newCapacity) {
      Entry[] oldTable = table;
      int oldCapacity = oldTable.length;
      if (oldCapacity == MAXIMUM_CAPACITY) {
          threshold = Integer.MAX_VALUE;
          return;
      }

      Entry[] newTable = new Entry[newCapacity];
      transfer(newTable);
      table = newTable;
      indexFor_length = table.length -1;
      threshold = (int)(newCapacity * loadFactor);
  }

  void transfer(Entry[] newTable) {
      Entry[] src = table;
      int newCapacity = newTable.length;
      for (int j = 0; j < src.length; j++) {
          Entry e = src[j];
          if (e != null) {
              src[j] = null;
              do {
                  Entry next = e.next;
                  int i = indexFor(e.hash, newCapacity);
                  e.next = newTable[i];
                  newTable[i] = e;
                  e = next;
              } while (e != null);
          }
      }
  }

  public CharDictInfo put(char key, CharDictInfo value) {
    int hash = hash(key);
    int i = indexFor(hash, table.length);

    for (Entry e = table[i]; e != null; e = e.next) {
        if (e.hash == hash && e.key == key) {
            CharDictInfo oldValue = e.value;
            e.value = value;
            //e.recordAccess(this);
            return oldValue;
        }
    }

    modCount++;
    addEntry(hash, key, value, i);
    return null;
  }

  static class Entry {
    final char key;
    CharDictInfo value;
    final int hash;
    Entry next;

    /**
     * Create new entry.
     */
    Entry(int h, char k, CharDictInfo v, Entry n) {
      value = v;
      next = n;
      key = k;
      hash = h;
    }
  }

  public class Iterator {
    int index; // current slot
    Entry current; // current entry

    Iterator() {
    }

    public boolean next() {
      if (current == null) {
        if (size == 0) return false;
      }
      else {
        current = current.next;
        if (current != null) return true;
      }
      while (current == null && index < table.length)
        current = table[index++];
      return current != null;
    }

    public char getCurrentKey() {
      return current.key;
    }
    public CharDictInfo getCurrentValue() {
      return current.value;
    }

  }
}


package io.lionweb.client.inmemory;

import java.util.BitSet;

/**
 * Probabilistic membership filter for node IDs within a single cold partition.
 *
 * <p>Uses the Kirsch-Mitzenmacher double-hashing trick: two independent 32-bit hashes derived from
 * a single 64-bit FNV-1a hash, combined as {@code h1 + i * h2} for each of the k hash functions.
 * This avoids the cost of k independent hash computations while maintaining good independence.
 *
 * <p>Sized for ~1% false-positive rate: m ≈ 9.6 bits per expected element. At 67 000 nodes per
 * partition this is roughly 80 KB per filter — negligible compared to the serialized partition data
 * that would otherwise be read from disk.
 *
 * <p>False negatives are impossible: a node ID that was {@link #add added} will always return
 * {@code true} from {@link #mightContain}. A {@code false} return is a definitive "not present" and
 * allows skipping the disk read entirely.
 */
class PartitionBloomFilter {

  private static final int NUM_HASH_FUNCTIONS = 7; // optimal for ~1% FPR with m ≈ 9.6 bits/element
  private static final double BITS_PER_ELEMENT = 9.6;

  private final BitSet bits;
  private final int numBits;

  PartitionBloomFilter(int expectedElements) {
    this.numBits = Math.max(64, (int) Math.ceil(expectedElements * BITS_PER_ELEMENT));
    this.bits = new BitSet(numBits);
  }

  void add(String element) {
    long h = hash64(element);
    int h1 = (int) (h >>> 32);
    int h2 = (int) h;
    for (int i = 0; i < NUM_HASH_FUNCTIONS; i++) {
      bits.set(bucketIndex(h1, h2, i));
    }
  }

  boolean mightContain(String element) {
    long h = hash64(element);
    int h1 = (int) (h >>> 32);
    int h2 = (int) h;
    for (int i = 0; i < NUM_HASH_FUNCTIONS; i++) {
      if (!bits.get(bucketIndex(h1, h2, i))) {
        return false;
      }
    }
    return true;
  }

  private int bucketIndex(int h1, int h2, int i) {
    // Treat h1 and h2 as unsigned, combine via double hashing, then reduce modulo numBits.
    // Using unsigned long arithmetic avoids negative indices without Math.abs.
    long combined = (h1 & 0xFFFFFFFFL) + (long) i * (h2 & 0xFFFFFFFFL);
    return (int) (combined % numBits);
  }

  /** 64-bit FNV-1a hash with SplitMix64 finalisation for better bit dispersion. */
  private static long hash64(String s) {
    long h = 0xcbf29ce484222325L;
    for (int i = 0; i < s.length(); i++) {
      h ^= s.charAt(i);
      h *= 0x00000100000001B3L;
    }
    h ^= h >>> 30;
    h *= 0xbf58476d1ce4e5b9L;
    h ^= h >>> 27;
    h *= 0x94d049bb133111ebL;
    h ^= h >>> 31;
    return h;
  }
}

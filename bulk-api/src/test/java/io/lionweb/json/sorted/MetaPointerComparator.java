package io.lionweb.json.sorted;

import io.lionweb.lioncore.java.serialization.data.MetaPointer;

import java.util.Comparator;
import java.util.Objects;

/**
 * Compares {@link MetaPointer }s
 * by {@link Comparator#naturalOrder() } of language, version, and key (in that order).
 */
public class MetaPointerComparator implements Comparator<MetaPointer> {
  @Override
  public int compare(MetaPointer a, MetaPointer b) {
    int lang = Objects.compare(a.getLanguage(), b.getLanguage(), Comparator.nullsLast(Comparator.naturalOrder()));
    if (lang != 0) {
      return lang;
    }
    int version = Objects.compare(a.getVersion(), b.getVersion(), Comparator.nullsLast(Comparator.naturalOrder()));
    if (version != 0) {
      return version;
    }
    return Objects.compare(a.getKey(), b.getKey(), Comparator.nullsLast(Comparator.naturalOrder()));
  }
}

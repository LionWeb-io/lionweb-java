package io.lionweb.serialization.data;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class SerializedContainmentValueTest {

  private static MetaPointer mp(String key) {
    return MetaPointer.get("L", "1", key);
  }

  @Test
  public void constructorCopiesInputListAndExposesUnmodifiableView() {
    MetaPointer c = mp("cont");
    ArrayList<String> input = new ArrayList<>(Arrays.asList("a", "b"));
    SerializedContainmentValue scv = new SerializedContainmentValue(c, input);

    // Defensive copy in constructor
    input.add("c");
    assertEquals(Arrays.asList("a", "b"), scv.getValue());

    // Unmodifiable view
    assertThrows(UnsupportedOperationException.class, () -> scv.getValue().add("x"));
  }

  @Test
  public void gettersReturnExpectedValues() {
    MetaPointer c = mp("contX");
    SerializedContainmentValue scv = new SerializedContainmentValue(c, Arrays.asList("n1", "n2"));

    assertSame(c, scv.getMetaPointer());
    assertEquals(Arrays.asList("n1", "n2"), scv.getValue());
  }

  @Test
  public void setValueReplacesContentAndIsExposedUnmodifiable() {
    SerializedContainmentValue scv =
        new SerializedContainmentValue(mp("cont"), Arrays.asList("a", "b"));

    scv.setValue(Arrays.asList("x", "y", "z"));
    assertEquals(Arrays.asList("x", "y", "z"), scv.getValue());

    assertThrows(UnsupportedOperationException.class, () -> scv.getValue().remove("x"));
  }

  @Test
  public void removeChildRemovesAndReturnsFlag() {
    SerializedContainmentValue scv =
        new SerializedContainmentValue(mp("cont"), new ArrayList<>(Arrays.asList("a", "b", "c")));

    assertTrue(scv.removeChild("b"));
    assertEquals(Arrays.asList("a", "c"), scv.getValue());

    assertFalse(scv.removeChild("missing"));
    assertEquals(Arrays.asList("a", "c"), scv.getValue());
  }

  @Test
  public void removeChildRemovesOnlyOneOccurrence() {
    SerializedContainmentValue scv =
        new SerializedContainmentValue(mp("cont"), new ArrayList<>(Arrays.asList("x", "y", "y")));

    assertTrue(scv.removeChild("y"));
    assertEquals(Arrays.asList("x", "y"), scv.getValue());
  }

  @Test
  public void equalsAndHashCodeDependOnMetaPointerAndValue() {
    MetaPointer c1 = mp("cont");
    MetaPointer c2 = mp("cont2");
    SerializedContainmentValue a = new SerializedContainmentValue(c1, Arrays.asList("n1", "n2"));
    SerializedContainmentValue b = new SerializedContainmentValue(c1, Arrays.asList("n1", "n2"));
    SerializedContainmentValue c =
        new SerializedContainmentValue(c1, Collections.singletonList("n1"));
    SerializedContainmentValue d = new SerializedContainmentValue(c2, Arrays.asList("n1", "n2"));

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());

    assertNotEquals(a, c);
    assertNotEquals(a, d);
  }

  @Test
  public void toStringContainsKeyInfo() {
    SerializedContainmentValue scv =
        new SerializedContainmentValue(mp("cont"), Arrays.asList("a", "b"));
    String s = scv.toString();
    assertNotNull(s);
    assertTrue(s.contains("metaPointer"));
    assertTrue(s.contains("value"));
  }
}

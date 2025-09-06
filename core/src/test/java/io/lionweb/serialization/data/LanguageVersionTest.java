package io.lionweb.serialization.data;

import static org.junit.Assert.*;

import io.lionweb.language.Language;
import org.junit.Test;

public class LanguageVersionTest {

  @Test
  public void ofReturnsInternedInstanceForSameKeyAndVersion() {
    LanguageVersion a = LanguageVersion.of("lang-key", "1.0");
    LanguageVersion b = LanguageVersion.of("lang-key", "1.0");

    assertSame("Expected canonical instance for same key+version", a, b);
    assertEquals(
        new LanguageVersionEqualsProxy("lang-key", "1.0"), new LanguageVersionEqualsProxy(a));
  }

  @Test
  public void ofHandlesNullValuesAndInterns() {
    LanguageVersion a = LanguageVersion.of(null, null);
    LanguageVersion b = LanguageVersion.of(null, null);
    LanguageVersion c = LanguageVersion.of("k", null);
    LanguageVersion d = LanguageVersion.of("k", null);

    assertSame(a, b);
    assertSame(c, d);
    assertNotSame(a, c);
    assertNull(a.getKey());
    assertNull(a.getVersion());
    assertEquals("k", c.getKey());
    assertNull(c.getVersion());
  }

  @Test
  public void internReturnsCanonicalInstance() {
    LanguageVersion a = LanguageVersion.of("A", "v");
    LanguageVersion b = LanguageVersion.intern(a);
    assertSame(a, b);
  }

  @Test
  public void fromLanguageProducesInternedInstance() {
    Language lang = new Language();
    lang.setKey("my-lang");
    lang.setVersion("2024.1");

    LanguageVersion lv1 = LanguageVersion.fromLanguage(lang);
    LanguageVersion lv2 = LanguageVersion.of("my-lang", "2024.1");

    assertSame(lv2, lv1);
    assertEquals("my-lang", lv1.getKey());
    assertEquals("2024.1", lv1.getVersion());
  }

  @Test
  public void fromMetaPointerProducesInternedInstance() {
    MetaPointer mp = MetaPointer.get("L", "2.0", "some-key");
    LanguageVersion lv = LanguageVersion.fromMetaPointer(mp);

    assertEquals("L", lv.getKey());
    assertEquals("2.0", lv.getVersion());
    assertSame(LanguageVersion.of("L", "2.0"), lv);
  }

  @Test
  public void equalsAndHashCodeRespectKeyAndVersion() {
    LanguageVersion a1 = LanguageVersion.of("A", "1");
    LanguageVersion a2 = LanguageVersion.of("A", "1");
    LanguageVersion b = LanguageVersion.of("A", "2");
    LanguageVersion c = LanguageVersion.of("B", "1");
    LanguageVersion n1 = LanguageVersion.of(null, "1");
    LanguageVersion n2 = LanguageVersion.of(null, "1");
    LanguageVersion n3 = LanguageVersion.of(null, null);

    assertEquals(a1, a2);
    assertEquals(a1.hashCode(), a2.hashCode());

    assertNotEquals(a1, b);
    assertNotEquals(a1, c);
    assertNotEquals(b, c);

    assertEquals(n1, n2);
    assertEquals(n1.hashCode(), n2.hashCode());
    assertNotEquals(n1, n3);
  }

  @Test
  public void toStringContainsKeyAndVersion() {
    LanguageVersion a = LanguageVersion.of("X", "9");
    String s = a.toString();
    assertTrue(s.contains("key='X'"));
    assertTrue(s.contains("version='9'"));
    // Sanity check on the prefix name used
    assertTrue("toString should start with UsedLanguage{", s.startsWith("UsedLanguage{"));
  }

  // Helper proxy to compare by value for equals tests without relying on reference equality
  private static class LanguageVersionEqualsProxy {
    final String key;
    final String version;

    LanguageVersionEqualsProxy(String key, String version) {
      this.key = key;
      this.version = version;
    }

    LanguageVersionEqualsProxy(LanguageVersion lv) {
      this.key = lv.getKey();
      this.version = lv.getVersion();
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof LanguageVersionEqualsProxy)) return false;
      LanguageVersionEqualsProxy other = (LanguageVersionEqualsProxy) o;
      return java.util.Objects.equals(key, other.key)
          && java.util.Objects.equals(version, other.version);
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(key, version);
    }
  }
}

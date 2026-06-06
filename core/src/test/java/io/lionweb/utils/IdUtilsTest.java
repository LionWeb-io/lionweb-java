package io.lionweb.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class IdUtilsTest {

  @Test
  public void testCleanString() {
    assertEquals("", IdUtils.cleanString(""));
    assertEquals("a", IdUtils.cleanString("a"));
    assertEquals("a-b", IdUtils.cleanString("a@b"));
    assertEquals("---FF-", IdUtils.cleanString("(@%FF?"));
    assertEquals("123_456", IdUtils.cleanString("123_456"));
    assertEquals("123-456", IdUtils.cleanString("123-456"));
  }

  @Test
  public void testEncodeToValidId() {
    String encoded = IdUtils.encodeToValidId("hello world");
    assertTrue(IdUtils.isValidID(encoded));

    // Strings that would collide under cleanString must produce distinct encoded IDs
    String a = IdUtils.encodeToValidId("a@b");
    String b = IdUtils.encodeToValidId("a#b");
    assertTrue(!a.equals(b));

    // Unicode and special characters round-trip correctly
    String unicode = IdUtils.encodeToValidId("こんにちは");
    assertTrue(IdUtils.isValidID(unicode));
    assertEquals("こんにちは", IdUtils.decodeFromValidId(unicode));
  }

  @Test
  public void testDecodeFromValidId() {
    assertEquals("hello world", IdUtils.decodeFromValidId(IdUtils.encodeToValidId("hello world")));
    assertEquals("", IdUtils.decodeFromValidId(IdUtils.encodeToValidId("")));
    assertEquals(
        "some/path?query=1&other=2",
        IdUtils.decodeFromValidId(IdUtils.encodeToValidId("some/path?query=1&other=2")));
  }

  @Test
  public void testEncodeProducesValidIds() {
    String[] inputs = {"", "simple", "with spaces", "with/slashes", "special!@#$%^&*()"};
    for (String input : inputs) {
      String encoded = IdUtils.encodeToValidId(input);
      // Empty string encodes to empty Base64; isValidID rejects empty, which is expected
      if (!input.isEmpty()) {
        assertTrue(IdUtils.isValidID(encoded), "Expected valid ID for input: " + input);
      }
    }
  }
}

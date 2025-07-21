package io.lionweb.serialization.data;

import static org.junit.Assert.*;

import io.lionweb.LionWebVersion;
import org.junit.Test;

public class SerializedChunkTest {

  @Test
  public void serializedChunkEquality() {
    SerializedChunk c1 = new SerializedChunk();
    SerializedChunk c2 = new SerializedChunk();
    assertEquals(c1, c2);
    c1.setSerializationFormatVersion(LionWebVersion.v2023_1.getVersionString());
    assertNotEquals(c1, c2);
    c2.setSerializationFormatVersion(LionWebVersion.v2023_1.getVersionString());
    assertEquals(c1, c2);
  }
}

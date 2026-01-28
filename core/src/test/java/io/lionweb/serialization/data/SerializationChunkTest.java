package io.lionweb.serialization.data;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.LionWebVersion;
import org.junit.jupiter.api.Test;

public class SerializationChunkTest {

  @Test
  public void serializationChunkEquality() {
    SerializationChunk c1 = new SerializationChunk();
    SerializationChunk c2 = new SerializationChunk();
    assertEquals(c1, c2);
    c1.setSerializationFormatVersion(LionWebVersion.v2023_1.getVersionString());
    assertNotEquals(c1, c2);
    c2.setSerializationFormatVersion(LionWebVersion.v2023_1.getVersionString());
    assertEquals(c1, c2);
  }
}

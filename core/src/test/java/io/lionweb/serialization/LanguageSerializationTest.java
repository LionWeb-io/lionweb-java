package io.lionweb.serialization;

import static org.junit.Assert.*;

import io.lionweb.language.*;
import io.lionweb.lioncore.LionCore;
import io.lionweb.serialization.data.*;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.Test;

/** Testing various functionalities of JsonSerialization. */
public class LanguageSerializationTest extends SerializationTest {

  /**
   * This test is intended to verify issue #220
   *
   * <p>The relevant part of the specification is: “If the chunk describes a language (M2), it might
   * include instances of builtins' language entities. In this case, builtins MUST be listed as used
   * language like any other language.{fn-org153}”
   */
  @Test
  public void serializeEmptyLanguage() {
    Language myLanguage = new Language();
    JsonSerialization serialization = SerializationProvider.getStandardJsonSerialization();
    SerializedChunk chunk = serialization.serializeTreeToSerializationChunk(myLanguage);
    // Given Language extends INamed, and the properties are listed, LionCoreBuiltins appear as a
    // meta-pointer
    assertEquals(
        new HashSet(
            Arrays.asList(
                new UsedLanguage(
                    LionCore.getInstance().getKey(), LionCore.getInstance().getVersion()),
                new UsedLanguage(
                    LionCoreBuiltins.getInstance().getKey(),
                    LionCoreBuiltins.getInstance().getVersion()))),
        new HashSet(chunk.getLanguages()));
  }
}

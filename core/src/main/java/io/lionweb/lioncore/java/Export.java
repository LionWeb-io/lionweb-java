package io.lionweb.lioncore.java;

import io.lionweb.lioncore.java.metamodel.LionCoreBuiltins;
import io.lionweb.lioncore.java.self.LionCore;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Export {
  public static void main(String[] args) throws IOException {
    String lioncore =
        JsonSerialization.getStandardSerialization()
            .serializeTreesToJsonString(LionCore.getInstance());
    String lioncoreBuiltns =
        JsonSerialization.getStandardSerialization()
            .serializeTreesToJsonString(LionCoreBuiltins.getInstance());

    {
      BufferedWriter writer = new BufferedWriter(new FileWriter(new File("lioncore.json"), true));
      writer.append(lioncore);
      writer.close();
    }
    {
      BufferedWriter writer =
          new BufferedWriter(new FileWriter(new File("lioncorebuiltins.json"), true));
      writer.append(lioncoreBuiltns);
      writer.close();
    }
  }
}

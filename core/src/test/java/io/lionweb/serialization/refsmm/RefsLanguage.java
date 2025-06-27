package io.lionweb.serialization.refsmm;

import io.lionweb.language.Concept;
import io.lionweb.language.Containment;
import io.lionweb.language.Language;
import io.lionweb.language.Reference;

public class RefsLanguage extends Language {
  public static final RefsLanguage INSTANCE = new RefsLanguage();
  public static Concept CONTAINER_NODE;
  public static Concept REF_NODE;

  private RefsLanguage() {
    setID("Refs");
    setKey("Refs");
    setName("Refs");
    setVersion("1");

    // We do not pass INSTANCE as it is still null at this point
    CONTAINER_NODE = new Concept(null, "Container", "RefsMM_Container").setKey("RefsMM_Container");
    REF_NODE = new Concept(null, "Ref", "RefsMM_Ref").setKey("RefsMM_Ref");
    addElement(CONTAINER_NODE);
    addElement(REF_NODE);

    CONTAINER_NODE.addFeature(
        Containment.createOptional("contained", CONTAINER_NODE)
            .setID("RefsMM_Container_contained")
            .setKey("RefsMM_Container_contained"));

    REF_NODE.addFeature(
        Reference.createOptional("referred", REF_NODE)
            .setID("RefsMM_Ref_referred")
            .setKey("RefsMM_Ref_referred"));
  }
}

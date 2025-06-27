package io.lionweb.serialization.simplemath;

import io.lionweb.language.*;

public class SimpleMathLanguage extends Language {
  public static final SimpleMathLanguage INSTANCE = new SimpleMathLanguage();
  public static Concept INT_LITERAL;
  public static Concept SUM;

  private SimpleMathLanguage() {
    setID("SimpleMath");
    setKey("SimpleMath");
    setName("SimpleMath");
    setVersion("1");

    // We do not pass INSTANCE as it is still null at this point
    INT_LITERAL =
        new Concept(null, "IntLiteral", "SimpleMath_IntLiteral").setKey("SimpleMath_IntLiteral");
    SUM = new Concept(null, "Sum", "SimpleMath_Sum").setKey("SimpleMath_Sum");
    addElement(INT_LITERAL);
    addElement(SUM);

    SUM.addFeature(
        Containment.createRequired("left", INT_LITERAL)
            .setID("SimpleMath_Sum_left")
            .setKey("SimpleMath_Sum_left"));
    SUM.addFeature(
        Containment.createRequired("right", INT_LITERAL)
            .setID("SimpleMath_Sum_right")
            .setKey("SimpleMath_Sum_right"));

    INT_LITERAL.addFeature(
        Property.createRequired("value", LionCoreBuiltins.getInteger())
            .setID("SimpleMath_IntLiteral_value")
            .setKey("SimpleMath_IntLiteral_value"));
  }
}

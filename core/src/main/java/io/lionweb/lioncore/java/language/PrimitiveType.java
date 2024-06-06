package io.lionweb.lioncore.java.language;

import io.lionweb.lioncore.java.self.LionCore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This represents an arbitrary primitive value, which is not an enumeration.
 *
 * <p>BooleanType, NumberType, and StringType are common PrimitiveTypes.
 *
 * @see org.eclipse.emf.ecore.EDataType Ecore equivalent <i>EDataType</i>
 * @see <a
 *     href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1083243159079">MPS
 *     equivalent <i>PrimitiveDataTypeDeclaration</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SPrimitiveDataType MPS equivalent
 *     <i>SPrimitiveDataType</i> in SModel
 *     <p>All PrimitiveTypes in LionCore are builtin.
 */
public class PrimitiveType extends DataType<PrimitiveType> {
  public PrimitiveType() {
    super();
  }

  public PrimitiveType(@Nonnull String id) {
    super(id);
  }

  public PrimitiveType(@Nullable Language language, @Nullable String name) {
    super(language, name);
  }

  public PrimitiveType(@Nullable Language language, @Nullable String name, String id) {
    super(language, name);
    setID(id);
  }

  @Override
  public Concept getClassifier() {
    return LionCore.getPrimitiveType();
  }
}

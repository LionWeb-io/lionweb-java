package io.lionweb.lioncore.java.emf.mapping;

import io.lionweb.lioncore.java.metamodel.DataType;
import io.lionweb.lioncore.java.metamodel.Enumeration;
import io.lionweb.lioncore.java.metamodel.LionCoreBuiltins;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EcorePackage;

public class DataTypeMapping {

  private Map<EEnum, Enumeration> eEnumsToEnumerations = new HashMap<>();

  public void registerMapping(EEnum eEnum, Enumeration enumeration) {
    eEnumsToEnumerations.put(eEnum, enumeration);
  }

  public Enumeration getEnumeratorForEEnum(EEnum eEnum) {
    return eEnumsToEnumerations.get(eEnum);
  }

  public EDataType toEDataType(DataType dataType) {
    if (dataType.equals(LionCoreBuiltins.getBoolean())) {
      return EcorePackage.eINSTANCE.getEBoolean();
    } else if (dataType.equals(LionCoreBuiltins.getInteger())) {
      return EcorePackage.eINSTANCE.getEInt();
    } else if (dataType.equals(LionCoreBuiltins.getString())) {
      return EcorePackage.eINSTANCE.getEString();
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public DataType convertEClassifierToDataType(EClassifier eClassifier) {
    if (eClassifier.equals(EcorePackage.Literals.ESTRING)) {
      return LionCoreBuiltins.getString();
    }
    if (eClassifier.equals(EcorePackage.Literals.EINT)) {
      return LionCoreBuiltins.getInteger();
    }
    if (eClassifier.equals(EcorePackage.Literals.EBOOLEAN)) {
      return LionCoreBuiltins.getBoolean();
    }
    if (eClassifier.eClass().equals(EcorePackage.Literals.EENUM)) {
      return eEnumsToEnumerations.get((EEnum) eClassifier);
    }
    if (eClassifier.getEPackage().getNsURI().equals("http://www.eclipse.org/emf/2003/XMLType")) {
      if (eClassifier.getName().equals("String")) {
        return LionCoreBuiltins.getString();
      }
      if (eClassifier.getName().equals("Int")) {
        return LionCoreBuiltins.getInteger();
      }
    }
    throw new UnsupportedOperationException();
  }
}

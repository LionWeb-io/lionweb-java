package io.lionweb.lioncore.java.emf.mapping;

import io.lionweb.lioncore.java.language.DataType;
import io.lionweb.lioncore.java.language.Enumeration;
import io.lionweb.lioncore.java.language.LionCoreBuiltins;
import io.lionweb.lioncore.java.language.PrimitiveType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.xml.type.impl.XMLTypePackageImpl;

public class DataTypeMapping {

  private Map<EEnum, Enumeration> eEnumsToEnumerations = new HashMap<>();
  private Map<Enumeration, EEnum> enumerationsToEEnums = new HashMap<>();

  private Map<EDataType, PrimitiveType> eDataTypesToPrimitiveTypes = new HashMap<>();
  private Map<PrimitiveType, EDataType> primitiveTypesToEDataTypes = new HashMap<>();

  public void registerMapping(EEnum eEnum, Enumeration enumeration) {
    eEnumsToEnumerations.put(eEnum, enumeration);
    enumerationsToEEnums.put(enumeration, eEnum);
  }

  public void registerMapping(EDataType eDataType, PrimitiveType primitiveType) {
    eDataTypesToPrimitiveTypes.put(eDataType, primitiveType);
    primitiveTypesToEDataTypes.put(primitiveType, eDataType);
  }

  public Enumeration getEnumerationForEEnum(EEnum eEnum) {
    return eEnumsToEnumerations.get(eEnum);
  }

  public EEnum getEEnumForEnumeration(Enumeration enumeration) {
    return enumerationsToEEnums.get(enumeration);
  }

  public EDataType toEDataType(DataType dataType) {
    if (dataType.equals(LionCoreBuiltins.getBoolean())) {
      return EcorePackage.eINSTANCE.getEBoolean();
    } else if (dataType.equals(LionCoreBuiltins.getInteger())) {
      return EcorePackage.eINSTANCE.getEInt();
    } else if (dataType.equals(LionCoreBuiltins.getString())) {
      return EcorePackage.eINSTANCE.getEString();
    } else if (dataType instanceof Enumeration) {
      Enumeration enumeration = (Enumeration) dataType;
      if (!enumerationsToEEnums.containsKey(enumeration)) {
        throw new IllegalStateException("Unable to convert Enumeration " + enumeration);
      }
      return enumerationsToEEnums.get(enumeration);
    } else {
      throw new UnsupportedOperationException("Unable to map data type " + dataType);
    }
  }

  public DataType convertEClassifierToDataType(EClassifier eClassifier) {
    if (eClassifier.equals(XMLTypePackageImpl.Literals.STRING)) {
      return LionCoreBuiltins.getString();
    }
    if (eClassifier.equals(XMLTypePackageImpl.Literals.INT)) {
      return LionCoreBuiltins.getInteger();
    }
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
    if (eClassifier instanceof EDataType) {
      PrimitiveType result = eDataTypesToPrimitiveTypes.get((EDataType) eClassifier);
      Objects.requireNonNull(result, "No data type for EDataType " + eClassifier);
      return result;
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

package io.lionweb.archive;

import io.lionweb.language.INamed;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.impl.DynamicNode;

public class Department extends DynamicNode implements INamed {

  public Department(String id, String name) {
    super(id, CompanyLanguage.getDepartment());
    ClassifierInstanceUtils.setPropertyValueByName(this, "name", name);
  }

  public Department(String id) {
    super(id, CompanyLanguage.getDepartment());
  }

  @Override
  public String getName() {
    return (String) ClassifierInstanceUtils.getPropertyValueByName(this, "name");
  }
}

package io.lionweb.archive;

import io.lionweb.language.INamed;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.impl.DynamicNode;

public class Role extends DynamicNode implements INamed  {

    public Role(String id) {
        super(id, CompanyLanguage.getRole());
    }

    public Role(String id, String name) {
        super(id, CompanyLanguage.getRole());
        ClassifierInstanceUtils.setPropertyValueByName(this, "name", name);
    }

    public void setSalaryBand(int minSalary, int maxSalary) {
        ClassifierInstanceUtils.setPropertyValueByName(this, "min salary", minSalary);
        ClassifierInstanceUtils.setPropertyValueByName(this, "max salary", maxSalary);
    }

    public int getMinSalary() {
        Object res = ClassifierInstanceUtils.getPropertyValueByName(this, "min salary");
        if (res == null) {
            return -1;
        }
        return (int) res;
    }

    public int getMaxSalary() {
        Object res = ClassifierInstanceUtils.getPropertyValueByName(this, "max salary");
        if (res == null) {
            return -1;
        }
        return (int) res;
    }

    @Override
    public String getName() {
        return (String) ClassifierInstanceUtils.getPropertyValueByName(this, "name");
    }
}

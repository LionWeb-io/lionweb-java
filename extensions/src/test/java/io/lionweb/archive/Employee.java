package io.lionweb.archive;

import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.impl.DynamicNode;

public class Employee extends DynamicNode {

    public Employee(String id, String firstName, String lastName) {
        super(id, CompanyLanguage.getEmployee());
        ClassifierInstanceUtils.setPropertyValueByName(this, "first name", firstName);
        ClassifierInstanceUtils.setPropertyValueByName(this, "last name", lastName);
    }

    public Employee(String id) {
        super(id, CompanyLanguage.getEmployee());
    }

    public void setSalary(int salary) {
        ClassifierInstanceUtils.setPropertyValueByName(this, "salary", salary);
    }

    public int getSalary() {
        Object res = ClassifierInstanceUtils.getPropertyValueByName(this, "salary");
        if (res == null) {
            return -1;
        } else {
            return (int) res;
        }
    }

    public void addDepartment(Department department) {
        ClassifierInstanceUtils.addReferenceByName(this, "departments", department);
    }

    public void addRole(Role role) {
        ClassifierInstanceUtils.addReferenceByName(this, "roles", role);
    }
}

package io.lionweb.archive;

import io.lionweb.language.INamed;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.impl.DynamicNode;

public class Company extends DynamicNode implements INamed {

  public Company(String id) {
    super(id, CompanyLanguage.getCompany());
  }

  public Company(String id, String name) {
    super(id, CompanyLanguage.getCompany());
    ClassifierInstanceUtils.setPropertyValueByName(this, "name", name);
  }

  public Role addRole(String id, String name) {
    Role role = new Role(id, name);
    ClassifierInstanceUtils.addChild(this, "roles", role);
    return role;
  }

  public Department addDepartment(String id, String name) {
    Department department = new Department(id, name);
    ClassifierInstanceUtils.addChild(this, "departments", department);
    return department;
  }

  public Employee addEmployee(String id, String firstName, String lastName) {
    Employee employee = new Employee(id, firstName, lastName);
    ClassifierInstanceUtils.addChild(this, "employees", employee);
    return employee;
  }

  @Override
  public String getName() {
    return (String) ClassifierInstanceUtils.getPropertyValueByName(this, "name");
  }
}

package io.lionweb.archive;

import io.lionweb.LionWebVersion;
import io.lionweb.language.Concept;
import io.lionweb.language.Language;
import io.lionweb.language.LionCoreBuiltins;
import io.lionweb.language.Multiplicity;
import io.lionweb.language.assigners.CommonIDAssigners;
import io.lionweb.language.assigners.CommonKeyAssigners;

public class CompanyLanguage extends Language {

    private static CompanyLanguage language = new CompanyLanguage();

    private CompanyLanguage() {
        super(LionWebVersion.v2023_1);
        setName("CompanyLanguage");
        setVersion("v1.0");
        Concept company = new Concept("Company");
        company.setPartition(true);
        Concept department = new Concept("Department");
        Concept employee = new Concept("Employee");
        Concept role = new Concept("Role");

        company.addImplementedInterface(LionCoreBuiltins.getINamed(LionWebVersion.v2023_1));
        company.addContainment("departments", department, Multiplicity.ZERO_OR_MORE);
        company.addContainment("employees", employee, Multiplicity.ZERO_OR_MORE);
        company.addContainment("roles", role, Multiplicity.ZERO_OR_MORE);

        department.addImplementedInterface(LionCoreBuiltins.getINamed(LionWebVersion.v2023_1));

        employee.addProperty("first name", LionCoreBuiltins.getString(LionWebVersion.v2023_1), Multiplicity.REQUIRED);
        employee.addProperty("last name", LionCoreBuiltins.getString(LionWebVersion.v2023_1), Multiplicity.REQUIRED);
        employee.addProperty("salary", LionCoreBuiltins.getInteger(LionWebVersion.v2023_1), Multiplicity.REQUIRED);
        employee.addReference("departments", department, Multiplicity.ZERO_OR_MORE);
        employee.addReference("roles", role, Multiplicity.ONE_OR_MORE);

        role.addImplementedInterface(LionCoreBuiltins.getINamed(LionWebVersion.v2023_1));
        role.addProperty("min salary", LionCoreBuiltins.getInteger(LionWebVersion.v2023_1), Multiplicity.OPTIONAL);
        role.addProperty("max salary", LionCoreBuiltins.getInteger(LionWebVersion.v2023_1), Multiplicity.OPTIONAL);

        this.addElement(company);
        this.addElement(department);
        this.addElement(employee);
        this.addElement(role);

        CommonIDAssigners.qualifiedIDAssigner.assignIDs(this);
        CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(this);
    }

    public static CompanyLanguage getLanguage() {
        return language;
    }

    public static Concept getCompany() {
        return language.requireConceptByName("Company");
    }

    public static Concept getRole() {
        return language.requireConceptByName("Role");
    }

    public static Concept getDepartment() {
        return language.requireConceptByName("Department");
    }

    public static Concept getEmployee() {
        return language.requireConceptByName("Employee");
    }
}

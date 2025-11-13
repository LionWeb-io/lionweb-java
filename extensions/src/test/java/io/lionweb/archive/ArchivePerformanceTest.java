package io.lionweb.archive;

import io.lionweb.LionWebVersion;
import io.lionweb.serialization.ProtoBufSerialization;
import io.lionweb.serialization.SerializationProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ArchivePerformanceTest {

    private static int nextId = 1;

    public static Company createCompany(Random rnd) {
        return createCompany(rnd, rnd.nextInt(10, 100), rnd.nextInt(5, 30), rnd.nextInt(1000,5000));
    }

    public static List<Company> createCompanies(Random rnd, int numCompanies) {
        List<Company> companies = new ArrayList<>(numCompanies);
        for (int i = 0; i < numCompanies; i++) {
            companies.add(createCompany(rnd));
        }
        return companies;
    }

    public static Company createCompany(Random rnd,
                                        int numRoles,
                                        int numDepartments,
                                        int numEmployees) {

        Company company = new Company("c-" + (nextId++), "Acme Inc.");

        // --- Generate roles ---
        List<Role> roles = new ArrayList<>(numRoles);
        for (int i = 1; i <= numRoles; i++) {
            Role r = company.addRole("r-" + (nextId++), "Role-" + i);

            // Arbitrary salary band: min between 20k–60k, max between 80k–200k.
            int min = 20_000 + rnd.nextInt(40_000);
            int max = min + 60_000 + rnd.nextInt(80_000);
            r.setSalaryBand(min, max);

            roles.add(r);
        }

        // --- Generate departments ---
        List<Department> departments = new ArrayList<>(numDepartments);
        for (int i = 1; i <= numDepartments; i++) {
            Department d = company.addDepartment("d-" + (nextId++), "Dept-" + i);
            departments.add(d);
        }

        // --- Generate employees ---
        for (int i = 1; i <= numEmployees; i++) {
            Employee e = company.addEmployee(
                    "e-" + (nextId++),
                    "Name" + i,
                    "Surname" + i
            );

            // Assign 1–3 random roles
            int roleCount = 1 + rnd.nextInt(Math.min(3, roles.size()));
            for (int k = 0; k < roleCount; k++) {
                Role chosen = roles.get(rnd.nextInt(roles.size()));
                e.addRole(chosen);
            }

            // Assign 0–2 departments
            int deptCount = rnd.nextInt(Math.min(2, departments.size()) + 1);
            for (int k = 0; k < deptCount; k++) {
                Department d = departments.get(rnd.nextInt(departments.size()));
                e.addDepartment(d);
            }
        }

        return company;
    }
    public static void main(String[] args) {
        Random rnd = new Random(12345);
        List<Company> companies = createCompanies(rnd, 100);

        ProtoBufSerialization ser = SerializationProvider.getStandardProtoBufSerialization(LionWebVersion.v2023_1);

        try {
            File file = Files.createTempFile("companies", ".lwa").toFile();
            long t0 = System.currentTimeMillis();
            LionWebArchive.store(file, LionWebVersion.v2023_1, Collections.singletonList(
                    ser.serializeTreeToSerializationChunk(CompanyLanguage.getLanguage())),
                    companies.stream().map(ser::serializeTreeToSerializationChunk).collect(Collectors.toList()));
            long t1 = System.currentTimeMillis();
            System.out.println("Time to store: " + (t1 - t0) + " ms");

            ser.enableDynamicNodes();
            long t2 = System.currentTimeMillis();
            LionWebArchive.loadNodes(file, ser);
            long t3 = System.currentTimeMillis();

            System.out.println("Time to load: " + (t3 - t2) + " ms");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}

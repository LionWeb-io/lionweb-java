package io.lionweb.archive;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.inmemory.InMemoryServer;
import io.lionweb.serialization.ProtoBufSerialization;
import io.lionweb.serialization.SerializationProvider;
import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.extensions.TransferFormat;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class LionWebArchiveTest {

    @Test
    public void createArchive() throws IOException {
        InMemoryServer server = new InMemoryServer();
        server.createRepository(new RepositoryConfiguration("CompaniesRepo", LionWebVersion.v2023_1, HistorySupport.DISABLED));

        Company acme = new Company("c-1", "Acme Inc.");

        Role ceo = acme.addRole("r-1", "CEO");
        ceo.setSalaryBand(50_000, 200_000);

        Role cfo = acme.addRole("r-2", "CFO");
        cfo.setSalaryBand(40_000, 150_000);

        Role developer = acme.addRole("r-3", "Developer");
        developer.setSalaryBand(30_000, 70_000);

        Role salesman = acme.addRole("r-4", "Salesman");
        salesman.setSalaryBand(30_000, 80_000);

        Department marketing = acme.addDepartment("d-1", "Marketing");
        Department sales = acme.addDepartment("d-2", "Sales");
        Department production = acme.addDepartment("d-3", "Production");

        Employee employee1 = acme.addEmployee("e-1", "Gino", "De pinis");
        employee1.addRole(ceo);

        Employee employee2 = acme.addEmployee("e-2", "Ginetto", "De pinis");
        employee2.addRole(cfo);

        Employee employee3 = acme.addEmployee("e-3", "Tizio", "Caius");
        employee3.addRole(salesman);
        employee3.addDepartment(sales);

        ProtoBufSerialization serialization = SerializationProvider.getStandardProtoBufSerialization(LionWebVersion.v2023_1);
        serialization.registerLanguage(CompanyLanguage.getLanguage());
        SerializationChunk chunk = serialization.serializeTreeToSerializationChunk(acme);
        server.createPartitionFromChunk("CompaniesRepo", chunk.getClassifierInstances());

        File archiveFile = Files.createTempFile("lionweb-archive", ".zip").toFile();
        LionWebArchive.store(archiveFile, server, "CompaniesRepo", LionWebVersion.v2023_1,
                TransferFormat.PROTOBUF);

        server.createRepository(new RepositoryConfiguration("ReplicatedCompaniesRepo", LionWebVersion.v2023_1, HistorySupport.DISABLED));
        LionWebArchive.load(archiveFile, server, "ReplicatedCompaniesRepo", LionWebVersion.v2023_1,
                TransferFormat.PROTOBUF);
    }
}

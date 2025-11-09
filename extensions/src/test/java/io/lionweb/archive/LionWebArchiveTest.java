package io.lionweb.archive;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.inmemory.InMemoryServer;
import io.lionweb.language.Classifier;
import io.lionweb.language.Property;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.Node;
import io.lionweb.serialization.Instantiator;
import io.lionweb.serialization.JsonSerialization;
import io.lionweb.serialization.ProtoBufSerialization;
import io.lionweb.serialization.SerializationProvider;
import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.serialization.extensions.TransferFormat;
import io.lionweb.utils.ModelComparator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LionWebArchiveTest {

    private Company createAcmeCompany() {
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
        return acme;
    }

    @Test
    public void storeAndLoadArchiveFromRepository() throws IOException {
        InMemoryServer server = new InMemoryServer();
        server.createRepository(new RepositoryConfiguration("CompaniesRepo", LionWebVersion.v2023_1, HistorySupport.DISABLED));

        ProtoBufSerialization serialization = SerializationProvider.getStandardProtoBufSerialization(LionWebVersion.v2023_1);
        serialization.registerLanguage(CompanyLanguage.getLanguage());
        SerializationChunk chunk = serialization.serializeTreeToSerializationChunk(createAcmeCompany());
        server.createPartitionFromChunk("CompaniesRepo", chunk.getClassifierInstances());

        File archiveFile = Files.createTempFile("lionweb-archive", ".zip").toFile();
        LionWebArchive.store(archiveFile, server, "CompaniesRepo", LionWebVersion.v2023_1,
                TransferFormat.PROTOBUF);

        server.createRepository(new RepositoryConfiguration("ReplicatedCompaniesRepo", LionWebVersion.v2023_1, HistorySupport.DISABLED));
        LionWebArchive.load(archiveFile, server, "ReplicatedCompaniesRepo", LionWebVersion.v2023_1,
                TransferFormat.PROTOBUF);
    }

    @Test
    public void loadArchiveAsNodes() throws IOException {
        InMemoryServer server = new InMemoryServer();
        server.createRepository(new RepositoryConfiguration("CompaniesRepo", LionWebVersion.v2023_1, HistorySupport.DISABLED));

        ProtoBufSerialization serialization = SerializationProvider.getStandardProtoBufSerialization(LionWebVersion.v2023_1);
        serialization.registerLanguage(CompanyLanguage.getLanguage());
        Company acmeOriginal = createAcmeCompany();
        SerializationChunk chunk = serialization.serializeTreeToSerializationChunk(acmeOriginal);
        server.createPartitionFromChunk("CompaniesRepo", chunk.getClassifierInstances());

        File archiveFile = Files.createTempFile("lionweb-archive", ".zip").toFile();
        LionWebArchive.store(archiveFile, server, "CompaniesRepo", LionWebVersion.v2023_1,
                TransferFormat.PROTOBUF);

        ProtoBufSerialization protoBufSerialization = SerializationProvider.getStandardProtoBufSerialization(LionWebVersion.v2023_1);
        protoBufSerialization.getInstantiator().registerCustomDeserializer(CompanyLanguage.getCompany().getID(), (Instantiator.ClassifierSpecificInstantiator<Company>) (classifier, serializedClassifierInstance, deserializedNodesByID, propertiesValues) -> new Company(serializedClassifierInstance.getID()));
        protoBufSerialization.getInstantiator().registerCustomDeserializer(CompanyLanguage.getRole().getID(), (Instantiator.ClassifierSpecificInstantiator<Role>) (classifier, serializedClassifierInstance, deserializedNodesByID, propertiesValues) -> new Role(serializedClassifierInstance.getID()));
        protoBufSerialization.getInstantiator().registerCustomDeserializer(CompanyLanguage.getDepartment().getID(), (Instantiator.ClassifierSpecificInstantiator<Department>) (classifier, serializedClassifierInstance, deserializedNodesByID, propertiesValues) -> new Department(serializedClassifierInstance.getID()));
        protoBufSerialization.getInstantiator().registerCustomDeserializer(CompanyLanguage.getEmployee().getID(), (Instantiator.ClassifierSpecificInstantiator<Employee>) (classifier, serializedClassifierInstance, deserializedNodesByID, propertiesValues) -> new Employee(serializedClassifierInstance.getID()));
        protoBufSerialization.registerLanguage(CompanyLanguage.getLanguage());

        List<Node> nodes = LionWebArchive.load(archiveFile, protoBufSerialization);
        assertEquals(1, nodes.size());
        assertTrue(nodes.get(0) instanceof Company);
        assertTrue(ModelComparator.areEquivalent(acmeOriginal, (Company) nodes.get(0)));
    }
}

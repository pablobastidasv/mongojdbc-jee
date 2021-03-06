package co.pablob.mongo.control;

import co.pablob.mongo.boundary.MongodbCustomizer;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.connection.ClusterSettings;
import org.bson.codecs.configuration.CodecRegistry;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import static co.pablob.mongo.control.Constants.*;

@ApplicationScoped
public class DocumentCollectionManagerProducer {

    private MongoClient mongoClient;
    private MongoDatabase database;

    @Inject
    private CodecRegistry codecRegistry;

    private char[] pwd;
    private String usr;
    private String server;
    private String connectionString;
    private int port;
    private String databaseName;
    private boolean buildConnectionString;
    private String options;

    private static final int NON_PORT = 0;
    private static final int MONGO_DEFAULT_PORT = 27017;

    @Inject
    private Instance<MongodbCustomizer> customizations;

    public DocumentCollectionManagerProducer() {
    }

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        initEnv();

        buildMongodbClient();

        database = mongoClient.getDatabase(databaseName);

        customizations.stream()
                .forEach(c -> c.customize(database));
    }

    private void buildMongodbClient() {
        final MongoClientSettings.Builder builder = MongoClientSettings.builder()
                .codecRegistry(codecRegistry);

        if (Objects.nonNull(connectionString)) {
            builder.applyConnectionString(new ConnectionString(connectionString));
        } else if(buildConnectionString) {
            String connectionStringBuilt = "mongodb+srv://" +
                (Objects.isNull(usr) ? "" : usr) +
                (Objects.isNull(pwd) ? "" : ":" + String.valueOf(pwd)) +
                (Objects.isNull(usr) ? "" : "@") +
                server +
                (port == NON_PORT ? "" : ":" + port) +
                "/" +
                databaseName +
                ("".equals(options) ? "" : "?" + options);

            builder.applyConnectionString(new ConnectionString(connectionStringBuilt));
        } else {
            builder.applyToClusterSettings(this::clusterSettings);
            credentials().ifPresent(builder::credential);
        }

        mongoClient = MongoClients.create(builder.build());
    }

    public void destroy(@Observes @Destroyed(ApplicationScoped.class) Object init) {
        database = null;
        mongoClient.close();
    }

    private void initEnv() {
        usr = System.getenv().get(ENV_MONGO_USERNAME);
        connectionString = System.getenv().get(ENV_MONGO_CONNECTION_STRING);
        pwd = Optional.ofNullable(System.getenv().get(ENV_MONGO_PASSWORD))
            .map(String::toCharArray)
            .orElse(null);
        server = System.getenv().getOrDefault(ENV_MONGO_SERVER, DEFAULT_MONGO_SERVER);
        buildConnectionString = Optional.ofNullable(System.getenv().get(ENV_BUILD_CONNECTION_STRING))
            .map(Boolean::new)
            .orElse(false);
        port = Optional.ofNullable(System.getenv().get(ENV_MONGO_PORT))
            .map(Integer::parseInt)
            .orElse(buildConnectionString ? NON_PORT : MONGO_DEFAULT_PORT);
        options = System.getenv().getOrDefault(ENV_MONGO_OPTIONS, "");

        databaseName = System.getenv().getOrDefault(ENV_DATABASE_NAME, DEFAULT_DATABASE_NAME);
    }

    private Optional<MongoCredential> credentials() {
        if (Objects.isNull(usr) || Objects.isNull(pwd)) {
            return Optional.empty();
        }

        return Optional.of(MongoCredential.createCredential(usr, databaseName, pwd));
    }

    private void clusterSettings(ClusterSettings.Builder builder) {
        builder.hosts(Collections.singletonList(new ServerAddress(server, port)));
    }

    @Produces
    public MongoClient produceMongoClient() {
        return mongoClient;
    }

    @Produces
    @Definition
    @SuppressWarnings("unchecked")
    public <T> MongoCollection<T> provideCollection(InjectionPoint ip) {
        Definition annotation = ip.getAnnotated().getAnnotation(Definition.class);
        Objects.requireNonNull(annotation, "Value with collection name is required.");

        String collectionName = annotation.collection();
        Objects.requireNonNull(collectionName);

        if (collectionName.trim().isEmpty()) {
            throw new IllegalArgumentException("Value must not to be empty.");
        } else {
            return database.getCollection(collectionName, annotation.clazz());
        }
    }
}

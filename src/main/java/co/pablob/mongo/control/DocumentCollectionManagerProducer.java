package co.pablob.mongo.control;

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

    private final CodecRegistry codecRegistry;

    private char[] pwd;
    private String usr;
    private String server;
    private int port;
    private String databaseName;

    @Inject
    public DocumentCollectionManagerProducer(CodecRegistry codecRegistry) {
        this.codecRegistry = codecRegistry;
    }

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        initEnv();

        final MongoClientSettings.Builder builder = MongoClientSettings.builder()
                .applyToClusterSettings(this::clusterSettings)
                .codecRegistry(codecRegistry);

        credentials().ifPresent(builder::credential);

        mongoClient = MongoClients.create(builder.build());
        database = mongoClient.getDatabase(databaseName);
    }

    public void destroy(@Observes @Destroyed(ApplicationScoped.class) Object init) {
        database = null;
        mongoClient.close();
    }

    private void initEnv() {
        usr = System.getenv().get(ENV_MONGO_USERNAME);
        pwd = Optional.ofNullable(System.getenv().get(ENV_MONGO_PASSWORD))
                .map(String::toCharArray)
                .orElse(null);
        server = System.getenv().getOrDefault(ENV_MONGO_SERVER, DEFAULT_MONGO_SERVER);
        port = Optional.ofNullable(System.getenv().get(ENV_MONGO_PORT))
                    .map(Integer::parseInt)
                    .orElse(27017);

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
    public MongoDatabase produceDatabase() {
        return database;
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

        String collectionName = annotation.value();
        Objects.requireNonNull(collectionName);

        if (collectionName.trim().isEmpty()) {
            throw new IllegalArgumentException("Value must not to be empty.");
        } else {
            return database.getCollection(collectionName, annotation.clazz());
        }
    }
}

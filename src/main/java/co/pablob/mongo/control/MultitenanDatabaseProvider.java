package co.pablob.mongo.control;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

public class MultitenanDatabaseProvider {

    @Inject
    private MongoClient client;

    @Inject
    private DatabaseNameProvider databaseNameProvider;

    @Produces
    @RequestScoped
    public MongoDatabase mongoDatabaseProducer(){
        String databaseName = databaseNameProvider.produceDatabaseName();

        return client.getDatabase(databaseName);
    }

}

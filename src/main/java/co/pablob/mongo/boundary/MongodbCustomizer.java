package co.pablob.mongo.boundary;

import com.mongodb.client.MongoDatabase;

public interface MongodbCustomizer {
    void customize(MongoDatabase database);
}

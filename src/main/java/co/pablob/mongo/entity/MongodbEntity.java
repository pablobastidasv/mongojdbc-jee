package co.pablob.mongo.entity;

import org.bson.Document;

import javax.json.bind.JsonbBuilder;

public interface MongodbEntity {
    default Document toDocument(){
        String json = JsonbBuilder.create().toJson(this);
        return Document.parse(json);
    }
}

package co.pablob.mongo.control;

import org.bson.types.ObjectId;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

public class ObjectIdSerializer implements JsonbSerializer<ObjectId> {
    @Override
    public void serialize(ObjectId obj, JsonGenerator generator, SerializationContext ctx) {
        generator.write(obj.toHexString());
    }
}

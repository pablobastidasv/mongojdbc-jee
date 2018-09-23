package co.pablob.mongo.control;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import javax.enterprise.inject.Produces;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class CodecRegistryProvider {

    @Produces
    public CodecRegistry produce(){
        final PojoCodecProvider codecProvider = PojoCodecProvider.builder()
                .automatic(true)
                .build();

        return fromRegistries(getDefaultCodecRegistry(), fromProviders(codecProvider));
    }

}

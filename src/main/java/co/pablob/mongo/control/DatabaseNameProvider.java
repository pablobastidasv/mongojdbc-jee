package co.pablob.mongo.control;

import static co.pablob.mongo.control.Constants.DEFAULT_DATABASE_NAME;
import static co.pablob.mongo.control.Constants.ENV_DATABASE_NAME;

public class DatabaseNameProvider {

    private String databaseName = System.getenv().getOrDefault(ENV_DATABASE_NAME, DEFAULT_DATABASE_NAME);

    public String produceDatabaseName(){
        return databaseName;
    }

}

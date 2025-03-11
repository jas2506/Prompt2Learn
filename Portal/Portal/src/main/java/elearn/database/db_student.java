package elearn.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class db_student {

    @Value("${spring.data.mongodb.uri}")
    private String uri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    private MongoCollection<Document> collectionStudents;

    // This method initializes the connection and the collection
    @PostConstruct
    public void init() {
        try {
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(uri))
                    .build();
            MongoClient mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            collectionStudents = database.getCollection("students");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Function to verify student credentials
    public Document verifyStudentCredentials(String email, String password) {
        try {
            return collectionStudents.find(
                    Filters.and(
                            Filters.eq("email", email),
                            Filters.eq("password", password)
                    )
            ).first();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public boolean insertStudent(Document newStudent) {
        try {
            collectionStudents.insertOne(newStudent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public Document getStudentDetails(String student_id) {
        try {
            return collectionStudents.find(new Document("student_id", student_id)).first();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}

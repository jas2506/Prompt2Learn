package elearn.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.*;
import javax.annotation.PostConstruct;
import com.mongodb.client.result.UpdateResult;
import static com.mongodb.client.model.Updates.push;
import static com.mongodb.client.model.Filters.eq;
@Component
public class db_teacher {

    @Value("${spring.data.mongodb.uri}")
    private String uri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    private MongoCollection<Document> collectionTeacher;

    @PostConstruct
    public void init() {
        try {
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(uri))
                    .build();
            var mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            collectionTeacher = database.getCollection("teacher");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Function to verify teacher credentials.
    // Here, we assume that the teacher's "username" is their email.
    public Document verifyTeacherCredentials(String username, String password) {
        try {
            return collectionTeacher.find(
                    Filters.and(
                            Filters.eq("email", username),
                            Filters.eq("password", password)
                    )
            ).first();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public boolean insertTeacher(Document newTeacher) {
        try {
            collectionTeacher.insertOne(newTeacher);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public Document getTeacherDetails(String teacher_id) {
        return collectionTeacher.find(new Document("teacher_id", teacher_id)).first();
    }
    public boolean addCourseToTeacher(String teacher_id, String course_id) {
        try {
            UpdateResult result = collectionTeacher.updateOne(
                    eq("teacher_id", teacher_id),
                    push("courses", course_id)
            );
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}

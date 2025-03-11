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
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
@Component
public class db_course {

    @Value("${spring.data.mongodb.uri}")
    private String uri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    private MongoCollection<Document> collectionCourses;

    // Initialize MongoDB connection and collection
    @PostConstruct
    public void init() {
        try {
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(uri))
                    .build();
            MongoClient mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            collectionCourses = database.getCollection("courses");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Insert course details
    public boolean insertCourse(Document newCourse) {
        try {
            collectionCourses.insertOne(newCourse);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Fetch course details
    public Document getCourseDetails(String course_id) {
        try {
            return collectionCourses.find(new Document("course_id", course_id)).first();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public boolean updateCourse(Document query, Document update) {
        try {
            // Use upsert=false so that a new document is not created if none match
            UpdateResult result = collectionCourses.updateOne(query, update, new UpdateOptions().upsert(false));
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public Document getCourseByModuleName(String moduleName) {
        try {
            return collectionCourses.find(new Document("modules.module_name", moduleName)).first();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



}
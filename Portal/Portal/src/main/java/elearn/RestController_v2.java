package elearn;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import elearn.database.db_student;
import elearn.database.db_teacher;
import elearn.database.db_course;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import elearn.FileUploader;
import java.io.File;
@SpringBootApplication
@CrossOrigin(origins = {"*"})
@RestController
@RequestMapping("/api")
public class RestController_v2 {

    @Value("${spring.data.mongodb.uri}")
    private String uri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    private MongoClient mongoClient;
    private MongoDatabase database;

    @Autowired
    private db_student dbStudent;  // Student operations

    @Autowired
    private db_teacher dbTeacher;  // Teacher operations

    @Autowired
    private db_course dbCourse;    // Course/PPT operations

    @PostConstruct
    public void init() {
        try {
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(uri))
                    .build();
            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(databaseName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Student login endpoint
    @PostMapping("/setCredentialsStudent")
    public ResponseEntity<Map<String, Object>> setCredentialsStudent(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String password = requestBody.get("password");

        Map<String, Object> response = new HashMap<>();
        Document student = dbStudent.verifyStudentCredentials(email, password);

        if (student != null) {
            response.put("status", "S");
            response.put("message", "Login successful");
            response.put("student_id", student.getString("student_id"));
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "E");
            response.put("message", "Invalid credentials");
            return ResponseEntity.status(404).body(response);
        }
    }


    // Teacher login endpoint
    @PostMapping("/setCredentialsTeacher")
    public ResponseEntity<Map<String, Object>> setCredentialsTeacher(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String password = requestBody.get("password");

        Map<String, Object> response = new HashMap<>();
        Document teacher = dbTeacher.verifyTeacherCredentials(email, password);

        if (teacher != null) {
            response.put("status", "S");
            response.put("message", "Login successful");
            response.put("teacher_id", teacher.getString("teacher_id"));
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "E");
            response.put("message", "Invalid credentials");
            return ResponseEntity.status(404).body(response);
        }
    }


    // Register Student endpoint
    @PostMapping("/RegisterStudent")
    public ResponseEntity<Map<String, Object>> registerStudent(@RequestBody Map<String, String> requestBody) {
        String student_id = requestBody.get("student_id");
        String email = requestBody.get("email");
        String password = requestBody.get("password");

        Map<String, Object> response = new HashMap<>();
        try {
            Document newStudent = new Document("student_id", student_id)
                    .append("email", email)
                    .append("password", password)
                    .append("courses", new ArrayList<String>());

            boolean inserted = dbStudent.insertStudent(newStudent);

            if (inserted) {
                response.put("status", "S");
                response.put("message", "Student registered successfully");
                response.put("student", newStudent);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "E");
                response.put("message", "Student registration failed");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            response.put("status", "E");
            response.put("message", "Error during registration: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }


    // Register Teacher endpoint
    @PostMapping("/RegisterTeacher")
    public ResponseEntity<Map<String, Object>> registerTeacher(@RequestBody Map<String, String> requestBody) {
        String teacher_id = requestBody.get("teacher_id");
        String email = requestBody.get("email");
        String password = requestBody.get("password");

        Map<String, Object> response = new HashMap<>();
        try {
            Document newTeacher = new Document("teacher_id", teacher_id)
                    .append("email", email)
                    .append("password", password)
                    .append("courses", new ArrayList<String>());

            boolean inserted = dbTeacher.insertTeacher(newTeacher);

            if (inserted) {
                response.put("status", "S");
                response.put("message", "Teacher registered successfully");
                response.put("teacher", newTeacher);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "E");
                response.put("message", "Teacher registration failed");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            response.put("status", "E");
            response.put("message", "Error during registration: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }


    // Get Student Details endpoint
    @PostMapping("/getStudentDetails")
    public ResponseEntity<Map<String, Object>> getStudentDetails(@RequestBody Map<String, String> requestBody) {
        String student_id = requestBody.get("student_id");
        Map<String, Object> response = new HashMap<>();
        Document student = dbStudent.getStudentDetails(student_id);

        if (student != null) {
            // Get the list of course IDs from the student document
            List<String> courseIds = student.getList("courses", String.class);
            List<Map<String, Object>> coursesDetails = new ArrayList<>();

            if (courseIds != null) {
                // For each course ID, fetch course details from the courses database
                for (String courseId : courseIds) {
                    Document courseDoc = dbCourse.getCourseDetails(courseId);
                    if (courseDoc != null) {
                        Map<String, Object> courseInfo = new HashMap<>();
                        courseInfo.put("course_id", courseDoc.getString("course_id"));
                        courseInfo.put("course_name", courseDoc.getString("course_name"));
                        courseInfo.put("teacher_id", courseDoc.getString("teacher_id"));
                        coursesDetails.add(courseInfo);
                    }
                }
            }
            response.put("status", "S");
            response.put("courses", coursesDetails);
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "E");
            response.put("message", "Student not found");
            return ResponseEntity.status(404).body(response);
        }
    }

    // Get Teacher Details endpoint
    @PostMapping("/getTeacherDetails")
    public ResponseEntity<Map<String, Object>> getTeacherDetails(@RequestBody Map<String, String> requestBody) {
        String teacher_id = requestBody.get("teacher_id");
        Map<String, Object> response = new HashMap<>();
        try {
            Document teacher = dbTeacher.getTeacherDetails(teacher_id);
            if (teacher != null) {
                // Assume teacher document has a field "courses" that is a list of course IDs.
                List<String> courseIds = teacher.getList("courses", String.class);
                List<Map<String, Object>> coursesDetails = new ArrayList<>();
                if (courseIds != null) {
                    for (String courseId : courseIds) {
                        Document courseDoc = dbCourse.getCourseDetails(courseId);
                        if (courseDoc != null) {
                            Map<String, Object> courseInfo = new HashMap<>();
                            courseInfo.put("course_id", courseDoc.getString("course_id"));
                            courseInfo.put("course_name", courseDoc.getString("course_name"));
                            courseInfo.put("teacher_id", courseDoc.getString("teacher_id"));
                            coursesDetails.add(courseInfo);
                        }
                    }
                }
                response.put("status", "S");
                response.put("courses", coursesDetails);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "E");
                response.put("message", "Teacher not found");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            response.put("status", "E");
            response.put("message", "Error retrieving teacher details: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Get Course Details endpoint
    @GetMapping("/getCourseDetails")
    public ResponseEntity<Map<String, Object>> getCourseDetails(@RequestParam String course_id) {
        Map<String, Object> response = new HashMap<>();
        Document course = dbCourse.getCourseDetails(course_id);

        if (course != null) {
            response.put("status", "S");
            response.put("course", course);
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "E");
            response.put("message", "Course not found");
            return ResponseEntity.status(404).body(response);
        }
    }
    @PostMapping("/convertppttotext")
    public ResponseEntity<Map<String, Object>> convertPptToText(@RequestBody Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();
        String pptLink = requestBody.get("ppt_link");

        if (pptLink == null || pptLink.isEmpty()) {
            response.put("status", "E");
            response.put("message", "Invalid PPT link provided.");
            return ResponseEntity.status(400).body(response);
        }

        try {
            // Retrieve the local PPTX file (only handles local file paths)
            File pptFile = getLocalPPTXFile(pptLink);
            if (pptFile == null) {
                response.put("status", "E");
                response.put("message", "File not found or invalid path.");
                return ResponseEntity.status(404).body(response);
            }

            // Extract text from the PPTX file using Apache POI
            String extractedText = extractTextFromPPTX(pptFile);

            // Build the query to find the course document with a module that has a matching ppt_link
            Document query = new Document("modules.content.ppt_link", pptLink);
            // Build the update to set the ppt_text field in the matched module's content
            Document update = new Document("$set", new Document("modules.$.content.ppt_text", extractedText));

            // Update the course document in the database
            boolean updated = dbCourse.updateCourse(query, update);

            if (updated) {
                response.put("status", "S");
                response.put("message", "PPT text successfully updated in the course document.");
                response.put("ppt_text", extractedText);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "E");
                response.put("message", "No course document found for the given PPT link or update failed.");
                return ResponseEntity.status(404).body(response);
            }

        } catch (Exception e) {
            response.put("status", "E");
            response.put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Helper method: Retrieve a local PPTX file
    private File getLocalPPTXFile(String filePath) {
        File localFile = new File(filePath);
        if (localFile.exists() && localFile.isFile()) {
            return localFile;
        } else {
            System.out.println("Error: File not found or invalid path - " + filePath);
            return null;
        }
    }

    //per method: Extract text from PPTX file using Apache POI
    private String extractTextFromPPTX(File pptFile) {
        StringBuilder presentationText = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(pptFile);
             XMLSlideShow ppt = new XMLSlideShow(fis)) {

            for (XSLFSlide slide : ppt.getSlides()) {
                StringBuilder slideText = new StringBuilder();

                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        String text = ((XSLFTextShape) shape).getText();
                        String[] lines = text.split("\\r?\\n");
                        for (String line : lines) {
                            String trimmedLine = line.trim();
                            // Skip unwanted lines
                            if (trimmedLine.toLowerCase().startsWith("slide:") ||
                                    trimmedLine.matches("[-_]+") ||
                                    trimmedLine.matches("(?i)^by\\s+.*") ||
                                    trimmedLine.matches("(?i)^reg no:.*") ||
                                    trimmedLine.matches("(?i)^cse\\s+.*")) {
                                continue;
                            }
                            if (!trimmedLine.isEmpty()) {
                                slideText.append(trimmedLine).append(" ");
                            }
                        }
                    }
                }
                String cleanedSlide = slideText.toString().trim();
                if (!cleanedSlide.isEmpty()) {
                    presentationText.append(cleanedSlide).append(" ");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return presentationText.toString().trim();
    }
    // Endpoint: Generate summary text for a module and update the course document
    @PostMapping("/setSummaryText")
    public ResponseEntity<Map<String, Object>> setSummaryText(@RequestBody Map<String, String> requestBody) {
        String moduleName = requestBody.get("module_name");
        Map<String, Object> response = new HashMap<>();

        if (moduleName == null || moduleName.isEmpty()) {
            response.put("status", "E");
            response.put("message", "Module name is required.");
            return ResponseEntity.status(400).body(response);
        }

        try {
            // Retrieve the course document containing the module with the given moduleName.
            Document course = dbCourse.getCourseByModuleName(moduleName);
            if (course == null) {
                response.put("status", "E");
                response.put("message", "No course found containing module: " + moduleName);
                return ResponseEntity.status(404).body(response);
            }

            // Locate the specific module within the course's modules array.
            List<Document> modules = course.getList("modules", Document.class);
            Document targetModule = null;
            for (Document mod : modules) {
                if (moduleName.equals(mod.getString("module_name"))) {
                    targetModule = mod;
                    break;
                }
            }
            if (targetModule == null) {
                response.put("status", "E");
                response.put("message", "Module not found in the course.");
                return ResponseEntity.status(404).body(response);
            }

            // Extract the ppt_text from the module's content.
            Document content = (Document) targetModule.get("content");
            if (content == null) {
                response.put("status", "E");
                response.put("message", "No content found for this module.");
                return ResponseEntity.status(404).body(response);
            }
            String pptText = content.getString("ppt_text");
            if (pptText == null || pptText.isEmpty()) {
                response.put("status", "E");
                response.put("message", "No PPT text available to summarize.");
                return ResponseEntity.status(404).body(response);
            }

            // Generate summary using an external API call.
            String summary = generateSummary(pptText);

            // Build the update query to set the ppt_summary field in the matched module.
            Document query = new Document("modules.module_name", moduleName);
            Document update = new Document("$set", new Document("modules.$.content.ppt_summary", summary));

            // Update the course document.
            boolean updated = dbCourse.updateCourse(query, update);
            if (!updated) {
                response.put("status", "E");
                response.put("message", "Failed to update the course with the summary.");
                return ResponseEntity.status(500).body(response);
            }

            response.put("status", "S");
            response.put("message", "Summary generated and updated successfully.");
            response.put("summary", summary);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "E");
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }


    // Helper method: Generate summary via external summarization API
    private String generateSummary(String text) throws Exception {
        String apiUrl = "http://localhost:5000/chat"; // Update with your summarization API endpoint if needed
        Gson gson = new Gson();
        JsonObject json = new JsonObject();
        json.addProperty("message", "Summarize: " + text);
        String jsonInputString = gson.toJson(json);

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder responseBuilder = new StringBuilder();
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
            responseBuilder.append(responseLine.trim());
        }
        String jsonResponseStr = responseBuilder.toString();
        JsonObject jsonResponse = gson.fromJson(jsonResponseStr, JsonObject.class);
        return jsonResponse.get("response").getAsString();
    }
    @PostMapping("/setSummaryAudio")
    public ResponseEntity<Map<String, Object>> setSummaryAudio(@RequestBody Map<String, String> requestBody) {
        String moduleName = requestBody.get("module_name");
        Map<String, Object> response = new HashMap<>();

        if (moduleName == null || moduleName.isEmpty()) {
            response.put("status", "E");
            response.put("message", "Module name is required.");
            return ResponseEntity.status(400).body(response);
        }

        try {
            // Retrieve the course document that contains a module with the given moduleName.
            Document course = dbCourse.getCourseByModuleName(moduleName);
            if (course == null) {
                response.put("status", "E");
                response.put("message", "No course found containing module: " + moduleName);
                return ResponseEntity.status(404).body(response);
            }

            // Locate the specific module within the course's modules array.
            List<Document> modules = course.getList("modules", Document.class);
            Document targetModule = null;
            for (Document mod : modules) {
                if (moduleName.equals(mod.getString("module_name"))) {
                    targetModule = mod;
                    break;
                }
            }
            if (targetModule == null) {
                response.put("status", "E");
                response.put("message", "Module not found in the course.");
                return ResponseEntity.status(404).body(response);
            }

            // Extract the audio_text from the module's content.
            Document content = (Document) targetModule.get("content");
            if (content == null) {
                response.put("status", "E");
                response.put("message", "No content found for this module.");
                return ResponseEntity.status(404).body(response);
            }
            String audioText = content.getString("audio_text");
            if (audioText == null || audioText.isEmpty()) {
                response.put("status", "E");
                response.put("message", "No audio text available to summarize.");
                return ResponseEntity.status(404).body(response);
            }

            // Generate summary using an external API call.
            String summary = generateSummary(audioText);

            // Build the update query to set the audio_summary field in the matched module.
            Document query = new Document("modules.module_name", moduleName);
            Document update = new Document("$set", new Document("modules.$.content.audio_summary", summary));

            // Update the course document.
            boolean updated = dbCourse.updateCourse(query, update);
            if (!updated) {
                response.put("status", "E");
                response.put("message", "Failed to update the course with the audio summary.");
                return ResponseEntity.status(500).body(response);
            }

            response.put("status", "S");
            response.put("message", "Audio summary generated and updated successfully.");
            response.put("audio_summary", summary);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "E");
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Upload Files by Link endpoint: accepts a file link as input, accesses the local file, and uploads it.
    @PostMapping("/uploadFilesByLink")
    public ResponseEntity<Map<String, Object>> uploadFilesByLink(@RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();

        // Expecting "file_links" to be a JSON array of strings
        Object fileLinksObj = requestBody.get("file_links");
        if (fileLinksObj == null) {
            response.put("status", "E");
            response.put("message", "Invalid file links provided.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        List<String> fileLinks;
        try {
            fileLinks = (List<String>) fileLinksObj;
        } catch (ClassCastException e) {
            response.put("status", "E");
            response.put("message", "File links should be a list of strings.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (fileLinks.isEmpty()) {
            response.put("status", "E");
            response.put("message", "No file links provided.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        List<File> files = new ArrayList<>();
        for (String link : fileLinks) {
            File file = new File(link);
            if (!file.exists() || !file.isFile()) {
                response.put("status", "E");
                response.put("message", "File not found or invalid path: " + link);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            files.add(file);
        }

        try {
            // Use your FileUploader class to forward these files to the Flask server.
            FileUploader.uploadFiles(files);
            response.put("status", "S");
            response.put("message", "Files uploaded successfully to Flask.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "E");
            response.put("message", "Error uploading files: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/chatbotquery")
    public ResponseEntity<Map<String, Object>> chatbotQuery(@RequestBody Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();
        String prompt = requestBody.get("prompt");

        if (prompt == null || prompt.isEmpty()) {
            response.put("status", "E");
            response.put("message", "Prompt is required.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            // Set up the connection to the external chatbot API
            URL url = new URL("http://127.0.0.1:5000/query");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Build the JSON payload using Gson
            Gson gson = new Gson();
            JsonObject json = new JsonObject();
            json.addProperty("query", prompt);
            String jsonInputString = gson.toJson(json);

            // Write JSON payload to the request body
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Check for a successful response code
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                response.put("status", "E");
                response.put("message", "Error from external chatbot API, response code: " + responseCode);
                return ResponseEntity.status(responseCode).body(response);
            }

            // Read the response from the external API
            StringBuilder externalResponse = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    externalResponse.append(responseLine.trim());
                }
            }

            String externalResponseStr = externalResponse.toString();
            String ragResponse;
            try {
                JsonObject jsonResponse = gson.fromJson(externalResponseStr, JsonObject.class);
                ragResponse = jsonResponse.get("response").getAsString();
            } catch (Exception ex) {
                ragResponse = externalResponseStr;
            }

            response.put("status", "S");
            response.put("message", "Chatbot query processed successfully.");
            response.put("response", ragResponse);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "E");
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



    @PostMapping("/addcourse")
    public ResponseEntity<Map<String, Object>> addCourse(
            @RequestParam String course_id,
            @RequestParam String course_name,
            @RequestParam String teacher_id) {

        Map<String, Object> response = new HashMap<>();
        try {
            // Create a new course document with an empty modules array.
            Document newCourse = new Document("course_id", course_id)
                    .append("course_name", course_name)
                    .append("teacher_id", teacher_id)
                    .append("modules", new ArrayList<Document>());

            // Insert the new course into the courses database.
            boolean inserted = dbCourse.insertCourse(newCourse);

            if (!inserted) {
                response.put("status", "E");
                response.put("message", "Course insertion failed");
                return ResponseEntity.status(500).body(response);
            }

            // Update the teacher's document by adding the new course_id to their courses array.
            boolean teacherUpdated = dbTeacher.addCourseToTeacher(teacher_id, course_id);
            if (!teacherUpdated) {
                response.put("status", "E");
                response.put("message", "Course added to courses but failed to update teacher's courses");
                return ResponseEntity.status(500).body(response);
            }

            response.put("status", "S");
            response.put("message", "Course added successfully and teacher updated.");
            response.put("course", newCourse);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "E");
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    @PostMapping("/addmodule")
    public ResponseEntity<Map<String, Object>> addModule(
            @RequestParam String course_id,
            @RequestParam String module_name,
            @RequestParam String ppt_link,
            @RequestParam String audio_link) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Create the content subdocument with provided links and empty values for others.
            Document content = new Document("ppt_link", ppt_link)
                    .append("audio_link", audio_link)
                    .append("quiz_link", "")
                    .append("audio_summary", "")
                    .append("ppt_summary", "")
                    .append("audio_text", "")
                    .append("ppt_text", "");

            // Create the module document.
            Document module = new Document("module_name", module_name)
                    .append("content", content);

            // Build the query to find the course document with the given course_id.
            Document query = new Document("course_id", course_id);
            // Build the update command to push the new module into the modules array.
            Document update = new Document("$push", new Document("modules", module));

            // Update the course document.
            boolean updated = dbCourse.updateCourse(query, update);

            if (updated) {
                response.put("status", "S");
                response.put("message", "Module added successfully.");
                response.put("module", module);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "E");
                response.put("message", "Failed to add module to course.");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            response.put("status", "E");
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    @PostMapping("/generateQuizFromModule")
    public ResponseEntity<Map<String, Object>> generateQuizFromModule(@RequestBody Map<String, String> requestBody) {
        String moduleName = requestBody.get("module_name");
        Map<String, Object> response = new HashMap<>();

        if (moduleName == null || moduleName.isEmpty()) {
            response.put("status", "E");
            response.put("message", "Module name is required.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            // Retrieve the course document that contains the module with the given moduleName.
            Document course = dbCourse.getCourseByModuleName(moduleName);
            if (course == null) {
                response.put("status", "E");
                response.put("message", "No course found containing module: " + moduleName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Locate the specific module within the course's modules array.
            List<Document> modules = course.getList("modules", Document.class);
            Document targetModule = null;
            for (Document mod : modules) {
                if (moduleName.equals(mod.getString("module_name"))) {
                    targetModule = mod;
                    break;
                }
            }
            if (targetModule == null) {
                response.put("status", "E");
                response.put("message", "Module not found in the course.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Extract the ppt_text from the module's content.
            Document content = (Document) targetModule.get("content");
            if (content == null) {
                response.put("status", "E");
                response.put("message", "No content found for this module.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            String pptText = content.getString("ppt_text");
            if (pptText == null || pptText.isEmpty()) {
                response.put("status", "E");
                response.put("message", "No PPT text available to generate quiz.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Construct a prompt to generate 10 MCQs with four options each and the correct answer.
            String prompt = "Based on the following text, generate 10 multiple choice questions (MCQs) with four options each and indicate the correct answer. Format each question as: { question: \"<question>\", options: [\"option1\", \"option2\", \"option3\", \"option4\"], answer: \"<correct_option>\" }.\n\nText: " + pptText;

            // Call the external chatbot API to generate the quiz.
            URL url = new URL("http://127.0.0.1:5000/chat");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            Gson gson = new Gson();
            JsonObject json = new JsonObject();
            json.addProperty("message", prompt);
            String jsonInputString = gson.toJson(json);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                response.put("status", "E");
                response.put("message", "Error from external chatbot API, response code: " + responseCode);
                return ResponseEntity.status(responseCode).body(response);
            }

            StringBuilder externalResponse = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    externalResponse.append(responseLine.trim());
                }
            }
            String externalResponseStr = externalResponse.toString();
            String quizOutput;
            try {
                JsonObject jsonResponse = gson.fromJson(externalResponseStr, JsonObject.class);
                quizOutput = jsonResponse.get("response").getAsString();
            } catch (Exception ex) {
                quizOutput = externalResponseStr;
            }

            response.put("status", "S");
            response.put("message", "Quiz generated successfully.");
            response.put("quiz", quizOutput);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "E");
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/getAllModules")
    public ResponseEntity<Map<String, Object>> getAllModules(@RequestBody Map<String, String> requestBody) {
        String course_id = requestBody.get("course_id");
        Map<String, Object> response = new HashMap<>();
        Document course = dbCourse.getCourseDetails(course_id);
        if (course != null) {
            // Retrieve the modules list from the course document
            List<Document> modules = course.getList("modules", Document.class);
            response.put("status", "S");
            response.put("modules", modules);
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "E");
            response.put("message", "Course not found");
            return ResponseEntity.status(404).body(response);
        }
    }
    @PostMapping("/addppt")
    public ResponseEntity<Map<String, Object>> addPpt(@RequestBody Map<String, String> requestBody) {
        String moduleName = requestBody.get("module_name");
        String pptLink = requestBody.get("ppt_link");
        Map<String, Object> response = new HashMap<>();

        if (moduleName == null || moduleName.isEmpty() || pptLink == null || pptLink.isEmpty()) {
            response.put("status", "E");
            response.put("message", "Module name and PPT link are required.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            // Retrieve the course document that contains the module with the given module name.
            Document course = dbCourse.getCourseByModuleName(moduleName);
            if (course == null) {
                response.put("status", "E");
                response.put("message", "No course found containing module: " + moduleName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Locate the specific module within the course's modules array.
            List<Document> modules = course.getList("modules", Document.class);
            Document targetModule = null;
            for (Document mod : modules) {
                if (moduleName.equals(mod.getString("module_name"))) {
                    targetModule = mod;
                    break;
                }
            }
            if (targetModule == null) {
                response.put("status", "E");
                response.put("message", "Module not found in the course.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Build the update query to set the ppt_link field in the module's content.
            Document query = new Document("modules.module_name", moduleName);
            Document update = new Document("$set", new Document("modules.$.content.ppt_link", pptLink));

            boolean updated = dbCourse.updateCourse(query, update);
            if (!updated) {
                response.put("status", "E");
                response.put("message", "Failed to update the PPT link in the database.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            response.put("status", "S");
            response.put("message", "PPT link updated successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "E");
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/addaudio")
    public ResponseEntity<Map<String, Object>> addAudio(@RequestBody Map<String, String> requestBody) {
        String moduleName = requestBody.get("module_name");
        String audioLink = requestBody.get("audio_link");
        Map<String, Object> response = new HashMap<>();

        if (moduleName == null || moduleName.isEmpty() || audioLink == null || audioLink.isEmpty()) {
            response.put("status", "E");
            response.put("message", "Module name and audio link are required.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            // Retrieve the course document containing the module with the given module name.
            Document course = dbCourse.getCourseByModuleName(moduleName);
            if (course == null) {
                response.put("status", "E");
                response.put("message", "No course found containing module: " + moduleName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Locate the specific module within the course's modules array.
            List<Document> modules = course.getList("modules", Document.class);
            Document targetModule = null;
            for (Document mod : modules) {
                if (moduleName.equals(mod.getString("module_name"))) {
                    targetModule = mod;
                    break;
                }
            }
            if (targetModule == null) {
                response.put("status", "E");
                response.put("message", "Module not found in the course.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Build the update query to set the audio_link field in the module's content.
            Document query = new Document("modules.module_name", moduleName);
            Document update = new Document("$set", new Document("modules.$.content.audio_link", audioLink));

            boolean updated = dbCourse.updateCourse(query, update);
            if (!updated) {
                response.put("status", "E");
                response.put("message", "Failed to update the course with the audio link.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            response.put("status", "S");
            response.put("message", "Audio link updated successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "E");
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/uploadquizlink")
    public ResponseEntity<Map<String, Object>> uploadQuizLink(@RequestBody Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();
        String moduleName = requestBody.get("module_name");
        String quizLink = requestBody.get("quiz_link");

        if (moduleName == null || moduleName.isEmpty() || quizLink == null || quizLink.isEmpty()) {
            response.put("status", "E");
            response.put("message", "Both module_name and quiz_link are required.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            // Retrieve the course document that contains the module with the given moduleName.
            Document course = dbCourse.getCourseByModuleName(moduleName);
            if (course == null) {
                response.put("status", "E");
                response.put("message", "No course found containing module: " + moduleName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            // Locate the specific module within the course's modules array.
            List<Document> modules = course.getList("modules", Document.class);
            Document targetModule = null;
            for (Document mod : modules) {
                if (moduleName.equals(mod.getString("module_name"))) {
                    targetModule = mod;
                    break;
                }
            }
            if (targetModule == null) {
                response.put("status", "E");
                response.put("message", "Module not found in the course.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Build the update query to set the quiz_link field in the module's content.
            Document query = new Document("modules.module_name", moduleName);
            Document update = new Document("$set", new Document("modules.$.content.quiz_link", quizLink));

            // Update the course document in the database.
            boolean updated = dbCourse.updateCourse(query, update);
            if (updated) {
                response.put("status", "S");
                response.put("message", "Quiz link updated successfully.");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "E");
                response.put("message", "Failed to update the quiz link.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("status", "E");
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/getQuizLink")
    public ResponseEntity<Map<String, Object>> getQuizLink(@RequestBody Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();
        String moduleName = requestBody.get("module_name");

        if (moduleName == null || moduleName.isEmpty()) {
            response.put("status", "E");
            response.put("message", "Module name is required.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            // Retrieve the course document that contains the module with the given moduleName.
            Document course = dbCourse.getCourseByModuleName(moduleName);
            if (course == null) {
                response.put("status", "E");
                response.put("message", "No course found containing module: " + moduleName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            // Locate the specific module within the course's modules array.
            List<Document> modules = course.getList("modules", Document.class);
            Document targetModule = null;
            for (Document mod : modules) {
                if (moduleName.equals(mod.getString("module_name"))) {
                    targetModule = mod;
                    break;
                }
            }
            if (targetModule == null) {
                response.put("status", "E");
                response.put("message", "Module not found in the course.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            Document content = (Document) targetModule.get("content");
            if (content == null) {
                response.put("status", "E");
                response.put("message", "No content found for this module.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            String quizLink = content.getString("quiz_link");
            if (quizLink == null || quizLink.isEmpty()) {
                response.put("status", "E");
                response.put("message", "No quiz link available for this module.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            response.put("status", "S");
            response.put("quiz_link", quizLink);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "E");
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }







    public static void main(String[] args) {
        SpringApplication.run(RestController_v2.class, args);
    }
}
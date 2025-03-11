package elearn;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class FileUploader {

    public static void uploadFiles(List<File> files) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        // Replace with your Flask server's upload endpoint
        HttpPost post = new HttpPost("http://127.0.0.1:5000/upload");

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        for (File file : files) {
            builder.addBinaryBody("files", Files.newInputStream(file.toPath()),
                    org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM, file.getName());
        }

        HttpEntity entity = builder.build();
        post.setEntity(entity);

        HttpResponse response = client.execute(post);
        System.out.println("Response Code: " + response.getStatusLine().getStatusCode());
        client.close();
    }

    public static void main(String[] args) throws IOException {
        // Use command-line arguments as file paths
        if (args.length == 0) {
            System.out.println("Please provide file paths as command-line arguments.");
            return;
        }
        List<File> files = new ArrayList<>();
        for (String path : args) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                files.add(file);
            } else {
                System.out.println("File not found or not a file: " + path);
            }
        }
        if (files.isEmpty()) {
            System.out.println("No valid files provided.");
            return;
        }
        uploadFiles(files);
    }
}

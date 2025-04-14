package com.example.healthtracking.controller;

import com.example.healthtracking.model.User;
import com.example.healthtracking.model.Doctor;
import com.example.healthtracking.service.UserService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final Firestore firestore;
    private final UserService userService;

    public UserController(Firestore firestore, UserService userService) {
        this.firestore = firestore;
        this.userService = userService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection("users").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
    
            List<User> users = documents.stream()
                .map(doc -> doc.toObject(User.class))
                .collect(Collectors.toList());
    
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            e.printStackTrace(); // Hata detaylarını logla
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }
    

    @GetMapping("/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection("users")
                .whereEqualTo("username", username)
                .get();
    
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
    
            if (!documents.isEmpty()) {
                User user = documents.get(0).toObject(User.class);
                return ResponseEntity.ok(user);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace(); // Hata detaylarını logla
            return ResponseEntity.status(500).build();
        }
    }
    
    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addUser(@RequestBody User user) {
        try {
            if (user.getId() == null || user.getId().isEmpty()) {
                user.setId(firestore.collection("users").document().getId());
            }
            firestore.collection("users").document(user.getId()).set(user);
            
            // JSON formatında cevap döndürmek için Map kullanıyoruz
            Map<String, String> response = new HashMap<>();
            response.put("message", "User added successfully!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }


    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody Map<String, Object> payload) {
        try {
            Firestore db = FirestoreClient.getFirestore();
    
            String username = (String) payload.get("username");
            String password = (String) payload.get("password");
            String role = (String) payload.get("role");
            String email = (String) payload.get("email");
 
            
            // Kullanıcı kaydı kontrolü
            ApiFuture<QuerySnapshot> userQuery = db.collection("users")
                    .whereEqualTo("username", username)
                    .get();
            List<QueryDocumentSnapshot> userDocs = userQuery.get().getDocuments();
            if (!userDocs.isEmpty()) {
                return ResponseEntity.status(400).body(Collections.singletonMap("message", "Username already exists"));
            }
    
            if ("DOCTOR".equals(role)) {
                // Doktorlar için ek kontrol
                String firstName = (String) payload.get("firstName");
                String lastName = (String) payload.get("lastName");
                String specialization = (String) payload.get("specialization");
    
                ApiFuture<QuerySnapshot> doctorQuery = db.collection("doctors")
                        .whereEqualTo("username", username)
                        .whereEqualTo("firstName", firstName)
                        .whereEqualTo("lastName", lastName)
                        .whereEqualTo("specialization", specialization)
                        .get();
                List<QueryDocumentSnapshot> doctorDocs = doctorQuery.get().getDocuments();
    
                if (doctorDocs.isEmpty()) {
                    return ResponseEntity.status(400).body(Collections.singletonMap("message", "Doctor information does not match any record"));
                }
    
                // Doktor kaydını doğrulayıp kullanıcı olarak ekleyin
                String doctorId = doctorDocs.get(0).getId();
                Map<String, Object> userData = new HashMap<>();
                userData.put("username", username);
                userData.put("password", password);
                userData.put("role", role);
                userData.put("email", email);
                userData.put("firstName", firstName);
                userData.put("lastName", lastName);
                userData.put("specialization", specialization);
                userData.put("doctorId", doctorId); // doctors tablosuyla ilişki
                db.collection("users").add(userData);
    
                return ResponseEntity.ok(Collections.singletonMap("message", "Doctor signup successful"));
            } else {
                // Normal kullanıcı kaydı
                String firstName = (String) payload.get("firstName");
                String lastName = (String) payload.get("lastName");
                Map<String, Object> userData = new HashMap<>();
                userData.put("username", username);
                userData.put("password", password);
                userData.put("role", role);
                userData.put("firstName", firstName);
                userData.put("lastName", lastName);
                userData.put("email", email);
                db.collection("users").add(userData);
    
                return ResponseEntity.ok(Collections.singletonMap("message", "User signup successful"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.singletonMap("message", "Error: " + e.getMessage()));
        }
    }
    

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody User user) {
        try {
            Firestore db = FirestoreClient.getFirestore();
    
            List<QueryDocumentSnapshot> documents = db.collection("users")
                    .whereEqualTo("username", user.getUsername())
                    .whereEqualTo("password", user.getPassword())
                    .get()
                    .get()
                    .getDocuments();
    
            if (!documents.isEmpty()) {
                QueryDocumentSnapshot document = documents.get(0);
                User foundUser = document.toObject(User.class);
    
                Map<String, String> response = new HashMap<>();
                response.put("message", "Login successful");
                response.put("role", foundUser.getRole());
                response.put("userId", document.getId()); // userId (document ID)
    
                // Eğer kullanıcı bir doktorsa, doctorId ekle
                if ("DOCTOR".equals(foundUser.getRole()) && foundUser.getDoctorId() != null) {
                    response.put("doctorId", foundUser.getDoctorId());
                }
    
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(401).body(Collections.singletonMap("message", "Invalid credentials"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.singletonMap("message", "Error: " + e.getMessage()));
        }
    }

    @GetMapping("{userId}/download")
    public ResponseEntity<byte[]> downloadPatientInfo(@PathVariable String userId) {
        Firestore db = FirestoreClient.getFirestore();
        try {
            DocumentSnapshot userDoc = db.collection("users").document(userId).get().get();
            if (userDoc.exists()) {
                Map<String, Object> userData = userDoc.getData();
                String xmlData = convertToXml(userData);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=patient-" + userId + ".xml")
                        .contentType(MediaType.APPLICATION_XML)
                        .body(xmlData.getBytes());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String convertToXml(Map<String, Object> data) {
        // XML dönüşümü için bir kütüphane kullanabilirsiniz (ör. Jackson, JAXB)
        // Basit bir şekilde dönüşüm yapıldı
        StringBuilder xml = new StringBuilder();
        xml.append("<Patient>");
        data.forEach((key, value) -> xml.append("<").append(key).append(">").append(value).append("</").append(key).append(">"));
        xml.append("</Patient>");
        return xml.toString();
    }
}

package com.example.healthtracking.controller;

import com.example.healthtracking.model.Doctor;
import com.example.healthtracking.model.Message;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final Firestore firestore;

    public DoctorController(Firestore firestore) {
        this.firestore = firestore;
    }



    // Add Doctor Endpoint
    @PostMapping("/add")
    public ResponseEntity<String> addDoctor(@RequestBody Doctor doctor) {
        try {
            if (doctor.getId() == null || doctor.getId().isEmpty()) {
                doctor.setId(firestore.collection("doctors").document().getId());
            }
            firestore.collection("doctors").document(doctor.getId()).set(doctor);
            return ResponseEntity.ok("Doctor added successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to add doctor: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable String id) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            ApiFuture<QuerySnapshot> future = db.collection("doctors")
                .whereEqualTo("id", id)
                .get();
    
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
    
            if (documents.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
    
            Doctor doctor = documents.get(0).toObject(Doctor.class);
            return ResponseEntity.ok(doctor);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
    

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getDoctorsWithUserIds() {
        try {
            Firestore db = FirestoreClient.getFirestore();
    
            // Doktor verilerini alın
            ApiFuture<QuerySnapshot> doctorQuery = db.collection("doctors").get();
            List<QueryDocumentSnapshot> doctorDocs = doctorQuery.get().getDocuments();
    
            if (doctorDocs.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
    
            List<Map<String, Object>> doctorsWithUsers = new ArrayList<>();
            for (QueryDocumentSnapshot doctorDoc : doctorDocs) {
                Map<String, Object> doctorData = doctorDoc.getData();
                doctorData.put("doctorId", doctorDoc.getId()); // Doktorun ID'sini ekle
    
                // Doctors koleksiyonundan username alın
                String doctorUsername = (String) doctorData.get("username");
                if (doctorUsername != null) {
                    // Users koleksiyonunda username ile arama yap ve userId'yi al
                    ApiFuture<QuerySnapshot> userQuery = db.collection("users")
                            .whereEqualTo("username", doctorUsername)
                            .get();
                    List<QueryDocumentSnapshot> userDocs = userQuery.get().getDocuments();
                    if (!userDocs.isEmpty()) {
                        String userId = userDocs.get(0).getId(); // Users koleksiyonundaki document ID
                        doctorData.put("userId", userId); // userId'yi ekle
                    }
                }
    
                doctorsWithUsers.add(doctorData);
            }
    
            return ResponseEntity.ok(doctorsWithUsers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    

    @GetMapping("/by-specialization/{specialization}")
    public ResponseEntity<List<Doctor>> getDoctorsBySpecialization(@PathVariable String specialization) {
        try {
            ApiFuture<QuerySnapshot> query = firestore.collection("doctors")
                .whereEqualTo("specialization", specialization)
                .get();

            List<QueryDocumentSnapshot> documents = query.get().getDocuments();
            List<Doctor> doctors = documents.stream()
                .map(doc -> doc.toObject(Doctor.class))
                .collect(Collectors.toList());

            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @PostMapping("/send-message")
    public ResponseEntity<Map<String, String>> sendMessage(@RequestBody Map<String, Object> payload) {
        String username = (String) payload.get("username");
        String doctorUsername = (String) payload.get("doctorUsername");
        String messageText = (String) payload.get("messageText");

        if (username == null || doctorUsername == null || messageText == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("message", "Missing required fields"));
        }

        try {
            Firestore db = FirestoreClient.getFirestore();

            // Gönderen kullanıcının ID'sini al
            ApiFuture<QuerySnapshot> userQuery = db.collection("users")
                .whereEqualTo("username", username)
                .get();
            List<QueryDocumentSnapshot> userDocs = userQuery.get().getDocuments();

            if (userDocs.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("message", "User not found"));
            }
            String userId = userDocs.get(0).getId();

            // Doktorun ID'sini al
            ApiFuture<QuerySnapshot> doctorQuery = db.collection("doctors")
                .whereEqualTo("username", doctorUsername)
                .get();
            List<QueryDocumentSnapshot> doctorDocs = doctorQuery.get().getDocuments();

            if (doctorDocs.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("message", "Doctor not found"));
            }
            String doctorId = doctorDocs.get(0).getId();

            // Mesajı oluştur ve kaydet
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("userId", userId);
            messageData.put("doctorId", doctorId);
            messageData.put("username", username);
            messageData.put("messageText", messageText);
            messageData.put("timestamp", System.currentTimeMillis());
            messageData.put("isRead", false);

            db.collection("messages").add(messageData);

            return ResponseEntity.ok(Collections.singletonMap("message", "Message sent successfully!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", "Failed to send message"));
        }
    }


    @GetMapping("/{doctorId}/messages")
    public ResponseEntity<List<Map<String, Object>>> getMessagesForDoctor(@PathVariable String doctorId) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            ApiFuture<QuerySnapshot> future = db.collection("messages")
                    .whereEqualTo("doctorId", doctorId)
                    .get();
    
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Map<String, Object>> messages = new ArrayList<>();
    
            for (QueryDocumentSnapshot doc : documents) {
                Map<String, Object> messageData = doc.getData();
                messageData.put("messageId", doc.getId()); // Mesajın ID'sini ekleyin
                messages.add(messageData);
            }
    
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{doctorId}/patients")
    public ResponseEntity<List<Map<String, Object>>> getDoctorPatients(@PathVariable String doctorId) {
        try {
            // Firestore'dan hastaları çekmek için doktor ID'sine göre filtrele
            ApiFuture<QuerySnapshot> query = firestore.collection("user_health_records")
                    .whereEqualTo("doctorId", doctorId)
                    .get();
    
            List<QueryDocumentSnapshot> documents = query.get().getDocuments();
    
            if (documents.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList()); // Eğer hasta yoksa boş liste döndür
            }
    
            // Hastaları listeye ekle
            List<Map<String, Object>> patients = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                Map<String, Object> patientData = document.getData();
                patients.add(patientData);
            }
    
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(Map.of("error", "An error occurred while fetching patients")));
        }
    }
    
    
    


}

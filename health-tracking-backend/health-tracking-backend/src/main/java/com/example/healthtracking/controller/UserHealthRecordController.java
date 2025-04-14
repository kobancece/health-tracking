package com.example.healthtracking.controller;

import com.example.healthtracking.model.UserHealthRecord;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/health-records")
@CrossOrigin(origins = "http://localhost:4200")

public class UserHealthRecordController {

    private final Firestore firestore;

    public UserHealthRecordController(Firestore firestore) {
        this.firestore = firestore;
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveHealthRecord(@RequestBody Map<String, Object> payload, @RequestHeader("Username") String username) {
        try {
            // JSON'dan gelen veriler
            Number weightNumber = (Number) payload.get("weight");
            Number heightNumber = (Number) payload.get("height");
            String bloodPressure = (String) payload.get("bloodPressure");
            Number heartRateNumber = (Number) payload.get("heartRate");
    
            // Değerlerin null olup olmadığını kontrol et
            if (username == null || weightNumber == null || heightNumber == null || bloodPressure == null || heartRateNumber == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing required fields");
            }
    
            // Sayısal değerleri Double ve Integer türlerine dönüştür
            Double weight = weightNumber.doubleValue();
            Double height = heightNumber.doubleValue();
            Integer heartRate = heartRateNumber.intValue();
    
            Firestore db = FirestoreClient.getFirestore();
    
            // "users" koleksiyonunda username'e göre kullanıcıyı ara
            ApiFuture<QuerySnapshot> query = db.collection("users")
                .whereEqualTo("username", username)
                .get();
    
            List<QueryDocumentSnapshot> documents = query.get().getDocuments();
    
            if (documents.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
    
            // Kullanıcının document ID'sini al
            String userId = documents.get(0).getId();
    
            // Sağlık verilerini kaydet
            UserHealthRecord healthRecord = new UserHealthRecord();
            healthRecord.setUserId(userId);
            healthRecord.setWeight(weight);
            healthRecord.setHeight(height);
            healthRecord.setBloodPressure(bloodPressure);
            healthRecord.setHeartRate(heartRate);
    
            // "user_health_records" koleksiyonuna ekle
            db.collection("user_health_records").document(userId).set(healthRecord);
    
            return ResponseEntity.ok(Collections.singletonMap("message", "Health record saved successfully!"));
        } catch (ClassCastException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid data type provided");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save health record");
        }
    }
    

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getHealthRecordByUserId(@PathVariable String userId) {
        try {
            // "user_health_records" koleksiyonunda userId ile eşleşen kayıtları ara
            ApiFuture<QuerySnapshot> future = firestore.collection("user_health_records")
                .whereEqualTo("userId", userId)
                .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (documents.isEmpty()) {
                // Eğer kayıt bulunamazsa, boş bir liste dön
                return ResponseEntity.ok(Collections.emptyList());
            }

            // Bulunan kayıtları döndür
            List<UserHealthRecord> records = documents.stream()
                .map(doc -> doc.toObject(UserHealthRecord.class))
                .collect(Collectors.toList());

            return ResponseEntity.ok(records);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("An error occurred while fetching health records");
        }
    }

}

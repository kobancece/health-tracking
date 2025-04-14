package com.example.healthtracking.controller;

import com.example.healthtracking.model.Message;
import com.example.healthtracking.model.User;
import com.example.healthtracking.model.UserHealthRecord;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "http://localhost:4200")
public class MessageController {

    private final Firestore firestore;

    public MessageController(Firestore firestore) {
        this.firestore = firestore;
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody Map<String, Object> payload) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            
            // Mesaj bilgilerini al
            String senderId = (String) payload.get("senderId");
            String receiverId = (String) payload.get("receiverId");
            String messageText = (String) payload.get("messageText");
            String senderRole = (String) payload.get("senderRole");
            String receiverRole = (String) payload.get("receiverRole");
    
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("senderId", senderId);
            messageData.put("receiverId", receiverId);
            messageData.put("messageText", messageText);
            messageData.put("senderRole", senderRole);
            messageData.put("receiverRole", receiverRole);
            messageData.put("isRead", false); // Mesaj okunmamış
    
            db.collection("messages").add(messageData);
    
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Message sent successfully!"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Failed to send message"
            ));
        }
    }
    

    @GetMapping("/sent/{userId}")
    public ResponseEntity<?> getMessagesByUserId(@PathVariable String userId) {
        try {
            Firestore db = FirestoreClient.getFirestore();
    
            // Kullanıcıyla ilgili mesajları al
            List<QueryDocumentSnapshot> sentMessages = db.collection("messages")
                .whereEqualTo("senderId", userId)
                .get()
                .get()
                .getDocuments();
    
            List<QueryDocumentSnapshot> receivedMessages = db.collection("messages")
                .whereEqualTo("receiverId", userId)
                .get()
                .get()
                .getDocuments();
    
            List<Message> messages = sentMessages.stream()
                .map(doc -> doc.toObject(Message.class))
                .collect(Collectors.toList());
    
            messages.addAll(receivedMessages.stream()
                .map(doc -> doc.toObject(Message.class))
                .collect(Collectors.toList()));
    
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to load messages");
        }
    }
    

    @PutMapping("/{messageId}/read")
    public ResponseEntity<?> markMessageAsRead(@PathVariable String messageId) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            db.collection("messages").document(messageId).update("isRead", true);
            return ResponseEntity.ok(Collections.singletonMap("message", "Message marked as read"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Failed to mark message as read"));
        }
    }

    @GetMapping("/inbox/{userId}")
    public ResponseEntity<Map<String, Object>> getMessagesForUser(@PathVariable String userId) {
        try {
            Firestore db = FirestoreClient.getFirestore();
    
            ApiFuture<QuerySnapshot> messageQuery = db.collection("messages")
                    .whereEqualTo("receiverId", userId)
                    .get();
    
            List<QueryDocumentSnapshot> messageDocs = messageQuery.get().getDocuments();
    
            if (messageDocs.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "messageCount", 0,
                        "messages", Collections.emptyList(),
                        "status", "success"
                ));
            }
    
            List<Map<String, Object>> messagesWithSenderInfo = new ArrayList<>();
            for (QueryDocumentSnapshot messageDoc : messageDocs) {
                Map<String, Object> messageData = messageDoc.getData();
    
                String senderId = (String) messageData.get("senderId");
                if (senderId != null) {
                    ApiFuture<DocumentSnapshot> senderQuery = db.collection("users").document(senderId).get();
                    DocumentSnapshot senderDoc = senderQuery.get();
                    if (senderDoc.exists()) {
                        messageData.put("senderUsername", senderDoc.getString("username"));
                    }
                }
    
                messageData.put("id", messageDoc.getId()); // Mesaj ID'sini ekle
                messagesWithSenderInfo.add(messageData);
            }
    
            return ResponseEntity.ok(Map.of(
                    "messageCount", messagesWithSenderInfo.size(),
                    "messages", messagesWithSenderInfo,
                    "status", "success"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "messageCount", 0,
                            "messages", Collections.emptyList(),
                            "status", "error",
                            "errorMessage", "Failed to fetch messages"
                    ));
        }
    }
    
    @GetMapping("/patients/{doctorId}")
    public ResponseEntity<List<Map<String, Object>>> getPatientsForDoctor(@PathVariable String doctorId) {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> messageQuery = db.collection("messages")
                .whereEqualTo("receiverId", doctorId)
                .get();

        try {
            List<QueryDocumentSnapshot> documents = messageQuery.get().getDocuments();
            Set<String> patientIds = new HashSet<>();
            for (QueryDocumentSnapshot doc : documents) {
                patientIds.add(doc.getString("senderId"));
            }

            List<Map<String, Object>> patients = new ArrayList<>();
            for (String patientId : patientIds) {
                DocumentSnapshot patientDoc = db.collection("users").document(patientId).get().get();
                if (patientDoc.exists()) {
                    Map<String, Object> patientData = patientDoc.getData();
                    patientData.put("id", patientDoc.getId());
                    patients.add(patientData);
                }
            }

            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}

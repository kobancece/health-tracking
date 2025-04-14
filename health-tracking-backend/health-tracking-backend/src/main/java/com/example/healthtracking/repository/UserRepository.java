package com.example.healthtracking.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.example.healthtracking.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;


@Repository
public class UserRepository {

    private final Firestore firestore;

    public UserRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<User> getAllUsers() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection("users").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            
            List<User> users = documents.stream()
                .map(doc -> doc.toObject(User.class))
                .collect(Collectors.toList());
            
            return users;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public User getUserByUsername(String username) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection("users")
                .whereEqualTo("username", username).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (!documents.isEmpty()) {
                return documents.get(0).toObject(User.class);
            } else {
                return null;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveUser(User user) {
        firestore.collection("users").document(user.getId()).set(user);
    }

}

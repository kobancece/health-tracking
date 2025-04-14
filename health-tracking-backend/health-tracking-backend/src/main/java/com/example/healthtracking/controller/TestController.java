package com.example.healthtracking.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.cloud.firestore.Firestore;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final Firestore firestore;

    public TestController(Firestore firestore) {
        this.firestore = firestore;
    }

    @GetMapping
    public String testFirestore() {
        return "Firestore connection successful!";
    }

    @PostMapping("/add")
    public String addTestData(@RequestBody Map<String, Object> data) {
        try {
            firestore.collection("users").add(data);
            return "Data added successfully!";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}

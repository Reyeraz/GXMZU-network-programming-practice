package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class GreetingService {

    public String getGreeting() {
        return "有趣的问候：Spring IoC 真香！";
    }

    public String timeGreeting() {
        LocalTime now = LocalTime.now();
        if (now.isBefore(LocalTime.NOON)) {
            return "Good morning!";
        } else if (now.isBefore(LocalTime.of(18, 0))) {
            return "Good afternoon!";
        } else {
            return "Good evening!";
        }
    }
}

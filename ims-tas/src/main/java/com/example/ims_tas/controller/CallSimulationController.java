package com.example.ims_tas.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/call")
@CrossOrigin(origins = "http://localhost:5173")
public class CallSimulationController {

    @PostMapping("/start")
    public String startCall(@RequestParam String from, @RequestParam String to) {
        // In this setup, signaling is done entirely via WebSocket
        return "Call start triggered from " + from + " to " + to;
    }

    @PostMapping("/answer")
    public String answerCall(@RequestParam String from, @RequestParam String to) {
        return "Call answered by " + from;
    }

    @PostMapping("/end")
    public String endCall(@RequestParam String from, @RequestParam String to) {
        return "Call ended by " + from;
    }
}

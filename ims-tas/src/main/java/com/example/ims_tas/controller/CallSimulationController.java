package com.example.ims_tas.controller;

import com.example.ims_tas.service.CallForwardingService;
import com.example.ims_tas.sip.SipListener;
import com.example.ims_tas.sip.SipUtils.SipMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller to trigger SIP message flow simulation manually.
 */
@RestController
public class CallSimulationController {

    private final SipListener sipListener;

    public CallSimulationController(CallForwardingService forwardingService) {
        this.sipListener = new SipListener(forwardingService);
    }

    @GetMapping("/simulate")
    public String simulateCallFlow() {
        // Simulate IMS sending INVITE
        SipMessage invite = new SipMessage("INVITE", "sip:1001@ims.operator.com", "sip:1000@ims.operator.com");
        sipListener.onMessage(invite);

        // Simulate IMS sending BYE later
        SipMessage bye = new SipMessage("BYE", "sip:1001@ims.operator.com", "sip:1000@ims.operator.com");
        sipListener.onMessage(bye);

        return "Simulation completed. Check logs for flow.";
    }
}

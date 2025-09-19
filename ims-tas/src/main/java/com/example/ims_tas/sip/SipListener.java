package com.example.ims_tas.sip;

import com.example.ims_tas.service.CallForwardingService;
import com.example.ims_tas.sip.SipUtils.SipMessage;

/**
 * Mock SIP Listener that simulates IMS → TAS interaction.
 */
public class SipListener {

    private final CallForwardingService forwardingService;

    public SipListener(CallForwardingService forwardingService) {
        this.forwardingService = forwardingService;
    }

    // Called when IMS sends a SIP message
    public void onMessage(SipMessage msg) {
        System.out.println("[IMS → TAS] Received: " + msg);
        forwardingService.processMessage(msg);
    }
}

package com.example.ims_tas.service;

import com.example.ims_tas.sip.SipUtils.SipMessage;
import org.springframework.stereotype.Service;

/**
 * Core TAS logic (e.g., unconditional call forwarding).
 */
@Service
public class CallForwardingService {

    // Example: always forward calls to this number
    private final String forwardingNumber = "sip:1002@ims.operator.com";

    public void processMessage(SipMessage msg) {
        if ("INVITE".equals(msg.type)) {
            System.out.println("[TAS] Handling call setup from " + msg.from + " to " + msg.to);

            // Apply call forwarding
            System.out.println("[TAS] Forwarding to " + forwardingNumber);
            SipMessage forwarded = new SipMessage("INVITE", msg.from, forwardingNumber);
            sendToIMS(forwarded);

        } else if ("BYE".equals(msg.type)) {
            System.out.println("[TAS] Call ended: " + msg.from + " -> " + msg.to);
            sendToIMS(msg);

        } else {
            System.out.println("[TAS] Passing message without changes: " + msg);
            sendToIMS(msg);
        }
    }

    private void sendToIMS(SipMessage msg) {
        System.out.println("[TAS → IMS] Sending: " + msg);
    }
}

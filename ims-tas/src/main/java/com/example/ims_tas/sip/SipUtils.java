package com.example.ims_tas.sip;

/**
 * Mock SIP message model (simplified).
 */
public class SipUtils {

    public static class SipMessage {
        public String type; // INVITE, BYE, 200 OK, etc.
        public String from;
        public String to;

        public SipMessage(String type, String from, String to) {
            this.type = type;
            this.from = from;
            this.to = to;
        }

        @Override
        public String toString() {
            return "SIP[" + type + "] From=" + from + " To=" + to;
        }
    }
}

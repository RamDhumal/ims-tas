<template>
  <div class="app">
    <h1>IMS TAS Call Simulator</h1>

    <div class="input-group">
      <label>Your SIP URI:</label>
      <input v-model="self" placeholder="sip:1001@ims.operator.com" />
    </div>

    <div class="input-group">
      <label>Call To:</label>
      <input v-model="peer" placeholder="sip:1000@ims.operator.com" />
    </div>

    <div class="buttons">
      <button @click="register" :disabled="wsReady">Register</button>
      <button @click="startCall" :disabled="!wsReady || callActive">Start Call</button>
      <button @click="answerCall" :disabled="!incomingCall">Answer Call</button>
      <button @click="endCall" :disabled="!callActive">End Call</button>
    </div>

    <div v-if="log" class="log">
      <strong>Status:</strong>
      <p>{{ log }}</p>
    </div>

    <audio ref="remoteAudio" autoplay></audio>
  </div>
</template>

<script setup>
import { ref } from "vue";

const self = ref("sip:1001@ims.operator.com");
const peer = ref("sip:1000@ims.operator.com");
const log = ref("");

let ws;
let pc;
let pendingCandidates = [];
const remoteAudio = ref(null);

let incomingCall = false;
let callActive = false;
let wsReady = false;

const API_BASE = "http://localhost:8082/call";
const WS_URL = "ws://localhost:8082/ws/signal";

// Safe WebSocket send
function sendWSMessage(msg) {
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify(msg));
  } else {
    console.warn("WebSocket not open", msg);
  }
}

// --- Registration ---
function register() {
  ws = new WebSocket(WS_URL);
  ws.onopen = () => {
    wsReady = true;
    log.value = `Registered as ${self.value}`;
    sendWSMessage({ type: "REGISTER", user: self.value });
    console.log("WebSocket connected");
  };

  ws.onmessage = async (event) => {
    const data = JSON.parse(event.data);
    console.log("WS message:", data);

    switch (data.type) {
      case "INCOMING_CALL":
        incomingCall = true;
        log.value = `Incoming call from ${data.from}`;
        break;

      case "OFFER":
        await handleOffer(data);
        break;

      case "ANSWER":
        await handleAnswer(data);
        break;

      case "ICE_CANDIDATE":
        await handleIceCandidate(data);
        break;

      case "CALL_ENDED":
        endCallLocal();
        log.value = "Call ended";
        break;
    }
  };

  ws.onclose = () => {
    wsReady = false;
    console.warn("WebSocket closed");
  };
}

// --- Start Call (Caller) ---
async function startCall() {
  callActive = true;
  pc = new RTCPeerConnection();

  // Local audio
  const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
  stream.getTracks().forEach(track => pc.addTrack(track, stream));

  // Remote audio
  pc.ontrack = (event) => {
    remoteAudio.value.srcObject = event.streams[0];
  };

  // ICE
  pc.onicecandidate = (event) => {
    if (event.candidate) {
      sendWSMessage({ type: "ICE_CANDIDATE", to: peer.value, candidate: event.candidate });
    }
  };

  // Create OFFER
  const offer = await pc.createOffer();
  await pc.setLocalDescription(offer);

  sendWSMessage({ type: "START_CALL", from: self.value, to: peer.value, sdp: offer });
  log.value = "Call started, waiting for answer...";
}

// --- Answer Call (Callee) ---
async function answerCall() {
  
  callActive = true;
  incomingCall = false;

  pc = new RTCPeerConnection();

  // Local audio
  const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
  stream.getTracks().forEach(track => pc.addTrack(track, stream));

  // Remote audio
  pc.ontrack = (event) => {
    remoteAudio.value.srcObject = event.streams[0];
  };

  // ICE
  pc.onicecandidate = (event) => {
    if (event.candidate) {
      sendWSMessage({ type: "ICE_CANDIDATE", to: peer.value, candidate: event.candidate });
    }
  };

  // Set remote description to the received OFFER
  const offer = incomingOffer;
  await pc.setRemoteDescription({ type: "offer", sdp: offer.sdp });

  // Add any pending ICE
  for (const c of pendingCandidates) await pc.addIceCandidate(c);
  pendingCandidates = [];

  // Create ANSWER
  const answer = await pc.createAnswer();
  await pc.setLocalDescription(answer);

  sendWSMessage({ type: "ANSWER", to: peer.value, sdp: answer });
  log.value = "Answered call, audio active!";
}

// Store incoming offer temporarily
let incomingOffer = null;

// Handle OFFER from caller
async function handleOffer(data) {
  incomingOffer = data;
  log.value = `Incoming call from ${data.from}`;
  incomingCall = true;
}

// Handle ANSWER from callee
async function handleAnswer(data) {
  await pc.setRemoteDescription({ type: "answer", sdp: data.sdp });

  // Add pending ICE candidates
  for (const c of pendingCandidates) await pc.addIceCandidate(c);
  pendingCandidates = [];

  log.value = "Call connected, audio active!";
}

// Handle ICE
async function handleIceCandidate(data) {
  const candidate = new RTCIceCandidate(data.candidate);
  if (pc && pc.remoteDescription) {
    await pc.addIceCandidate(candidate);
  } else {
    pendingCandidates.push(candidate);
  }
}

// End call
async function endCall() {
  sendWSMessage({ type: "CALL_ENDED", to: peer.value });
  endCallLocal();
}

function endCallLocal() {
  if (pc) {
    pc.close();
    pc = null;
  }
  callActive = false;
  incomingCall = false;
}
</script>

<style>
.app { max-width: 450px; margin:50px auto; font-family:Arial; padding:20px; border:1px solid #ccc; border-radius:12px;}
.input-group { margin-bottom: 15px; }
input { width:100%; padding:8px; margin-top:4px; border-radius:8px; border:1px solid #ccc; }
.buttons { display:flex; justify-content:space-between; margin-top:20px;}
button { flex:1; padding:10px; margin:0 5px; background-color:#3b82f6; color:white; border:none; border-radius:8px; cursor:pointer;}
button:hover { background-color:#2563eb;}
.log { margin-top:20px; background:#f3f4f6; padding:10px; border-radius:8px;}
</style>

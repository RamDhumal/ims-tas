<template>
  <div class="app">
    <h1>📞 IMS TAS Call Simulator</h1>

    <!-- Input fields -->
    <div class="input-group">
      <label>From:</label>
      <input v-model="from" placeholder="sip:1001@ims.operator.com" />
    </div>

    <div class="input-group">
      <label>To:</label>
      <input v-model="to" placeholder="sip:1000@ims.operator.com" />
    </div>

    <!-- Buttons -->
    <div class="buttons">
      <button class="start" @click="startCall">Start Call</button>
      <button v-if="incoming" class="answer" @click="answerCall">Receive Call</button>
      <button class="end" @click="endCall">End Call</button>
      <button class="simulate" @click="simulateCall">Simulate</button>
    </div>

    <!-- Notifications -->
    <div v-if="notification" class="notification">
      📢 {{ notification }}
    </div>

    <!-- Response log -->
    <div v-if="message" class="log">
      <strong>Response:</strong>
      <p>{{ message }}</p>
    </div>

    <audio ref="remoteAudio" autoplay></audio>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";

const from = ref("sip:1001@ims.operator.com");
const to = ref("sip:1000@ims.operator.com");
const message = ref("");
const notification = ref("");
const incoming = ref(false);

const API_BASE = "http://localhost:8080/call";

let pc;
let localStream;
const remoteAudio = ref(null);

// Initialize microphone
async function initMedia() {
  localStream = await navigator.mediaDevices.getUserMedia({ audio: true });
}

// Create WebRTC peer connection
async function createPeer() {
  pc = new RTCPeerConnection();
  localStream.getTracks().forEach(track => pc.addTrack(track, localStream));

  // Play remote audio
  pc.ontrack = (event) => {
    remoteAudio.value.srcObject = event.streams[0];
  };
}

// Start call
const startCall = async () => {
  await initMedia();
  await createPeer();

  const offer = await pc.createOffer();
  await pc.setLocalDescription(offer);

  // Send SIP INVITE via REST
  const res = await fetch(`${API_BASE}/start?from=${encodeURIComponent(from.value)}&to=${encodeURIComponent(to.value)}`, { method: "POST" });
  message.value = await res.text();

  // Simulate remote loopback for demo (Answer automatically)
  pc.onnegotiationneeded = async () => {
    const answer = await pc.createAnswer();
    await pc.setLocalDescription(answer);
  };
};

// Answer incoming call
const answerCall = async () => {
  await initMedia();
  await createPeer();

  const res = await fetch(`${API_BASE}/answer?from=${encodeURIComponent(to.value)}&to=${encodeURIComponent(from.value)}`, { method: "POST" });
  message.value = await res.text();
  incoming.value = false;

  // Create answer for local peer
  const answer = await pc.createAnswer();
  await pc.setLocalDescription(answer);
};

// End call
const endCall = async () => {
  const res = await fetch(`${API_BASE}/end?from=${encodeURIComponent(from.value)}&to=${encodeURIComponent(to.value)}`, { method: "POST" });
  message.value = await res.text();

  if (pc) pc.close();
  remoteAudio.value.srcObject = null;
  incoming.value = false;
};

// Simulate
const simulateCall = async () => {
  const res = await fetch(`${API_BASE}/simulate`);
  message.value = await res.text();
};

// Poll notifications
onMounted(() => {
  setInterval(async () => {
    const res = await fetch(`${API_BASE}/notification`);
    const text = await res.text();
    notification.value = text;
    if (text && text.includes("Incoming call")) incoming.value = true;
  }, 2000);
});
</script>

<style>
.app {
  max-width: 500px;
  margin: 60px auto;
  font-family: "Segoe UI", Arial, sans-serif;
  padding: 30px;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.app h1 { color: #1e3a8a; margin-bottom: 25px; font-size: 22px; }

.input-group { margin-bottom: 18px; text-align: left; }

.input-group label { display: block; font-weight: bold; margin-bottom: 6px; color: #374151; }

.input-group input { width: 100%; padding: 10px; border-radius: 10px; border: 1px solid #cbd5e1; font-size: 14px; }

.buttons { display: flex; justify-content: space-between; margin-top: 20px; flex-wrap: wrap; gap: 8px; }

button { flex: 1; padding: 12px; border: none; border-radius: 10px; font-size: 15px; font-weight: bold; cursor: pointer; color: white; transition: transform 0.2s, background 0.3s; }

button.start { background: #3b82f6; } button.start:hover { background: #2563eb; transform: scale(1.05); }
button.answer { background: #22c55e; } button.answer:hover { background: #16a34a; transform: scale(1.05); }
button.end { background: #ef4444; } button.end:hover { background: #dc2626; transform: scale(1.05); }
button.simulate { background: #f59e0b; } button.simulate:hover { background: #b45309; transform: scale(1.05); }

.notification { margin-top: 25px; padding: 15px; border-radius: 12px; background: #fef9c3; border: 1px solid #facc15; color: #854d0e; font-weight: bold; }
.log { margin-top: 20px; padding: 15px; background: #f3f4f6; border-radius: 12px; text-align: left; border: 1px solid #e5e7eb; }
</style>

<template>
  <div class="app">
    <h1>IMS TAS Call Simulator</h1>

    <div class="input-group">
      <label>From:</label>
      <input v-model="from" placeholder="sip:1001@ims.operator.com" />
    </div>

    <div class="input-group">
      <label>To:</label>
      <input v-model="to" placeholder="sip:1000@ims.operator.com" />
    </div>

    <div class="buttons">
      <button @click="startCall">Start Call</button>
      <button @click="endCall">End Call</button>
    </div>

    <div v-if="message" class="log">
      <strong>Response:</strong>
      <p>{{ message }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";

const from = ref("sip:1001@ims.operator.com");
const to = ref("sip:1000@ims.operator.com");
const message = ref("");

// Base backend URL (Spring Boot server)
const API_BASE = "http://localhost:8080/call";

const startCall = async () => {
  try {
    const res = await fetch(
      `${API_BASE}/start?from=${encodeURIComponent(from.value)}&to=${encodeURIComponent(to.value)}`,
      { method: "POST" }
    );
    message.value = await res.text();
  } catch (err) {
    message.value = "Error: " + err.message;
  }
};

const endCall = async () => {
  try {
    const res = await fetch(
      `${API_BASE}/end?from=${encodeURIComponent(from.value)}&to=${encodeURIComponent(to.value)}`,
      { method: "POST" }
    );
    message.value = await res.text();
  } catch (err) {
    message.value = "Error: " + err.message;
  }
};
</script>

<style>
.app {
  max-width: 400px;
  margin: 50px auto;
  font-family: Arial, sans-serif;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 12px;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);
}

.input-group {
  margin-bottom: 15px;
}

input {
  width: 100%;
  padding: 8px;
  margin-top: 4px;
  border-radius: 8px;
  border: 1px solid #ccc;
}

.buttons {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
}

button {
  flex: 1;
  padding: 10px;
  margin: 0 5px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

button:hover {
  background-color: #2563eb;
}

.log {
  margin-top: 20px;
  background: #f3f4f6;
  padding: 10px;
  border-radius: 8px;
}
</style>

<template>
  <div v-if="endTime" class="countdown-timer">
    <span class="countdown-label">距结束</span>
    <div class="countdown-digits">
      <span class="digit-box">{{ pad(days) }}</span>
      <span class="digit-sep">天</span>
      <span class="digit-box">{{ pad(hours) }}</span>
      <span class="digit-sep">:</span>
      <span class="digit-box">{{ pad(minutes) }}</span>
      <span class="digit-sep">:</span>
      <span class="digit-box">{{ pad(seconds) }}</span>
    </div>
  </div>
  <div v-else class="countdown-ended">
    活动已结束
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  endTime: {
    type: String,
    default: ''
  }
})

const now = ref(Date.now())
let timer = null

const endTimestamp = computed(() => {
  if (!props.endTime) return 0
  return new Date(props.endTime).getTime()
})

const remaining = computed(() => {
  const diff = endTimestamp.value - now.value
  return Math.max(0, diff)
})

const days = computed(() => Math.floor(remaining.value / 86400000))
const hours = computed(() => Math.floor((remaining.value % 86400000) / 3600000))
const minutes = computed(() => Math.floor((remaining.value % 3600000) / 60000))
const seconds = computed(() => Math.floor((remaining.value % 60000) / 1000))

const expired = computed(() => remaining.value <= 0)

const pad = (n) => String(n).padStart(2, '0')

onMounted(() => {
  timer = setInterval(() => {
    now.value = Date.now()
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.countdown-timer {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
  padding: 0.75rem 1rem;
  background: linear-gradient(135deg, #fff5f5, #ffe8e8);
  border-radius: 8px;
  border: 1px solid #ffcccc;
}

.countdown-label {
  font-size: 0.95rem;
  color: #e74c3c;
  font-weight: bold;
}

.countdown-digits {
  display: flex;
  align-items: center;
  gap: 2px;
}

.digit-box {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 28px;
  background: #2c3e50;
  color: #fff;
  font-size: 0.9rem;
  font-weight: bold;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
}

.digit-sep {
  font-size: 0.9rem;
  color: #e74c3c;
  font-weight: bold;
  margin: 0 1px;
}

.countdown-ended {
  color: #999;
  font-size: 1rem;
  margin-bottom: 1rem;
}
</style>

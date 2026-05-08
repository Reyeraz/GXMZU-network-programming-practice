<template>
  <el-dialog
    v-model="dialogVisible"
    title="限时优惠券"
    width="90%"
    :max-width="400"
    :close-on-click-modal="false"
    @close="close"
  >
    <div class="coupon-content">
      <div class="coupon-value">¥{{ coupon.value }}</div>
      <div class="coupon-condition">满{{ coupon.minSpend }}元可用</div>
      <div class="coupon-expiry">有效期至: {{ coupon.expiryDate }}</div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button type="primary" @click="claimCoupon" style="width: 100%;">
          立即领取
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, defineProps, defineEmits, watch } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  coupon: {
    type: Object,
    default: () => ({
      value: 50,
      minSpend: 200,
      expiryDate: '2026-12-31'
    })
  }
})

const emit = defineEmits(['close', 'claim'])

const dialogVisible = ref(props.visible)

// 监听visible属性的变化
watch(() => props.visible, (newValue) => {
  dialogVisible.value = newValue
})

const close = () => {
  dialogVisible.value = false
  emit('close')
}

const claimCoupon = () => {
  emit('claim')
  close()
}
</script>

<style scoped>
.coupon-content {
  padding: 2rem 1rem;
  text-align: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 8px;
}

.coupon-value {
  font-size: 2.5rem;
  font-weight: bold;
  color: #ff4757;
  margin-bottom: 0.5rem;
}

.coupon-condition {
  font-size: 1rem;
  color: #666;
  margin-bottom: 0.5rem;
}

.coupon-expiry {
  font-size: 0.9rem;
  color: #999;
}

.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: center;
}
</style>
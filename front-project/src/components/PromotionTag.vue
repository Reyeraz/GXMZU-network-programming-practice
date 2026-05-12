<template>
  <div v-if="promotion" class="promotion-tag">
    <el-tag
      :type="tagType"
      size="large"
      effect="dark"
      class="promotion-label"
    >
      {{ promotion.tag }}
    </el-tag>
    <span class="discount-text">
      {{ discountLabel }}
    </span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  promotion: {
    type: Object,
    default: null
  }
})

const tagType = computed(() => {
  if (!props.promotion) return ''
  const map = {
    flash_sale: 'danger',
    new_arrival: 'success',
    hot_sale: 'warning'
  }
  return map[props.promotion.type] || 'info'
})

const discountLabel = computed(() => {
  if (!props.promotion) return ''
  const rate = props.promotion.discountRate
  return rate ? `${(rate * 10).toFixed(1)}折` : ''
})
</script>

<style scoped>
.promotion-tag {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.promotion-label {
  font-size: 1rem;
  font-weight: bold;
}

.discount-text {
  font-size: 1.25rem;
  font-weight: bold;
  color: #e74c3c;
}
</style>

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface CartItem {
  id: number
  name: string
  price: number
  image: string
  quantity: number
}

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>([])

  const totalQuantity = computed(() =>
    items.value.reduce((sum, item) => sum + item.quantity, 0)
  )

  const totalPrice = computed(() =>
    items.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
  )

  function loadCart() {
    const cart = JSON.parse(localStorage.getItem('cart') || '[]')
    items.value = cart
  }

  function saveCart() {
    localStorage.setItem('cart', JSON.stringify(items.value))
    window.dispatchEvent(new Event('storage'))
  }

  function addToCart(product: {
    id: number
    name: string
    price: number
    image: string
  }, quantity: number = 1) {
    const existing = items.value.find(item => item.id === product.id)
    if (existing) {
      existing.quantity += quantity
    } else {
      items.value.push({ ...product, quantity })
    }
    saveCart()
  }

  function removeFromCart(id: number) {
    const index = items.value.findIndex(item => item.id === id)
    if (index !== -1) {
      items.value.splice(index, 1)
      saveCart()
    }
  }

  function updateQuantity(id: number, quantity: number) {
    const item = items.value.find(item => item.id === id)
    if (item && quantity > 0) {
      item.quantity = quantity
      saveCart()
    }
  }

  function clearCart() {
    items.value = []
    saveCart()
  }

  return {
    items,
    totalQuantity,
    totalPrice,
    loadCart,
    addToCart,
    removeFromCart,
    updateQuantity,
    clearCart
  }
})

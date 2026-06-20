import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { cartAPI } from '../api/api'

export interface CartItem {
  cartId: number
  productId: number
  name: string
  price: number
  quantity: number
  selected: number
  stock: number
  isAvailable: number
}

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>([])

  const totalQuantity = computed(() =>
    items.value.reduce((sum, item) => sum + item.quantity, 0)
  )

  const totalPrice = computed(() =>
    items.value
      .filter(item => item.selected === 1)
      .reduce((sum, item) => sum + item.price * item.quantity, 0)
  )

  const selectedCount = computed(() =>
    items.value
      .filter(item => item.selected === 1)
      .reduce((sum, item) => sum + item.quantity, 0)
  )

  // 从后端加载购物车
  async function loadCart() {
    try {
      const res = await cartAPI.getCartList()
      if (res.success) {
        items.value = (res.data || []).map((item: any) => ({
          cartId: item.cartId,
          productId: item.productId,
          name: item.productName,
          price: item.price,
          quantity: item.quantity,
          selected: item.selected,
          stock: item.stock,
          isAvailable: item.isAvailable
        }))
      }
    } catch (err) {
      console.error('加载购物车失败:', err)
    }
  }

  // 添加商品到购物车
  async function addToCart(product: { id: number; name: string; price: number }, quantity: number = 1) {
    try {
      const res = await cartAPI.addToCart(product.id, quantity)
      if (res.success) {
        await loadCart()
      }
      return res
    } catch (err) {
      console.error('添加购物车失败:', err)
      throw err
    }
  }

  // 更新商品数量
  async function updateQuantity(cartId: number, quantity: number) {
    try {
      const res = await cartAPI.updateQuantity(cartId, quantity)
      if (res.success) {
        await loadCart()
      }
      return res
    } catch (err) {
      console.error('更新数量失败:', err)
      throw err
    }
  }

  // 更新勾选状态
  async function updateSelected(cartId: number, selected: number) {
    try {
      const res = await cartAPI.updateSelected(cartId, selected)
      if (res.success) {
        await loadCart()
      }
      return res
    } catch (err) {
      console.error('更新勾选状态失败:', err)
      throw err
    }
  }

  // 全选/全不选
  async function batchUpdateSelected(selected: number) {
    try {
      const res = await cartAPI.batchUpdateSelected(selected)
      if (res.success) {
        await loadCart()
      }
      return res
    } catch (err) {
      console.error('批量更新勾选状态失败:', err)
      throw err
    }
  }

  // 删除单个商品
  async function removeCartItem(cartId: number) {
    try {
      const res = await cartAPI.deleteCartItem(cartId)
      if (res.success) {
        await loadCart()
      }
      return res
    } catch (err) {
      console.error('删除商品失败:', err)
      throw err
    }
  }

  // 批量删除已勾选商品
  async function deleteSelected() {
    try {
      const res = await cartAPI.deleteSelected()
      if (res.success) {
        await loadCart()
      }
      return res
    } catch (err) {
      console.error('批量删除失败:', err)
      throw err
    }
  }

  return {
    items,
    totalQuantity,
    totalPrice,
    selectedCount,
    loadCart,
    addToCart,
    updateQuantity,
    updateSelected,
    batchUpdateSelected,
    removeCartItem,
    deleteSelected
  }
})

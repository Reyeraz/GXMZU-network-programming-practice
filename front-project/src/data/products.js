// 生成促销截止时间（从现在起偏移指定天数）
const endTimeFromNow = (days) => new Date(Date.now() + days * 86400000).toISOString()

// 模拟商品数据
export const products = [
  {
    id: 1,
    name: 'iPhone 15 Pro',
    description: 'A17 Pro芯片，钛金属设计，支持USB-C接口，5G网络',
    price: 7999,
    image: 'https://picsum.photos/id/1/300/300',
    category: '手机',
    brand: 'Apple',
    stock: 50,
    promotion: {
      type: 'flash_sale',
      tag: '限时优惠',
      discountRate: 0.85,
      endTime: endTimeFromNow(3)
    }
  },
  {
    id: 2,
    name: 'Samsung Galaxy S24 Ultra',
    description: '骁龙8 Gen 3处理器，200MP主摄，S Pen内置，5G网络',
    price: 8999,
    image: 'https://picsum.photos/id/2/300/300',
    category: '手机',
    brand: 'Samsung',
    stock: 30
  },
  {
    id: 3,
    name: 'MacBook Pro 14英寸',
    description: 'M3 Pro芯片，Liquid Retina XDR显示屏，MagSafe充电',
    price: 15999,
    image: 'https://picsum.photos/id/3/300/300',
    category: '笔记本电脑',
    brand: 'Apple',
    stock: 20,
    promotion: {
      type: 'new_arrival',
      tag: '新品首发',
      discountRate: 0.92,
      endTime: endTimeFromNow(7)
    }
  },
  {
    id: 4,
    name: 'Dell XPS 13 Plus',
    description: '13.4英寸4K+触摸屏，第13代Intel酷睿i7处理器，16GB内存',
    price: 11999,
    image: 'https://picsum.photos/id/4/300/300',
    category: '笔记本电脑',
    brand: 'Dell',
    stock: 15
  },
  {
    id: 5,
    name: 'iPad Pro 12.9英寸',
    description: 'M2芯片，Liquid Retina XDR显示屏，Apple Pencil支持',
    price: 8999,
    image: 'https://picsum.photos/id/5/300/300',
    category: '平板电脑',
    brand: 'Apple',
    stock: 25
  },
  {
    id: 6,
    name: 'Surface Pro 9',
    description: '第12代Intel酷睿处理器，13英寸PixelSense Flow显示屏，触控笔支持',
    price: 7999,
    image: 'https://picsum.photos/id/6/300/300',
    category: '平板电脑',
    brand: 'Microsoft',
    stock: 18
  },
  {
    id: 7,
    name: 'AirPods Pro 2',
    description: '主动降噪，自适应通透模式，个性化空间音频',
    price: 1899,
    image: 'https://picsum.photos/id/7/300/300',
    category: '耳机',
    brand: 'Apple',
    stock: 40,
    promotion: {
      type: 'hot_sale',
      tag: '热卖爆款',
      discountRate: 0.88,
      endTime: endTimeFromNow(2)
    }
  },
  {
    id: 8,
    name: 'Sony WH-1000XM5',
    description: '业界领先的降噪技术，30小时电池续航，舒适佩戴',
    price: 2999,
    image: 'https://picsum.photos/id/8/300/300',
    category: '耳机',
    brand: 'Sony',
    stock: 22
  }
]

// 获取所有商品
export const getAllProducts = () => products

// 根据ID获取商品
export const getProductById = (id) => products.find(product => product.id === parseInt(id))

// 根据关键词搜索商品
export const searchProducts = (keyword) => {
  if (!keyword) return products
  const lowerKeyword = keyword.toLowerCase()
  return products.filter(product => 
    product.name.toLowerCase().includes(lowerKeyword) ||
    product.description.toLowerCase().includes(lowerKeyword) ||
    product.category.toLowerCase().includes(lowerKeyword) ||
    product.brand.toLowerCase().includes(lowerKeyword)
  )
}
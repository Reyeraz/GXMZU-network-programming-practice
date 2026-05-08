<template>
  <div class="carousel-container">
    <div class="carousel" ref="carouselRef">
      <div 
        class="carousel-track" 
        :style="{ transform: `translateX(-${currentIndex * 100}%)` }"
      >
        <div 
          v-for="(item, index) in carouselItems" 
          :key="index" 
          class="carousel-item"
        >
          <img :src="item.image" :alt="item.title" />
          <div class="carousel-caption">
            <h3>{{ item.title }}</h3>
            <p>{{ item.description }}</p>
          </div>
        </div>
      </div>
      
      <!-- 指示器 -->
      <div class="carousel-indicators">
        <span 
          v-for="(item, index) in carouselItems" 
          :key="index" 
          class="indicator" 
          :class="{ active: currentIndex === index }"
          @click="goToSlide(index)"
        ></span>
      </div>
      
      <!-- 控制按钮 -->
      <button class="carousel-control prev" @click="prevSlide">
        &lt;
      </button>
      <button class="carousel-control next" @click="nextSlide">
        &gt;
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'

// 路由实例
const route = useRoute()

// 轮播容器引用
const carouselRef = ref(null)

// 当前轮播索引
const currentIndex = ref(0)

// 轮播定时器
let carouselTimer = null

// 轮播数据
const carouselItems = ref([
  {
    image: "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20digital%20products%20banner%20with%20smartphones%20and%20laptops&image_size=landscape_16_9",
    title: "数码产品特惠",
    description: "限时优惠，全场数码产品低至5折"
  },
  {
    image: "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20smartphone%20promotion%20banner&image_size=landscape_16_9",
    title: "新品上市",
    description: "最新款智能手机，引领科技潮流"
  },
  {
    image: "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20laptop%20promotion%20banner&image_size=landscape_16_9",
    title: "笔记本电脑专场",
    description: "高性能笔记本，满足您的工作娱乐需求"
  }
])

// 开始轮播
const startCarousel = () => {
  // 清除已有定时器
  if (carouselTimer) {
    clearInterval(carouselTimer)
  }
  
  // 设置新定时器，每3秒切换一次
  carouselTimer = setInterval(() => {
    nextSlide()
  }, 3000)
}

// 停止轮播
const stopCarousel = () => {
  if (carouselTimer) {
    clearInterval(carouselTimer)
    carouselTimer = null
  }
}

// 下一张
const nextSlide = () => {
  currentIndex.value = (currentIndex.value + 1) % carouselItems.value.length
}

// 上一张
const prevSlide = () => {
  currentIndex.value = (currentIndex.value - 1 + carouselItems.value.length) % carouselItems.value.length
}

// 跳转到指定幻灯片
const goToSlide = (index) => {
  currentIndex.value = index
}

// 监听路由变化，路由切换时停止轮播
watch(() => route.path, () => {
  stopCarousel()
})

// 组件挂载时开始轮播
onMounted(() => {
  startCarousel()
})

// 组件卸载时停止轮播
onUnmounted(() => {
  stopCarousel()
})
</script>

<style scoped>
.carousel-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto 2rem;
  overflow: hidden;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.carousel {
  position: relative;
  width: 100%;
  height: 400px;
  overflow: hidden;
}

.carousel-track {
  display: flex;
  width: 100%;
  height: 100%;
  transition: transform 0.5s ease;
}

.carousel-item {
  position: relative;
  flex: 0 0 100%;
  height: 100%;
}

.carousel-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.carousel-caption {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), rgba(0, 0, 0, 0));
  color: white;
  padding: 2rem;
}

.carousel-caption h3 {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
}

.carousel-caption p {
  font-size: 1rem;
  margin: 0;
}

.carousel-indicators {
  position: absolute;
  bottom: 1rem;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 0.5rem;
}

.indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.indicator.active {
  background-color: white;
}

.carousel-control {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  border-radius: 50%;
  font-size: 1.5rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.3s ease;
}

.carousel-control:hover {
  background-color: rgba(0, 0, 0, 0.8);
}

.carousel-control.prev {
  left: 1rem;
}

.carousel-control.next {
  right: 1rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .carousel {
    height: 250px;
  }
  
  .carousel-caption {
    padding: 1rem;
  }
  
  .carousel-caption h3 {
    font-size: 1.2rem;
  }
  
  .carousel-caption p {
    font-size: 0.8rem;
  }
}
</style>
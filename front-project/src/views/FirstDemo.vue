<template>
  <div class="page-container">
    <!-- 导航栏 -->
    <header class="navbar">
      <div class="nav-left">
        <span class="nav-icon">🖥️</span>
        <h1 class="nav-title">数码商城</h1>
      </div>
      <div class="nav-right">
        <button class="cart-btn">
          🛒
        </button>
      </div>
    </header>

    <!-- 返回首页按钮 -->
    <div class="back-section">
      <button class="back-btn">返回首页</button>
    </div>

    <!-- 时间日期显示 -->
    <main class="time-section">
      <div class="time-display">
        {{ currentTime }}
      </div>
      <div class="date-display">
        {{ currentDate }}
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

// 定义响应式数据
const currentTime = ref('');
const currentDate = ref('');
let timer = null;

// 更新时间日期函数
const updateTime = () => {
  const now = new Date();
  
  // 更新时间
  const hours = now.getHours().toString().padStart(2, '0');
  const minutes = now.getMinutes().toString().padStart(2, '0');
  const seconds = now.getSeconds().toString().padStart(2, '0');
  currentTime.value = `${hours}:${minutes}:${seconds}`;
  
  // 更新日期
  const year = now.getFullYear();
  const month = (now.getMonth() + 1).toString().padStart(2, '0');
  const day = now.getDate().toString().padStart(2, '0');
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
  const weekday = weekdays[now.getDay()];
  currentDate.value = `${year}年${month}月${day}日 ${weekday}`;
};

// 挂载时启动定时器
onMounted(() => {
  updateTime(); // 立即执行一次
  timer = setInterval(updateTime, 1000); // 每秒更新一次
});

// 卸载时清除定时器
onUnmounted(() => {
  if (timer) {
    clearInterval(timer);
  }
});
</script>

<style scoped>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: Arial, sans-serif;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  min-height: 100vh;
  color: white;
}

/* 页面容器 */
.page-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 导航栏 */
.navbar {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.nav-icon {
  font-size: 24px;
}

.nav-title {
  font-size: 20px;
  font-weight: bold;
  margin: 0;
}

.nav-right {
  display: flex;
  align-items: center;
}

.cart-btn {
  background: transparent;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  padding: 5px;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.cart-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* 返回首页按钮区域 */
.back-section {
  padding: 20px;
}

.back-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  padding: 10px 20px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

/* 时间日期显示区域 */
.time-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20px;
  text-align: center;
}

.time-display {
  font-size: 4rem;
  font-weight: bold;
  margin-bottom: 20px;
  letter-spacing: 3px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.date-display {
  font-size: 1.2rem;
  opacity: 0.9;
  letter-spacing: 1px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .time-display {
    font-size: 3rem;
  }
  
  .date-display {
    font-size: 1rem;
  }
  
  .nav-title {
    font-size: 18px;
  }
}
</style>
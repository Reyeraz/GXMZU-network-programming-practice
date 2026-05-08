import http from 'http';
import { URL } from 'url';

const products = [
  { id: 1, name: 'iPhone 15 Pro', description: 'A17 Pro芯片，钛金属设计，支持USB-C接口，5G网络', price: 7999, image: 'https://picsum.photos/id/1/300/300', category: '手机', brand: 'Apple', stock: 50 },
  { id: 2, name: 'Samsung Galaxy S24 Ultra', description: '骁龙8 Gen 3处理器，200MP主摄，S Pen内置，5G网络', price: 8999, image: 'https://picsum.photos/id/2/300/300', category: '手机', brand: 'Samsung', stock: 30 },
  { id: 3, name: 'MacBook Pro 14英寸', description: 'M3 Pro芯片，Liquid Retina XDR显示屏，MagSafe充电', price: 15999, image: 'https://picsum.photos/id/3/300/300', category: '笔记本电脑', brand: 'Apple', stock: 20 },
  { id: 4, name: 'Dell XPS 13 Plus', description: '13.4英寸4K+触摸屏，第13代Intel酷睿i7处理器，16GB内存', price: 11999, image: 'https://picsum.photos/id/4/300/300', category: '笔记本电脑', brand: 'Dell', stock: 15 },
  { id: 5, name: 'iPad Pro 12.9英寸', description: 'M2芯片，Liquid Retina XDR显示屏，Apple Pencil支持', price: 8999, image: 'https://picsum.photos/id/5/300/300', category: '平板电脑', brand: 'Apple', stock: 25 },
  { id: 6, name: 'Surface Pro 9', description: '第12代Intel酷睿处理器，13英寸PixelSense Flow显示屏，触控笔支持', price: 7999, image: 'https://picsum.photos/id/6/300/300', category: '平板电脑', brand: 'Microsoft', stock: 18 },
  { id: 7, name: 'AirPods Pro 2', description: '主动降噪，自适应通透模式，个性化空间音频', price: 1899, image: 'https://picsum.photos/id/7/300/300', category: '耳机', brand: 'Apple', stock: 40 },
  { id: 8, name: 'Sony WH-1000XM5', description: '业界领先的降噪技术，30小时电池续航，舒适佩戴', price: 2999, image: 'https://picsum.photos/id/8/300/300', category: '耳机', brand: 'Sony', stock: 22 }
];

function sendJSON(res, statusCode, data) {
  res.writeHead(statusCode, { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' });
  res.end(JSON.stringify(data));
}

const server = http.createServer((req, res) => {
  const parsedUrl = new URL(req.url, 'http://localhost:3000');
  const pathname = parsedUrl.pathname;
  const query = Object.fromEntries(parsedUrl.searchParams);

  if (pathname === '/api/products' && req.method === 'GET') {
    sendJSON(res, 200, { success: true, data: products });
  } else if (pathname === '/api/products/search' && req.method === 'GET') {
    const keyword = (query.keyword || '').toLowerCase();
    const results = products.filter(p =>
      p.name.toLowerCase().includes(keyword) ||
      p.description.toLowerCase().includes(keyword) ||
      p.category.toLowerCase().includes(keyword) ||
      p.brand.toLowerCase().includes(keyword)
    );
    sendJSON(res, 200, { success: true, data: results });
  } else if (pathname === '/api/products/page' && req.method === 'GET') {
    const page = parseInt(query.page) || 1;
    const pageSize = parseInt(query.pageSize) || 4;
    const category = query.category;
    const keyword = (query.keyword || '').toLowerCase();

    let filtered = [...products];
    if (category) {
      filtered = filtered.filter(p => p.category === category);
    }
    if (keyword) {
      filtered = filtered.filter(p =>
        p.name.toLowerCase().includes(keyword) ||
        p.description.toLowerCase().includes(keyword) ||
        p.brand.toLowerCase().includes(keyword)
      );
    }

    const total = filtered.length;
    const start = (page - 1) * pageSize;
    const items = filtered.slice(start, start + pageSize);

    sendJSON(res, 200, { success: true, data: { items, total, page, pageSize } });
  } else if (pathname.startsWith('/api/products/') && req.method === 'GET') {
    const idStr = pathname.split('/')[3];
    const id = parseInt(idStr);
    if (isNaN(id)) {
      sendJSON(res, 400, { success: false, message: '无效的商品ID' });
      return;
    }
    const product = products.find(p => p.id === id);
    if (product) {
      sendJSON(res, 200, { success: true, data: product });
    } else {
      sendJSON(res, 404, { success: false, message: '商品不存在' });
    }
  } else if (pathname === '/api/categories' && req.method === 'GET') {
    const categories = [...new Set(products.map(p => p.category))];
    sendJSON(res, 200, { success: true, data: categories });
  } else {
    sendJSON(res, 404, { success: false, message: 'API不存在' });
  }
});

server.listen(3000, () => {
  console.log('Mock API Server running at http://localhost:3000/');
});
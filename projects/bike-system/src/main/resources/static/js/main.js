// 配置
const CONFIG = {
    API_BASE: 'http://localhost:8080',
    ENDPOINTS: {
        HEALTH: '/test/health',
        USERS: '/api/users',
        BIKES: '/api/bikes',
        ORDERS: '/api/orders',
        TEST_FLOW: '/test/business-flow'
    }
};

// 页面初始化
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚲 校园共享单车系统初始化...');

    // 更新API基础URL显示
    document.getElementById('api-base-url').textContent = CONFIG.API_BASE;

    // 初始加载
    checkSystemStatus();
    loadDataOverview();

    // 添加动画效果
    animateElements();
});

// 检查系统状态
async function checkSystemStatus() {
    const statusDiv = document.getElementById('system-status');
    if (!statusDiv) return;

    showLoading(statusDiv, '正在检查系统状态...');

    try {
        const response = await fetch(CONFIG.API_BASE + CONFIG.ENDPOINTS.HEALTH);

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();

        if (data.status === 'healthy' || data.status === 'UP') {
            showSuccess(statusDiv, '✅ 系统运行正常', '所有服务组件已就绪');
        } else {
            showWarning(statusDiv, '⚠️ 系统状态异常', data.message || '未知状态');
        }
    } catch (error) {
        showError(statusDiv, '❌ 连接服务器失败', error.message);
    }
}

// 加载数据概览
async function loadDataOverview() {
    try {
        // 并行请求所有数据
        const [usersRes, bikesRes, ordersRes] = await Promise.all([
            fetch(CONFIG.API_BASE + CONFIG.ENDPOINTS.USERS).catch(() => null),
            fetch(CONFIG.API_BASE + CONFIG.ENDPOINTS.BIKES).catch(() => null),
            fetch(CONFIG.API_BASE + CONFIG.ENDPOINTS.ORDERS).catch(() => null)
        ]);

        // 更新用户数量
        if (usersRes && usersRes.ok) {
            const users = await usersRes.json();
            updateCount('user-count', users.length);
        } else {
            updateCount('user-count', '--');
        }

        // 更新车辆数据
        if (bikesRes && bikesRes.ok) {
            const bikes = await bikesRes.json();
            updateCount('bike-count', bikes.length);

            // 计算可用车辆
            const available = Array.isArray(bikes)
                ? bikes.filter(b => b.bikeStatus === '可用' || b.status === '可用').length
                : '--';
            updateCount('available-bikes', available);
        } else {
            updateCount('bike-count', '--');
            updateCount('available-bikes', '--');
        }

        // 更新订单数量
        if (ordersRes && ordersRes.ok) {
            const orders = await ordersRes.json();
            updateCount('order-count', Array.isArray(orders) ? orders.length : '--');
        } else {
            updateCount('order-count', '--');
        }

    } catch (error) {
        console.error('加载数据失败:', error);
        resetAllCounts();
    }
}

// API测试函数
async function testApi(endpoint, resultDivId) {
    const resultDiv = document.getElementById(resultDivId);
    if (!resultDiv) return;

    showLoading(resultDiv, '发送请求...');

    try {
        const response = await fetch(CONFIG.API_BASE + endpoint);
        const data = await response.json();

        const summary = formatResponseSummary(data);
        showSuccess(resultDiv, '✅ 请求成功', `${endpoint}<br>${summary}`);

    } catch (error) {
        showError(resultDiv, '❌ 请求失败', `${endpoint}<br>${error.message}`);
    }
}

// 辅助函数
function showLoading(element, message) {
    element.innerHTML = `
        <div class="d-flex align-items-center">
            <div class="spinner-border spinner-border-sm me-2" role="status"></div>
            <span>${message}</span>
        </div>
    `;
}

function showSuccess(element, title, message) {
    element.innerHTML = `
        <div class="alert alert-success p-2 mb-0">
            <strong>${title}</strong><br>
            <small>${message}</small>
        </div>
    `;
}

function showError(element, title, message) {
    element.innerHTML = `
        <div class="alert alert-danger p-2 mb-0">
            <strong>${title}</strong><br>
            <small>${message}</small>
        </div>
    `;
}

function showWarning(element, title, message) {
    element.innerHTML = `
        <div class="alert alert-warning p-2 mb-0">
            <strong>${title}</strong><br>
            <small>${message}</small>
        </div>
    `;
}

function updateCount(elementId, count) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = count;

        // 添加数字动画
        element.style.transform = 'scale(1.2)';
        setTimeout(() => {
            element.style.transform = 'scale(1)';
        }, 300);
    }
}

function resetAllCounts() {
    ['user-count', 'bike-count', 'order-count', 'available-bikes'].forEach(id => {
        updateCount(id, '--');
    });
}

function formatResponseSummary(data) {
    if (Array.isArray(data)) {
        return `返回 ${data.length} 条记录`;
    } else if (typeof data === 'object') {
        const keys = Object.keys(data);
        return `返回对象 (${keys.length} 个属性)`;
    } else {
        return `返回: ${String(data)}`;
    }
}

function animateElements() {
    // 卡片动画
    const cards = document.querySelectorAll('.card');
    cards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';

        setTimeout(() => {
            card.style.transition = 'all 0.5s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, 100 * index);
    });
}

// 更新HTML中的JavaScript部分
// 将原来的testApi函数调用改为：
function testUserApi(endpoint) {
    testApi(endpoint, 'user-result');
}

function testBikeApi(endpoint) {
    testApi(endpoint, 'bike-result');
}

function testSystemApi(endpoint) {
    testApi(endpoint, 'test-result');
}
// State Management
let currentState = 'home';
let posts = [
    { id: 1, author: '꿀피부관리사', time: '10분 전', title: '지성 피부 관리 꿀팁 공유합니다!', content: '요즘 날씨가 더워지면서 피지 분비가 늘어나는데...', likes: 24, comments: 8, melanin: 30, skinType: '지성', image: true },
    { id: 2, author: '스킨뉴비', time: '30분 전', title: 'CapSkin 분석 결과 어떤가요?', content: '멜라닌 농도가 좀 높게 나왔는데 자외선 차단제 추천 부탁드려요.', likes: 12, comments: 5, melanin: 65, skinType: '건성', image: false }
];

// Initialize
window.onload = () => {
    setTimeout(() => {
        document.getElementById('splash').classList.add('hidden');
        document.getElementById('app').classList.remove('hidden');
        document.getElementById('bottom-nav').classList.remove('hidden');
        navigate('home');
    }, 2000);
};

// Navigation
function navigate(screen) {
    currentState = screen;
    const content = document.getElementById('screen-content');
    updateNavUI(screen);

    switch(screen) {
        case 'home':
            content.innerHTML = renderHome();
            break;
        case 'community':
            content.innerHTML = renderCommunity();
            break;
        case 'camera':
            content.innerHTML = renderCamera();
            startCamera();
            break;
        case 'result':
            content.innerHTML = renderResult();
            break;
    }
}

function updateNavUI(screen) {
    const btns = document.querySelectorAll('.nav-btn');
    btns.forEach(btn => {
        const label = btn.querySelector('span:last-child').innerText;
        const icon = btn.querySelector('.material-icons');
        if ((label === '홈' && screen === 'home') ||
            (label === '커뮤니티' && screen === 'community') ||
            (label === '분석' && screen === 'camera')) {
            btn.classList.add('text-[#E91E63]');
            btn.classList.remove('text-gray-400');
        } else {
            btn.classList.remove('text-[#E91E63]');
            btn.classList.add('text-gray-400');
        }
    });
}

// Rendering Functions
function renderHome() {
    return `
        <div class="p-6">
            <p class="text-gray-500 text-lg">안녕하세요!</p>
            <h2 class="text-2xl font-bold mb-8">오늘의 피부 상태는 어떤가요?</h2>

            <div onclick="navigate('camera')" class="w-full h-48 rounded-3xl p-6 text-white flex flex-col justify-end cursor-pointer" style="background: linear-gradient(180deg, #E91E63 0%, #F06292 100%);">
                <span class="material-icons text-3xl mb-2">photo_camera</span>
                <h3 class="text-xl font-bold">피부 분석 시작하기</h3>
                <p class="opacity-80 text-sm">AI 분광 분석으로 정밀하게</p>
            </div>

            <h3 class="font-bold mt-8 mb-4">오늘의 스킨 케어 팁</h3>
            <div class="bg-gray-100 p-4 rounded-2xl flex items-center">
                <span class="material-icons text-[#E91E63] mr-4">auto_awesome</span>
                <div>
                    <p class="font-bold text-sm">자외선 차단제의 중요성</p>
                    <p class="text-xs text-gray-500">흐린 날에도 자외선은 피부 노화의 주범입니다.</p>
                </div>
            </div>
        </div>
    `;
}

function renderCommunity() {
    return `
        <div class="p-6">
            <h2 class="text-2xl font-bold mb-4">커뮤니티</h2>
            <div class="relative mb-6">
                <span class="material-icons absolute left-3 top-2.5 text-gray-400">search</span>
                <input type="text" placeholder="피부 타입, 키워드 검색" class="w-full bg-gray-100 rounded-xl py-2 pl-10 pr-4 outline-none border-none text-sm">
            </div>

            <div class="space-y-4">
                ${posts.map(post => `
                    <div class="bg-white border border-gray-100 rounded-2xl p-4 shadow-sm">
                        <div class="flex justify-between items-center mb-2">
                            <div class="flex items-center">
                                <span class="text-[#E91E63] text-sm font-semibold mr-2">${post.author}</span>
                                <span class="text-gray-400 text-[10px]">${post.time}</span>
                            </div>
                            <span class="bg-pink-50 text-[#E91E63] text-[10px] font-bold px-2 py-1 rounded">${post.skinType}</span>
                        </div>
                        <h4 class="font-bold text-md mb-1">${post.title}</h4>
                        <div class="flex items-center space-x-3 mb-3">
                            <div class="flex items-center text-[10px] font-bold text-[#E91E63]">
                                <span class="w-1.5 h-1.5 bg-[#E91E63] rounded-full mr-1"></span> 멜라닌 ${post.melanin}%
                            </div>
                        </div>
                        <div class="flex justify-between">
                            <p class="text-gray-500 text-sm line-clamp-2 flex-1">${post.content}</p>
                            ${post.image ? `<div class="w-16 h-16 bg-gray-100 rounded-lg ml-3 flex items-center justify-center text-gray-300"><span class="material-icons">image</span></div>` : ''}
                        </div>
                    </div>
                `).join('')}
            </div>
        </div>
        <button onclick="navigate('camera')" class="fixed bottom-20 right-6 w-14 h-14 bg-[#E91E63] text-white rounded-full shadow-lg flex items-center justify-center z-50">
            <span class="material-icons">edit</span>
        </button>
    `;
}

function renderCamera() {
    return `
        <div class="relative h-screen bg-black overflow-hidden">
            <video id="web-video" class="h-full w-full object-cover opacity-60" autoplay playsinline></video>
            <div class="absolute inset-0 camera-overlay flex flex-col items-center justify-center">
                <!-- Landmark dots -->
                <div class="landmark" style="top:35%; left:50%"></div>
                <div class="landmark" style="top:42%; left:40%"></div>
                <div class="landmark" style="top:42%; left:60%"></div>
                <div class="landmark" style="top:48%; left:50%"></div>
                <div class="landmark" style="top:55%; left:42%"></div>
                <div class="landmark" style="top:55%; left:58%"></div>
                <div class="landmark" style="top:62%; left:50%"></div>

                <div class="absolute bottom-32 text-center w-full px-10">
                    <p class="bg-black/40 text-white text-xs py-2 px-4 rounded-full inline-block backdrop-blur-sm">얼굴을 가이드 점에 맞춰주세요</p>
                    <div class="mt-8 flex justify-center">
                        <button onclick="navigate('result')" class="w-20 h-20 bg-[#E91E63] rounded-full border-4 border-white/30 flex items-center justify-center">
                            <span class="material-icons text-white text-4xl">photo_camera</span>
                        </button>
                    </div>
                </div>
            </div>
            <button onclick="navigate('home')" class="absolute top-6 left-6 text-white">
                <span class="material-icons">close</span>
            </button>
        </div>
    `;
}

function renderResult() {
    return `
        <div class="p-6">
            <h2 class="text-2xl font-bold mb-6">분석 결과</h2>
            <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 space-y-4">
                <div class="flex justify-between items-center">
                    <span class="text-gray-500">피부 타입</span>
                    <span class="text-[#E91E63] font-bold">복합성</span>
                </div>
                <hr class="border-gray-50">
                <div class="flex justify-between items-center">
                    <span class="text-gray-500">멜라닌 농도</span>
                    <span class="text-pink-400 font-bold">45%</span>
                </div>
                <div class="flex justify-between items-center">
                    <span class="text-gray-500">헤모글로빈 농도</span>
                    <span class="text-red-400 font-bold">32%</span>
                </div>
            </div>

            <div class="mt-8 bg-pink-50 p-6 rounded-3xl">
                <div class="flex items-center mb-3 text-[#E91E63] font-bold">
                    <span>✨ AI 맞춤형 가이드</span>
                </div>
                <p class="text-sm text-pink-900 leading-relaxed opacity-80">
                    현재 고객님의 피부는 헤모글로빈 농도가 다소 높아 자극에 민감할 수 있습니다. 자외선 차단제를 필수로 사용하세요.
                </p>
            </div>

            <div class="mt-10 flex gap-3">
                <button onclick="navigate('home')" class="flex-1 py-4 bg-gray-100 rounded-2xl font-bold text-gray-500">홈으로</button>
                <button onclick="navigate('community')" class="flex-1 py-4 bg-[#E91E63] text-white rounded-2xl font-bold shadow-lg">공유하기</button>
            </div>
        </div>
    `;
}

async function startCamera() {
    try {
        const stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'user' } });
        const video = document.getElementById('web-video');
        if (video) video.srcObject = stream;
    } catch (err) {
        console.error("Camera access error:", err);
    }
}

const app = document.getElementById('app');

async function fetchPosts(){
  try{const res = await fetch('./data/posts.json'); return await res.json()}catch(e){return[]}
}

function nav(){
  return `<div class="nav">
    <button data-link="#/">홈</button>
    <button data-link="#/community">커뮤니티</button>
    <button data-link="#/create">작성</button>
    <button data-link="#/camera">카메라</button>
    <button data-link="#/history">히스토리</button>
  </div>`
}

function renderSplash(){
  app.innerHTML = `
  <div class="container splash center">
    <div class="card">
      <h1>Capskin</h1>
      <p class="small">웹 포팅 샘플 — 깔끔한 UI로 재구현되었습니다.</p>
      <div style="margin-top:12px">
        <a class="btn" href="#/">시작하기</a>
      </div>
    </div>
  </div>`
}

async function renderHome(){
  const posts = await fetchPosts();
  app.innerHTML = `<div class="container">
    <div class="header">
      <h2>홈</h2>
      <div class="small">게시물 ${posts.length}개</div>
    </div>
    ${nav()}
    <div class="list">${posts.map(p=>`<div class="card"><div class="post-title">${p.title}</div><div class="small">by ${p.author}</div><p>${p.content.slice(0,120)}</p><a href="#/post/${p.id}" class="small">자세히</a></div>`).join('')}</div>
  </div>`
}

async function renderCommunity(){
  await renderHome();
}

function renderCreate(){
  app.innerHTML = `<div class="container">
    <h2>새 게시물 작성</h2>
    ${nav()}
    <div class="card form">
      <input id="title" placeholder="제목" />
      <input id="author" placeholder="작성자" />
      <textarea id="content" rows="6" placeholder="내용"></textarea>
      <div style="margin-top:8px"><button id="save" class="btn">저장(로컬)</button></div>
    </div>
  </div>`
  document.getElementById('save').addEventListener('click', ()=>{
    alert('이 데모는 로컬 저장만 지원합니다. 실제 백엔드 연동을 원하시면 알려주세요.');
  })
}

async function renderPost(id){
  const posts = await fetchPosts();
  const p = posts.find(x=>x.id==id);
  if(!p){ app.innerHTML = `<div class="container"><h3>찾을 수 없음</h3><a href="#/">돌아가기</a></div>`; return }
  app.innerHTML = `<div class="container">
    <h2>${p.title}</h2>
    ${nav()}
    <div class="card">
      <div class="small">by ${p.author} · ${new Date(p.created).toLocaleString()}</div>
      <p style="margin-top:12px">${p.content}</p>
    </div>
  </div>`
}

function renderCamera(){
  app.innerHTML = `<div class="container">
    <h2>카메라</h2>
    ${nav()}
    <div class="card center">카메라 기능은 데모에서 제공되지 않습니다.</div>
  </div>`
}

function renderHistory(){
  app.innerHTML = `<div class="container">
    <h2>히스토리</h2>
    ${nav()}
    <div class="card">최근 활동이 없습니다.</div>
  </div>`
}

function router(){
  const hash = location.hash || '#/splash';
  const parts = hash.replace('#/','').split('/');
  if(hash==='#/splash') return renderSplash();
  if(parts[0]==='post') return renderPost(parts[1]);
  if(parts[0]==='community') return renderCommunity();
  if(parts[0]==='create') return renderCreate();
  if(parts[0]==='camera') return renderCamera();
  if(parts[0]==='history') return renderHistory();
  return renderHome();
}

window.addEventListener('hashchange', router);
window.addEventListener('load', ()=>{ if(!location.hash) location.hash='#/splash'; router(); document.body.addEventListener('click', e=>{ const t=e.target.closest('[data-link]'); if(t){ location.hash = t.getAttribute('data-link').replace('#',''); e.preventDefault(); } }) });

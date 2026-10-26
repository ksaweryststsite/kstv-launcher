'use strict';
const NATIVE=typeof KScreenNative!=='undefined';
const films=[
 ['Poza horyzontem','KSCREEN PREMIERA','Tam, gdzie konczy sie znany swiat, zaczyna sie nowa historia.','cinema_space.jpg','NOWOSC'],
 ['Ostatnie swiatlo','FILM','Jedna podroz. Jeden wybor, ktory zmieni wszystko.','cinema_desert.jpg','PREMIERA'],
 ['Dzikie serce','SERIAL','Tajemnica ukryta gleboko posrod lasow.','cinema_forest.jpg','NOWY SEZON'],
 ['Neonowe noce','FILM','Miasto nigdy nie zasypia. Jego sekrety tez nie.','cinema_urban.jpg','NOWOSC'],
 ['Granica ciszy','SERIAL','Po drugiej stronie zimna czeka prawda.','cinema_arctic.jpg','PREMIERA'],
 ['Miedzy cieniami','FILM','Nie wszystko jest takie, na jakie wyglada.','cinema_noir.jpg','POLECANE']
];
const demo=[
 {label:'Netflix',symbol:'NETFLIX',bg:'#f5f5f5',fg:'#e50914',pkg:'com.netflix.ninja'},
 {label:'YouTube',symbol:'▶',bg:'#ffffff',fg:'#ed122f',pkg:'com.google.android.youtube.tv'},
 {label:'Prime Video',symbol:'prime\nvideo',bg:'#116de8',fg:'#ffffff',pkg:'com.amazon.amazonvideo.livingroom'},
 {label:'Disney+',symbol:'Disney+',bg:'#072f60',fg:'#ffffff',pkg:'com.disney.disneyplus'},
 {label:'Max',symbol:'max',bg:'#2938ab',fg:'#ffffff',pkg:'com.wbd.stream'},
 {label:'Spotify',symbol:'◉',bg:'#1ed760',fg:'#041a09',pkg:'com.spotify.tv.android'},
 {label:'Apple TV',symbol:'● tv',bg:'#131929',fg:'#ffffff',pkg:'com.apple.atve.androidtv.appletv'},
 {label:'TVP VOD',symbol:'TVP\nVOD',bg:'#159fd9',fg:'#ffffff',pkg:'pl.tvp.vod'}
];
let apps=[],page='home',idx=0,front='A';
function resize(){let s=Math.min(innerWidth/1920,innerHeight/1080),e=document.getElementById('stage');e.style.transform='scale('+s+')';e.style.left=Math.round((innerWidth-s*1920)/2)+'px';e.style.top=Math.round((innerHeight-s*1080)/2)+'px';}
addEventListener('resize',resize);resize();
function toast(t){const e=document.getElementById('toast');e.innerText=t;e.classList.add('show');clearTimeout(toast.t);toast.t=setTimeout(()=>e.classList.remove('show'),2300);}
function getApps(){try{let a=JSON.parse(NATIVE?KScreenNative.apps():'[]');if(a.length)return a;}catch(e){console.warn(e);}return demo;}
function appbutton(a){let b=document.createElement('button');b.className='app';b.setAttribute('aria-label',a.label);let c=document.createElement('div');c.className='circle';if(a.icon){let i=new Image();i.src=a.icon;c.appendChild(i)}else{c.style.background=a.bg||'#f5f5f5';let s=document.createElement('span');s.style.color=a.fg||'#17212f';s.innerText=a.symbol||a.label;c.appendChild(s)}b.appendChild(c);let t=document.createElement('small');t.textContent=a.label;b.appendChild(t);b.addEventListener('click',()=>{if(NATIVE)KScreenNative.launch(a.pkg,a.activity||'');else toast('Podglad: '+a.label)});return b;}
function fillApps(el,list){el.replaceChildren();list.forEach(a=>el.appendChild(appbutton(a)))}
function card(i){const f=films[i],e=document.createElement('button');e.className='card';let img=new Image();img.src=f[3];e.appendChild(img);let fade=document.createElement('span');fade.className='fade';e.appendChild(fade);let tag=document.createElement('em');tag.textContent=f[4];e.appendChild(tag);let strong=document.createElement('strong');strong.textContent=f[0];e.appendChild(strong);e.addEventListener('click',()=>hero(i));return e;}
function hero(i){idx=i;const f=films[i];document.getElementById('heroTitle').textContent=f[0];document.getElementById('heroService').textContent=f[1];document.getElementById('heroDescription').textContent=f[2];const to=front==='A'?'bgB':'bgA',from=front==='A'?'bgA':'bgB';document.getElementById(to).style.backgroundImage="url('"+f[3]+"')";document.getElementById(to).classList.add('active');document.getElementById(from).classList.remove('active');front=to==='bgA'?'A':'B';document.querySelectorAll('#dots i').forEach((d,j)=>d.classList.toggle('active',j===idx%5));}
function show(p){page=p;document.querySelectorAll('.page').forEach(x=>x.classList.toggle('show',x.id===p));document.querySelectorAll('header button[data-action]').forEach(x=>x.classList.toggle('chosen',x.dataset.action===p));const target=p==='search'?document.getElementById('query'):document.querySelector('#'+p+' button');target?.focus();}
function action(a){if(a==='home'||a==='apps'||a==='search'||a==='settings')show(a);else if(a==='system'){if(NATIVE)KScreenNative.settings();else toast('Ustawienia Android TV')}else if(a==='live')toast('Kanaly na zywo: wybierz aplikacje');else if(a==='ambient')toast('Tryb demonstracyjny');else if(a==='profile')toast('Profil KScreen TV');}
document.querySelectorAll('[data-action]').forEach(b=>b.addEventListener('click',()=>action(b.dataset.action)));
function direction(key){const shown=[...document.querySelectorAll('button')].filter(e=>e.getClientRects().length&&e.offsetParent!==null);let current=document.activeElement;if(!shown.includes(current)){shown[0]?.focus();return;}const a=current.getBoundingClientRect(),ax=(a.left+a.right)/2,ay=(a.top+a.bottom)/2;let best=Infinity,choice=null;const isH=key==='ArrowLeft'||key==='ArrowRight',sign=key==='ArrowRight'||key==='ArrowDown';shown.forEach(e=>{if(e===current)return;const r=e.getBoundingClientRect(),dx=(r.left+r.right)/2-ax,dy=(r.top+r.bottom)/2-ay,main=isH?dx:dy,cross=isH?dy:dx;if(sign?main<=6:main>=-6)return;let s=Math.abs(main)+Math.abs(cross)*2.1;if(s<best){choice=e;best=s}});if(choice){choice.focus();choice.scrollIntoView({block:'nearest',inline:'nearest',behavior:'smooth'})}}
document.addEventListener('keydown',e=>{if(e.target?.tagName==='INPUT'&&!['Escape','ArrowDown'].includes(e.key))return;if(['ArrowLeft','ArrowRight','ArrowUp','ArrowDown'].includes(e.key)){e.preventDefault();direction(e.key)}else if(e.key==='Escape'||e.key==='Backspace'){e.preventDefault();window.ksBack()}else if(e.key==='Enter'&&e.target?.tagName!=='INPUT'){e.preventDefault();document.activeElement?.click()}});
window.ksBack=()=>{if(page!=='home')show('home');else toast('KScreen TV')};
const search=document.getElementById('query');search.addEventListener('input',()=>fillApps(document.getElementById('matches'),apps.filter(x=>x.label.toLowerCase().includes(search.value.toLowerCase()))));
apps=getApps();const priorities=['netflix','youtube','prime','amazon','disney','max','spotify'];apps.sort((a,b)=>{let r=o=>{let s=(o.label+' '+o.pkg).toLowerCase(),i=priorities.findIndex(p=>s.includes(p));return i<0?99:i};return r(a)-r(b)||a.label.localeCompare(b.label)});
fillApps(document.getElementById('appsHome'),apps);fillApps(document.getElementById('allApps'),apps);fillApps(document.getElementById('matches'),apps);
films.forEach((f,i)=>document.getElementById('featured').appendChild(card(i)));[3,1,4,5,2].forEach(i=>document.getElementById('more').appendChild(card(i)));
function clock(){document.getElementById('time').textContent=new Date().toLocaleTimeString('pl-PL',{hour:'2-digit',minute:'2-digit'})}clock();setInterval(clock,30000);setInterval(()=>{if(page==='home')hero((idx+1)%films.length)},12000);
requestAnimationFrame(()=>document.querySelector('#featured button')?.focus());
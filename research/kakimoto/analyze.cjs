const fs=require('fs');
const path=require('path');
const input=process.argv[2]||'reference.f32';
const buf=fs.readFileSync(path.join(__dirname,input));
const x=new Float32Array(buf.buffer,buf.byteOffset,buf.length/4),rate=12000;
function fft(re,im){const n=re.length;for(let i=1,j=0;i<n;i++){let b=n>>1;for(;j&b;b>>=1)j^=b;j^=b;if(i<j){[re[i],re[j]]=[re[j],re[i]];[im[i],im[j]]=[im[j],im[i]];}}for(let len=2;len<=n;len<<=1){const a=-2*Math.PI/len;for(let i=0;i<n;i+=len){for(let j=0;j<len/2;j++){const c=Math.cos(a*j),s=Math.sin(a*j),k=i+j,q=k+len/2,tr=re[q]*c-im[q]*s,ti=re[q]*s+im[q]*c;re[q]=re[k]-tr;im[q]=im[k]-ti;re[k]+=tr;im[k]+=ti;}}}}
function inspect(t){const n=16384,off=Math.round(t*rate),re=new Float64Array(n),im=new Float64Array(n);let rms=0;for(let i=0;i<n;i++){const v=x[off+i]||0;re[i]=v*(.5-.5*Math.cos(2*Math.PI*i/(n-1)));rms+=v*v;}fft(re,im);const p=Array.from(re, (v,i)=>v*v+im[i]*im[i]);const peaks=[];for(let i=10;i<550;i++)if(p[i]>p[i-1]&&p[i]>p[i+1])peaks.push({hz:+(i*rate/n).toFixed(2),db:+(10*Math.log10(p[i])).toFixed(1)});peaks.sort((a,b)=>b.db-a.db);const ac=[];for(let lag=100;lag<1800;lag++){let s=0,a=0,b=0;for(let i=0;i<12000;i++){const v=x[off+i]||0,w=x[off+i+lag]||0;s+=v*w;a+=v*v;b+=w*w;}ac[lag]=s/Math.sqrt(a*b);}const aa=[];for(let lag=101;lag<1799;lag++)if(ac[lag]>ac[lag-1]&&ac[lag]>ac[lag+1])aa.push({hz:+(rate/lag).toFixed(2),cor:+ac[lag].toFixed(3)});aa.sort((a,b)=>b.cor-a.cor);return {t,rms:+Math.sqrt(rms/n).toFixed(4),peaks:peaks.slice(0,10),periodicity:aa.slice(0,5)};}
const times=process.argv[3]?process.argv[3].split(',').map(Number):[3,7,11,15,18,20,21,23,24,26,30,31,33,34,36,40,44,49,53,57,60];
const report=times.map(inspect);
fs.writeFileSync(path.join(__dirname,input==='reference.f32'?'analysis.json':path.basename(input,'.f32')+'-analysis.json'),JSON.stringify(report,null,2));
for(const r of report)console.log(JSON.stringify(r));

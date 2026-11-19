// Offline extraction from the FIRST, user-supplied WRX recording only.
// No GR86 samples, spectra or timing enter this generator.
const fs=require('fs'),path=require('path'),crypto=require('crypto');
const base=__dirname,rate=12000,size=2048,harmonics=128;
const raw=fs.readFileSync(path.join(base,'reference.f32'));
const expectedDecoded='37CB2B3C56DBD9C2D8E12625A309F4FFF7420F47E8E403E839B274A5F82F6DFD';
if(crypto.createHash('sha256').update(raw).digest('hex').toUpperCase()!==expectedDecoded)throw new Error('Input is not the verified FIRST WRX reference decode.');
const data=new Float32Array(raw.buffer,raw.byteOffset,raw.length/4);
function at(i){const j=Math.floor(i),a=i-j;return (data[j]||0)*(1-a)+(data[j+1]||0)*a;}
function correlation(t,duration,lag){const off=Math.round((t-duration/2)*rate),n=Math.round(duration*rate);let a=0,b=0,s=0;for(let i=0;i<n;i++){const x=at(off+i),y=at(off+i+lag);a+=x*x;b+=y*y;s+=x*y;}return s/Math.sqrt(a*b);}
function period(t,duration,hint){let best={cor:-1,lag:0};for(let lag=rate/(hint*1.12);lag<rate/(hint*.88);lag+=.25){const cor=correlation(t,duration,lag);if(cor>best.cor)best={cor,lag};}return {...best,hz:rate/best.lag};}
// Choose the most repeatable short WRX high-rev window, rather than averaging a whole sweep.
let high={cor:-1};for(let t=30;t<=34;t+=.05){const p=period(t,.10,57);if(p.cor>high.cor)high={...p,t};}
const anchors=[{t:11.65,seconds:.34,hint:12.25},{t:15.35,seconds:.34,hint:14.55},{t:44.5,seconds:.34,hint:21.4},{t:high.t,seconds:.10,hint:high.hz}];
function extract(a){const p=period(a.t,a.seconds,a.hint),off=(a.t-a.seconds/2)*rate,n=Math.round(a.seconds*rate),cs=[],sn=[];let weight=0;
 for(let i=0;i<n;i++)weight+=.5-.5*Math.cos(2*Math.PI*i/(n-1));
 for(let h=1;h<=harmonics;h++){let c=0,s=0;if(h*p.hz<5600)for(let i=0;i<n;i++){const w=.5-.5*Math.cos(2*Math.PI*i/(n-1)),v=at(off+i)*w,angle=2*Math.PI*h*p.hz*(i/rate-a.seconds/2);c+=v*Math.cos(angle);s+=v*Math.sin(angle);}cs[h]=2*c/weight;sn[h]=2*s/weight;}
 const table=new Float64Array(size);for(let j=0;j<size;j++)for(let h=1;h<=harmonics;h++){// Taper the top orders; preserve measured phase relationships and uneven pulses.
  const taper=h<96?1:.5+.5*Math.cos(Math.PI*(h-96)/32);table[j]+=(cs[h]*Math.cos(2*Math.PI*h*j/size)+sn[h]*Math.sin(2*Math.PI*h*j/size))*taper;
 }
 let energy=0;for(const v of table)energy+=v*v;const rms=Math.sqrt(energy/size);for(let j=0;j<size;j++)table[j]*=.22/rms;
 return {table,metadata:{source:'WRX only',centerSeconds:+a.t.toFixed(3),windowSeconds:a.seconds,periodHz:+p.hz.toFixed(5),periodCorrelation:+p.cor.toFixed(5),modelRpm:+(p.hz*60).toFixed(2),rawCycleRms:+rms.toFixed(6)}};
}
const result=anchors.map(extract);result.sort((a,b)=>a.metadata.modelRpm-b.metadata.modelRpm);
// Align adjacent cycles by circular correlation so RPM interpolation does not cancel their body.
let max=0;for(let i=1;i<size;i++)if(result[0].table[i]>result[0].table[max])max=i;
result[0].table=Float64Array.from({length:size},(_,i)=>result[0].table[(i+max)%size]);
for(let k=1;k<result.length;k++){let best=-Infinity,shift=0;for(let s=0;s<size;s++){let dot=0;for(let j=0;j<size;j+=4)dot+=result[k-1].table[j]*result[k].table[(j+s)%size];if(dot>best){best=dot;shift=s;}}const old=result[k].table;result[k].table=Float64Array.from({length:size},(_,i)=>old[(i+shift)%size]);}
const encoded=result.map(r=>{const b=Buffer.alloc(size*2);for(let j=0;j<size;j++)b.writeInt16LE(Math.round(Math.max(-.99,Math.min(.99,r.table[j]))*32767),j*2);return b.toString('base64');});
const rpms=result.map(r=>r.metadata.modelRpm);
const java=`package hk.local.enginex7;
/** Periodic exhaust waveforms extracted from the user-supplied WRX GT1.0Z recording.
 * Generated offline by research/kakimoto/build-wrx-tables.cjs. No GR86 material.
 * RPM anchors are a virtual-engine mapping (period Hz * 60), not measured ECU RPM. */
public final class WrxCycles {
 public static final int SIZE=${size};
 public static final double[] RPM={${rpms.join(',')}};
 private static final String[] DATA={${encoded.map(v=>JSON.stringify(v)).join(',\n')}};
 public static final float[][] TABLES=decode();
 private static float[][] decode(){float[][] tables=new float[DATA.length][SIZE];for(int k=0;k<DATA.length;k++){byte[] bytes=java.util.Base64.getDecoder().decode(DATA[k]);for(int i=0;i<SIZE;i++)tables[k][i]=(short)((bytes[i*2]&255)|(bytes[i*2+1]<<8))/32767f;}return tables;}
 private WrxCycles(){}
}
`;
fs.writeFileSync(path.join(base,'../../app/src/main/java/hk/local/enginex7/WrxCycles.java'),java);
const report={referenceSha256:'3D297F42D250CC32CA4A58E2947AF6223DD72596A5182FCB5E31ECCE5324FD48',decodedFloatSha256:crypto.createHash('sha256').update(raw).digest('hex').toUpperCase(),referenceDurationSeconds:data.length/rate,gr86Used:false,method:'Hann-weighted cycle Fourier projection; 128 orders tapered after 96; phase alignment; per-cycle RMS normalization',rpmMapping:'Model assumption: extracted periodicity Hz multiplied by 60; no recorded ECU RPM is available.',anchors:result.map(r=>r.metadata),javaSha256:crypto.createHash('sha256').update(java).digest('hex').toUpperCase()};
fs.writeFileSync(path.join(base,'wrx-extraction.json'),JSON.stringify(report,null,2));console.log(JSON.stringify(report,null,2));

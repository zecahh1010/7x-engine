package hk.local.enginex7;
import android.content.Context;
import android.media.*;
import android.os.SystemClock;
import java.util.*;

/** PCM delivery and route checks are separate from physical speaker audibility. */
public final class AudioOutput {
 private final AudioManager manager;private final Config config;private final EngineModel model;
 private volatile Thread worker;private volatile boolean requested;
 public volatile String status="",error="",internalActual="—",externalActual="—";
 public volatile long frames,playedFrames,routeChecks;public volatile int internalRouted=-1,externalRouted=-1,rate;
 public volatile double peak,focusScale=1;public volatile boolean ready,testActive,lastWasTest,testCancelled;public volatile String activeOutput="internal";
 public AudioOutput(Context c,Config config,EngineModel model){manager=(AudioManager)c.getSystemService(Context.AUDIO_SERVICE);this.config=config;this.model=model;}
 public static boolean external(AudioDeviceInfo d){String a=d.getAddress().toUpperCase(Locale.US);return a.contains("BUS12_OUTER_NOTIFY")||a.contains("OUTER")||a.contains("EXTERNAL_SPEAKER");}
 public static String key(AudioDeviceInfo d){return d.getType()+"|"+d.getAddress()+"|"+d.getProductName();}
 public static String describe(AudioDeviceInfo d){return d==null?"—":d.getProductName()+" · "+(d.getAddress().isEmpty()?"type "+d.getType():d.getAddress())+" · #"+d.getId();}
 public List<AudioDeviceInfo> devices(){List<AudioDeviceInfo> all=new ArrayList<>();if(manager!=null)for(AudioDeviceInfo d:manager.getDevices(AudioManager.GET_DEVICES_OUTPUTS))if(d.isSink())all.add(d);return all;}
 public AudioDeviceInfo resolve(boolean outer){String stored=outer?config.externalDevice:config.internalDevice;
  if(!stored.isEmpty()){AudioDeviceInfo found=null;for(AudioDeviceInfo d:devices())if(key(d).equals(stored)){if(found!=null)return null;found=d;}return found;}
  // Automatic cabin output follows the OEM's normal media route, not a forced builtin speaker.
  if(outer)for(AudioDeviceInfo d:devices())if(d.getAddress().toUpperCase(Locale.US).contains("BUS12_OUTER_NOTIFY"))return d;return null;
 }
 public int systemVolume(){return manager==null?0:manager.getStreamVolume(AudioManager.STREAM_MUSIC);}
 public int systemMaximum(){return manager==null?1:manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);}
 public boolean systemMuted(){return manager==null||manager.isStreamMute(AudioManager.STREAM_MUSIC)||systemVolume()==0;}
 public void systemVolume(int n){if(manager!=null)manager.setStreamVolume(AudioManager.STREAM_MUSIC,n,0);}
 public String canStart(){return canStart(config.output);}
 private String canStart(String selected){boolean in=!"external".equals(selected),out=!"internal".equals(selected);AudioDeviceInfo a=in?resolve(false):null,b=out?resolve(true):null;
  if(in&&!config.internalDevice.isEmpty()&&a==null)return config.t("車內裝置已離線；選自動或重新對應。","Cabin device offline. Choose automatic or map again.");
  if(in&&a!=null&&external(a))return config.t("車內對應了車外裝置，請重新選擇。","Cabin maps to an exterior device. Select again.");
  if(out&&b==null)return config.t("找不到車外 BUS12；請到設定重新列出裝置。","External BUS12 unavailable. Refresh devices in Settings.");
  if(in&&out&&a!=null&&a.getId()==b.getId())return config.t("兩路對應同一裝置；請重新選擇。","Both outputs map to one device. Select again.");return "";
 }
 public boolean start(){return begin(config.output,false);}
 public boolean startTest(String output){return begin(output,true);}
 private synchronized boolean begin(String selected,boolean test){if(worker!=null)return false;activeOutput=selected;lastWasTest=test;testCancelled=false;error=status="";frames=playedFrames=0;peak=0;internalActual=externalActual="—";String problem=canStart(selected);if(!problem.isEmpty()){error=problem;return false;}
  requested=true;ready=false;testActive=test;error="";status="";frames=playedFrames=0;peak=0;rate=config.sampleRate;internalRouted=externalRouted=-1;internalActual=externalActual="—";
  boolean cabin=!"external".equals(selected),outer=!"internal".equals(selected);AudioDeviceInfo in=cabin?resolve(false):null,out=outer?resolve(true):null;worker=new Thread(()->pump(in,out,cabin,outer,test),"7X-engine-audio");worker.start();return true;
 }
 public boolean active(){return worker!=null;}public void stop(){if(testActive){testCancelled=true;status=config.t("試音已停止","Test stopped");}requested=false;}
 private AudioTrack track(AudioDeviceInfo d){int min=AudioTrack.getMinBufferSize(rate,AudioFormat.CHANNEL_OUT_STEREO,AudioFormat.ENCODING_PCM_16BIT);if(min<=0)throw new IllegalStateException("PCM format unavailable: "+rate);
  AudioTrack t=new AudioTrack.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).setAudioFormat(new AudioFormat.Builder().setSampleRate(rate).setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).setEncoding(AudioFormat.ENCODING_PCM_16BIT).build()).setBufferSizeInBytes(Math.max(min*2,8192)).setTransferMode(AudioTrack.MODE_STREAM).build();
  try{if(t.getState()!=AudioTrack.STATE_INITIALIZED||(d!=null&&!t.setPreferredDevice(d)))throw new IllegalStateException("Route rejected: "+describe(d));if(t.setVolume(0)!=AudioTrack.SUCCESS)throw new IllegalStateException("Cannot mute track");t.play();return t;}catch(RuntimeException e){t.release();throw e;}
 }
 private boolean matches(AudioTrack t,AudioDeviceInfo d){AudioDeviceInfo a=t.getRoutedDevice();routeChecks++;return a!=null&&(d==null?!external(a):a.getId()==d.getId());}
 private void prime(AudioTrack t,AudioDeviceInfo d)throws Exception{short[] silence=new short[1024];long end=SystemClock.elapsedRealtime()+2200;while(requested){if(matches(t,d))return;if(SystemClock.elapsedRealtime()>end)throw new IllegalStateException(config.t("路由未確認；手動選裝置或切換取樣率。","Route unconfirmed. Select a device or change sample rate."));int n=t.write(silence,0,silence.length,AudioTrack.WRITE_NON_BLOCKING);if(n<0)throw new IllegalStateException("Route prime "+n);Thread.sleep(5);}}
 private void pump(AudioDeviceInfo in,AudioDeviceInfo out,boolean cabin,boolean outer,boolean test){AudioTrack a=null,b=null;try{
  android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);status=config.t("正在核對音訊路由…","Verifying audio routes…");if(cabin)a=track(in);if(outer)b=track(out);if(a!=null)prime(a,in);if(b!=null)prime(b,out);if(!requested)return;
  if(a!=null){if(!matches(a,in))throw new IllegalStateException("Cabin route changed");internalRouted=a.getRoutedDevice().getId();internalActual=describe(a.getRoutedDevice());}
  if(b!=null){if(!matches(b,out))throw new IllegalStateException("Exterior route changed");externalRouted=b.getRoutedDevice().getId();externalActual=describe(b.getRoutedDevice());}
  if(a!=null&&b!=null&&internalRouted==externalRouted)throw new IllegalStateException("Both routes resolved to one device");
  if(a!=null&&a.setVolume(1)!=AudioTrack.SUCCESS)throw new IllegalStateException("Cannot enable cabin output");if(b!=null&&b.setVolume(1)!=AudioTrack.SUCCESS)throw new IllegalStateException("Cannot enable exterior output");
  ready=true;status=config.t("PCM 已送出；實際喇叭請試音確認","PCM sent; confirm physical output with speaker test");EngineSynth synth=new EngineSynth(rate);float[] mono=new float[512];short[] ia=new short[1024],ob=new short[1024];double ga=0,gb=0;long checkAt=0;
  while(requested&&(a!=null||b!=null)){
   long now=SystemClock.elapsedRealtime();if(now-checkAt>100){checkAt=now;if(a!=null&&!matches(a,in)){release(a);a=null;internalRouted=-1;error=config.t("車內路由中斷；重新試音","Cabin route lost; test again");}if(b!=null&&!matches(b,out)){release(b);b=null;externalRouted=-1;error=config.t("車外路由中斷；重新試音","Exterior route lost; test again");}}
   if(a==null&&b==null)break;
   if(test){for(int i=0;i<mono.length;i++){double sec=(frames+i)/(double)rate;double envelope=Math.min(1,sec/.04)*Math.min(1,Math.max(0,(2.2-sec)/.08));mono[i]=(float)(.055*envelope*Math.sin(2*Math.PI*(sec<1.1?440:660)*sec));}}else synth.render(model.frame,mono);
   double ta=EngineSynth.dbToLinear(config.internalDb)*config.volume/100.*focusScale,tb=EngineSynth.dbToLinear(config.externalDb)*config.volume/100.*focusScale,p=0;
   for(int i=0;i<mono.length;i++){ga+=(ta-ga)*.001;gb+=(tb-gb)*.001;short va=EngineSynth.pcm(mono[i],ga),vb=EngineSynth.pcm(mono[i],gb);ia[i*2]=ia[i*2+1]=va;ob[i*2]=ob[i*2+1]=vb;p=Math.max(p,Math.abs(mono[i]));}peak=p;
   if(a!=null)write(a,ia);if(b!=null)write(b,ob);frames+=mono.length;playedFrames=Math.max(a==null?0:Integer.toUnsignedLong(a.getPlaybackHeadPosition()),b==null?0:Integer.toUnsignedLong(b.getPlaybackHeadPosition()));if(test&&frames>rate*2.2)break;
  }
  if(test&&requested){long end=SystemClock.elapsedRealtime()+1000;while(requested&&SystemClock.elapsedRealtime()<end&&((a!=null&&Integer.toUnsignedLong(a.getPlaybackHeadPosition())<frames)||(b!=null&&Integer.toUnsignedLong(b.getPlaybackHeadPosition())<frames)))Thread.sleep(10);status=config.t("試音結束；請確認實際喇叭有聲","Test finished; confirm the physical speaker was audible");}
 }catch(Exception e){if(requested){error=config.t("音訊停止：","Audio stopped: ")+e.getMessage();status="";}}
 finally{release(a);release(b);internalRouted=externalRouted=-1;requested=false;ready=false;testActive=false;worker=null;}}
 private void write(AudioTrack t,short[] x)throws Exception{int off=0;long end=SystemClock.elapsedRealtime()+1000;while(requested&&off<x.length){int n=t.write(x,off,x.length-off,AudioTrack.WRITE_NON_BLOCKING);if(n<0)throw new IllegalStateException("PCM write "+n);if(n==0){if(SystemClock.elapsedRealtime()>end)throw new IllegalStateException("Playback stalled");Thread.sleep(2);}else{off+=n;end=SystemClock.elapsedRealtime()+1000;}}}
 private void release(AudioTrack t){if(t!=null){try{t.setVolume(0);t.pause();t.flush();}catch(Exception ignored){}try{t.release();}catch(Exception ignored){}}}
}

package hk.local.enginex7;
import java.io.*;import java.util.*;

public final class CoreTests {
 public static int count;static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);count++;}
 static void tick(EngineModel m,double seconds){for(int i=0;i<(int)(seconds*100);i++)m.step(.01,0,0,false);}
 public static void run() throws Exception {
  EngineModel m=new EngineModel();m.start();m.step(.1,0,0,false);check(m.frame.igniting&&m.frame.master==0,"startup sweep muted");tick(m,.9);check(m.frame.phase.equals("crank")&&m.frame.audioRpm>100,"crank audio");tick(m,.6);check(m.frame.phase.equals("catch"),"ignition catch");tick(m,1);check(!m.frame.igniting&&m.frame.gear==0&&m.frame.rpm>=m.profile.idle,"zero pedal idle");
  m.pedal=1;tick(m,15);check(m.frame.speed>25,"sim accelerates");check(m.frame.gear>1&&m.frame.shiftSerial>0,"automatic upshifts");check(m.frame.boost>.2,"virtual boost builds");int before=m.frame.bovSerial;m.pedal=0;tick(m,.8);check(m.frame.bovSerial>before,"BOV on throttle release");int cooldown=m.frame.bovSerial;tick(m,.3);check(m.frame.bovSerial==cooldown,"BOV cooldown");int oldGear=m.frame.gear;m.brake=1;tick(m,6);check(m.frame.gear<oldGear,"braking downshifts");tick(m,10);check(m.frame.speed==0&&m.frame.gear==0&&m.frame.rpm>=m.profile.idle,"stop returns to idle");m.stop();tick(m,.1);check(!m.frame.running&&m.frame.master==0&&m.frame.rpm==0&&m.frame.load==0,"stop zeroes output");
  m.setMode(false);m.start();tick(m,3);for(int i=0;i<50;i++)m.step(.02,20,1.8,true);check(Math.abs(m.frame.speed-20)<.01&&m.frame.load>0,"GPS drives model");for(int i=0;i<200;i++)m.step(.02,99,5,false);check(m.frame.speed==0&&m.frame.load<.01,"stale GPS ignores stale speed");
  check(Math.abs(EngineSynth.dbToLinear(6)-1.995262)<.0001,"+6 dB amplitude");check(Math.abs(EngineSynth.dbToLinear(-6)-.501187)<.0001,"-6 dB amplitude");check(EngineSynth.pcm(100,8)>0&&EngineSynth.pcm(-100,8)<0,"peak limit no wrap");
  Set<Long> fingerprints=new HashSet<>();for(Profile p:Profile.ALL){EngineModel x=new EngineModel();x.select(p);x.start();EngineSynth synth=new EngineSynth(48000);float[] block=new float[480];double sum=0,peak=0;long hash=1;int n=0;
   for(int k=0;k<600;k++){x.pedal=k<300?0:.55;x.step(.01,0,0,false);synth.render(x.frame,block);for(float sample:block){checkFinite(sample,p.id);if(k>250){sum+=sample*sample;n++;peak=Math.max(peak,Math.abs(sample));}hash=hash*31+Float.floatToIntBits(sample);}}
   check(peak>.005&&peak<=1.01,p.id+" audible finite PCM");check(Math.sqrt(sum/n)>.001,p.id+" nonzero RMS");fingerprints.add(hash);
  }check(fingerprints.size()==Profile.ALL.length,"seven distinct synthesis outputs");
  EngineModel b=new EngineModel();b.start();tick(b,3);EngineSynth s1=new EngineSynth(48000),s2=new EngineSynth(48000);float[] a=new float[480],c=new float[480];EngineModel.Frame f=new EngineModel.Frame(b.profile,5000,5000,0,0,.08,.7,1,0,0,0,false,true,false,true,"run");s1.render(f,a);s2.render(f,c);
  EngineModel.Frame trigger=new EngineModel.Frame(f.profile,f.rpm,f.audioRpm,f.speed,f.accel,f.load,f.boost,f.master,f.gear,f.bovSerial+1,f.shiftSerial,f.cut,f.running,false,true,f.phase);s1.render(trigger,a);s2.render(f,c);double delta=0;for(int i=0;i<a.length;i++)delta+=Math.abs(a[i]-c[i]);check(delta>.1,"BOV changes actual PCM");
 }
 private static void checkFinite(float v,String name){if(Float.isNaN(v)||Float.isInfinite(v))throw new AssertionError("Nonfinite synthesis "+name);}
 public static void main(String[] args)throws Exception{run();System.out.println("PASS "+count+" core assertions");if(args.length>0){File folder=new File(args[0]);folder.mkdirs();for(Profile p:Profile.ALL)sample(p,new File(folder,p.id+".wav"));}}
 private static void le(DataOutputStream d,int x,int bytes)throws IOException{for(int i=0;i<bytes;i++)d.write(x>>>(8*i)&255);}
 private static void sample(Profile p,File file)throws Exception {int frames=48000*14;try(DataOutputStream d=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))){d.writeBytes("RIFF");le(d,36+frames*2,4);d.writeBytes("WAVEfmt ");le(d,16,4);le(d,1,2);le(d,1,2);le(d,48000,4);le(d,96000,4);le(d,2,2);le(d,16,2);d.writeBytes("data");le(d,frames*2,4);EngineModel m=new EngineModel();m.select(p);m.start();EngineSynth synth=new EngineSynth(48000);float[] block=new float[480];for(int k=0;k<1400;k++){m.pedal=k<400?0:k<1000?.8:0;m.step(.01,0,0,false);synth.render(m.frame,block);for(float f:block)le(d,EngineSynth.pcm(f,.7),2);}}}
}

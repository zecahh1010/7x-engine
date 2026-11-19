package hk.local.enginex7;
import java.util.*;
/** BOV trigger uses pre-lift RPM; sound decay is latched for the complete event. */
public final class BovRpmTests {
 static int count;static void check(boolean b,String s){if(!b)throw new AssertionError(s);count++;}
 static int tail(float[] sound){int last=0;for(int i=0;i<400;i++)if(AudioTuningProbe.rms(sound,i*480,(i+1)*480)>.001)last=i+1;return last*10;}
 static long now=10000;static void step(EngineModel m,double pedal,boolean fresh){now+=10;long stamp=fresh?now:now-2000;m.stepVehicle(.01,new VehicleSnapshot(pedal,stamp,0,stamp,0,stamp,0,stamp),now);}
 static EngineModel atRpm(double wantedRpm){EngineModel m=new EngineModel();m.setMode(false);m.start();for(int i=0;i<300;i++)step(m,0,true);double pedal=100*(wantedRpm-m.profile.idle)/((m.profile.redline-m.profile.idle)*.88);for(int i=0;i<300;i++)step(m,pedal,true);check(Math.abs(m.frame.rpm-wantedRpm)<1,"fixture reaches requested RPM");return m;}
 public static void main(String[] args)throws Exception{
  EngineModel below=atRpm(3900);int before=below.frame.bovSerial;for(int i=0;i<100;i++)step(below,0,true);check(below.frame.bovSerial==before,"below 4000 no Subaru release event");
  EngineModel above=atRpm(4100);before=above.frame.bovSerial;for(int i=0;i<150;i++)step(above,0,true);check(above.frame.bovSerial==before+1,"above 4000 triggers exactly one release");check(above.frame.bovRpm>4090&&above.frame.rpm<above.profile.idle+10,"event remembers pre-lift RPM after engine has returned to idle");
  EngineModel stale=atRpm(6000);before=stale.frame.bovSerial;for(int i=0;i<100;i++)step(stale,0,false);check(stale.frame.bovSerial==before,"stale input does not create a release event");
  EngineModel light=atRpm(3900);for(int i=0;i<300;i++){now+=10;light.stepVehicle(.01,new VehicleSnapshot(15,now,0,now,10.6,now,0,now),now);}before=light.frame.bovSerial;now+=10;light.stepVehicle(.01,new VehicleSnapshot(0,now,0,now,10.6,now,0,now),now);check(light.frame.bovSerial==before+1&&light.frame.bovRpm>=4000,"light-pedal release above 4000 is not blocked by a pressure/load threshold");
  float[] gated=AudioTuningProbe.bov(3999);check(AudioTuningProbe.rms(gated,0,gated.length)==0,"DSP rejects BOV below 4000");
  int[] tails=new int[4];float[] comparison=new float[AudioTuningProbe.RATE*16];for(int i=0;i<4;i++){float[] sound=AudioTuningProbe.bov(4000+i*1000);tails[i]=tail(sound);System.arraycopy(sound,0,comparison,i*sound.length,sound.length);}if(args.length>0)AudioTuningProbe.wav(new java.io.File(args[0],"bov-rpm-comparison.wav"),comparison);
  check(tails[0]>=300&&tails[0]<=650,"4000 boundary has an audible short release");for(int i=1;i<4;i++)check(tails[i]>tails[i-1]+100,"higher release RPM has longer tail");check(tails[3]<1500,"top-end BOV avoids former 2.67-second tail");
  System.out.printf(Locale.US,"{\"result\":\"PASS\",\"assertions\":%d,\"thresholdRpm\":4000,\"tail4000ms\":%d,\"tail5000ms\":%d,\"tail6000ms\":%d,\"tail7000ms\":%d,\"physicalSpeakerMeasured\":false}%n",count,tails[0],tails[1],tails[2],tails[3]);
 }
}

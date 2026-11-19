package hk.local.enginex7;
import java.util.Locale;

/** Audio-specific regressions: streaming determinism, RPM sweeps and release behavior. */
public final class WrxAudioTests {
 static int checks;
 static void check(boolean ok,String text){if(!ok)throw new AssertionError(text);checks++;}
 static EngineModel.Frame frame(double rpm,double master,boolean cut){return new EngineModel.Frame(Profile.get("subaru"),rpm,rpm,0,0,.35,.4,master,0,0,0,cut,true,false,true,"run");}
 static double rms(float[] b){double e=0;for(float v:b)e+=v*v;return Math.sqrt(e/b.length);}
 public static void main(String[] args){
  // Longer than the 100-cycle phase wrap, with different producer buffer sizes.
  EngineSynth a=new EngineSynth(48000),b=new EngineSynth(48000);float[] full=new float[48000*17],part=new float[257];
  EngineModel.Frame idle=frame(820,1,false);a.render(idle,full);int offset=0;boolean equal=true;
  while(offset<full.length){int n=Math.min(part.length,full.length-offset);float[] chunk=n==part.length?part:new float[n];b.render(idle,chunk);for(int i=0;i<n;i++)if(Float.floatToIntBits(full[offset+i])!=Float.floatToIntBits(chunk[i]))equal=false;offset+=n;}
  check(equal,"PCM must not depend on AudioTrack producer buffer size, including phase wrap");
  EngineSynth sweep=new EngineSynth(48000);float[] block=new float[480];double peak=0,jump=0,last=0,lowest=1;long started=System.nanoTime();
  for(int k=0;k<3000;k++){double rpm=k<1500?300+6700*k/1500.0:7000-6700*(k-1500)/1500.0;sweep.render(frame(rpm,1,k%300>285),block);for(float v:block){checkFinite(v);peak=Math.max(peak,Math.abs(v));jump=Math.max(jump,Math.abs(v-last));last=v;}if(k>100&&k%300<270)lowest=Math.min(lowest,rms(block));}
  double seconds=(System.nanoTime()-started)/1e9;
  check(peak>.1&&peak<1,"RPM sweep has signal and PCM headroom");
  check(jump<.35,"continuous RPM/table transitions and cut gain ramps avoid large discontinuities");
  check(lowest>.025,"no silent holes through anchor interpolation");
  for(int k=0;k<100;k++)sweep.render(frame(820,0,false),block);
  check(rms(block)<.00001,"stop fades to silence");
  sweep.render(frame(820,1,false),block);check(rms(block)>0,"render can restart after fade");
  System.out.printf(Locale.US,"{\"result\":\"PASS\",\"assertions\":%d,\"sweepSeconds\":30,\"renderWallSeconds\":%.4f,\"maxFloat\":%.6f,\"maxAdjacentDelta\":%.6f,\"minSweepBlockRms\":%.6f,\"blockSizeInvariant\":true}%n",checks,seconds,peak,jump,lowest);
 }
 static void checkFinite(float v){if(!Float.isFinite(v))throw new AssertionError("nonfinite audio");}
}

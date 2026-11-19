package hk.local.enginex7;
import java.security.*;import java.util.*;
/** Fixed DSP inputs separate sound-preset preservation from changed gearbox scheduling. */
public final class ProfileSoundProbe {
 public static void main(String[] args)throws Exception{
  System.out.print("{");boolean first=true;
  for(Profile p:Profile.ALL){EngineSynth s=new EngineSynth(48000);MessageDigest hash=MessageDigest.getInstance("SHA-256");float[] out=new float[480];
   for(int k=0;k<700;k++){double rpm=k<150?p.idle:k<450?p.idle+(p.redline-p.idle)*(k-150)/300.:p.redline-(p.redline-p.idle)*(k-450)/250.;
    s.render(new EngineModel.Frame(p,rpm,rpm,20,0,k<450?.65:.05,.5,1,2,k>=450?1:0,0,false,true,false,true,"run"),out);
    for(float v:out){int n=Float.floatToIntBits(v);hash.update((byte)n);hash.update((byte)(n>>>8));hash.update((byte)(n>>>16));hash.update((byte)(n>>>24));}
   }
   if(!first)System.out.print(",");first=false;StringBuilder hex=new StringBuilder();for(byte b:hash.digest())hex.append(String.format(Locale.US,"%02x",b&255));System.out.print("\""+p.id+"\":\""+hex+"\"");
  }System.out.println("}");
 }
}

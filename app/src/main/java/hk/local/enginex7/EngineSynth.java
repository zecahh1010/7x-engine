package hk.local.enginex7;

/** Local PCM synthesis; Subaru exhaust uses cycles from the supplied WRX recording. */
public final class EngineSynth {
 private final int rate;private Profile profile;private double phase,rpm=800,load,boost,master,env,low,low2,body,bodyVel,air,whistle,bov,bovLow,dc,lastOut;private int serial=-1,random=7193,eventIndex;private double next;
 private double bovHold,bovDecayTime=.12,wrxCut=1,wrxPhase,wrxLow,wrxBass,blip,blipAir,popTime=-1;private int popSerial=-1;
 public EngineSynth(int rate){this.rate=rate;}
 private double noise(){random=random*1664525+1013904223;return (random>>>1)/1073741824.0-1;}
 public void render(EngineModel.Frame f,float[] out){
  if(profile!=f.profile){profile=f.profile;phase=wrxPhase=0;eventIndex=0;next=0;env=low=low2=body=bodyVel=bov=bovHold=wrxLow=wrxBass=blip=blipAir=0;wrxCut=1;serial=f.bovSerial;popSerial=f.backfireSerial;popTime=-1;bovDecayTime=profile.bovDecaySeconds;}
  if(serial!=f.bovSerial){if(serial>=0&&(!profile.id.equals("subaru")||f.bovRpm>=4000)){bov=profile.bov;if(profile.id.equals("subaru")){double high=Math.max(0,Math.min(1,(f.bovRpm-4000)/3000));bovDecayTime=.085+.145*high;bovHold=.015+.020*high;}else{bovDecayTime=profile.bovDecaySeconds;bovHold=profile.bovHoldSeconds;}}serial=f.bovSerial;}
  double rateSmooth=1-Math.exp(-1.0/(rate*.025)),masterSmooth=1-Math.exp(-1.0/(rate*.06));double exhaustDecay=Math.exp(-1.0/(rate*(.0014+.002*profile.rumble))),bovDecay=Math.exp(-1.0/(rate*bovDecayTime));double resonant=75+profile.cylinders*5+profile.brightness*50,omega=2*Math.PI*resonant/rate,k=.1;
  boolean wrx=profile.id.equals("subaru");double cutSmooth=1-Math.exp(-1.0/(rate*.004));
  if(wrx&&popSerial!=f.backfireSerial){if(popSerial>=0)popTime=0;popSerial=f.backfireSerial;}
  for(int i=0;i<out.length;i++){
   rpm+=(f.audioRpm-rpm)*rateSmooth;load+=(f.load-load)*rateSmooth;boost+=(f.boost-boost)*rateSmooth;master+=(f.master-master)*masterSmooth;
   phase+=Math.max(90,rpm)/120/rate;
   if(phase>=next){double uneven=profile.rumble;double strength=(.23+.77*load)*(1+profile.rough*noise()*.45);if(f.cut)strength*=.07;env+=strength*(eventIndex%4==0?1+uneven*.35:1-uneven*.12);
    eventIndex++;int idx=eventIndex%profile.cylinders;double cycle=eventIndex/profile.cylinders;
    double delay=(profile.cylinders==4&&idx%2==1?Math.min(.2,profile.rumble*.012*Math.max(90,rpm)/120):0);
    next=cycle+idx/(double)profile.cylinders+delay;
   }
   if(phase>100){phase-=100;next-=100;eventIndex-=100*profile.cylinders;}
   env*=exhaustDecay;
   double n=noise(),pulse=env*(1+profile.rough*.8*n);
   double cutoff=480+profile.brightness*2200+Math.min(1,rpm/profile.redline)*2400+load*1400;
   if((i&127)==0)k=1-Math.exp(-2*Math.PI*cutoff/rate);low+=(pulse-low)*k;low2+=(low-low2)*k;
   bodyVel+=(pulse-body)*omega*omega;bodyVel*=.991;body+=bodyVel;
   air+=(n-air)*.18;whistle+=2*Math.PI*(2300+boost*4800)/rate;if(whistle>2*Math.PI)whistle-=2*Math.PI;
   double x=(low2*.85+body*(.6+profile.rumble*1.7))*(.32+.4*load)+air*load*.025+Math.sin(whistle)*boost*boost*profile.turbo*.023;
   // The WRX's measured within-cycle timing replaces the artificial pitched drum layer.
   // Phase remains continuous through RPM changes; short gain ramps soften ignition cuts.
   if(wrx){
    // Preserve the recording-derived cycles, with a 22% slower cadence and darker body.
    // Dedicated wrapped phase keeps pitch continuous through RPM sweeps and producer buffers.
    wrxPhase+=Math.max(90,rpm)/60*.78/rate;if(wrxPhase>=1)wrxPhase-=Math.floor(wrxPhase);
    double recorded=wrxSample(wrxPhase,rpm),dark=1-Math.exp(-2*Math.PI*(620+rpm*.10+load*280)/rate);
    wrxLow+=(recorded-wrxLow)*dark;wrxBass+=(recorded-wrxBass)*(1-Math.exp(-2*Math.PI*180/rate));
    wrxCut+=((f.cut?.12:1)-wrxCut)*cutSmooth;blip+=(f.revMatch-blip)*rateSmooth;
    double tone=recorded*.20+wrxLow*.72+wrxBass*.95;
    // Make the requested boxer "burble" explicit: a strong rounded pulse, a smaller
    // uneven companion and a real gap between groups. Shape the original recording,
    // rather than adding a separate pitched drum. Keep the slower 0.78 cadence.
    double primary=.5+.5*Math.cos(wrxPhase*2*Math.PI),secondary=.5+.5*Math.cos((wrxPhase-.23)*2*Math.PI);
    primary*=primary;primary*=primary;secondary*=secondary;secondary*=secondary;secondary*=secondary;
    double group=.22+1.9*primary+.35*secondary,presence=Math.max(0,Math.min(1,(3600-rpm)/2400));
    double burble=(recorded*.35+wrxLow*.85+wrxBass*.85)*group;
    tone+=(burble-tone)*presence;
    x=tone*(.72+.9*load+blip*1.2)*wrxCut+air*load*.004+Math.sin(whistle)*boost*boost*profile.turbo*.015;
    // One audible rev-match breath, synchronized to the 520ms downshift envelope.
    blipAir+=(n-blipAir)*(1-Math.exp(-2*Math.PI*900/rate));x+=blip*(blipAir*.55+Math.sin(wrxPhase*2*Math.PI)*.12);
    // Only the model can schedule a high-RPM post-blip backfire. No random coasting pops.
    if(popTime>=0){double e=Math.exp(-popTime/.035);if(popTime>.075)e+=.55*Math.exp(-(popTime-.075)/.025);x+=(n*.65+Math.sin(popTime*2*Math.PI*92)*.55)*e*.8;popTime+=1.0/rate;if(popTime>.20)popTime=-1;}
   }
   // A decaying filtered hiss with a falling tonal edge marks pressure release.
   bovLow+=(n-bovLow)*.32;x+=(n-bovLow)*bov*.20+Math.sin(whistle*.6)*bov*.022;if(bovHold>0)bovHold-=1.0/rate;else bov*=bovDecay;
   if((f.cut||load<.06)&&rpm>profile.idle*2&&noise()>.99965)env+=.55;
   x=Math.tanh(x*2.2);double hp=x-lastOut+.996*dc;lastOut=x;dc=hp;out[i]=(float)(hp*master*.72);
  }
 }
 private static double wrxSample(double cycle,double rpm){
  int lo=0;while(lo<WrxCycles.RPM.length-2&&rpm>WrxCycles.RPM[lo+1])lo++;
  double blend=Math.max(0,Math.min(1,(rpm-WrxCycles.RPM[lo])/(WrxCycles.RPM[lo+1]-WrxCycles.RPM[lo])));
  double position=(cycle-Math.floor(cycle))*WrxCycles.SIZE;int a=(int)position,b=(a+1)&(WrxCycles.SIZE-1);double frac=position-a;
  float[] first=WrxCycles.TABLES[lo],second=WrxCycles.TABLES[lo+1];double x=first[a]+(first[b]-first[a])*frac,y=second[a]+(second[b]-second[a])*frac;
  return x+(y-x)*blend;
 }
 public static double dbToLinear(double db){return Math.pow(10,db/20);}
 public static short pcm(double sample,double gain){return (short)Math.round(32767*Math.tanh(sample*gain));}
}

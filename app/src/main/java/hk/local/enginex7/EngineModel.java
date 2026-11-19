package hk.local.enginex7;

/** Time-based pure model; called by the service, shared immutable frames feed UI and DSP. */
public final class EngineModel {
 public static double clamp(double x,double lo,double hi){return Math.max(lo,Math.min(hi,x));}
 public static final class Frame {
  public final Profile profile; public final double rpm,audioRpm,speed,accel,load,boost,master,revMatch,bovRpm; public final int gear,bovSerial,shiftSerial,shiftDirection,backfireSerial; public final boolean cut,running,igniting,fresh; public final String phase;
  Frame(Profile p,double rpm,double audio,double speed,double accel,double load,double boost,double master,int gear,int bov,int shift,boolean cut,boolean run,boolean ign,boolean fresh,String phase){this(p,rpm,audio,speed,accel,load,boost,master,gear,bov,shift,cut,run,ign,fresh,phase,0,0,0);}
  Frame(Profile p,double rpm,double audio,double speed,double accel,double load,double boost,double master,int gear,int bov,int shift,boolean cut,boolean run,boolean ign,boolean fresh,String phase,int direction,double revMatch,int backfire){this(p,rpm,audio,speed,accel,load,boost,master,gear,bov,shift,cut,run,ign,fresh,phase,direction,revMatch,backfire,rpm);}
  Frame(Profile p,double rpm,double audio,double speed,double accel,double load,double boost,double master,int gear,int bov,int shift,boolean cut,boolean run,boolean ign,boolean fresh,String phase,int direction,double revMatch,int backfire,double bovRpm){profile=p;this.rpm=rpm;audioRpm=audio;this.speed=speed;this.accel=accel;this.load=load;this.boost=boost;this.master=master;this.gear=gear;bovSerial=bov;shiftSerial=shift;this.cut=cut;running=run;igniting=ign;this.fresh=fresh;this.phase=phase;shiftDirection=direction;this.revMatch=revMatch;backfireSerial=backfire;this.bovRpm=bovRpm;}
 }
 public volatile Profile profile=Profile.ALL[0]; public volatile boolean running,simulation=true,auto; public volatile double pedal,brake,intensity=.5,compensation=.12,simulatedPedal;private double autoTime;private boolean autoWas;
 private double time,started,speed,accel,rpm,load,boost,previousLoad,shiftUntil,lastShift=-9,bovAt=-9,fromRpm,toRpm,shiftDuration,revMatch,candidateTime,liftUntil,previousWanted,peakWanted,troughWanted; private int gear,nextGear,bovSerial,shiftSerial,backfireSerial,candidateGear=-1; private boolean shiftUp,downshiftPop,liftBlocked,gearInitialized;
 public volatile Frame frame;
 private VehicleSnapshot vehicle;private long vehicleNow;
 private double releaseRpm,releaseTime,bovRpm;private boolean releasePending;
 public EngineModel(){publish(0,0,0,false,false,false,"off",true);}
 public synchronized void select(Profile p){profile=p;gear=0;rpm=p.idle;boost=0;releasePending=false;resetShift();}
 public synchronized void start(){if(running)return;running=true;started=time;rpm=profile.idle;gear=0;speed=0;boost=0;previousLoad=previousWanted=0;lastShift=time-2;releasePending=false;resetShift();}
 public synchronized void stop(){running=false;auto=false;pedal=brake=0;boost=0;speed=accel=0;rpm=load=previousLoad=previousWanted=simulatedPedal=autoTime=0;gear=0;autoWas=releasePending=false;resetShift();}
 public synchronized void setMode(boolean sim){simulation=sim;speed=accel=0;pedal=brake=0;auto=false;gear=0;releasePending=false;resetShift();boost=0;rpm=profile.idle;}
 private void resetShift(){shiftUntil=revMatch=candidateTime=liftUntil=peakWanted=troughWanted=0;candidateGear=-1;downshiftPop=liftBlocked=gearInitialized=false;}
 public synchronized void step(double dt,double gpsSpeed,double gpsAcceleration,boolean fresh){
  dt=clamp(dt,0,.1);time+=dt;if(!running){publish(0,0,0,false,false,false,"off",fresh);return;}
  boolean ign=time-started<2.3; double command=clamp(pedal,0,1),braking=brake;
  if(simulation){if(auto){if(!autoWas)autoTime=0;else autoTime+=dt;double t=autoTime%35;command=t<2?0:t<9?.75:t<13?0:t<20?.22:t<23.5?1:0;braking=t>=27&&t<32?.9:0;}autoWas=auto;simulatedPedal=command;
   accel=command*Math.min(5,140/Math.max(speed,1))-.12-.00019*speed*speed-clamp(braking,0,1)*7;
   if(command<.03&&speed>1)accel-=.65;if(ign){accel=0;speed=0;}else{speed=Math.max(0,speed+accel*dt);if(speed==0)accel=0;}
  }else{speed=fresh?Math.max(0,gpsSpeed):0;accel+=( (fresh?clamp(gpsAcceleration,-9,9):0)-accel)*(1-Math.exp(-dt/.3));}
  double predicted=Math.max(0,speed+accel*compensation);
  double wanted=simulation?command:vehicle!=null?vehicle.pedal(vehicleNow):clamp((accel+.12+.00019*speed*speed)/(4.8-2.8*intensity),0,1);
  boolean brakingNow=vehicle!=null?vehicle.braking(vehicleNow):simulation?braking>.05:accel<-.7;
  if(brakingNow)wanted=0;
  if(!simulation&&!fresh&&vehicle==null)wanted=0;
  load+=(wanted-load)*(1-Math.exp(-dt/(wanted>load?.09:.18)));
  // Capture a real pedal lift before RPM falls, including light-pedal release at road speed.
  if((previousWanted>.08&&wanted<=.08)||previousWanted-wanted>.12){releaseRpm=rpm;releaseTime=time;releasePending=true;}
  if(time-releaseTime>.8)releasePending=false;
  if(wanted<previousWanted-.08||wanted<.06)liftUntil=time+.5;previousWanted=wanted;
  if(liftBlocked){troughWanted=Math.min(troughWanted,wanted);if(wanted>troughWanted+.12){liftBlocked=false;peakWanted=wanted;}}
  else{peakWanted=Math.max(peakWanted,wanted);if(wanted<peakWanted-.12){liftBlocked=true;troughWanted=wanted;}}
  boolean inputReady=vehicle==null?simulation||fresh:vehicle.throttleFresh(vehicleNow)&&vehicle.speedFresh(vehicleNow);
  revMatch=0;boolean cut=false;double target;
  if(!inputReady&&vehicle!=null)resetShift();
  if(speed<.8){gear=0;resetShift();}
  // Attaching at road speed must not sweep through first gear at the rev limiter.
  if(inputReady&&speed>2&&!gearInitialized){gear=0;while(gear<profile.ratios.length-1&&wheelRpm(speed,gear)>profile.redline*.68)gear++;gearInitialized=true;}
  if(shiftUntil>0&&shiftUntil<=time){gear=nextGear;shiftUntil=0;if(downshiftPop&&inputReady)backfireSerial++;downshiftPop=false;}
  if(shiftUntil<=0){
   double wheel=wheelRpm(predicted,gear);
   // Raised shift points, wide hysteresis, one-step changes and stable demand stop hunting.
   double up=profile.redline*(.62+.30*Math.pow(load,.6));
   double down=profile.redline*(brakingNow?.52:.30);
   int candidate=-1;
   if(!ign&&inputReady&&predicted>2&&time-lastShift>1.2){
    if(gear<profile.ratios.length-1&&wanted>.10&&load>.10&&time>liftUntil&&!liftBlocked&&!brakingNow&&accel>-.5&&wheel>up)candidate=gear+1;
    else if(gear>0&&wheelRpm(predicted,gear-1)<profile.redline*.88&&
     (wheel<down||(wanted>.85&&wheel<profile.redline*.44)))candidate=gear-1;
   }
   if(candidate!=candidateGear){candidateGear=candidate;candidateTime=0;}else if(candidate>=0)candidateTime+=dt;
   if(candidate>=0&&candidateTime>=.20){shift(candidate,predicted);candidateGear=-1;candidateTime=0;}
  }
  if(shiftUntil>time){
   double progress=clamp(1-(shiftUntil-time)/shiftDuration,0,1),smooth=progress*progress*(3-2*progress);
   toRpm=Math.max(profile.idle,wheelRpm(predicted,nextGear));target=fromRpm+(toRpm-fromRpm)*smooth;
   cut=shiftUp&&progress<.70;
   if(!shiftUp){revMatch=Math.sin(Math.PI*progress);target+=Math.min(280,toRpm*.05)*revMatch;}
  }else{target=wheelRpm(predicted,gear);if(gear==0&&speed<6)target=Math.max(target,profile.idle+load*(vehicle==null?1800:(profile.redline-profile.idle)*.88)*(1-speed/6));target=Math.max(profile.idle,target);}
  if(vehicle!=null&&!vehicle.throttleFresh(vehicleNow)){target=profile.idle;revMatch=0;}
  target=clamp(target,profile.idle,profile.redline+150);if(target>profile.redline)cut=((int)(time*14)&1)==0;
  rpm+=(target-rpm)*(1-Math.exp(-dt/.065));
  double boostTarget=profile.turbo*load*clamp((rpm-1800)/2200,0,1);boost+=(boostTarget-boost)*(1-Math.exp(-dt/(boostTarget>boost?.55:.2)));
  if(profile.id.equals("subaru")){
   if(!inputReady)releasePending=false;
   if(!ign&&releasePending){if(releaseRpm>=4000&&time-bovAt>1)triggerBov(releaseRpm);releasePending=false;}
  }else if(!ign&&boost>.18&&previousLoad>.3&&load<=.3&&time-bovAt>1)triggerBov(rpm);
  previousLoad=load;
  double audio=rpm,visual=rpm,master=1;String phase=shiftUntil>time?(shiftUp?"shift-up":"rev-match"):"run";
  if(ign){double t=time-started;boost=0;if(t<.85){phase="sweep";double x=t/.85;visual=(profile.redline+100)*Math.sin(Math.PI*x);audio=120;master=0;}
   else if(t<1.4){phase="crank";audio=visual=220+65*Math.sin(t*40);master=.85;}
   else{phase="catch";double x=t-1.4;audio=visual=profile.idle+profile.idle*1.5*(x<.15?x/.15:Math.exp(-(x-.15)/.19));}
  }
  publish(visual,audio,master,cut,true,ign,phase,fresh);
 }
 /** Vehicle speed is measured, not simulated; a stationary pedal still changes virtual RPM. */
 public synchronized void stepVehicle(double dt,VehicleSnapshot input,long now){
  vehicle=input;vehicleNow=now;double v=input.speedFresh(now)?input.speed:0;
  double a=dt>0?(v-frame.speed)/dt:0;
  try{step(dt,v,a,input.speedFresh(now));}finally{vehicle=null;}
 }
 private double wheelRpm(double v,int g){return v/2.07*60*profile.ratios[g]*profile.finalDrive;}
 private void triggerBov(double atRpm){bovSerial++;bovAt=time;bovRpm=atRpm;}
 private void shift(int next,double v){lastShift=time;nextGear=next;shiftUp=next>gear;fromRpm=rpm;toRpm=Math.max(profile.idle,wheelRpm(v,next));shiftDuration=shiftUp?.30:.52;shiftUntil=time+shiftDuration;shiftSerial++;downshiftPop=!shiftUp&&profile.id.equals("subaru")&&fromRpm>=profile.redline*.45&&toRpm>=profile.redline*.60;if(shiftUp&&boost>.15&&profile.turbo>0&&time-bovAt>.8&&(!profile.id.equals("subaru")||rpm>=4000))triggerBov(rpm);}
 private void publish(double visual,double audio,double master,boolean cut,boolean run,boolean ign,String phase,boolean fresh){frame=new Frame(profile,visual,audio,speed,accel,load,boost,master,run&&(speed>.8||gear>0)?gear+1:0,bovSerial,shiftSerial,cut,run,ign,fresh,phase,run&&shiftUntil>time?(shiftUp?1:-1):0,revMatch,backfireSerial,bovRpm);}
}

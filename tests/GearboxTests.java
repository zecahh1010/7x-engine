package hk.local.enginex7;
import java.io.*;import java.util.*;
/** End-to-end virtual driving traces, using fresh read-only vehicle snapshots. */
public final class GearboxTests {
 static int checks;static long now=10000;static PrintWriter trace;static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 static EngineModel.Frame step(EngineModel m,double pedal,double speed,boolean brake){now+=20;m.stepVehicle(.02,new VehicleSnapshot(pedal,now,brake?14.84375:0,now,speed,now,brake?1:0,now),now);EngineModel.Frame f=m.frame;if(trace!=null)trace.printf(Locale.US,"%.2f,%.1f,%.3f,%d,%.1f,%d,%d,%.3f,%d%n",now/1000.,pedal,speed*3.6,f.gear,f.rpm,f.shiftSerial,f.shiftDirection,f.revMatch,f.backfireSerial);return f;}
 static EngineModel ready(){EngineModel m=new EngineModel();m.setMode(false);m.start();for(int i=0;i<150;i++)step(m,0,0,false);return m;}
 static void accelerate(EngineModel m){int previous=0;double lastShift=-999;for(int i=0;i<1600;i++){double v=Math.min(50,i*.032);EngineModel.Frame f=step(m,80,v,false);if(f.shiftSerial!=previous){check(f.shiftDirection==1,"acceleration only shifts up");check(f.rpm>m.profile.redline*.60,"upshift starts at raised RPM");check(now/1000.-lastShift>=1.2,"shift dwell avoids hunting");lastShift=now/1000.;previous=f.shiftSerial;}}check(m.frame.gear>=4,"normal acceleration advances through gears");}
 public static void main(String[] args)throws Exception{
  if(args.length>0){trace=new PrintWriter(args[0],"UTF-8");trace.println("seconds,pedalPercent,speedKmh,gear,rpm,shiftSerial,direction,revMatch,backfireSerial");}
  try{
   EngineModel m=ready();accelerate(m);int highGear=m.frame.gear,serial=m.frame.shiftSerial;
   boolean coastUp=false;for(int i=0;i<200;i++){EngineModel.Frame f=step(m,0,50,false);coastUp|=f.gear>highGear;}check(!coastUp,"lift at constant speed never upshifts");check(m.frame.shiftSerial==serial,"lift does not schedule phantom shifts");
   int pops=m.frame.backfireSerial,down=0;double maxJump=0,blip=0;EngineModel.Frame previous=m.frame;
   for(int i=0;i<1100;i++){EngineModel.Frame f=step(m,0,Math.max(0,50-i*.05),true);maxJump=Math.max(maxJump,Math.abs(f.rpm-previous.rpm));blip=Math.max(blip,f.revMatch);if(f.shiftSerial!=previous.shiftSerial){check(f.shiftDirection==-1,"braking never initiates upshift");down++;}previous=f;}
   check(down>=2,"braking performs sequential downshifts");check(blip>.95,"downshift has one full rev-match envelope");check(maxJump<500,"downshift RPM progression has no abrupt jump");check(m.frame.backfireSerial>pops,"high RPM downshift schedules post-blip backfire");check(m.frame.gear==0&&m.frame.rpm<m.profile.idle+5,"stopping returns to idle");
   EngineModel slow=ready();accelerate(slow);pops=slow.frame.backfireSerial;down=0;previous=slow.frame;
   for(int i=0;i<2600;i++){EngineModel.Frame f=step(slow,0,Math.max(0,50-i*.02),false);if(f.shiftSerial!=previous.shiftSerial){check(f.shiftDirection==-1,"slow coast never upshifts");down++;}previous=f;}
   check(down>=2,"low RPM coast still downshifts");check(slow.frame.backfireSerial==pops,"low RPM downshifts have no backfire");
   EngineModel hold=ready();accelerate(hold);serial=hold.frame.shiftSerial;int gear=hold.frame.gear;
   for(int i=0;i<400;i++)step(hold,45,50+(i%2==0?.05:-.05),false);
   check(hold.frame.shiftSerial==serial&&hold.frame.gear==gear,"small alternating speed samples do not cause gear hunting");
   EngineModel rolling=ready();for(int i=0;i<200;i++)step(rolling,25,100/3.6,false);check(rolling.frame.gear>=3&&rolling.frame.rpm<rolling.profile.redline*.8,"joining at road speed selects a coherent initial gear");check(rolling.frame.shiftSerial==0,"rolling attach does not race through a string of upshifts");
   check(Profile.ALL.length==7,"all seven engine presets retained");Set<String> ids=new HashSet<>();for(Profile p:Profile.ALL)ids.add(p.id);check(ids.size()==7,"preset keys stay distinct");
   System.out.println("{\"result\":\"PASS\",\"assertions\":"+checks+",\"maxDownshiftRpmStep\":"+maxJump+",\"physicalCarVerified\":false}");
  }finally{if(trace!=null)trace.close();}
 }
}

package hk.local.enginex7;
public final class VehicleInputTests {
 public interface SensorApi {float getSensorLatestValue(int id);}
 public interface FunctionApi {int getFunctionValue(int id);}
 private static final class HiddenSensor implements SensorApi {public float getSensorLatestValue(int id){if(id==0x00101400)return 61;if(id==0x00101300)return 14.84375f;if(id==0x00100100)return 8.333333f;return Float.NaN;}}
 private static final class HiddenFunction implements FunctionApi {public int getFunctionValue(int id){return id==0x20317A00?1:255;}}
 static int checks;static void check(boolean b,String text){if(!b)throw new AssertionError(text);checks++;}
 static VehicleSnapshot reading(double throttle,double brake,double speed,double sw,long at){return new VehicleSnapshot(throttle,at,brake,at,speed,at,sw,at);}
 static void drive(EngineModel m,VehicleSnapshot v,int frames,long now){for(int i=0;i<frames;i++)m.stepVehicle(.01,v,now);}
 public static void main(String[] args)throws Exception{
  EcarxReader reader=new EcarxReader();reader.sensor=new HiddenSensor();reader.function=new HiddenFunction();
  check(((Number)reader.read("SENSOR_VALUE",0x00101400,0)).doubleValue()==61,"public interface read on nonpublic ECARX implementation");
  check(((Number)reader.read("SENSOR_VALUE",0x00101300,0)).doubleValue()==14.84375,"brake sensor getter and ID");
  check(Math.abs(((Number)reader.read("SENSOR_VALUE",0x00100100,0)).doubleValue()-8.333333)<.00001,"speed sensor getter and ID");
  check(((Number)reader.read("FUNCTION",0x20317A00,0)).intValue()==1,"brake switch uses function getter, not sensor getter");
  check(VehicleSnapshot.numeric(0f,true)==0,"zero throttle is valid");check(VehicleSnapshot.numeric(100f,true)==100,"observed full throttle is 100 percent");check(VehicleSnapshot.numeric(14.84375f,true)==14.84375,"brake raw float preserved");
  for(Object invalid:new Object[]{null,"100",Float.NaN,Float.POSITIVE_INFINITY,-1,-65535,255,254,253,1e-20,101})check(Double.isNaN(VehicleSnapshot.numeric(invalid,true)),"reject invalid pedal "+invalid);
  check(!new VehicleSnapshot().throttleFresh(1000),"absent pedal is not zero reading");check(!reading(80,0,10,0,1000).throttleFresh(2600),"stale pedal expires");
  EngineModel m=new EngineModel();m.setMode(false);m.start();drive(m,reading(0,0,0,0,1000),300,1001);check(m.frame.master>0&&m.frame.rpm>=m.profile.idle,"live idle without GPS");
  drive(m,reading(61,0,0,0,1000),100,1001);double part=m.frame.rpm;check(part>2500&&m.frame.load>.60,"stationary observed 61 percent revs");
  drive(m,reading(100,0,0,0,1000),100,1001);check(m.frame.rpm>part+1000&&m.frame.speed==0,"stationary full throttle changes RPM without faking car movement");
  check(m.frame.boost>.2,"live pedal builds virtual boost");int serial=m.frame.bovSerial;drive(m,reading(0,0,0,0,1000),100,1001);check(m.frame.rpm<m.profile.idle+100&&m.frame.bovSerial>serial,"real throttle release idles and triggers BOV");
  drive(m,reading(100,0,8.333333,0,1000),100,1001);check(Math.abs(m.frame.speed*3.6-30)<.001,"ECARX speed m/s converted once for display");
  drive(m,reading(100,14.84375,8.333333,1,1000),100,1001);check(m.frame.load<.01,"brake depth/switch overrides simultaneous throttle");
  VehicleSnapshot switchOnly=new VehicleSnapshot(100,1000,Double.NaN,0,0,1000,1,1000);drive(m,switchOnly,100,1001);check(m.frame.load<.01,"brake switch works if brake depth unavailable");
  check(!reading(61,14.8,0,0,1000).braking(1001),"fresh released brake switch takes priority over depth pressure");
  drive(m,reading(100,0,10,0,1000),100,1001);drive(m,reading(100,0,10,0,1000),150,3000);check(m.frame.load<.01&&m.frame.rpm<m.profile.idle+1,"stale input cannot hold acceleration");
  VehicleSnapshot partial=new VehicleSnapshot(61,3000,0,1000,15,3000,0,1000);check(partial.throttleFresh(3100)&&partial.speedFresh(3100)&&!partial.brakeFresh(3100),"ages independent per signal");
  System.out.println("{\"result\":\"PASS\",\"assertions\":"+checks+",\"input\":\"ECARX fixtures; no physical car claimed\"}");
 }
}

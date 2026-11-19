package hk.local.enginex7;
import android.content.Context;
import android.os.*;
import java.lang.reflect.Method;

/** Read-only ECARX polling, using the same public-interface reflection as Explorer. */
public final class VehicleInput {
 public static final int SPEED=0x00100100,THROTTLE=0x00101400,BRAKE=0x00101300,BRAKE_SWITCH=0x20317A00;
 public volatile VehicleSnapshot snapshot=new VehicleSnapshot();
 public volatile String status="connecting",error="";
 public volatile long polls;
 private final Context context;private HandlerThread thread;private Handler handler;private EcarxReader reader;private Method sensor,function;private volatile boolean stopped=true;
 public VehicleInput(Context context){this.context=context.getApplicationContext();}
 public void start(){if(!stopped)return;stopped=false;thread=new HandlerThread("7X-ECARX-readonly");thread.start();handler=new Handler(thread.getLooper());handler.post(poll);}
 public void stop(){stopped=true;if(handler!=null)handler.removeCallbacksAndMessages(null);if(thread!=null)thread.quitSafely();snapshot=new VehicleSnapshot();status="stopped";}
 private Object read(boolean scalar,int id){try{Method m=scalar?sensor:function;Object target=scalar?reader.sensor:reader.function;return m==null?null:m.invoke(target,id);}catch(Exception e){error=EcarxReader.describe(e);return null;}}
 private void publish(int id,Object value){if(stopped)return;double v=VehicleSnapshot.numeric(value,id!=SPEED);if(!Double.isFinite(v)||(id==BRAKE_SWITCH&&v!=0&&v!=1))return;long at=SystemClock.elapsedRealtime();VehicleSnapshot o=snapshot;
  snapshot=new VehicleSnapshot(id==THROTTLE?v:o.throttle,id==THROTTLE?at:o.throttleAt,id==BRAKE?v:o.brake,id==BRAKE?at:o.brakeAt,id==SPEED?Math.abs(v):o.speed,id==SPEED?at:o.speedAt,id==BRAKE_SWITCH?v:o.brakeSwitch,id==BRAKE_SWITCH?at:o.switchAt);
 }
 private final Runnable poll=new Runnable(){public void run(){if(stopped)return;long delay=100;
  try{
   if(reader==null){EcarxReader r=new EcarxReader();r.connect(context);reader=r;sensor=r.sensor==null?null:r.getter("SENSOR_VALUE");try{function=r.function==null?null:r.getter("FUNCTION");}catch(Exception ignored){}if(sensor==null)throw new IllegalStateException("ECARX sensor manager unavailable");}
   publish(THROTTLE,read(true,THROTTLE));publish(BRAKE_SWITCH,read(false,BRAKE_SWITCH));publish(BRAKE,read(true,BRAKE));publish(SPEED,read(true,SPEED));long now=SystemClock.elapsedRealtime();
   if(stopped)return;
   status=snapshot.throttleFresh(now)?"live":"waiting";polls++;if(!snapshot.throttleFresh(now)&&polls%30==0)reader=null;
  }catch(Exception|LinkageError e){status="unavailable";error=EcarxReader.describe(e);reader=null;sensor=function=null;delay=3000;}
  if(!stopped)handler.postDelayed(this,delay);
 }};
 public String description(Config cfg){long now=SystemClock.elapsedRealtime();String prefix="fixture".equals(status)?cfg.t("測試資料 · 非實車 · ","TEST DATA · not a car · "):"";if(snapshot.throttleFresh(now))return prefix+cfg.t("ECARX 油門已連接","ECARX pedal connected");return prefix+cfg.t("ECARX 油門未收到有效讀值；保持怠速","No fresh ECARX pedal data; idle only");}
}

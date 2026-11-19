package hk.local.enginex7;
import android.Manifest;import android.content.*;import android.content.pm.PackageManager;import android.hardware.*;import android.location.*;import android.os.*;

/** GPS anchored speed with optional acceleration projected after forward-axis calibration. */
public final class DrivingInput implements LocationListener,SensorEventListener {
 private final Context context;private final LocationManager locations;private final SensorManager sensors;private Location last;private double speed,accel;private long fixNanos;private int samples;private final double[] motion=new double[3],gravity=new double[3],axis=new double[3];private boolean aligned,linear;
 public volatile String status="";public volatile float accuracy;public volatile boolean listening;
 public DrivingInput(Context c){context=c;locations=(LocationManager)c.getSystemService(Context.LOCATION_SERVICE);sensors=(SensorManager)c.getSystemService(Context.SENSOR_SERVICE);}
 public synchronized void start(){stop();if(context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){status="permission";return;}try{locations.requestLocationUpdates(LocationManager.GPS_PROVIDER,300,0,this,Looper.getMainLooper());Sensor s=sensors.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);linear=s!=null;if(s==null)s=sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);if(s!=null)sensors.registerListener(this,s,SensorManager.SENSOR_DELAY_GAME);listening=true;status=locations.isProviderEnabled(LocationManager.GPS_PROVIDER)?"waiting":"disabled";}catch(Exception e){status="error: "+e.getClass().getSimpleName();}}
 public synchronized void stop(){if(locations!=null)try{locations.removeUpdates(this);}catch(Exception ignored){}if(sensors!=null)sensors.unregisterListener(this);listening=false;last=null;fixNanos=0;speed=accel=0;aligned=false;samples=0;for(int i=0;i<3;i++){motion[i]=gravity[i]=axis[i]=0;}}
 public synchronized boolean fresh(){return fixNanos>0&&(SystemClock.elapsedRealtimeNanos()-fixNanos)<3_000_000_000L;}
 public synchronized double acceleration(){return aligned?EngineModel.clamp(motion[0]*axis[0]+motion[1]*axis[1]+motion[2]*axis[2],-7,7):accel;}
 public synchronized double speed(){double age=(SystemClock.elapsedRealtimeNanos()-fixNanos)/1e9;return fresh()?Math.max(0,speed+acceleration()*Math.min(age,1)):0;}
 public synchronized String description(Config c){if(!listening)return c.t("GPS 尚未啟動","GPS not started");if(!fresh())return c.t("等待有效 GPS；保持怠速","Waiting for valid GPS; idle only");return String.format(java.util.Locale.US,"GPS ±%.0fm · %s",accuracy,aligned?c.t("加速度已對齊","motion aligned"):c.t("負載由 GPS 估算","GPS estimated load"));}
 @Override public synchronized void onLocationChanged(Location l){accuracy=l.getAccuracy();if(!l.hasAccuracy()||accuracy>50){status="weak";return;}long n=l.getElapsedRealtimeNanos();if(n<=0)n=SystemClock.elapsedRealtimeNanos();if(SystemClock.elapsedRealtimeNanos()-n>3_000_000_000L)return;double v=l.hasSpeed()?l.getSpeed():last!=null?l.distanceTo(last)/Math.max(.3,(n-last.getElapsedRealtimeNanos())/1e9):0;v=EngineModel.clamp(v,0,100);
  if(fixNanos>0){double dt=(n-fixNanos)/1e9;if(dt<.1)return;double a=(v-speed)/dt;if(Math.abs(a)>10){status="jump";return;}accel+=.6*(a-accel);if(Math.abs(accel)>.3&&v>2){for(int i=0;i<3;i++)axis[i]=axis[i]*.98+motion[i]*accel;samples++;double norm=Math.sqrt(axis[0]*axis[0]+axis[1]*axis[1]+axis[2]*axis[2]);if(samples>=6&&norm>1){for(int i=0;i<3;i++)axis[i]/=norm;aligned=true;}}}
  last=new Location(l);speed=v;fixNanos=n;status="ok";
 }
 @Override public synchronized void onSensorChanged(SensorEvent e){for(int i=0;i<3;i++){double v=e.values[i];if(!linear){gravity[i]+=.04*(v-gravity[i]);v-=gravity[i];}motion[i]+=.2*(v-motion[i]);}}
 @Override public void onAccuracyChanged(Sensor s,int a){}@Override public synchronized void onProviderEnabled(String p){status="waiting";}@Override public synchronized void onProviderDisabled(String p){fixNanos=0;status="disabled";}@Override public void onStatusChanged(String p,int s,Bundle b){}
}

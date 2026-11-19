package hk.local.enginex7;

/** Independent signal ages; missing or stale measurements are never a zero-percent reading. */
public final class VehicleSnapshot {
 public static final long FRESH_MS=1500;
 public final double throttle,brake,speed,brakeSwitch;
 public final long throttleAt,brakeAt,speedAt,switchAt;
 public VehicleSnapshot(){this(Double.NaN,0,Double.NaN,0,Double.NaN,0,Double.NaN,0);}
 public VehicleSnapshot(double t,long ta,double b,long ba,double s,long sa,double sw,long swa){throttle=t;throttleAt=ta;brake=b;brakeAt=ba;speed=s;speedAt=sa;brakeSwitch=sw;switchAt=swa;}
 public static boolean fresh(long at,long now){return at>0&&now>=at&&now-at<=FRESH_MS;}
 public boolean throttleFresh(long now){return fresh(throttleAt,now)&&Double.isFinite(throttle);}
 public boolean brakeFresh(long now){return fresh(brakeAt,now)&&Double.isFinite(brake);}
 public boolean speedFresh(long now){return fresh(speedAt,now)&&Double.isFinite(speed);}
 public boolean braking(long now){if(fresh(switchAt,now)&&(brakeSwitch==0||brakeSwitch==1))return brakeSwitch==1;return brakeFresh(now)&&brake>.2;}
 public double pedal(long now){return throttleFresh(now)?throttle/100:0;}
 public static double numeric(Object value,boolean percent){
  if(!(value instanceof Number))return Double.NaN;double v=((Number)value).doubleValue();
  if(!Double.isFinite(v)||v==255||v==254||v==253||v==-1||v==-65535||(v!=0&&Math.abs(v)<1e-6))return Double.NaN;
  if(percent&&(v<0||v>100))return Double.NaN;
  if(!percent&&Math.abs(v)>100)return Double.NaN;return v;
 }
}

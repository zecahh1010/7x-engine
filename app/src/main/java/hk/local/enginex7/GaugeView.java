package hk.local.enginex7;
import android.content.Context;import android.graphics.*;import android.view.View;
import java.util.Locale;

public final class GaugeView extends View {
 private final EngineService service;private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private double shown;private long described;
 public GaugeView(Context c,EngineService s){super(c);service=s;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);}
 private float sp(float n){android.util.DisplayMetrics m=getResources().getDisplayMetrics();float scale=Math.max(m.density,Math.min(getWidth()/430f,getHeight()/445f));return n*scale*(m.scaledDensity/m.density);}
 private void text(Canvas c,String v,float x,float y,float size,int color,boolean bold){p.setStyle(Paint.Style.FILL);p.setTextAlign(Paint.Align.CENTER);p.setColor(color);p.setTypeface(bold?Typeface.create("sans-serif-medium",Typeface.NORMAL):Typeface.create("sans-serif",Typeface.NORMAL));p.setTextSize(sp(size));c.drawText(v,x,y,p);}
 @Override protected void onDraw(Canvas c){super.onDraw(c);EngineModel.Frame f=service.model.frame;Config cfg=service.config;float w=getWidth(),h=getHeight(),x=w/2,y=h*.44f,r=Math.min(w*.41f,h*.35f);shown+=(f.rpm-shown)*.28;double max=Math.ceil((f.profile.redline+300)/1000)*1000;int accent=PanelView.accent(cfg);RectF oval=new RectF(x-r,y-r,x+r,y+r);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(r*.065f);p.setColor(PanelView.TRACK);c.drawArc(oval,135,270,false,p);p.setColor(PanelView.RED);c.drawArc(oval,(float)(135+270*f.profile.redline/max),(float)(270*(1-f.profile.redline/max)),false,p);p.setColor(shown>f.profile.redline?PanelView.RED:accent);c.drawArc(oval,135,(float)(270*shown/max),false,p);
  for(int i=0;i<=max;i+=500){double a=Math.toRadians(135+270*i/max);float rr=i%1000==0?.82f:.87f;p.setStrokeWidth(i%1000==0?2:1);p.setColor(i>=f.profile.redline?PanelView.RED:PanelView.MUTED);c.drawLine(x+(float)Math.cos(a)*r*.94f,y+(float)Math.sin(a)*r*.94f,x+(float)Math.cos(a)*r*rr,y+(float)Math.sin(a)*r*rr,p);if(i%1000==0)text(c,""+(i/1000),x+(float)Math.cos(a)*r*.68f,y+(float)Math.sin(a)*r*.68f+sp(5),16,PanelView.MUTED,true);}
  double a=Math.toRadians(135+270*shown/max);p.setColor(accent);p.setStrokeWidth(3);c.drawLine(x,y,x+(float)Math.cos(a)*r*.93f,y+(float)Math.sin(a)*r*.93f,p);p.setStyle(Paint.Style.FILL);p.setColor(PanelView.BG);c.drawCircle(x,y,r*.35f,p);text(c,f.running?(f.gear==0?"N":""+f.gear):"—",x,y+sp(20),54,PanelView.TEXT,true);
  text(c,String.format(Locale.US,"%,d",Math.round(shown)),x,y+r*.65f,30,PanelView.TEXT,true);text(c,"RPM",x,y+r*.84f,12,PanelView.MUTED,false);
  long nowMs=android.os.SystemClock.elapsedRealtime();VehicleSnapshot live=service.vehicle.snapshot;String speed=cfg.mode.equals("vehicle")?(live.speedFresh(nowMs)?String.format(Locale.US,"%.0f",live.speed*3.6):"—"):!service.model.simulation&&!f.fresh?"—":String.format(Locale.US,"%.0f",f.speed*3.6);text(c,speed+" km/h",x,h*.86f,27,PanelView.TEXT,true);
  text(c,cfg.mode.equals("vehicle")?cfg.t("ECARX 車速","ECARX vehicle speed"):cfg.mode.equals("sim")?cfg.t("模擬車速","Simulated speed"):cfg.t("GPS 車速 · 估算負載","GPS speed · estimated load"),x,h*.96f,13,PanelView.MUTED,false);
  long now=android.os.SystemClock.elapsedRealtime();if(now-described>1000){described=now;setContentDescription(cfg.t("轉速 ","RPM ")+Math.round(f.rpm)+cfg.t("，檔位 ",", gear ")+f.gear+", "+speed+" km/h");}if(isShown())postInvalidateDelayed(33);
 }
 @Override protected void onVisibilityChanged(View changed,int visibility){super.onVisibilityChanged(changed,visibility);if(visibility==VISIBLE)invalidate();}
}

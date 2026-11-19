package hk.local.enginex7;
import android.content.Context;
import android.content.SharedPreferences;

public final class Config {
 private final SharedPreferences prefs;
 public volatile boolean english; public volatile String profileId,mode,output,internalDevice,externalDevice; public volatile int internalDb,externalDb,intensity,latency,volume; public volatile int theme,sampleRate;
 public Config(Context c){prefs=c.getSharedPreferences("engine",0);english=prefs.getBoolean("english",false);profileId=Profile.get(prefs.getString("profile","subaru")).id;mode=prefs.getString("mode","vehicle");if(!prefs.getBoolean("vehicleDefault110",false)&&mode.equals("gps"))mode="vehicle";output=prefs.getString("output","internal");internalDevice=prefs.getString("internalDevice","");externalDevice=prefs.getString("externalDevice","");internalDb=prefs.getInt("internalDb",0);externalDb=prefs.getInt("externalDb",0);intensity=prefs.getInt("intensity",50);latency=prefs.getInt("latency",120);volume=prefs.getInt("volume",60);sampleRate=prefs.getInt("sampleRate",44100);if(sampleRate!=48000)sampleRate=44100;}
 public void save(){internalDb=(int)EngineModel.clamp(internalDb,-36,18);externalDb=(int)EngineModel.clamp(externalDb,-36,18);prefs.edit().putBoolean("vehicleDefault110",true).putInt("sampleRate",sampleRate).putBoolean("english",english).putString("profile",profileId).putString("mode",mode).putString("output",output).putString("internalDevice",internalDevice).putString("externalDevice",externalDevice).putInt("internalDb",internalDb).putInt("externalDb",externalDb).putInt("intensity",intensity).putInt("latency",latency).putInt("volume",volume).putInt("theme",theme).apply();}
 public String t(String zh,String en){return english?en:zh;}
}

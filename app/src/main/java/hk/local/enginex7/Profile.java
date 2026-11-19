package hk.local.enginex7;

/** Original synthesis presets. RPM/gearing describe a virtual drivetrain, not the EV. */
public final class Profile {
 public final String id,zh,en; public final int cylinders; public final double idle,redline; public final double[] ratios;
 public final double finalDrive,rumble,rough,brightness,turbo,bov,bovDecaySeconds,bovHoldSeconds;
 private Profile(String id,String zh,String en,int c,double idle,double red,double[] ratios,double fd,double rumble,double rough,double bright,double turbo,double bov){this.id=id;this.zh=zh;this.en=en;this.cylinders=c;this.idle=idle;this.redline=red;this.ratios=ratios;finalDrive=fd;this.rumble=rumble;this.rough=rough;brightness=bright;this.turbo=turbo;this.bov=bov;bovDecaySeconds=id.equals("subaru")?.12:.22;bovHoldSeconds=id.equals("subaru")?.025:0;}
 public static final Profile[] ALL={
  new Profile("subaru","Subaru WRX · GT1.0Z + BOV","Subaru WRX · GT1.0Z + BOV",4,820,7000,new double[]{3.45,2.33,1.66,1.24,1,.8},3.9,1,.36,.36,.95,1.9),
  new Profile("v8","美式 V8 · 低沉肌肉聲","American V8 · muscle",8,720,6500,new double[]{3,2.1,1.44,1,.84,.57},3.4,.72,.42,.28,0,0),
  new Profile("flat8","意式 V8 · 高轉","Italian V8 · high rev",8,950,8500,new double[]{3.1,2.2,1.63,1.29,1.03,.84,.64},4.4,.12,.12,.84,0,0),
  new Profile("i6","直六 Turbo · 順滑","Inline six · turbo",6,760,7000,new double[]{4.7,3.14,2.11,1.67,1.29,1,.84,.67},3.15,.1,.07,.53,.65,.72),
  new Profile("v10","V10 · 尖叫","V10 · scream",10,1000,8700,new double[]{3.13,2.59,1.96,1.24,1,.84,.69},4.4,.08,.11,.95,0,0),
  new Profile("v12","V12 GT · 厚滑","V12 GT · smooth",12,800,8000,new double[]{3.91,2.44,1.81,1.46,1.19,1,.84,.67},3.36,.06,.04,.65,0,0),
  new Profile("boxer","Boxer Turbo · 經典","Boxer turbo · classic",4,800,7000,new double[]{3.45,2.33,1.66,1.24,1,.8},3.9,.55,.22,.46,.55,.65)
 };
 public static Profile get(String id){for(Profile p:ALL)if(p.id.equals(id))return p;return ALL[0];}
 public String name(boolean english){return english?en:zh;}
}

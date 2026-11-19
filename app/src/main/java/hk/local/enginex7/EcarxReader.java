package hk.local.enginex7;

import java.lang.reflect.*;
import java.util.*;

/** Standalone reusable read-only helper. No proprietary SDK imports; usable in another Android app. */
public final class EcarxReader {
    public Object car,function,sensor,info;
    public final Map<String,String> connectionDiagnostics=new LinkedHashMap<>();
    public static Throwable cause(Throwable t){while((t instanceof InvocationTargetException || t instanceof UndeclaredThrowableException)&&t.getCause()!=null)t=t.getCause();return t;}
    public static String describe(Throwable t){Throwable x=cause(t);return x.getClass().getName()+": "+String.valueOf(x.getMessage());}
    private static void interfaces(Class<?> c,Set<Class<?>> out){if(c==null)return;for(Class<?> i:c.getInterfaces()){if(out.add(i))interfaces(i,out);}interfaces(c.getSuperclass(),out);}
    public static List<Class<?>> publicTypes(Object o) {
        LinkedHashSet<Class<?>> all=new LinkedHashSet<>();interfaces(o.getClass(),all);
        List<Class<?>> r=new ArrayList<>();for(Class<?> c:all)if(Modifier.isPublic(c.getModifiers()))r.add(c);
        for(Class<?> c=o.getClass();c!=null;c=c.getSuperclass())if(Modifier.isPublic(c.getModifiers()))r.add(c);
        return r;
    }
    public static List<Method> methods(Object o){LinkedHashMap<String,Method>r=new LinkedHashMap<>();for(Class<?> c:publicTypes(o))for(Method m:c.getMethods())if(Modifier.isPublic(m.getDeclaringClass().getModifiers()))r.putIfAbsent(m.getName()+Arrays.toString(m.getParameterTypes()),m);return new ArrayList<>(r.values());}
    public static Method resolve(Object o,String name,Class<?>...params)throws NoSuchMethodException {
        for(Method m:methods(o))if(m.getName().equals(name)&&Arrays.equals(m.getParameterTypes(),params))return m;
        throw new NoSuchMethodException(name+Arrays.toString(params)+" on public interfaces of "+o.getClass().getName());
    }
    public static Object call(Object o,String name)throws ReflectiveOperationException{return resolve(o,name).invoke(o);}
    public void connect(Object androidContext)throws ReflectiveOperationException {
        if(car==null) {
            Class<?> c=Class.forName("com.ecarx.xui.adaptapi.car.Car");Class<?> ctx=Class.forName("android.content.Context");
            Method create=c.getMethod("create",ctx);if(!Modifier.isStatic(create.getModifiers()))throw new NoSuchMethodException("Car.create(Context) is not static");
            car=create.invoke(null,androidContext);if(car==null)throw new IllegalStateException("Car.create(Context) returned null");
        }
        // Failure of one manager does not hide accessible alternatives.
        connectionDiagnostics.clear();
        try{function=call(car,"getICarFunction");connectionDiagnostics.put("getICarFunction",function==null?"returned null":"manager obtained");}catch(Throwable e){function=null;connectionDiagnostics.put("getICarFunction",describe(e));}
        try{sensor=call(car,"getSensorManager");connectionDiagnostics.put("getSensorManager",sensor==null?"returned null":"manager obtained");}catch(Throwable e){sensor=null;connectionDiagnostics.put("getSensorManager",describe(e));}
        try{info=call(car,"getCarInfoManager");connectionDiagnostics.put("getCarInfoManager",info==null?"returned null":"manager obtained");}catch(Throwable e){info=null;connectionDiagnostics.put("getCarInfoManager",describe(e));}
    }
    public Object target(String category){switch(category){case "FUNCTION":case "FUNCTION_ZONE":return function;case "SENSOR_EVENT":case "SENSOR_VALUE":return sensor;case "VEHICLE_INFO":return info;default:return null;}}
    public Method getter(String category)throws NoSuchMethodException {
        Object o=target(category);if(o==null)throw new NoSuchMethodException("manager unavailable for "+category);
        switch(category){case "FUNCTION":return resolve(o,"getFunctionValue",int.class);case "FUNCTION_ZONE":return resolve(o,"getFunctionValue",int.class,int.class);case "SENSOR_EVENT":return resolve(o,"getSensorEvent",int.class);case "SENSOR_VALUE":return resolve(o,"getSensorLatestValue",int.class);case "VEHICLE_INFO":return resolve(o,"getCarInfoInt",int.class);default:throw new NoSuchMethodException("Unsupported category "+category);}
    }
    public Object read(String category,int id,int zone)throws ReflectiveOperationException {Method m=getter(category);return "FUNCTION_ZONE".equals(category)?m.invoke(target(category),id,zone):m.invoke(target(category),id);}
    /** Only exact existing read-only zone query signatures; no generic method invocation. */
    public int[] supportedZones(int id)throws ReflectiveOperationException {if(function==null)return new int[0];for(String name:new String[]{"getSupportedFunctionZones","getSupportedZones"}){try{Method m=resolve(function,name,int.class);if(m.getReturnType()!=int[].class)continue;Object value=m.invoke(function,id);return value==null?new int[0]:(int[])value;}catch(NoSuchMethodException ignored){}}return new int[0];}
}

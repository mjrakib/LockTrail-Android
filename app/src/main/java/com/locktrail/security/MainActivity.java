package com.locktrail.security;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.*;
import android.provider.Settings;
import android.text.InputType;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.widget.*;

public class MainActivity extends Activity {
    private final int bg=0xff081212, panel=0xff112322, green=0xff23be8b;
    private SharedPreferences p;
    private LinearLayout root;
    private LocationManager lm;
    private static final int LOCATION_REQUEST=42;

    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable shape(int color){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(14));return d;}
    private LinearLayout.LayoutParams lp(){return new LinearLayout.LayoutParams(-1,-2);}
    private TextView txt(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
    private void gap(LinearLayout parent,android.view.View child,int top){LinearLayout.LayoutParams a=lp();a.topMargin=dp(top);parent.addView(child,a);}

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        p=getSharedPreferences("locktrail",MODE_PRIVATE);
        lm=(LocationManager)getSystemService(LOCATION_SERVICE);
        if(p.getBoolean("signed",false))dashboard();else login();
    }
    private void base(String title,String desc){
        ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);
        root.setPadding(dp(22),dp(28),dp(22),dp(28));root.setBackgroundColor(bg);sc.addView(root);setContentView(sc);
        root.addView(txt("LOCKTRAIL  /  PHONE SECURITY",13,green,true),lp());
        gap(root,txt(title,29,0xffffffff,true),22);gap(root,txt(desc,15,0xffb0c4bf,false),8);
    }
    private EditText input(String hint,boolean secret){
        EditText e=new EditText(this);e.setSingleLine(true);e.setHint(hint);e.setTextColor(-1);e.setHintTextColor(0xff91a59f);
        e.setPadding(dp(14),dp(12),dp(14),dp(12));e.setBackground(shape(panel));
        if(secret)e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);return e;
    }
    private void btn(String label,boolean primary,Runnable action){
        Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(15);b.setTypeface(null,Typeface.BOLD);
        b.setTextColor(primary?bg:-1);b.setBackground(shape(primary?green:panel));gap(root,b,12);b.setOnClickListener(v->action.run());
    }
    private void login(){
        base("Your phone. Your control.","Sign in to configure LockTrail. This prototype stores sign-in locally on this device.");
        EditText account=input("Email or phone number",false),password=input("Password (6+ characters)",true);
        gap(root,account,20);gap(root,password,12);
        btn("Continue",true,()->{
            String a=account.getText().toString().trim(),pw=password.getText().toString();
            if(a.isEmpty()||pw.length()<6){toast("Enter email/phone and a password of at least 6 characters.");return;}
            p.edit().putBoolean("signed",true).putString("account",a).apply();dashboard();
        });
        gap(root,txt("Demo login only: no cloud account is created.",13,0xffffcb80,false),20);
    }
    private void card(String title,String detail){
        LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(dp(16),dp(16),dp(16),dp(16));c.setBackground(shape(panel));
        c.addView(txt(title,19,-1,true),lp());gap(c,txt(detail,14,0xffb0c4bf,false),8);gap(root,c,16);
    }
    private boolean hasLocationPermission(){
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED ||
               checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED;
    }
    private void dashboard(){
        boolean tracking=p.getBoolean("tracking",false);
        base("Phone security","Manage your phone's screen lock and location.");
        gap(root,txt("ACCOUNT  ·  "+p.getString("account","Local account"),12,green,true),16);

        card("01  Secure Your Phone","A LockTrail app PIN cannot protect Android during shutdown or startup. Set your PHONE'S system screen lock below. Android does not let an ordinary app block the power-off menu.");
        btn("Set phone screen-lock PIN",true,()->{
            try{startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));}
            catch(Exception e){startActivity(new Intent(Settings.ACTION_SETTINGS));}
        });
        btn(p.getBoolean("pin",false)?"Change LockTrail app PIN":"Set LockTrail app PIN (optional)",false,()->setAppPin());

        card("02  Track Location",tracking
            ?"Background location logging is ON on this phone. Keep location enabled and allow the required permission."
            :"Start background location logging on this phone. This prototype stores locations on this device; it cannot yet show the phone remotely.");
        btn(tracking?"Stop location logging":"Start location logging",!tracking,()->{
            if(tracking){stopService(new Intent(this,LocationTrackingService.class));p.edit().putBoolean("tracking",false).apply();dashboard();}
            else if(!hasLocationPermission()){requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},LOCATION_REQUEST);}
            else startTracking();
        });
        btn("View last recorded location",false,this::lastLocation);
        gap(root,txt("REMOTE TRACKING IS NOT CONNECTED YET. To locate this phone from another phone/PC, LockTrail needs a secure backend and web map/dashboard.",13,0xffffcb80,false),20);
        btn("Sign out",false,()->{p.edit().putBoolean("signed",false).apply();login();});
    }
    private void startTracking(){
        try{
            Intent i=new Intent(this,LocationTrackingService.class);
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)startForegroundService(i);else startService(i);
            p.edit().putBoolean("tracking",true).apply();toast("Background location logging started.");dashboard();
        }catch(Exception e){toast("Could not start location logging: "+e.getMessage());}
    }
    private void setAppPin(){
        EditText e=input("App PIN/passphrase (6+ characters)",true);
        new AlertDialog.Builder(this).setTitle("Optional LockTrail PIN")
            .setMessage("This PIN is only for LockTrail, not your Android lock screen.")
            .setView(e).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{
                String s=e.getText().toString();if(s.length()<6){toast("Use at least 6 characters.");return;}
                p.edit().putString("pin_hash",Integer.toString(s.hashCode())).putBoolean("pin",true).apply();
                toast("LockTrail app PIN saved.");dashboard();
            }).show();
    }
    private void lastLocation(){
        if(p.contains("last_lat")&&p.contains("last_lon")){
            long time=p.getLong("last_time",0);
            String age=time==0?"unknown":android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss",time).toString();
            new AlertDialog.Builder(this).setTitle("Last recorded location")
                .setMessage("Latitude: "+p.getString("last_lat","")+"\nLongitude: "+p.getString("last_lon","")+
                    "\nAccuracy: "+p.getFloat("last_accuracy",0)+" m\nRecorded: "+age+
                    "\n\nThis location is stored on this phone only.")
                .setPositiveButton("OK",null).show();return;
        }
        if(!hasLocationPermission()){toast("Grant location permission first.");return;}
        try{
            Location best=null;
            for(String provider:lm.getProviders(true)){
                Location l=lm.getLastKnownLocation(provider);
                if(l!=null&&(best==null||l.getTime()>best.getTime()))best=l;
            }
            if(best==null){toast("No location yet. Turn on phone Location and start logging.");return;}
            p.edit().putString("last_lat",Double.toString(best.getLatitude())).putString("last_lon",Double.toString(best.getLongitude()))
                .putFloat("last_accuracy",best.getAccuracy()).putLong("last_time",best.getTime()).apply();
            lastLocation();
        }catch(SecurityException e){toast("Location permission is required.");}
    }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] grants){
        super.onRequestPermissionsResult(request,permissions,grants);
        if(request==LOCATION_REQUEST){boolean granted=false;for(int g:grants)if(g==PackageManager.PERMISSION_GRANTED)granted=true;
            if(granted)startTracking();else toast("Location permission not granted.");}
    }
    private void toast(String message){Toast.makeText(this,message,Toast.LENGTH_LONG).show();}
}

package com.locktrail.security;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

public class LocationTrackingService extends Service implements LocationListener {
    private static final String CHANNEL="locktrail_location";
    private LocationManager manager;

    @Override public void onCreate(){
        super.onCreate();
        createChannel();
        Intent open=new Intent(this,MainActivity.class);
        PendingIntent pending=PendingIntent.getActivity(this,0,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification notification=new Notification.Builder(this,CHANNEL)
            .setContentTitle("LockTrail location logging is active")
            .setContentText("Recording this phone's location locally. Remote tracking is not connected.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pending).setOngoing(true).build();
        startForeground(4102,notification);
        manager=(LocationManager)getSystemService(LOCATION_SERVICE);
        beginUpdates();
    }
    private void createChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel c=new NotificationChannel(CHANNEL,"LockTrail location",NotificationManager.IMPORTANCE_LOW);
            c.setDescription("Persistent notification while location logging is active.");
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);
        }
    }
    private void beginUpdates(){
        if(manager==null)return;
        try{
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
               checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){stopSelf();return;}
            if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000L,10f,this);
            if(manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,15000L,25f,this);
            Location last=null;
            for(String provider:manager.getProviders(true)){
                Location candidate=manager.getLastKnownLocation(provider);
                if(candidate!=null&&(last==null||candidate.getTime()>last.getTime()))last=candidate;
            }
            if(last!=null)onLocationChanged(last);
        }catch(SecurityException ignored){stopSelf();}
        catch(Exception ignored){stopSelf();}
    }
    @Override public void onLocationChanged(Location location){
        if(location==null)return;
        getSharedPreferences("locktrail",MODE_PRIVATE).edit()
            .putString("last_lat",Double.toString(location.getLatitude()))
            .putString("last_lon",Double.toString(location.getLongitude()))
            .putFloat("last_accuracy",location.getAccuracy())
            .putLong("last_time",location.getTime())
            .apply();
    }
    @Override public void onStatusChanged(String provider,int status,Bundle extras){}
    @Override public void onProviderEnabled(String provider){}
    @Override public void onProviderDisabled(String provider){}
    @Override public int onStartCommand(Intent intent,int flags,int startId){return START_STICKY;}
    @Override public void onDestroy(){
        if(manager!=null){try{manager.removeUpdates(this);}catch(Exception ignored){}}
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent){return null;}
}

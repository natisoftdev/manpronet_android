package com.servizi;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.costanti.CostantiWeb;
import com.database.myDbAdapter;
import com.internet.InviaPosizione;
import com.natisoftnavigazione.MainActivity;
import com.natisoftnavigazione.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundTicketService extends Service {

    private final BackgroundTicketServiceBinder binder = new BackgroundTicketServiceBinder();
    private final String TAG = "BackgroundTicketService";
    private final String notificationIdChannel = "channel_BackgroundTicketService";
    private NotificationManager notificationManager = null;

    private final int INTERVALLO = 1000*20; //10 sec

    private static Handler handler = null;
    private static Timer timer = null;

    private static String LabelRic1 = "";
    private static int NumRic1 = 0;
    private int idNotifica = CostantiWeb.idNotificaArray.get("trasporto_pazienti");
    private static NotificationCompat.Builder builder;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        //return START_NOT_STICKY;
        return START_STICKY ;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        startTracking();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy - inizio");

        stopTracking();

        Log.d(TAG, "notificationManager>"+notificationManager);

        if(notificationManager!=null) {
            Log.d(TAG, "cancellazione notifica");
            notificationManager.deleteNotificationChannel(notificationIdChannel);
        }
        Log.d(TAG, "onDestroy - fine");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startTracking() {

        startForeground(123456789, getNotification());

        handler = new Handler();
        timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences( BackgroundTicketService.this);
                            String indirizzo = pref.getString("indirizzo_portale", "NESSUN_INDIRIZZO");
                            String name_odbc = pref.getString("nome_odbc", "NESSUN_ODBC");
                            String Login = pref.getString("prefDBUsername", "NESSUN_LOGIN");
                            String Password = pref.getString("prefDBPassword", "NESSUN_PASSWORD");

                            String URL = indirizzo+"mobile/accesso_esterno/get_notifiche_per_app_android_json.php";

                            RequestQueue reQueue = Volley.newRequestQueue(BackgroundTicketService.this);

                            StringRequest request=new StringRequest(com.android.volley.Request.Method.POST,
                                    URL,
                                    new Response.Listener<String>() {

                                        @Override
                                        public void onResponse(String response) {

                                            Log.d(TAG, "Cosa ricevo: "+response);

                                            try {
                                                JSONObject jsonObject = new JSONObject(response);
                                                Log.d(TAG, "jsonObject: "+jsonObject.toString());

                                                getNotification2(jsonObject);

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    },

                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(
                                                VolleyError error) {
                                            Log.e(TAG, ""+error);
                                            error.printStackTrace();
                                        }
                                    })
                            {
                                @Override
                                protected Map<String, String> getParams() {
                                    Map<String, String> params = new HashMap<>();
                                    params.put("Content-Type", "application/json; charset=ansi");

                                    params.put("name_odbc", ""+name_odbc);
                                    params.put("Password", ""+Password);
                                    params.put("Login", ""+Login);

                                    return params;
                                }
                            };

                            try{ reQueue.add(request); }
                            catch(Exception e){ Log.e(TAG, ""+e); }

                        } catch (Exception e) {
                            Log.e(TAG, "Errore dentro doAsynchronousTask"+e);

                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, INTERVALLO); //execute in every 10 minutes
        Log.d(TAG, "startTracking");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void stopTracking() {

        if(timer!=null) timer.cancel();
        if(handler!=null) handler.removeCallbacksAndMessages(null);

        Log.d(TAG, "stopTracking");
        this.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("WrongConstant")
    private Notification getNotification() {

        String titolo = getResources().getString(R.string.titolo_msg_notif);

        NotificationChannel channel = new NotificationChannel(notificationIdChannel, titolo, NotificationManager.IMPORTANCE_DEFAULT);

        notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        builder = new NotificationCompat.Builder(getApplicationContext(), notificationIdChannel).setAutoCancel(true);

        builder.setSmallIcon(R.drawable.ic_launcher)
               .setContentTitle(titolo)
               .setContentText("")
               .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder.build();
    }

    private void getNotification2(JSONObject jsonObject) {

        Iterator<String> keys = jsonObject.keys();

        try {
            while(keys.hasNext()) {
                String key = keys.next();
                if (jsonObject.get(key) instanceof JSONObject) {
                    String nomeModuloDinamico = key;
                    //Log.e(TAG, "nomeModuloDinamico: "+nomeModuloDinamico);
                    String NomeNotificaSuApp = ((JSONObject) jsonObject.get(key)).getString("NomeNotificaSuApp");
                    String elementi = ((JSONObject) jsonObject.get(key)).getString("elementi");
                    JSONObject jsonObject2 = new JSONObject(elementi);
                    Iterator<String> keys2 = jsonObject2.keys();

                    while(keys2.hasNext()) {
                        String key2 = keys2.next();
                        if (jsonObject2.get(key2) instanceof JSONObject) {

                            String IdRich = key2;
                            String nome = ((JSONObject) jsonObject2.get(key2)).getString("nome");
                            String des = ((JSONObject) jsonObject2.get(key2)).getString("des");

                            NotificationCompat.Builder mBuilder2 = new NotificationCompat.Builder(BackgroundTicketService.this, nomeModuloDinamico)
                                    .setSmallIcon(R.drawable.ic_launcher)
                                    .setContentTitle(NomeNotificaSuApp+" - "+nome)
                                    .setContentText(des)
                                    .setStyle(new NotificationCompat.BigTextStyle()
                                            .bigText(des))
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                CharSequence name  = NomeNotificaSuApp+" - "+nome;
                                String description = des;

                                int importance = NotificationManager.IMPORTANCE_MAX;
                                @SuppressLint("WrongConstant")
                                NotificationChannel channel = new NotificationChannel(nomeModuloDinamico, name, importance);

                                channel.setDescription(description);
                                // Register the channel with the system; you can't change the importance
                                // or other notification behaviors after this
                                NotificationManager notificationManager = (BackgroundTicketService.this).getSystemService(NotificationManager.class);
                                notificationManager.createNotificationChannel(channel);
                            }

                            if( nomeModuloDinamico.equals("manutenzione") ){
                                idNotifica = CostantiWeb.idNotificaArray.get( nomeModuloDinamico );
                            }
                            try{ idNotifica = Integer.parseInt(IdRich); }
                            catch (NumberFormatException ex){ ex.printStackTrace(); }

                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(BackgroundTicketService.this);

                            new Timer().schedule(new TimerTask() {

                                @Override
                                public void run() {
                                    notificationManager.notify(idNotifica, mBuilder2.build());

                                }
                            }, 1000);

                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Errore dentro getNotification2: "+e);
        }

    }

    private void getNotificationManutenzione(JSONObject jsonObject) {
        Iterator<String> keys = jsonObject.keys();
        try {
            while(keys.hasNext()) {
                String key = keys.next();
                if (jsonObject.get(key) instanceof JSONObject) {
                    String nomeModuloDinamico = key;
                    String NomeNotificaSuApp = ((JSONObject) jsonObject.get(key)).getString("NomeNotificaSuApp");
                    String elementi = ((JSONObject) jsonObject.get(key)).getString("elementi");
                    JSONObject jsonObject2 = new JSONObject(elementi);
                    Iterator<String> keys2 = jsonObject2.keys();

                    while(keys2.hasNext()) {
                        String key2 = keys2.next();
                        if (jsonObject2.get(key2) instanceof JSONObject) {

                            String IdRich = key2;
                            String nome = ((JSONObject) jsonObject2.get(key2)).getString("nome");
                            String des = ((JSONObject) jsonObject2.get(key2)).getString("des");

                            NotificationCompat.Builder mBuilder2 = new NotificationCompat.Builder(BackgroundTicketService.this, nomeModuloDinamico)
                                    .setSmallIcon(R.drawable.ic_launcher)
                                    .setContentTitle(NomeNotificaSuApp+" - "+nome)
                                    .setContentText(des)
                                    .setStyle(new NotificationCompat.BigTextStyle()
                                            .bigText(des))
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                CharSequence name  = NomeNotificaSuApp+" - "+nome;
                                String description = des;

                                int importance = NotificationManager.IMPORTANCE_MAX;
                                @SuppressLint("WrongConstant")
                                NotificationChannel channel = new NotificationChannel(nomeModuloDinamico, name, importance);

                                channel.setDescription(description);
                                // Register the channel with the system; you can't change the importance
                                // or other notification behaviors after this
                                NotificationManager notificationManager = (BackgroundTicketService.this).getSystemService(NotificationManager.class);
                                notificationManager.createNotificationChannel(channel);
                            }

                            if( nomeModuloDinamico.equals("manutenzione") ){
                                idNotifica = CostantiWeb.idNotificaArray.get( nomeModuloDinamico );
                            }
                            try{ idNotifica = Integer.parseInt(IdRich); }
                            catch (NumberFormatException ex){ ex.printStackTrace(); }

                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(BackgroundTicketService.this);

                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    notificationManager.notify(idNotifica, mBuilder2.build());

                                }
                            }, 1000);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Errore dentro getNotification2: "+e);
        }
    }

    public class BackgroundTicketServiceBinder extends Binder { public BackgroundTicketService getService() { return BackgroundTicketService.this; } }
}

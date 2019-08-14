package com.cowbell.cordova.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import de.appplant.cordova.plugin.localnotification.TriggerReceiver;
import de.appplant.cordova.plugin.notification.Manager;
import de.appplant.cordova.plugin.notification.Options;
import de.appplant.cordova.plugin.notification.Request;



public class TransitionReceiver extends BroadcastReceiver {



    private Options notificationOptions(Notification notification) throws JSONException {

        JSONObject notData = new JSONObject(notification.getDataJson());
        int id = notData.optString("$id","").hashCode();
        JSONObject dict = new JSONObject();
        JSONObject trig = new JSONObject(("{\"type\":\"calendar\"}"));
        JSONObject progBar = new JSONObject(("{\"enabled\":false}"));
        dict.put("trigger",trig);
        dict.put("progressBar",progBar);

        dict.put("title",notification.getTitle());
        dict.put("text",notification.getText());
        dict.put("smallIcon","res://mipmap-ldpi/ic_launcher.png");
        dict.put("icon","res://mipmap-ldpi/ic_launcher.png");
        dict.put("foreground",true);
        dict.put("showWhen",true);
        dict.put("launch",true);
        dict.put("led",true);
        dict.put("lockscreen",true);
        dict.put("silent",false);
        dict.put("sound",false);
        dict.put("vibrate",true);
        dict.put("wakeup",true);
        dict.put("autoClear",true);
        dict.put("defaults",0);
        dict.put("id",id);
        dict.put("number",1);
        dict.put("priority",1);
        dict.put("group","places");



        Options options = new Options(dict);

        return options;
        /*
        "groupSummary": false,
        "meta": { "plugin": "cordova-plugin-local-notification","version": "0.9-beta.2"}
        */
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.setLogger(new Logger(GeofencePlugin.TAG, context, false));
        Logger logger = Logger.getLogger();

        String error = intent.getStringExtra("error");


        logger.log(Log.DEBUG,"BroadcastReceiver awake!");
        Log.println(Log.DEBUG,"GeofencePlugin","TEST BCRec-Log");
        if (error != null) {
            //handle error
            logger.log(Log.DEBUG, error);
        } else {
            String geofencesJson = intent.getStringExtra("transitionData");
            //PostLocationTask task = new TransitionReceiver.PostLocationTask();
            //AsyncParams params = new AsyncParams(context,geofencesJson);
            //task.execute(params);
            try {

                logger.log(Log.DEBUG, "Executing PostLocationTask#doInBackground" );

                GeoNotification[] geoNotifications = Gson.get().fromJson(geofencesJson, GeoNotification[].class);

                for (int i=0; i < geoNotifications.length; i++){
                    GeoNotification geoNotification = geoNotifications[i];
                    if(geoNotification.notification != null) {
                        Options options = notificationOptions(geoNotification.notification);
                        Request request = new Request(options);
                        Manager.getInstance(context).schedule(request, TriggerReceiver.class);
                    }
                }
            } catch (Throwable e) {
                logger.log(Log.ERROR, "Exception receiving geofence: " + e);
            }
        }
    }

}

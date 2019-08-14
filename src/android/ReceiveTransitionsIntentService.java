package com.cowbell.cordova.geofence;

import android.app.IntentService;
//import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.appplant.cordova.plugin.localnotification.TriggerReceiver;
import de.appplant.cordova.plugin.notification.Manager;
import de.appplant.cordova.plugin.notification.Options;
import de.appplant.cordova.plugin.notification.Request;


public class ReceiveTransitionsIntentService extends IntentService {
    protected static final String GeofenceTransitionIntent = "com.cowbell.cordova.geofence.TRANSITION";
    protected BeepHelper beepHelper;
    //protected GeoNotificationNotifier notifier;
    protected GeoNotificationStore store;

    private Options notificationOptions(Notification notification) throws JSONException{

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
        dict.put("id",1);
        dict.put("number",1);
        dict.put("priority",1);


        Options options = new Options(dict);

        return options;
    }
    /**
     * Sets an identifier for the service
     */
    public ReceiveTransitionsIntentService() {
        super("ReceiveTransitionsIntentService");
        beepHelper = new BeepHelper();
        store = new GeoNotificationStore(this);
        Logger.setLogger(new Logger(GeofencePlugin.TAG, this, false));
    }

    /**
     * Handles incoming intents
     *
     * @param intent
     *            The Intent sent by Location Services. This Intent is provided
     *            to Location Services (inside a PendingIntent) when you call
     *            addGeofences()
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Logger logger = Logger.getLogger();
        logger.log(Log.DEBUG, "ReceiveTransitionsIntentService - onHandleIntent");
        Intent broadcastIntent = new Intent(GeofenceTransitionIntent);
        /*notifier = new GeoNotificationNotifier(
            (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE),
            this
        );*/

        // TODO: refactor this, too long
        // First check for errors
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            // Get the error code with a static method
            int errorCode = geofencingEvent.getErrorCode();
            String error = "Location Services error: " + Integer.toString(errorCode);
            // Log the error
            logger.log(Log.ERROR, error);
            broadcastIntent.putExtra("error", error);
        } else {
            // Get the type of transition (entry or exit)
            int transitionType = geofencingEvent.getGeofenceTransition();
            if ((transitionType == Geofence.GEOFENCE_TRANSITION_ENTER)
                    || (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT)) {
                logger.log(Log.DEBUG, "Geofence transition detected");
                List<Geofence> triggerList = geofencingEvent.getTriggeringGeofences();
                List<GeoNotification> geoNotifications = new ArrayList<GeoNotification>();
                for (Geofence fence : triggerList) {
                    String fenceId = fence.getRequestId();
                    GeoNotification geoNotification = store
                            .getGeoNotification(fenceId);

                    if (geoNotification != null) {
                        /*if (geoNotification.notification != null) {
                            logger.log(Log.DEBUG, "Geofence transition notifying");
                            //notifier.notify(geoNotification.notification);
                            try {
                                Options options = notificationOptions(geoNotification.notification);
                                Request request = new Request(options);
                                Manager.getInstance(getApplicationContext()).schedule(request,TriggerReceiver.class);

                            }catch(JSONException err){
                                logger.log(Log.ERROR, err.getMessage());
                            }
                        }*/
                        geoNotification.transitionType = transitionType;
                        geoNotifications.add(geoNotification);
                    }
                }

                if (geoNotifications.size() > 0) {
                    broadcastIntent.putExtra("transitionData", Gson.get().toJson(geoNotifications));
                    GeofencePlugin.onTransitionReceived(geoNotifications);

                }
            } else {
                String error = "Geofence transition error: " + transitionType;
                logger.log(Log.ERROR, error);
                broadcastIntent.putExtra("error", error);

            }
        }
        logger.log(Log.DEBUG,"Broadcasting Intent");
        sendBroadcast(broadcastIntent);
    }
}

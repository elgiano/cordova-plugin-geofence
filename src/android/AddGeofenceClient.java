package com.cowbell.cordova.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

import org.apache.cordova.LOG;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddGeofenceClient  extends AbstractGoogleServiceCommand{
    private List<Geofence> geofencesToAdd;
    private PendingIntent pendingIntent;
    private GeofencingClient client;

    public AddGeofenceClient(Context context, PendingIntent pendingIntent,
                              List<Geofence> geofencesToAdd){
        super(context);
        this.client = LocationServices.getGeofencingClient(context);
        this.geofencesToAdd = geofencesToAdd;
        this.pendingIntent = pendingIntent;
    }

    @Override
    public void ExecuteCustomCode() {
        logger.log(Log.DEBUG,"Adding new geofences...");
        if (geofencesToAdd != null && geofencesToAdd.size() > 0) try {
            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
            builder.addGeofences(geofencesToAdd);
            this.client
                    .addGeofences(builder.build(), pendingIntent)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            logger.log(Log.DEBUG, "Geofences successfully added");
                            CommandExecuted();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to add geofences
                            // ...
                            try {
                                Map<Integer, String> errorCodeMap = new HashMap<Integer, String>();
                                errorCodeMap.put(GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE, GeofencePlugin.ERROR_GEOFENCE_NOT_AVAILABLE);
                                errorCodeMap.put(GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES, GeofencePlugin.ERROR_GEOFENCE_LIMIT_EXCEEDED);

                                Integer statusCode = ((ApiException)e).getStatusCode();
                                String message = "Adding geofences failed: " + ((ApiException)e).getMessage();
                                JSONObject error = new JSONObject();

                                if (statusCode == GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE) {
                                    error.put("code", GeofencePlugin.ERROR_GEOFENCE_NOT_AVAILABLE);
                                    message = message + "GEOFENCE_NOT_AVAILABLE";
                                } else if (statusCode == GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES) {
                                    error.put("code", GeofencePlugin.ERROR_GEOFENCE_LIMIT_EXCEEDED);
                                    message = message + "GEOFENCE_LIMIT_EXCEEDED";

                                } else {
                                    error.put("code", GeofencePlugin.ERROR_UNKNOWN);
                                    message = message + "ERROR UNKNOWN";

                                }
                                error.put("message", message);


                                logger.log(Log.ERROR, message);
                                CommandExecuted(error);
                            } catch (JSONException exception) {
                                CommandExecuted(exception);
                            }
                            CommandExecuted(e);
                        }
                    });

        }catch (SecurityException exception){
            logger.log(LOG.ERROR, "SecurityException while adding geofences");
            exception.printStackTrace();
            CommandExecuted(exception);
        } catch (Exception exception) {
            logger.log(LOG.ERROR, "Exception while adding geofences");
            exception.printStackTrace();
            CommandExecuted(exception);
        }
    }
}

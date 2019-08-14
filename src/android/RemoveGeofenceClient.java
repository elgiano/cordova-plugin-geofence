package com.cowbell.cordova.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import org.apache.cordova.LOG;

import java.util.List;

public class RemoveGeofenceClient  extends AbstractGoogleServiceCommand{
    private List<String> geofencesIds;
    private PendingIntent pendingIntent;
    private GeofencingClient client;

    public RemoveGeofenceClient(Context context,  List<String> geofencesIds){
        super(context);
        this.client = LocationServices.getGeofencingClient(context);
        this.geofencesIds = geofencesIds;
    }

    @Override
    public void ExecuteCustomCode() {
      if (geofencesIds != null && geofencesIds.size() > 0) {
          logger.log(Log.DEBUG, "Removing geofences...");
          try{
            this.client
                    .removeGeofences(geofencesIds)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                          logger.log(Log.DEBUG, "Geofences successfully removed");
                          CommandExecuted();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                          String message = "Removing geofences failed - " + ((ApiException)e).getMessage();
                          logger.log(Log.ERROR, message);
                          CommandExecuted(new Error(message));
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
}

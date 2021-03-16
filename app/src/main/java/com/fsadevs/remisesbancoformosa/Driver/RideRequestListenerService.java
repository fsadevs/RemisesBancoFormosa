package com.fsadevs.remisesbancoformosa.Driver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.fsadevs.remisesbancoformosa.Driver.DriverMapActivity;
import com.fsadevs.remisesbancoformosa.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;



public class RideRequestListenerService extends Service {

    private PendingIntent resultPendingIntent;
    private String customer_ID, customerName;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("childListener", "Remises Banco Formosa", importance);
            channel.setDescription("Servicio de alerta de solicitudes de pasajeros.");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        Intent resultIntent = new Intent(this, DriverMapActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        customerRequestListener();

        return START_STICKY;
    }

    public void customerRequestListener() {
        String driver_ID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driver_ID).child("CustomerRequest").child("customerRideId");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    customer_ID = dataSnapshot.getValue().toString();
                    getCustomerInfo();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

    }


    private void createNotification(int nId, String title, String body) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this, "childListener");
                mBuilder.setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(resultPendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setDefaults(Notification.VISIBILITY_PUBLIC)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(nId, mBuilder.build());
    }

    private void getCustomerInfo() {
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customer_ID).child("name");
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    customerName = dataSnapshot.getValue().toString();

                    createNotification(173120,"Solicitud de servicio:",customerName+" esta solicitando un remis.");

                }
            } @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

}
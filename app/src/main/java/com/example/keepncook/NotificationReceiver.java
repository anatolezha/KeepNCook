package com.example.keepncook;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.example.keepncook.dummy.DummyContent.DummyItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NotificationReceiver extends BroadcastReceiver {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userId;
    private FirebaseUser user;
    private Query query;
    List<DummyItem> list = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {
            checkProductsList(context);
    }

    private void checkProductsList(final Context context) {

        auth = FirebaseAuth.getInstance();

        user = auth.getCurrentUser();
        assert user != null;

        userId = auth.getCurrentUser().getUid();
        //System.out.println("USER ID : " + userId.toString());

        db = FirebaseFirestore.getInstance();
        query = db.collection("products").whereEqualTo("id_user", userId);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                //System.out.println("OnComplete");
                handleResult(task.getResult().toObjects(DummyItem.class), context);
                //System.out.println("ListOnComplete = " + list.toString());

            }
        });
    }
    private void handleResult(List<DummyItem> result, Context context){
        list = result;
        if(productsOutOfDate() == true){



                NotificationCompat.Builder notifBuilder;


                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    notifBuilder = new NotificationCompat.Builder(context);
                } else {
                    NotificationChannel channel =
                            new NotificationChannel("com.example.keepncook", "KeepNCook", NotificationManager.IMPORTANCE_DEFAULT);
                    NotificationManager nm = context.getSystemService(NotificationManager.class);
                    nm.createNotificationChannel(channel);
                    notifBuilder = new NotificationCompat.Builder(context, channel.getId());
                }


                Intent myIntent = new Intent(context, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, myIntent, 0);

                Notification notif = notifBuilder.setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("KeepNCook")
                        .setContentIntent(pendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Un de vos produits est sur le point de périmer ! Vite, mangez-le !"))
                        .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                        .build();

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1, notif);

        }
    }


    private boolean productsOutOfDate() {
        boolean notify = false;

        Date currentDate = Calendar.getInstance().getTime();
        //System.out.println("PASSSE");
        for(int i = 0; i < list.size(); i++){
            Date productExpirationDate = list.get(i).expiration_date;

            //System.out.println("CURRENT PRODUCT = " + list.get(i).name);
            //System.out.println(productExpirationDate.compareTo(currentDate));
           if(productExpirationDate.compareTo(currentDate) <= 0){
                notify = true;
                return notify;
            }
        }
        return notify;
    }
}

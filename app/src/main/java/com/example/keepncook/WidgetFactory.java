package com.example.keepncook;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.EventLog;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.keepncook.dummy.DummyContent.DummyItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class WidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    List<DummyItem> list = new ArrayList<>();
    Intent intent;
    Context context;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userId;
    private FirebaseUser user;
    private Query query;


    private Comparator dummyItemComparator = new Comparator<DummyItem>() {
        @Override
        public int compare(DummyItem param1, DummyItem param2) {
            return param1.expiration_date.compareTo(param2.expiration_date);
        }
    };
    private void initializeData() throws NullPointerException {
        System.out.println("Initialize Data");

        try {
            list.clear();

            auth = FirebaseAuth.getInstance();

            user = auth.getCurrentUser();
            assert user != null;

            userId = auth.getCurrentUser().getUid();
            System.out.println("USER ID : " + userId.toString());

            db = FirebaseFirestore.getInstance();
           query = db.collection("products").whereEqualTo("id_user", userId).limit(3);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    System.out.println("OnComplete");
                    System.out.println(task.getResult().toObjects(DummyItem.class).toString());
                    list = task.getResult().toObjects(DummyItem.class);
                    System.out.println("ListOnComplete = " + list.toString());
                }
            });
            System.out.println("List = " + list.toString());






        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }




    public WidgetFactory(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;
    }

    @Override
    public void onCreate() {
        System.out.println("ON CREATE");

        initializeData();
    }

    @Override
    public void onDataSetChanged() {
        System.out.println("ON DATA SET CHANGED");

        initializeData();
    }

    @Override
    public void onDestroy() {
        System.out.println("ON DESTROY");

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        System.out.println("GET VIEW AT");
        RemoteViews remoteViews =  new RemoteViews(context.getPackageName(), R.layout.fragment_product);
        remoteViews.setTextViewText(R.id.item_number, list.get(position).getContent());
        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        System.out.println("GET LOADING VIEW");
        return (new RemoteViews(context.getPackageName(), R.layout.app_widget_loading));
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}

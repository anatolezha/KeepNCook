package com.example.keepncook;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.keepncook.ProductFragment.OnListFragmentInteractionListener;
import com.example.keepncook.dummy.DummyContent.DummyItem;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.util.Collections.*;

public class MyProductRecyclerViewAdapter extends RecyclerView.Adapter<MyProductRecyclerViewAdapter.ViewHolder> implements EventListener<QuerySnapshot> {

    private final List<DummyItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    private Query query;
    private ListenerRegistration listenerRegistration;


    private Comparator dummyItemComparator = new Comparator<DummyItem>() {
        @Override
        public int compare(DummyItem param1, DummyItem param2) {
            return param1.expiration_date.compareTo(param2.expiration_date);
        }
    };

    public MyProductRecyclerViewAdapter(Query query, OnListFragmentInteractionListener listener) {
        mValues = new ArrayList<DummyItem>();
        mListener = listener;
        this.query = query;
    }

    public void startListening(){
        if(query != null && listenerRegistration == null){
            listenerRegistration = query.addSnapshotListener(this);
        }
    }

    public void stopListening(){
        if(listenerRegistration != null){
            listenerRegistration.remove();
            listenerRegistration = null;
        }
        mValues.clear();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).name);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        holder.mContentView.setText(simpleDateFormat.format(mValues.get(position).expiration_date));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
        if(e != null){
            //TODO : handle exception
            return;
        }

        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
            switch (dc.getType()){
                case ADDED:
                    mValues.add(dc.getNewIndex(), dc.getDocument().toObject(DummyItem.class));
                    notifyItemChanged(dc.getNewIndex());
                    break;
                case MODIFIED:
                    if(dc.getOldIndex() == dc.getNewIndex()){
                        mValues.set(dc.getNewIndex(), dc.getDocument().toObject(DummyItem.class));
                        notifyItemChanged(dc.getNewIndex());
                    } else {
                        mValues.remove(dc.getOldIndex()) ;
                        mValues.add(dc.getNewIndex(), dc.getDocument().toObject(DummyItem.class));
                        notifyItemMoved(dc.getOldIndex(), dc.getNewIndex());
                    }
                    break;
                case REMOVED:
                    mValues.remove(dc.getOldIndex());
                    notifyItemRemoved(dc.getOldIndex());
                    break;
            }
            Collections.sort(mValues, dummyItemComparator);


        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public DummyItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}

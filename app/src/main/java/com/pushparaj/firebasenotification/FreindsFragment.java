package com.pushparaj.firebasenotification;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class FreindsFragment extends Fragment {
    View mView;
    RecyclerView mRecyclerView;
    DatabaseReference mDatabaseRef;
    FirebaseUser current_user;
    RecyclerAdapter re;
    ArrayList<DataFields> list;
    public FreindsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView= inflater.inflate(R.layout.fragment_freinds, container, false);
        list = new ArrayList<DataFields>();
        current_user = FirebaseAuth.getInstance().getCurrentUser();
        mRecyclerView = (RecyclerView)mView.findViewById(R.id.recycleView);
        mRecyclerView.setHasFixedSize(true);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(current_user.getUid());
        mDatabaseRef.keepSynced(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);

        setUpAdapter();
        return mView;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void setUpAdapter() {

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(current_user.getUid());
        mDatabaseRef.keepSynced(true);
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ArrayList<String> listId = new ArrayList<String>();
                for(DataSnapshot ds :dataSnapshot.getChildren()){
                    listId.add(ds.getKey());
                }
                mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
                mDatabaseRef.keepSynced(true);
                mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        list.clear();
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            if(listId.contains(ds.getKey())) {
                                DataFields ob = new DataFields();
                                ob.setImage(ds.child("tumb_image").getValue().toString());
                                ob.setStatus(ds.child("status").getValue().toString());
                                ob.setFull_image(ds.child("image").getValue().toString());
                                ob.setName(ds.child("name").getValue().toString());
                                ob.setId(ds.getKey());
                                list.add(ob);
                            }
                        }
                        if(re==null) {
                            re = new RecyclerAdapter(list, getActivity());
                            mRecyclerView.setAdapter(re);
                        }else{
                            re.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}

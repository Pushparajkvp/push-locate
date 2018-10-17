package com.pushparaj.firebasenotification;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.zip.DataFormatException;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestsFragment extends Fragment {
    View mView;
    Context mContext;
    RecyclerView mRecyclerView;
    DatabaseReference mDatabaseRef;
    Button searchButton,showButton;
    EditText searchText;
    ProgressDialog mProgress;
    FirebaseUser current_user;
    RecyclerAdapter re;
    ArrayList<String> list;
    ArrayList<DataFields> NameList;
    ProgressDialog pd;
    ArrayList<DataFields> dataList;
    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onPause() {
        super.onPause();
       // Toast.makeText(getActivity(),"pause",Toast.LENGTH_LONG).show();
        list.clear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_search, container, false);
        //fields
        mContext=getActivity().getApplicationContext();
        mRecyclerView = (RecyclerView)mView.findViewById(R.id.recycleView);
       // mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setItemAnimator(null);
        searchButton = (Button)mView.findViewById(R.id.searchButt);
        searchText = (EditText)mView.findViewById(R.id.editSearch);
        showButton = (Button)mView.findViewById(R.id.showButt);
        mProgress = new ProgressDialog(getActivity());
        NameList = new ArrayList<DataFields>();
        pd=new ProgressDialog(getActivity());
        pd.setMessage("loading please wait");
        pd.setTitle("loading");
        current_user= FirebaseAuth.getInstance().getCurrentUser();
        list=new ArrayList<String>();
        dataList=new ArrayList<DataFields>();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String searchTxt = searchText.getText().toString().toLowerCase().trim();
                if(searchTxt.equals("")){
                    Toast.makeText(getActivity(),"Please Enter a Name",Toast.LENGTH_LONG).show();
                    return;
                }
                mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
                mDatabaseRef.keepSynced(true);
                mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        NameList.clear();
                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            if(ds.child("name").getValue()!=null){
                                if(ds.child("name").getValue().toString().toLowerCase().contains(searchTxt.trim())){
                                    DataFields ob = new DataFields();
                                    ob.setId(ds.getKey());
                                    ob.setImage(ds.child("tumb_image").getValue().toString());
                                    ob.setFull_image(ds.child("image").getValue().toString());
                                    ob.setName(ds.child("name").getValue().toString());
                                    ob.setStatus(ds.child("status").getValue().toString());
                                    NameList.add(ob);
                                }
                            }
                        }
                        if(NameList.size()<=0)
                            Toast.makeText(getActivity(),"No Results Found",Toast.LENGTH_LONG).show();
                        if(re==null) {
                            re = new RecyclerAdapter(NameList, getActivity());
                            mRecyclerView.setAdapter(re);
                        }else{
                            re.setData(NameList);
                            re.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });
        show();
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show();
            }
        });

        return mView;
    }


    public void show(){
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Requests").child(current_user.getUid());
        mDatabaseRef.keepSynced(true);
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list.clear();
                if(dataSnapshot.getKey() ==null ) {
                    Toast.makeText(getActivity(),"No Results Found",Toast.LENGTH_LONG).show();
                    return;
                }
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(ds.child("request_type").getValue()==null)
                        continue;
                    if(ds.child("request_type").getValue().toString().equals("got")){
                        list.add(ds.getKey());
                    }
                }
                mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
                mDatabaseRef.keepSynced(true);
                mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                       dataList.clear();
                        for(DataSnapshot ds :dataSnapshot.getChildren()){
                            if(list.contains(ds.getKey())){
                                DataFields ob = new DataFields();
                                ob.setImage(ds.child("tumb_image").getValue().toString());
                                ob.setId(ds.getKey());
                                ob.setFull_image(ds.child("image").getValue().toString());
                                ob.setName(ds.child("name").getValue().toString());
                                ob.setStatus("Sent You A Friend Request");
                                dataList.add(ob);
                            }
                        }
                        if(re==null) {
                            re = new RecyclerAdapter(dataList, getActivity());
                            mRecyclerView.setAdapter(re);
                        }else{
                            re.setData(dataList);
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

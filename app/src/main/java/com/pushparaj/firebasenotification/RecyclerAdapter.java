package com.pushparaj.firebasenotification;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.AllUsersAdapter> {
    ArrayList<DataFields> data;
    Context mContext;
    @Override
    public RecyclerAdapter.AllUsersAdapter onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_row,parent,false);
        return new AllUsersAdapter(view);
    }

    public void setData(ArrayList<DataFields> data) {
        this.data = data;
    }

    public RecyclerAdapter(ArrayList<DataFields> data, Context mContext) {
        this.data = data;
        this.mContext = mContext;
    }

    @Override
    public void onBindViewHolder(AllUsersAdapter viewHolder, int position) {
        final String name = data.get(position).getName();
        final String image = data.get(position).getImage();
        final String full_image = data.get(position).getFull_image();
        final String status = data.get(position).getStatus();
        final String user_id = data.get(position).getId();

        viewHolder.setName(name);
        viewHolder.setImage(image);
        viewHolder.setStatus(status);
        if(status.equals("Sent You A Friend Request")){
            viewHolder.setRequest(mContext);
        }else{
            viewHolder.setNormal(mContext);
        }

        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent allUserProfile = new Intent(mContext, AllUserProfile.class);
                allUserProfile.putExtra("user_id", user_id);
                allUserProfile.putExtra("pic", full_image);
                allUserProfile.putExtra("name", name);
                allUserProfile.putExtra("status", status);
                mContext.startActivity(allUserProfile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static  class AllUsersAdapter extends RecyclerView.ViewHolder {
        View mView;
        public AllUsersAdapter(View itemView) {
            super(itemView);
            mView= itemView;
        }
        public void setImage(final String url){
            final CircleImageView circleImageView = (CircleImageView)mView.findViewById(R.id.row_circle_image_view);
            Picasso.with(circleImageView.getContext()).load(url).fit().networkPolicy(NetworkPolicy.OFFLINE).into(circleImageView, new Callback() {
                @Override
                public void onSuccess() {

                }
                @Override
                public void onError() {
                    Picasso.with(circleImageView.getContext()).load(url).fit().into(circleImageView);
                }
            });

        }
        public void setName(String name){
            TextView txt_name = (TextView)mView.findViewById(R.id.row_name);
            txt_name.setText(name);
        }
        public void setStatus(String status){
            TextView txt_status = (TextView)mView.findViewById(R.id.row_status);
            txt_status.setText(status);
        }

        public void setRequest(Context mContext) {
            mView.setBackground(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.borderreq, null));
        }

        public void setNormal(Context mContext) {
            mView.setBackground(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.border, null));

        }
    }
}

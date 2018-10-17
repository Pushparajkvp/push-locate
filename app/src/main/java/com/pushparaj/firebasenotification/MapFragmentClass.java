package com.pushparaj.firebasenotification;


import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class MapFragmentClass extends Fragment implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "pushpa";
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    GoogleMap mGoogleMap;
    View mView;
    MapView mMapView;
    DatabaseReference databaseReference;
    FirebaseUser current_user;
    Marker myMarker;
    MarkerOptions myOptions;
    String image_url;
    myTarget target;
    ArrayList<FriendsTarget> friendsTargets;
    HashMap<String,Marker> mFriendsMarkers;
    HashMap<String,Marker> mFreindsName;
    Button serachButton;
    EditText searchText;

    public MapFragmentClass() {
    }

    protected void stopLocationUpdates() {

    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (googlePlaySevicesAvailable()) {
            mView = inflater.inflate(R.layout.fragment_map, container, false);
            try {MapsInitializer.initialize(getActivity().getApplicationContext());
            } catch (Exception e){e.printStackTrace();}

            mMapView = (MapView) mView.findViewById(R.id.mapFragment);
            mMapView.onCreate(savedInstanceState);
            mMapView.onResume();
            serachButton = (Button)mView.findViewById(R.id.buttonSearchName);
            searchText = (EditText)mView.findViewById(R.id.editTextSearch);
            serachButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mGoogleMap==null){
                        Toast.makeText(getActivity(),"Please Wait For Markers To Load",Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(searchText.getText().toString().equals("")){
                        Toast.makeText(getActivity(),"Please Enter Name In The Text Field",Toast.LENGTH_LONG).show();
                        return;
                    }
                    String name = searchText.getText().toString().toLowerCase();
                    for(Map.Entry<String,Marker> sb : mFreindsName.entrySet()){
                        if(sb.getKey().toLowerCase().startsWith(name) || sb.getKey().toLowerCase().equals(name)){
                            LatLng location = sb.getValue().getPosition();
                            sb.getValue().showInfoWindow();
                            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,(float)15.5));
                            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            return;
                        }
                    }
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    Toast.makeText(getActivity(),"No Results Found",Toast.LENGTH_LONG).show();

                }
            });
            mFriendsMarkers = new HashMap<String,Marker>();
            mFreindsName = new HashMap<String,Marker>();
            friendsTargets = new ArrayList<FriendsTarget>();

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();


            target= new myTarget();
            current_user = FirebaseAuth.getInstance().getCurrentUser();

            if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                return mView;
            }
            //Toast.makeText(getActivity(),"yes",Toast.LENGTH_LONG).show();
            mMapView.getMapAsync(new callBack());


        } else {
            mView = inflater.inflate(R.layout.noplayservices, container, false);
        }
        return mView;
    }




    public boolean googlePlaySevicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int availability = googleApiAvailability.isGooglePlayServicesAvailable(getActivity());
        if (availability == ConnectionResult.SUCCESS) {
            return true;
        } else if (googleApiAvailability.isUserResolvableError(availability)) {
            Dialog dialog = googleApiAvailability.getErrorDialog(getActivity(), availability, 0);
            dialog.show();
        } else {
            Toast.makeText(getActivity(), "Cannot Connect To Google Play Services", Toast.LENGTH_LONG).show();
        }
        return false;
    }




    public class callBack implements OnMapReadyCallback {

        @Override
        public void onMapReady(final GoogleMap mMap) {
            mGoogleMap = mMap;
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(),"Permission denied",Toast.LENGTH_LONG).show();
                return;
            }

            mGoogleMap.setMyLocationEnabled(true);
            //Get Current User Details For Marker
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user.getUid()).child("tumb_image");
            databaseReference.keepSynced(true);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    image_url = (String) dataSnapshot.getValue();
                    target.setUrl(dataSnapshot.getValue().toString());
                    target.setLastSeen("online");
                    Picasso.with(getActivity()).load(image_url).networkPolicy(NetworkPolicy.OFFLINE).transform(new CircleTransform()).resize(75,75).into(target);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getActivity(),databaseError.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
            //Get Friends Details For Markers
            databaseReference= FirebaseDatabase.getInstance().getReference().child("Friends").child(current_user.getUid());
            databaseReference.keepSynced(true);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds :dataSnapshot.getChildren()){
                        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(ds.getKey());
                        databaseReference.keepSynced(true);
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot ds) {
                                final String image_url = (String) ds.child("tumb_image").getValue();
                                final String name = (String) ds.child("name").getValue();
                                final String userId=ds.getKey();
                                final String lastseen = timeAgo.getTimeAgo((long)ds.child("lastseen").getValue());
                                if(ds.hasChild("lat")){
                                    final double lat = (double) ds.child("lat").getValue();
                                    final double lng = (double) ds.child("long").getValue();
                                    if(mFriendsMarkers.containsKey(userId)){
                                        mFriendsMarkers.get(userId).setPosition(new LatLng(lat,lng));
                                    }else {
                                        FriendsTarget friendsTarget = new FriendsTarget();
                                        friendsTarget.setUrl(image_url);
                                        friendsTarget.setName(name);
                                        friendsTarget.setLng(lng);
                                        friendsTarget.setLat(lat);
                                        friendsTarget.setId(userId);
                                        friendsTarget.setLastseen(lastseen);
                                        friendsTargets.add(friendsTarget);
                                        Picasso.with(getActivity()).load(image_url).transform(new CircleTransform()).resize(75, 75).networkPolicy(NetworkPolicy.OFFLINE).into(friendsTarget);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }
    }
    public class myTarget implements Target{
        String url,lastseen;

        public void setUrl(String url) {
            this.url = url;
        }

        public myTarget() {

        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            myOptions = new MarkerOptions().title("you").snippet(lastseen).icon(BitmapDescriptorFactory.fromBitmap(bitmap));
            if(myMarker!=null)
                myMarker.remove();
            myMarker=null;
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            Picasso.with(getActivity()).load(url).transform(new CircleTransform()).resize(75,75).into(this);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }

        public void setLastSeen(String lastSeen) {
            this.lastseen = lastSeen;
        }
    }
    public class FriendsTarget implements Target{
        String url,name,lastseen;
        double lat,lng;
        MarkerOptions temp;
        String id;

        public String getLastseen() {
            return lastseen;
        }

        public void setLastseen(String lastseen) {
            this.lastseen = lastseen;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void setName(String name) {
            this.name = name;
        }
        public void setId(String id) {
            this.id = id;
        }

        public FriendsTarget() {

        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
          //  Log.i("pushpa","Success: "+name);
            temp = new MarkerOptions().title(name).snippet(lastseen).icon(BitmapDescriptorFactory.fromBitmap(bitmap)).position(new LatLng(lat,lng));
            Marker markers = mGoogleMap.addMarker(temp);
            mFriendsMarkers.put(id, markers);
            mFreindsName.put(name,markers);
            friendsTargets.remove(this);

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : mFriendsMarkers.values()) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 0);
            mGoogleMap.moveCamera(cu);
          //  Log.i("pushpa", String.valueOf(friendsTargets.size()));
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
           // Log.i("pushpa","Failed: "+name);
            Picasso.with(getActivity()).load(url).transform(new CircleTransform()).resize(75,75).into(this);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {}


    }
    //Life Cycle
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        if(mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    //start location
    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }

    //API!!
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());

    }


    //Listener!!
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;
        if(myOptions !=null) {
            if (myMarker != null) {
                myMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            } else {
                myMarker = mGoogleMap.addMarker(myOptions.position(new LatLng(location.getLatitude(), location.getLongitude())));
            }
        }

        databaseReference= FirebaseDatabase.getInstance().getReference().child("Friends").child(current_user.getUid());
        databaseReference.keepSynced(true);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds :dataSnapshot.getChildren()){
                    databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(ds.getKey());
                    databaseReference.keepSynced(true);
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot ds) {
                            final String image_url = (String) ds.getKey();
                            final String name = (String) ds.child("name").getValue();
                            if(ds.hasChild("lat")){
                                final double lat = (double) ds.child("lat").getValue();
                                final double lng = (double) ds.child("long").getValue();
                                final String lastseen = timeAgo.getTimeAgo((long)ds.child("lastseen").getValue());
                               // Log.i("pushparajj",lastseen);
                                if(mFriendsMarkers.containsKey(image_url)){
                                    mFriendsMarkers.get(image_url).setSnippet(lastseen);
                                    mFriendsMarkers.get(image_url).setPosition(new LatLng(lat,lng));
                                }else {
                                    FriendsTarget friendsTarget = new FriendsTarget();
                                    friendsTarget.setUrl(image_url);
                                    friendsTarget.setName(name);
                                    friendsTarget.setLng(lng);
                                    friendsTarget.setLat(lat);
                                    friendsTarget.setLastseen(lastseen);
                                    friendsTargets.add(friendsTarget);
                                    Picasso.with(getActivity()).load(image_url).transform(new CircleTransform()).resize(75, 75).networkPolicy(NetworkPolicy.OFFLINE).into(friendsTarget);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}

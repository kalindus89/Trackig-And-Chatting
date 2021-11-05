package com.trackigandchatting.main_chat_activities;

import static android.content.ContentValues.TAG;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TransitMode;
import com.google.maps.model.TravelMode;
import com.trackigandchatting.R;
import com.trackigandchatting.chat_adapters.AllFriendsAdapter;
import com.trackigandchatting.models.ChatsModel;
import com.trackigandchatting.models.ClusterMarker;
import com.trackigandchatting.models.PolylineData;
import com.trackigandchatting.models.UserLocation;
import com.trackigandchatting.util.MyClusterManagerRenderer;
import com.trackigandchatting.util.ViewWeightAnimationWrapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.collections.MapsKt;

public class FriendsListActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener,
        ClusterManager.OnClusterItemInfoWindowClickListener<ClusterMarker>, GoogleMap.OnPolylineClickListener {

    private static final int MAP_LAYOUT_STATE_CONTRACTED = 0;
    private static final int MAP_LAYOUT_STATE_EXPANDED = 1;
    private int mMapLayoutState = 0;

    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;

    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    private ArrayList<UserLocation> mUserLocations = new ArrayList<>();
    private ArrayList<PolylineData> mPolylineData = new ArrayList<>();
    private ArrayList<Marker> mTripMakers = new ArrayList<>();// keep track of tripe makers. if user select another trip, then previous marker remove and new marker appear

    private ClusterManager<ClusterMarker> mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private GeoApiContext mGeoApiContext;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    RecyclerView recyclerViewChat;
    AllFriendsAdapter allChatsAdapter;
    GoogleMap googleMap;
    FusedLocationProviderClient fusedLocationProviderClient;
    MapFragment mapFragment;
    LatLngBounds mMapBoundary;
    RelativeLayout map_container;
    ImageButton btn_full_screen_map,btn_reset,open_in_google_map;
    private Marker mSelectedMarker = null;

    UserLocation userLocationForGeoPoints = new UserLocation();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        recyclerViewChat = findViewById(R.id.recyclerViewFriendList);
        map_container = findViewById(R.id.map_container);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.user_list_map);
        btn_full_screen_map = findViewById(R.id.btn_full_screen_map);
        btn_full_screen_map.setOnClickListener(this);
        btn_reset = findViewById(R.id.btn_reset);
        btn_reset.setOnClickListener(this);
        open_in_google_map = findViewById(R.id.open_in_google_map);
        open_in_google_map.setOnClickListener(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(FriendsListActivity.this);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showMap();
                syncChatPeopleFromFirestore();
            }
        });


    }


    @Override
    protected void onResume() {
        super.onResume();
        startUserLocationsRunnable();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    //update users locations on map very 3s
    private void startUserLocationsRunnable() {
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                retrieveUserLocations();
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void stopLocationUpdates() {
        mHandler.removeCallbacks(mRunnable);
    }

    private void retrieveUserLocations() {
        Log.d(TAG, "retrieveUserLocations: retrieving location of all users in the chatroom.");

        try {
            for (final ClusterMarker clusterMarker : mClusterMarkers) {

                DocumentReference userLocationRef = FirebaseFirestore.getInstance()
                        .collection("UserCurrentLocation")
                        .document(clusterMarker.getuId());

                userLocationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            final UserLocation updatedUserLocation = task.getResult().toObject(UserLocation.class);

                            // update the location
                            for (int i = 0; i < mClusterMarkers.size(); i++) {
                                try {
                                    if (mClusterMarkers.get(i).getuId().equals(updatedUserLocation.getUid())) {

                                        LatLng updatedLatLng = new LatLng(
                                                updatedUserLocation.getGeo_point().getLatitude(),
                                                updatedUserLocation.getGeo_point().getLongitude()
                                        );

                                        mClusterMarkers.get(i).setPosition(updatedLatLng);
                                        mClusterManagerRenderer.setUpdateMarker(mClusterMarkers.get(i));
                                    }


                                } catch (NullPointerException e) {
                                    Log.e(TAG, "retrieveUserLocations: NullPointerException: " + e.getMessage());
                                }
                            }
                        }
                    }
                });
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "retrieveUserLocations: Fragment was destroyed during Firestore query. Ending query." + e.getMessage());
        }

    }

    private void getAllUserLocationsFromFirebase() {

        CollectionReference collectionReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid()).collection("myChats");
        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                for (QueryDocumentSnapshot queryDocumentSnapshot : value) {
                    getFriendGeoCode(queryDocumentSnapshot.get("uid").toString(), value.size());
                }


            }
        });

    }

    private void getFriendGeoCode(String uid, int numberOfFriends) {


        DocumentReference nycRef = firebaseFirestore.collection("UserCurrentLocation").document(uid);

        nycRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //  userLocationForGeoPoints=task.getResult().toObject(UserLocation.class);

                        mUserLocations.add(task.getResult().toObject(UserLocation.class));

                        if (mUserLocations.size() == (numberOfFriends + 1)) {
                            addMapMakers();
                        }
                        //System.out.println("aaaaaaaa2222 "+userLocationForGeoPoints.getGeo_point().getLatitude()+" "+userLocationForGeoPoints.getGeo_point().getLongitude());

                    } else {
                        Toast.makeText(getApplicationContext(), "No document exist", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Not ok big", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void showMap() {

        // if you use mapfragment then lifecycle methods are not needed
        //otherwise map will not display
        // https://developers.google.com/maps/documentation/android-sdk/reference/com/google/android/libraries/maps/MapView?hl=en
        //https://developers.google.com/maps/documentation/android-sdk/reference/com/google/android/libraries/maps/MapFragment?hl=en


        mapFragment.getMapAsync(this); //onMapReady method automatically call. your Default map

        if(mGeoApiContext==null){
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_map_api_key))
                    .build();
        }

    }


    private void addMapMakers() {
        // clusters use to customized markers.(with images and avatars. when map zoom out avatars combine together)
        //but for project we use only single clusters. means not grouping when zoom out
        // https://developers.google.com/maps/documentation/android-sdk/utility/marker-clustering?hl=en

        if (googleMap != null) {

            resetMap();
            open_in_google_map.setVisibility(View.GONE);

            if (mClusterManager == null) {
                mClusterManager = new ClusterManager<ClusterMarker>(getApplicationContext(), googleMap);
            }

            if (mClusterManagerRenderer == null) {
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        getApplicationContext(),
                        googleMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }


            for (UserLocation userLocation : mUserLocations) {

                Log.d(TAG, "addMapMarkers: location: " + userLocation.getGeo_point().toString());
                try {

                    String snippet = "";

                    int avatar = R.drawable.cartman_cop; // set the default avatar

                    if (userLocation.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                        snippet = "This is you";
                        avatar=R.drawable.cwm_logo;
                    } else {
                        snippet = "Determine route to " + userLocation.getName() + "?";
                    }


                   /*try{
                        avatar = Integer.parseInt(userLocation.getUser().getAvatar());
                    }catch (NumberFormatException e){
                        Log.d(TAG, "addMapMarkers: no avatar for " + userLocation.getName() + ", setting default.");
                    }*/
                    ClusterMarker newClusterMarker = new ClusterMarker(
                            new LatLng(userLocation.getGeo_point().getLatitude(), userLocation.getGeo_point().getLongitude()),
                            userLocation.getName(),
                            snippet,
                            avatar, userLocation.getUid()
                    );
                    mClusterManager.addItem(newClusterMarker);
                    mClusterMarkers.add(newClusterMarker);

                } catch (NullPointerException e) {
                    Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage());
                }

            }
            mClusterManager.cluster();

            mClusterManager.setOnClusterItemInfoWindowClickListener(this);

            // googleMap.setOnInfoWindowClickListener(this);

            markOnMapForZoomBoundary((new GeoPoint(6.8649, 79.8997)));

        }

    }

    private void resetMap(){
        if(googleMap != null) {
            googleMap.clear();

            if(mClusterManager != null){
                mClusterManager.clearItems();
            }

            if (mClusterMarkers.size() > 0) {
                mClusterMarkers.clear();
                mClusterMarkers = new ArrayList<>();
            }

            if(mPolylineData.size() > 0){
                mPolylineData.clear();
                mPolylineData = new ArrayList<>();
            }
        }
    }

    public void markOnMapForZoomBoundary(GeoPoint geoPoint) {

        //zoom specific area

        double bottomBoundary = geoPoint.getLatitude() - .1;
        double leftBoundary = geoPoint.getLongitude() - .1;
        double topBoundary = geoPoint.getLatitude() + .1;
        double rightBoundary = geoPoint.getLongitude() + .1;

        mMapBoundary = new LatLngBounds(new LatLng(bottomBoundary, leftBoundary), new LatLng(topBoundary, rightBoundary));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));

    }

    public void markOnMap(double latitude, double longitude, float zoomLevel, String locationName, String snippet) {

        googleMap.clear(); // clear map and remove markers

        LatLng latLng = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(locationName);
        markerOptions.position(latLng);
        markerOptions.snippet(snippet); //place info's
        // googleMap.addMarker(markerOptions).remove();
        googleMap.addMarker(markerOptions);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel);  //max zoom 21. 1world, 5Continents, 10Cities, 15Streets, 20Buildings
        //googleMap.moveCamera(cameraUpdate); //directly show
        // googleMap.animateCamera(cameraUpdate); // moving to position without time

        googleMap.animateCamera(CameraUpdateFactory.zoomTo(2.0f));
        googleMap.animateCamera(cameraUpdate, 3000, new GoogleMap.CancelableCallback() { // moving to position with time
            @Override
            public void onFinish() {

            }

            @Override
            public void onCancel() {

            }
        });
    }

    public void getUserKnowLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {

                Location location = (Location) task.getResult();


                userLocationForGeoPoints.setUid(firebaseAuth.getUid());
                userLocationForGeoPoints.setGeo_point(new GeoPoint(location.getLatitude(), location.getLongitude()));
                userLocationForGeoPoints.setName("kalindu");

                mUserLocations.add(userLocationForGeoPoints);
                getAllUserLocationsFromFirebase();


            }
        });
    }

    private void removeTripMarkers(){
        for (Marker marker: mTripMakers){
            marker.remove();
        }
    }

    private void resetSelectedMaker(){
        if(mSelectedMarker !=null){
            mSelectedMarker.setVisible(true);
            mSelectedMarker=null;
            removeTripMarkers();

        }
    }

    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {


                //remove all polyline if new user selected
                if(mPolylineData.size() > 0){
                    for(PolylineData polylineData: mPolylineData){
                        polylineData.getPolyline().remove();
                    }
                    mPolylineData.clear();
                    mPolylineData = new ArrayList<>();
                }

                double duration =999999999;
                for(DirectionsRoute route: result.routes){
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = googleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(FriendsListActivity.this, R.color.darkGrey));
                    polyline.setClickable(true);

                    googleMap.setOnPolylineClickListener(FriendsListActivity.this); // suer can select the root

                    mPolylineData.add(new PolylineData(polyline,route.legs[0]));

                    mSelectedMarker.setVisible(false); // remove maker when polyline is selected

                    double tempDuration=route.legs[0].duration.inSeconds;

                    if(tempDuration<duration){
                        // mark the shortest path on map(automatically selected the path in blue color)
                        duration=tempDuration;
                        onPolylineClick(polyline);
                        zoomRoute(polyline.getPoints());
                    }

                }
            }
        });
    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {


        int index=0;

        for(PolylineData polylineData: mPolylineData){

            index++;

            if(polyline.getId().equals(polylineData.getPolyline().getId())){

                open_in_google_map.setVisibility(View.VISIBLE);

                polylineData.getPolyline().setColor(ContextCompat.getColor(this, R.color.blue));
                polylineData.getPolyline().setZIndex(1);// line elevation

                calculateDuration(polylineData,index);

            }
            else{
                polylineData.getPolyline().setColor(ContextCompat.getColor(this, R.color.darkGrey));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }

    private void calculateDuration(PolylineData polylineData, int index) {

        LatLng endLocation = new LatLng(
                polylineData.getLeg().endLocation.lat,
                polylineData.getLeg().endLocation.lng);

        Marker marker =googleMap.addMarker(new MarkerOptions()
                .position(endLocation)
                .title("Trip #"+index)
                .snippet("Duration: "+polylineData.getLeg().duration));
        marker.showInfoWindow();
        mTripMakers.add(marker);

    }

    public void zoomRoute(List<LatLng> lstLatLngRoute) {

        if (googleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 50;
        LatLngBounds latLngBounds = boundsBuilder.build();

        googleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        getUserKnowLocation();

        //when click on clustermaker snippet, dialog box will appear. check onInfoWindowClick()


        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true); // Compass not showing until you rotate the map

        // googleMap.getUiSettings().setZoomGesturesEnabled(false); // map cant zoom
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        //googleMap.getUiSettings().setScrollGesturesEnabled(false);// map cant Scroll
        googleMap.getUiSettings().setScrollGesturesEnabled(true);// map cant Scroll

        //googleMap.getUiSettings().setRotateGesturesEnabled(false);// map cant Rotate
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setTiltGesturesEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true); // when you tap on marker, shows open from google map and direction

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);

    }


    private void syncChatPeopleFromFirestore() {

        Query query = firebaseFirestore.collection("Users").whereNotEqualTo("uid", firebaseAuth.getUid());
        FirestoreRecyclerOptions<ChatsModel> allChats = new FirestoreRecyclerOptions.Builder<ChatsModel>().setQuery(query, ChatsModel.class).build();

        allChatsAdapter = new AllFriendsAdapter(this, allChats);
        recyclerViewChat.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerViewChat.setLayoutManager(linearLayoutManager);
        recyclerViewChat.setAdapter(allChatsAdapter);
        allChatsAdapter.notifyDataSetChanged();
    }


    //expand map with animation
    private void expandMapAnimation() {
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(map_container);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                50,
                100);
        mapAnimation.setDuration(800);

        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(recyclerViewChat);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                50,
                0);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();
    }

    //contract map with animation
    private void contractMapAnimation() {
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(map_container);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                100,
                50);
        mapAnimation.setDuration(800);

        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(recyclerViewChat);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                0,
                50);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();
    }

    @Override
    public void onStart() {
        super.onStart();
        allChatsAdapter.startListening();
        recyclerViewChat.setAdapter(allChatsAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (allChatsAdapter != null) {
            allChatsAdapter.stopListening();
            //noteAdapter.startListening();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_full_screen_map: {

                if (mMapLayoutState == MAP_LAYOUT_STATE_CONTRACTED) {
                    mMapLayoutState = MAP_LAYOUT_STATE_EXPANDED;
                    expandMapAnimation();
                } else if (mMapLayoutState == MAP_LAYOUT_STATE_EXPANDED) {
                    mMapLayoutState = MAP_LAYOUT_STATE_CONTRACTED;
                    contractMapAnimation();
                }
                break;
            }
            case R.id.btn_reset: {

                addMapMakers();
                break;
            }
            case R.id.open_in_google_map: {

                if(!mSelectedMarker.getTitle().isEmpty()){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Open Google Maps?")
                            .setCancelable(true)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                    String latitude = String.valueOf(mSelectedMarker.getPosition().latitude);
                                    String longitude = String.valueOf(mSelectedMarker.getPosition().longitude);
                                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                    mapIntent.setPackage("com.google.android.apps.maps");

                                    try{
                                        if (mapIntent.resolveActivity(getPackageManager()) != null) {
                                            startActivity(mapIntent);
                                        }
                                    }catch (NullPointerException e){
                                        Log.e(TAG, "onClick: NullPointerException: Couldn't open map." + e.getMessage() );
                                        Toast.makeText(FriendsListActivity.this, "Couldn't open map", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                    dialog.cancel();
                                }
                            });
                    final AlertDialog alert = builder.create();
                    alert.show();
                }
                break;
            }

        }
    }


    private BitmapDescriptor getBitmapDescriptor(@DrawableRes int id) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    @Override
    public void onClusterItemInfoWindowClick(ClusterMarker item) {

        if (!item.getSnippet().equals("This is you")) { // not equal to userId

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(item.getSnippet())
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                            //used to get the data of selected location to change avatar to marker.
                            resetSelectedMaker();
                            mSelectedMarker=mClusterManagerRenderer.getMarker(item); // get the maker
                            calculateDirections(item);

                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }

    }

    private void calculateDirections(ClusterMarker marker){

        //https://developers.google.com/maps/documentation/directions/get-directions

        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(true);

       // directions.mode(TravelMode.BICYCLING); can change the travel mode WALKING, DRIVING, BICYCLING

        //my location
        directions.origin(
                new com.google.maps.model.LatLng(
                        userLocationForGeoPoints.getGeo_point().getLatitude(),
                        userLocationForGeoPoints.getGeo_point().getLongitude()
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
             /*   Log.d(TAG, "onResult: all: " + result.routes.length);
                Log.d(TAG, "onResult: routes: " + result.routes[0].toString());
                Log.d(TAG, "onResult: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "onResult: distance: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "onResult: durationInTraffic: " + result.routes[0].legs[0].durationInTraffic);
                Log.d(TAG, "onResult: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
*/
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "onFailure: " + e.getMessage() );

            }
        });
    }

    public void moveCamaraOnFriendClick(int position){
        String selectedUserId = mUserLocations.get(position+1).getUid(); //position+1 because user Uid manually added to the list

        for(ClusterMarker clusterMarker: mClusterMarkers){
            if(selectedUserId.equals(clusterMarker.getuId())){
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(
                        new LatLng(clusterMarker.getPosition().latitude, clusterMarker.getPosition().longitude)),
                        600,
                        null
                );
                break;
            }
        }
    }

}
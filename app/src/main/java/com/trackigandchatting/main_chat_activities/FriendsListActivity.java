package com.trackigandchatting.main_chat_activities;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.google.maps.android.clustering.ClusterManager;
import com.trackigandchatting.R;
import com.trackigandchatting.chat_adapters.AllFriendsAdapter;
import com.trackigandchatting.models.ChatsModel;
import com.trackigandchatting.models.ClusterMarker;
import com.trackigandchatting.models.UserLocation;
import com.trackigandchatting.util.MyClusterManagerRenderer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FriendsListActivity extends AppCompatActivity implements OnMapReadyCallback {

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    RecyclerView recyclerViewChat;
    AllFriendsAdapter allChatsAdapter;
    GoogleMap googleMap;
    FusedLocationProviderClient fusedLocationProviderClient;
    MapFragment mapFragment;
    LatLngBounds mMapBoundary;

    private ClusterManager<ClusterMarker> mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;

    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    private ArrayList<UserLocation> mUserLocations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        recyclerViewChat = findViewById(R.id.recyclerViewFriendList);
         mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.user_list_map);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(FriendsListActivity.this);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showMap();
                syncChatPeopleFromFirestore();
            }
        });


    }

    private void getAllUserLocationsFromFirebase() {



        CollectionReference collectionReference =firebaseFirestore.collection("Users").document(firebaseAuth.getUid()).collection("myChats");
        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                for(QueryDocumentSnapshot queryDocumentSnapshot:value){
                    getFriendGeoCode(queryDocumentSnapshot.get("uid").toString(),value.size());
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

                        if(mUserLocations.size()==(numberOfFriends+1)){
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

    }


    private void addMapMakers(){
        // clusters use to customized markers.(with images and avatars. when map zoom out avatars combine together)
        //but for project we use only single clusters. means not grouping when zoom out
        // https://developers.google.com/maps/documentation/android-sdk/utility/marker-clustering?hl=en

        if(googleMap != null){
            if(mClusterManager == null){
                mClusterManager = new ClusterManager<ClusterMarker>(getApplicationContext(), googleMap);
            }

            if(mClusterManagerRenderer == null){
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        getApplicationContext(),
                        googleMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }

            for(UserLocation userLocation: mUserLocations){

                Log.d(TAG, "addMapMarkers: location: " + userLocation.getGeo_point().toString());
                try{
                    String snippet = "";
                    if(userLocation.getUid().equals(FirebaseAuth.getInstance().getUid())){
                        snippet = "This is you";
                    }
                    else{
                        snippet = "Determine route to " + userLocation.getName() + "?";
                    }

                    int avatar = R.drawable.cartman_cop; // set the default avatar
                   /* try{
                        avatar = Integer.parseInt(userLocation.getUser().getAvatar());
                    }catch (NumberFormatException e){
                        Log.d(TAG, "addMapMarkers: no avatar for " + userLocation.getUser().getUsername() + ", setting default.");
                    }*/
                    ClusterMarker newClusterMarker = new ClusterMarker(
                            new LatLng(userLocation.getGeo_point().getLatitude(), userLocation.getGeo_point().getLongitude()),
                            userLocation.getName(),
                            snippet,
                            avatar
                    );
                    mClusterManager.addItem(newClusterMarker);
                    mClusterMarkers.add(newClusterMarker);

                }catch (NullPointerException e){
                    Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage() );
                }

            }
            mClusterManager.cluster();

            markOnMapForZoomBoundary((new GeoPoint(6.8649, 79.8997)));

        }

    }

    public void markOnMapForZoomBoundary(GeoPoint geoPoint){

        //zoom specific area

        double bottomBoundary=geoPoint.getLatitude()-.1;
        double leftBoundary=geoPoint.getLongitude()-.1;
        double topBoundary=geoPoint.getLatitude()+.1;
        double rightBoundary=geoPoint.getLongitude()+.1;

        mMapBoundary= new LatLngBounds(new LatLng(bottomBoundary,leftBoundary),new LatLng(topBoundary,rightBoundary));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary,0));

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

    public void getLastKnowLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {

                Location location = (Location) task.getResult();
                //markOnMap(location.getLatitude(), location.getLongitude(), 15,"My Location","Address: Nugegoda\nPhone Number: +94777");

                //markOnMapForZoomBoundary((new GeoPoint(location.getLatitude(), location.getLongitude())));
               // addMapMakers();

                Map<String, Object> userLocation = new HashMap<>();
                userLocation.put("geo_point", (new GeoPoint(location.getLatitude(), location.getLongitude())));

                firebaseFirestore.collection("UserCurrentLocation").document(firebaseAuth.getUid()).update(userLocation).
                        addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        UserLocation userLocationForGeoPoints = new UserLocation();
                        userLocationForGeoPoints.setUid(firebaseAuth.getUid());
                        userLocationForGeoPoints.setGeo_point(new GeoPoint(location.getLatitude(), location.getLongitude()));
                        userLocationForGeoPoints.setName("kalindu");
                        mUserLocations.add(userLocationForGeoPoints);

                        getAllUserLocationsFromFirebase();
                    }
                });


            }
        });
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap=googleMap;

        getLastKnowLocation();

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

        Query query = firebaseFirestore.collection("Users").whereNotEqualTo("uid",firebaseAuth.getUid());
        FirestoreRecyclerOptions<ChatsModel> allChats = new FirestoreRecyclerOptions.Builder<ChatsModel>().setQuery(query, ChatsModel.class).build();

        allChatsAdapter = new AllFriendsAdapter(this,allChats);
        recyclerViewChat.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerViewChat.setLayoutManager(linearLayoutManager);
        recyclerViewChat.setAdapter(allChatsAdapter);
        allChatsAdapter.notifyDataSetChanged();
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

        if(allChatsAdapter!=null)
        {
            allChatsAdapter.stopListening();
            //noteAdapter.startListening();
        }
    }
}
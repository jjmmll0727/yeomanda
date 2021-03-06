package com.example.yeomanda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.yeomanda.Retrofit.ResponseDto.ChatRoomResponseDto;
import com.example.yeomanda.Retrofit.ResponseDto.ProfileResponseDto;
import com.example.yeomanda.Retrofit.ResponseDto.WithoutDataResponseDto;
import com.example.yeomanda.Retrofit.RequestDto.LocationDto;
import com.example.yeomanda.Retrofit.ResponseDto.LocationResponseDto;
import com.example.yeomanda.Retrofit.RetrofitClient;
import com.example.yeomanda.Retrofit.RequestDto.TeamInfoDto;
import com.example.yeomanda.chatPkg.ChatActivity;
import com.example.yeomanda.chatPkg.ChatListActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback , GoogleMap.OnMarkerClickListener{
    private GoogleMap mMap;
    private Context context=this;
    private Marker currentMarker = null;

    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1???
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5???


    // onRequestPermissionsResult?????? ????????? ???????????? ActivityCompat.requestPermissions??? ????????? ????????? ????????? ???????????? ?????? ???????????????.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;


    // ?????? ???????????? ?????? ????????? ???????????? ???????????????.
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // ?????? ?????????


    ListView listView;
    ArrayAdapter<String> adapter;

    Location mCurrentLocatiion;
    LatLng currentPosition;

    RetrofitClient retrofitClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;
    private LocationResponseDto locationResponseDto=null;
    Button createBoardBtn;
    double lat,lon;
    int teamNumCount;
    String locationArr[];
    String myToken, myEmail;
    Boolean hasPlanned;
    View boardDialogView,profileDialogView;
    ArrayList<String> items=new ArrayList<>();
    private View mLayout;  // Snackbar ???????????? ???????????? View??? ???????????????.
    TextView customTravelDate,favoriteTeam,profileRetouch,cancelTravel,chatRoom;
    ArrayList<ArrayList<TeamInfoDto>> sameLocationTeams=new ArrayList<ArrayList<TeamInfoDto>>();

    ImageView menuBtn;

    private DrawerLayout drawerLayout;
    private View drawerView;



    ImageView personSubImage1, personMainImage, personSubImage2,nextTeamBtn, backTeamBtn, chatBtn, favoriteBtn;
    TextView personEmail,personSex,personName,personBirth,teamName;
    ProfileResponseDto profileResponseDto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent=getIntent();
        myToken =intent.getStringExtra("token");
        myEmail=intent.getStringExtra("email");
        hasPlanned=intent.getBooleanExtra("hasPlanned",false);

        init();
    }

    public void init(){
        retrofitClient = new RetrofitClient();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mLayout = findViewById(R.id.layout_main);


        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);


        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        createBoardBtn=findViewById(R.id.createBoardBtn);
        menuBtn=findViewById(R.id.menuIcon);
        favoriteTeam=findViewById(R.id.favoriteTeam);
        profileRetouch=findViewById(R.id.profileRetouch);
        cancelTravel = findViewById(R.id.cancelTravel);
        chatRoom = findViewById(R.id.chatRoom);
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerView = findViewById(R.id.drawer);

        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(drawerView);
            }
        });

        createBoardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(getApplicationContext(),CreateBoard.class);
                intent.putExtra("lat",location.getLatitude());
                intent.putExtra("lon",location.getLongitude());
                startActivity(intent);

            }

        });
        favoriteTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent favoriteIntent=new Intent(getApplicationContext(),MyFavoriteList.class);
                favoriteIntent.putExtra("token", myToken);
                startActivity(favoriteIntent);
            }
        });
        cancelTravel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setMessage("????????? ????????? ?????????????????????????");
                builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        retrofitClient=new RetrofitClient();
                        WithoutDataResponseDto withoutDataResponseDto =retrofitClient.deleteBoard(myToken);
                        while(withoutDataResponseDto ==null){
                            Log.d("error", " withoutDataResponseDto is null");
                        }
                        if(withoutDataResponseDto.getSuccess()) {
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        }
                        Toast.makeText(getApplicationContext(), withoutDataResponseDto.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        profileRetouch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),MyProfile.class);
                intent.putExtra("token",myToken);
                intent.putExtra("email",myEmail);
                startActivity(intent);
            }
        });
        chatRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent=new Intent(getApplicationContext(), ChatListActivity.class);
                chatIntent.putExtra("token", myToken);
                chatIntent.putExtra("myEmail",myEmail);
                startActivity(chatIntent);
            }
        });
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "onMapReady :");

        mMap = googleMap;

        //????????? ????????? ?????? ??????????????? GPS ?????? ?????? ???????????? ???????????????
        //????????? ??????????????? ????????? ??????
        setDefaultLocation();



        //????????? ????????? ??????
        // 1. ?????? ???????????? ????????? ????????? ???????????????.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);



        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {

            // 2. ?????? ???????????? ????????? ?????????
            // ( ??????????????? 6.0 ?????? ????????? ????????? ???????????? ???????????? ????????? ?????? ????????? ?????? ???????????????.)


            startLocationUpdates(); // 3. ?????? ???????????? ??????


        }else {  //2. ????????? ????????? ????????? ?????? ????????? ????????? ????????? ???????????????. 2?????? ??????(3-1, 4-1)??? ????????????.

            // 3-1. ???????????? ????????? ????????? ??? ?????? ?????? ????????????
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. ????????? ???????????? ?????? ?????????????????? ???????????? ????????? ????????? ???????????? ????????? ????????????.
                Snackbar.make(mLayout, "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.",
                        Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        // 3-3. ??????????????? ????????? ????????? ?????????. ?????? ????????? onRequestPermissionResult?????? ???????????????.
                        ActivityCompat.requestPermissions( MainActivity.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();


            } else {
                // 4-1. ???????????? ????????? ????????? ??? ?????? ?????? ???????????? ????????? ????????? ?????? ?????????.
                // ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }



        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMarkerClickListener(this);
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                if(locationResponseDto==null) {
                    currentPosition=new LatLng(location.getLatitude(),location.getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentPosition, 15);
                    mMap.moveCamera(cameraUpdate);

                    //?????? TeamInfo ????????????
                    LocationDto locationDto = new LocationDto();
                    locationDto.setLatitude(Double.toString(location.getLatitude()));
                    locationDto.setLongitude(Double.toString(location.getLongitude()));
                    locationResponseDto = retrofitClient.sendLocation(locationDto);
                    while(locationResponseDto == null) {
                        Log.d("error", "locationResponse is null");
                    }
                    ArrayList<TeamInfoDto> teamArr = new ArrayList<>();
                    boolean isOverlap = false;
                    if (locationResponseDto.getData().size() != 0){
                        for(int i=0;i<locationResponseDto.getData().size();i++) {
                            for (int j = 0; j < sameLocationTeams.size(); j++) {
                                if (locationResponseDto.getData().get(i).getLocationGps().equals(sameLocationTeams.get(j).get(0).getLocationGps())) {
                                    sameLocationTeams.get(j).add(locationResponseDto.getData().get(i));
                                    isOverlap=true;
                                    break;
                                }
                            }
                            if(isOverlap) {
                                isOverlap=false;
                            }else{
                                teamArr.add(locationResponseDto.getData().get(i));
                                sameLocationTeams.add(teamArr);
                                teamArr=new ArrayList<>();
                            }
                        }

                        for(int i=0;i<sameLocationTeams.size();i++) {
                            locationArr=sameLocationTeams.get(i).get(0).getLocationGps().split(",");
                            lat=Double.parseDouble(locationArr[0]);
                            lon=Double.parseDouble(locationArr[1]);
                            LatLng teamsGPS = new LatLng(lat, lon);

                            mMap.addMarker(new MarkerOptions()
                                    .position(teamsGPS)
                                    .title(locationResponseDto.getData().get(i).getTeamNo().toString()))
                                    .setTag(sameLocationTeams.get(i));
                        }

                    }
                }

                mCurrentLocatiion = location;
            }


        }

    };


    //?????? ?????? ???????????? ?????????
    private void startLocationUpdates() {

        if (!checkLocationServicesStatus()) {

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        }else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);



            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED   ) {

                Log.d(TAG, "startLocationUpdates : ????????? ???????????? ??????");
                return;
            }


            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission())
                mMap.setMyLocationEnabled(true);

        }

    }


    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        if (checkPermission()) {

            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            if (mMap!=null)
                mMap.setMyLocationEnabled(true);

        }


    }


    @Override
    protected void onStop() {

        super.onStop();

        if (mFusedLocationClient != null) {

            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }



    //??????????????? ??????????????? ???????????? GPS -> ????????? ??????
    public String getCurrentAddress(LatLng latlng) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //???????????? ??????
            Toast.makeText(this, "???????????? ????????? ????????????", Toast.LENGTH_LONG).show();
            return "???????????? ????????? ????????????";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "????????? GPS ??????", Toast.LENGTH_LONG).show();
            return "????????? GPS ??????";

        }


        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "?????? ?????????", Toast.LENGTH_LONG).show();
            return "?????? ?????????";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }


    //????????? ??????????????? ????????? ????????? ??????
    public void setDefaultLocation() {

        //????????? ??????, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "???????????? ????????? ??? ??????";
        String markerSnippet = "?????? ???????????? GPS ?????? ?????? ???????????????";


        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);

    }


    //?????? ????????? ?????????(????????? AlertDialog??? ????????????)
    @Override
    public boolean onMarkerClick(Marker marker) {
        ArrayList<TeamInfoDto> teamInfoDto= (ArrayList<TeamInfoDto>) marker.getTag();

        items=new ArrayList<>();
        boardDialogView = getLayoutInflater().inflate(R.layout.custom_show_travelers, null);

        profileDialogView = getLayoutInflater().inflate(R.layout.activity_person_info,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(boardDialogView);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();


        backTeamBtn=alertDialog.findViewById(R.id.backTeamBtn);
        nextTeamBtn=alertDialog.findViewById(R.id.nextTeamBtn);
        favoriteBtn=alertDialog.findViewById(R.id.favoriteBtn);
        chatBtn=alertDialog.findViewById(R.id.chatBtn);
        customTravelDate=alertDialog.findViewById(R.id.customTravelDate);
        teamName=alertDialog.findViewById(R.id.customTeamName);
        favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrofitClient=new RetrofitClient();
                WithoutDataResponseDto withoutDataResponseDto =retrofitClient.postFavoriteTeam(myToken,teamInfoDto.get(teamNumCount).getTeamNo());
                while(withoutDataResponseDto ==null){
                    Log.d("error", " withoutDataResponseDto is null");
                }
                Toast.makeText(getApplicationContext(), withoutDataResponseDto.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RetrofitClient retrofitClient = new RetrofitClient();
                ChatRoomResponseDto chatRoomResponseDto = retrofitClient.inToChatRoom(myToken, teamInfoDto.get(teamNumCount).getTeamNo().toString());
                while (chatRoomResponseDto == null) {
                    Log.e("error", "chatRoomResponseDto is null");
                }
                if (chatRoomResponseDto.getSuccess()) {

                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    intent.putExtra("roomId", chatRoomResponseDto.getData().getRoomId());
                    intent.putExtra("token", myToken);
                    intent.putExtra("myEmail", myEmail);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(),"???????????? ??????????????????",Toast.LENGTH_LONG).show();
                }
            }
        });
        listView=alertDialog.findViewById(R.id.personList);

        //?????? ????????? ??????
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(),items.get(position),Toast.LENGTH_SHORT).show();

                profileDialogView = getLayoutInflater().inflate(R.layout.activity_person_info,null);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(profileDialogView);

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                personSubImage1 =alertDialog.findViewById(R.id.personSubImage1);
                personMainImage =alertDialog.findViewById(R.id.personMainImage);
                personSubImage2 =alertDialog.findViewById(R.id.personsubImage2);
                personEmail=alertDialog.findViewById(R.id.personEmail);
                personSex=alertDialog.findViewById(R.id.personSex);
                personName=alertDialog.findViewById(R.id.personName);
                personBirth=alertDialog.findViewById(R.id.personBirth);
                retrofitClient=new RetrofitClient();
                profileResponseDto=retrofitClient.showProfile(myToken,teamInfoDto.get(teamNumCount).getEmail().get(position));
                while(profileResponseDto==null){
                    System.out.println("ProfileResponseDto is null");
                }
                personEmail.setText(profileResponseDto.getData().getEmail());
                personSex.setText(profileResponseDto.getData().getSex());
                personBirth.setText(profileResponseDto.getData().getBirth());
                personName.setText(profileResponseDto.getData().getName());

                Glide.with(context)
                        .load(profileResponseDto.getData().getFiles().get(0))
                        .into(personMainImage);


                Glide.with(context)
                        .load(profileResponseDto.getData().getFiles().get(1))
                        .into(personSubImage1);

                Glide.with(context)
                        .load(profileResponseDto.getData().getFiles().get(2))
                        .into(personSubImage2);

                //????????? ?????? ??????1
                personSubImage1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(getApplicationContext(),SelectImageActivity.class);
                        intent.putExtra("uri",profileResponseDto.getData().getFiles().get(1));
                        startActivity(intent);

                    }
                });
                //????????? ?????? ??????2
                personMainImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(getApplicationContext(),SelectImageActivity.class);
                        intent.putExtra("uri",profileResponseDto.getData().getFiles().get(0));
                        startActivity(intent);

                    }
                });
                //????????? ?????? ??????3
                personSubImage2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(getApplicationContext(),SelectImageActivity.class);
                        intent.putExtra("uri",profileResponseDto.getData().getFiles().get(2));
                        startActivity(intent);

                    }
                });
            }
        });

        teamNumCount =0;
        //?????? ????????? ??? ?????? ?????? ??????
        if(teamInfoDto.size()==1){
            items.addAll(teamInfoDto.get(teamNumCount).getNameList());
            customTravelDate.setText(teamInfoDto.get(teamNumCount).getTravelDate());
            teamName.setText(teamInfoDto.get(teamNumCount).getTeamName());
            adapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.single_listview_item,items);
            listView.setAdapter(adapter);
        }
        //?????? ????????? 2??? ????????? ?????? ?????? ??????
        else{
            nextTeamBtn.setVisibility(View.VISIBLE);
            items.addAll(teamInfoDto.get(teamNumCount).getNameList());
            customTravelDate.setText(teamInfoDto.get(teamNumCount).getTravelDate());
            teamName.setText(teamInfoDto.get(teamNumCount).getTeamName());
            adapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.single_listview_item,items);
            listView.setAdapter(adapter);
            nextTeamBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    teamNumCount++;
                    backTeamBtn.setVisibility(View.VISIBLE);
                    items.clear();
                    items.addAll(teamInfoDto.get(teamNumCount).getNameList());
                    customTravelDate.setText(teamInfoDto.get(teamNumCount).getTravelDate());
                    teamName.setText(teamInfoDto.get(teamNumCount).getTeamName());
                    adapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.single_listview_item,items);
                    listView.setAdapter(adapter);
                    //????????? ???(???????????? ?????????)
                    if(teamInfoDto.size()== teamNumCount +1) {
                        nextTeamBtn.setVisibility(View.INVISIBLE);
                    }
                }

            });

            backTeamBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    teamNumCount--;
                    nextTeamBtn.setVisibility(View.VISIBLE);
                    items.clear();
                    //????????? ???
                    items.addAll(teamInfoDto.get(teamNumCount).getNameList());
                    customTravelDate.setText(teamInfoDto.get(teamNumCount).getTravelDate());
                    teamName.setText(teamInfoDto.get(teamNumCount).getTeamName());
                    adapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.single_listview_item,items);
                    listView.setAdapter(adapter);
                    if (teamNumCount==0)
                        backTeamBtn.setVisibility(View.INVISIBLE);
                }
            });
        }


        return false;
    }



    //??????????????? ????????? ????????? ????????? ?????? ????????????
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);



        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
            return true;
        }

        return false;

    }



    /*
     * ActivityCompat.requestPermissions??? ????????? ????????? ????????? ????????? ???????????? ??????????????????.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // ?????? ????????? PERMISSIONS_REQUEST_CODE ??????, ????????? ????????? ???????????? ??????????????????

            boolean check_result = true;


            // ?????? ???????????? ??????????????? ???????????????.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {

                // ???????????? ??????????????? ?????? ??????????????? ???????????????.
                startLocationUpdates();
            } else {
                // ????????? ???????????? ????????? ?????? ????????? ??? ?????? ????????? ??????????????? ?????? ???????????????.2 ?????? ????????? ????????????.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {


                    // ???????????? ????????? ????????? ???????????? ?????? ?????? ???????????? ????????? ???????????? ?????? ????????? ??? ????????????.
                    Snackbar.make(mLayout, "???????????? ?????????????????????. ?????? ?????? ???????????? ???????????? ??????????????????. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();

                } else {


                    // "?????? ?????? ??????"??? ???????????? ???????????? ????????? ????????? ???????????? ??????(??? ??????)?????? ???????????? ???????????? ?????? ????????? ??? ????????????.
                    Snackbar.make(mLayout, "???????????? ?????????????????????. ??????(??? ??????)?????? ???????????? ???????????? ?????????. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            }

        }
    }


    //??????????????? GPS ???????????? ?????? ????????????
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("?????? ????????? ????????????");
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n"
                + "?????? ????????? ???????????????????");
        builder.setCancelable(true);
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //???????????? GPS ?????? ???????????? ??????
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d(TAG, "onActivityResult : GPS ????????? ?????????");


                        needRequest = true;

                        return;
                    }
                }

                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    @Override
    protected void onRestart(){
        super.onRestart();
        locationResponseDto=null;
        sameLocationTeams=new ArrayList<>();
        init();
        Log.d(TAG, "onRestart()");
    }


}
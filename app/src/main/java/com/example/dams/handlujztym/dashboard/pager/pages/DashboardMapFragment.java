package com.example.dams.handlujztym.dashboard.pager.pages;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.dams.handlujztym.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.google.android.gms.common.api.CommonStatusCodes.RESOLUTION_REQUIRED;
import static com.google.android.gms.common.api.CommonStatusCodes.SUCCESS;
import static com.google.android.gms.location.LocationServices.API;
import static com.google.android.gms.location.LocationServices.SettingsApi;
import static com.google.android.gms.location.LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE;

@SuppressWarnings("ConstantConditions")
public class DashboardMapFragment extends Fragment {

    private static final int LOCATION_PERMISSION = 420;
    private static final int INITIAL_ZOOM = 10;
    private static final int DEFAULT_ZOOM = 15;

    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private PlaceAutocompleteFragment autocompleteFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private boolean areLocalShopsDisplayed;

    public static Fragment newInstance() { return new DashboardMapFragment(); }

    // -----------------------------------------------------------------------------------------------------------------

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initLocationClients();

        initAutoComplete();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this::onMapReady);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) == PERMISSION_GRANTED)
            if (googleMap != null) googleMap.setMyLocationEnabled(true);
        requestLocationUpdates();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(getActivity(), ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        } else {
            displayLocationDialog();
            requestLocationUpdates();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION && grantResults[0] == PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(getActivity(), ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
                displayLocationDialog();
            } else {
                showErrorToast();
            }
        } else {
            showErrorToast();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_PERMISSION) {
            if (resultCode == RESULT_OK) {
                displayLocationDialog();
            } else if (resultCode == RESULT_CANCELED) {
                showErrorToast();
            }
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    private void onMapReady(GoogleMap map) {
        googleMap = map;

        map.setContentDescription(getString(R.string.open_shops));
        map.getUiSettings().setAllGesturesEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            map.setOnMyLocationButtonClickListener(() -> {
                requestLocationUpdates();
                return false;
            });
        }

        // warsaw
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(52.237049, 21.017532), INITIAL_ZOOM));
    }

    private void displayLocationDialog() {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> locationSettings = SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        locationSettings.setResultCallback(result -> {
            Status status = result.getStatus();
            switch (status.getStatusCode()) {
                case SUCCESS:
                    requestLocationUpdates();
                    break;
                case RESOLUTION_REQUIRED:
                    try {
                        // show the location dialog and check the result in onActivityResult()
                        status.startResolutionForResult(getActivity(), LOCATION_PERMISSION);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                    break;
                case SETTINGS_CHANGE_UNAVAILABLE:
                    showErrorToast();
                    break;
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        FragmentActivity activity = getActivity();
        if (googleApiClient.isConnected() && activity != null &&
            ActivityCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationCallback, null);
            refreshMapData();
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    private void initLocationClients() {
        googleApiClient = new GoogleApiClient.Builder(getActivity()).addApi(API)
                                                                    .addApi(Places.GEO_DATA_API)
                                                                    .build();
        googleApiClient.connect();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                requestLocationUpdates();
            }
        };
    }

    private void initAutoComplete() {
        autocompleteFragment = (PlaceAutocompleteFragment) getActivity().getFragmentManager()
                                                                        .findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setText("Żabka");
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                requestLocationUpdates();
                googleMap.addMarker(
                        new MarkerOptions().title(place.getName() + ": " + place.getAddress()).position(place.getLatLng()));
                googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(new LatLng(place.getLatLng().latitude, place.getLatLng().longitude),
                                                          DEFAULT_ZOOM));
            }

            @Override
            public void onError(Status status) { }
        });
    }

    @SuppressLint("MissingPermission")
    private void refreshMapData() {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (lastLocation != null) {
            double latitude = lastLocation.getLatitude();
            double longitude = lastLocation.getLongitude();
            LatLngBounds latLngBounds = new LatLngBounds(new LatLng(latitude, longitude), new LatLng(latitude, longitude));
            autocompleteFragment.setBoundsBias(latLngBounds);

            if (!areLocalShopsDisplayed) {
                Task<AutocompletePredictionBufferResponse> results = Places.getGeoDataClient(getActivity(), null)
                                                                           .getAutocompletePredictions("Żabka", latLngBounds, null);
                results.addOnSuccessListener(autocompletePredictions -> {
                    for (AutocompletePrediction autocompletePrediction : autocompletePredictions) {
                        Places.GeoDataApi.getPlaceById(googleApiClient, autocompletePrediction.getPlaceId())
                                         .setResultCallback(places -> {
                                             if (places.getStatus().isSuccess()) {
                                                 for (Place place : places) {
                                                     googleMap.addMarker(
                                                             new MarkerOptions().title(place.getName() + ": " + place.getAddress())
                                                                                .position(place.getLatLng()));
                                                     areLocalShopsDisplayed = true;
                                                 }
                                             }
                                             places.release();
                                         });
                    }
                });
            }
        }
    }

    private void showErrorToast() {
        Toast.makeText(getActivity(), "Location permissions are required", Toast.LENGTH_LONG).show();
    }
}

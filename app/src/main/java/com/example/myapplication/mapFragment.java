package com.example.myapplication;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.app.SearchManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
public class mapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static final String ARG_SECTION_NUMBER = "section_number1";
    public static final int IMAGE_PICK_CODE= 1000;
    LinearLayout marker;
    ImageView place_image;
    GoogleMap map;
    SupportMapFragment mapFragment;
    SearchView searchView;
    String name;
    LatLng position;
    Uri selectedImageUri;
    Bitmap bitmap;
    public mapFragment() {
        // Required empty public constructor
    }
    public static mapFragment newInstance(int index) {
        mapFragment fragment = new mapFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this::onMapReady);
        searchView = view.findViewById(R.id.sv_location);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                List<Address> addressList = null;
                if(location != null || !location.equals("")){
                    Geocoder geocoder = new Geocoder(getActivity());
                    try{
                        addressList = geocoder.getFromLocationName(location,1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    map.addMarker(new MarkerOptions().position(latLng));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mapFragment.getMapAsync(this);
        return view;
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        //map.setOnInfoWindowLongClickListener(this::onMarkerClick);
        map.setOnMarkerClickListener(this::onMarkerClick);
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        addPlace();
        //Toast.makeText(getContext(), marker.getTitle()+"\n"+marker.getPosition(), Toast.LENGTH_SHORT).show();
        name = marker.getTitle();
        position = marker.getPosition();
        return false;
    }
    private void addPlace() {
        LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        marker = (LinearLayout) vi.inflate(R.layout.dialog, null);
        final EditText place_name = (EditText)marker.findViewById(R.id.place_name);
        final Button place_gallery = (Button) marker.findViewById(R.id.place_gallery);
        place_image = (ImageView) marker.findViewById(R.id.place_image);
        final EditText place_memo = (EditText) marker.findViewById(R.id.place_memo);
        place_gallery.setBackgroundColor(Color.WHITE);
        place_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                intent.setAction(intent.ACTION_GET_CONTENT);
                startActivityForResult(intent.createChooser(intent, "Select Picture"), IMAGE_PICK_CODE);
            }
        });
        new AlertDialog.Builder(getActivity()).setTitle("PLACE").setView(marker).
                setPositiveButton("추가", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override public void onClick(DialogInterface dialog, int which)
                    {
//                        map.addMarker(new MarkerOptions()
//                                .position(new LatLng(position.latitude, position.longitude))
//                                .title(place_name.getText().toString())
//                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
//                                .snippet(place_memo.getText().toString())
//                                //.draggable(true)
//                        );
                        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                            // Use default InfoWindow frame
                            @Override
                            public View getInfoWindow(Marker arg0) {
                                return null;
                            }
                            // Defines the contents of the InfoWindow
                            @Override
                            public View getInfoContents(Marker arg0) {
                                View v = null;
                                try {
                                    // Getting view from the layout file info_window_layout
                                    v = getLayoutInflater().inflate(R.layout.map_marker_info_window, null);
                                    // Getting reference to the TextView to set latitude
                                    TextView info_name = (TextView) v.findViewById(R.id.info_name);
                                    info_name.setText(place_name.getText().toString());
                                    ImageView info_image = (ImageView) v.findViewById(R.id.info_image);
                                    info_image.setImageBitmap(bitmap);
                                    TextView info_memo = (TextView) v.findViewById(R.id.info_memo);
                                    info_memo.setText(place_memo.getText().toString());
                                } catch (Exception ev) {
                                    System.out.print(ev.getMessage());
                                }
                                return v;
                            }
                        });
                    }
                }).show();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_PICK_CODE){
            selectedImageUri = data.getData();
            // Get the path from the Uri
            final String path = getPathFromURI(selectedImageUri);
            if (path != null) {
                File f = new File(path);
                selectedImageUri = Uri.fromFile(f);
            }
            // Set the image in ImageView
            place_image.setImageURI(selectedImageUri);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContext().getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }
}
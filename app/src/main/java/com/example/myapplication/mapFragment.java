package com.example.myapplication;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentManager;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class mapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static final String ARG_SECTION_NUMBER = "section_number1";
    public static final int IMAGE_PICK_CODE= 1000;
    public static ArrayList<Maps> mapList;
    LinearLayout marker;
    ImageView place_image;
    GoogleMap map;
    SupportMapFragment mapFragment;
    SearchView searchView;
    String name;
    LatLng position;
    Uri selectedImageUri;
    Bitmap bitmap;

    String temp=null;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mapList = new ArrayList<Maps>();
        new JSONTask6().execute("http://192.249.18.166:3000/downdiary");
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
                    Marker marker = map.addMarker(new MarkerOptions().position(latLng));
                    marker.setTitle(location);
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
        map.setOnInfoWindowLongClickListener(this::onMarkerClick);
        /*map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.imagefrommap, null);
                AlertDialog.Builder aDialog = new AlertDialog.Builder(getContext());
                aDialog.setTitle("상세 정보");
                aDialog.setView(layout);
                aDialog.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog ad = aDialog.create();
                ad.show();
                return true;
            }
        });*/
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.imagefrommap, null);
                AlertDialog.Builder aDialog = new AlertDialog.Builder(getContext());
                aDialog.setTitle("Download");
                aDialog.setView(layout);
                aDialog.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                aDialog.setNegativeButton("저장", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        boolean exist=false;
                        String encodedString=null;
                        for (int i=0; i<mapList.size(); i++) {
                            if (mapList.get(i).getMarker_name().equalsIgnoreCase(marker.getTitle())) {
                                exist = true;
                                encodedString = mapList.get(i).getBm();
                            }
                        }
                        if (exist) {
                            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
                            Bitmap bm = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            bm.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                            String path1 = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bm, "Title", null);
                            ((MainActivity) getActivity()).refresh();
                        }
                    }
                });
                AlertDialog ad = aDialog.create();
                ad.show();
            }
        });
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
                    //info_name.setText(place_name.getText().toString());
                    ImageView info_image = (ImageView) v.findViewById(R.id.info_image);
                    //info_image.setImageBitmap(bitmap);
                    TextView info_memo = (TextView) v.findViewById(R.id.info_memo);
                    //info_memo.setText(place_memo.getText().toString());
                    //System.out.println(mapList.size());
                    boolean exist=false;
                    for (int i=0; i<mapList.size(); i++) {
                        //System.out.println(mapList.get(i).getMarker_name());
                        //System.out.println(arg0.getTitle());
                        if (mapList.get(i).getMarker_name().equalsIgnoreCase(arg0.getTitle())) {
                            exist = true;
                            info_name.setText(mapList.get(i).getName());
                            byte[] encodeByte = Base64.decode(mapList.get(i).getBm(), Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                            info_image.setImageBitmap(bitmap);
                            System.out.println(mapList.get(i).getMemo());
                            info_memo.setText(mapList.get(i).getMemo());
                        }
                    }
                    if (!exist) {
                        return null;
                    }
                } catch (Exception ev) {
                    System.out.print(ev.getMessage());
                }
                return v;
            }
        });
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        System.out.println("hello");
        name = marker.getTitle();
        position = marker.getPosition();
        addPlace();
        //Toast.makeText(getContext(), marker.getTitle()+"\n"+marker.getPosition(), Toast.LENGTH_SHORT).show();
        return false;
    }
    public void addPlace() {
        LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        marker = (LinearLayout) vi.inflate(R.layout.dialog, null);
        final EditText place_name = (EditText)marker.findViewById(R.id.place_name);
        final Button place_gallery = (Button) marker.findViewById(R.id.place_gallery);
        place_image = (ImageView) marker.findViewById(R.id.place_image);
        final EditText place_memo = (EditText) marker.findViewById(R.id.place_memo);
        place_gallery.setBackgroundColor(Color.WHITE);

        /* add or modify item in imageList */

        place_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                intent.setAction(intent.ACTION_GET_CONTENT);
                startActivityForResult(intent.createChooser(intent, "Select Picture"), IMAGE_PICK_CODE);
            }
        });
        AlertDialog.Builder aDialog = new AlertDialog.Builder(getContext());
        aDialog.setTitle("PLACE").setView(marker);
        aDialog.setPositiveButton("추가", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean mod = false;
                for (int i=0; i<mapList.size(); i++) {
                    if (mapList.get(i).getMarker_name().equalsIgnoreCase(name)) {
                        mod = true;
                        mapList.get(i).setBm(temp);
                        mapList.get(i).setMemo(place_memo.getText().toString());
                        mapList.get(i).setName(place_name.getText().toString());
                    }
                }
                if (!mod) {
                    //System.out.println(name);
                    Maps m = new Maps(name, position);
                    m.setBm(temp);
                    m.setMemo(place_memo.getText().toString());
                    m.setName(place_name.getText().toString());
                    mapList.add(m);
                    //System.out.println(mapList.get(0).getMarker_name());
                }
                //System.out.println(mapList.size());

                new JSONTask5().execute("http://192.249.18.166:3000/updiary");
            }
        });
        AlertDialog ad = aDialog.create();
        ad.show();
        /*new AlertDialog.Builder(getActivity()).setTitle("PLACE").setView(marker).
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
                        //System.out.println(which);
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
                }).show();*/
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
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] bytes = baos.toByteArray();
                temp = Base64.encodeToString(bytes, Base64.DEFAULT);
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

    public class JSONTask5 extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                JSONObject jsonObject = new JSONObject();
                JSONArray newArray0 = new JSONArray();
                try {
                    for (int i = 0; i < mapList.size(); i++) {
                        JSONObject jsonObject1 = new JSONObject();
                        try {
                            jsonObject1.put("marker name", mapList.get(i).getMarker_name());
                            jsonObject1.put("latlng", mapList.get(i).getLatLng());
                            jsonObject1.put("bm", mapList.get(i).getBm());
                            jsonObject1.put("name", mapList.get(i).getName());
                            jsonObject1.put("memo", mapList.get(i).getMemo());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        newArray0.put(jsonObject1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    jsonObject.put("Diary", newArray0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                HttpURLConnection con = null;
                BufferedReader reader = null;
                try{
                    URL url = new URL(urls[0]);
                    //연결을 함
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");//POST방식으로 보냄
                    con.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                    con.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
                    con.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
                    con.connect();
                    //서버로 보내기위해서 스트림 만듬
                    OutputStream outStream = con.getOutputStream();
                    //버퍼를 생성하고 넣음
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    writer.write(jsonObject.toString());
                    writer.flush();
                    writer.close();//버퍼를 받아줌
                    //서버로 부터 데이터를 받음
                    InputStream stream = con.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuffer buffer = new StringBuffer();
                    String line = null;
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }
                    return buffer.toString();//서버로 부터 받은 값을 리턴해줌 아마 OK!!가 들어올것임
                } catch (MalformedURLException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(con != null){
                        con.disconnect();
                    }
                    try {
                        if(reader != null){
                            reader.close();//버퍼를 닫아줌
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    public class JSONTask6 extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                HttpURLConnection con = null;
                BufferedReader reader = null;
                try{
                    URL url = new URL(urls[0]);
                    //연결을 함
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");//POST방식으로 보냄
                    con.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                    con.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
                    con.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
                    con.connect();

                    //서버로 보내기위해서 스트림 만듬
                    OutputStream outStream = con.getOutputStream();
                    //버퍼를 생성하고 넣음
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    //System.out.println(temp.length());
                    writer.write("ok");
                    writer.flush();
                    writer.close();//버퍼를 받아줌


                    InputStream stream = con.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuffer buffer = new StringBuffer();
                    String line = null;
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }
                    return buffer.toString();//서버로 부터 받은 값을 리턴해줌 아마 OK!!가 들어올것임
                } catch (MalformedURLException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(con != null){
                        con.disconnect();
                    }
                    try {
                        if(reader != null){
                            reader.close();//버퍼를 닫아줌
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            //System.out.println(result);
            if (result == null){
                Toast.makeText(getContext(), "데이터가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
            else {
                //System.out.println("download: " + result);
                JSONParser parser = new JSONParser();
                mapList.clear();
                try {
                    org.json.simple.JSONObject jsonObj = (org.json.simple.JSONObject) parser.parse(result);
                    org.json.simple.JSONArray contactArray = (org.json.simple.JSONArray) jsonObj.get("Diary");
                    for (int i = 0; i < contactArray.size(); i++) {
                        jsonObj = (org.json.simple.JSONObject) contactArray.get(i);
                        //System.out.println(jsonObj.getString("name")+ jsonObj.getString("number"));
                        Maps m = new Maps((String)jsonObj.get("marker name"), null);
                        m.setName((String) jsonObj.get("name"));
                        m.setBm((String) jsonObj.get("bm"));
                        m.setMemo((String) jsonObj.get("memo"));
                        System.out.println((String) jsonObj.get("memo"));
                        mapList.add(m);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            super.onPostExecute(result);
        }
    }
}
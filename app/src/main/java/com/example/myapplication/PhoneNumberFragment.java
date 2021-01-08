package com.example.myapplication;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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

public class PhoneNumberFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final int REQUEST_RUNTIME_PERMISSION = 123;
    String[] permissons = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS};

    public static ArrayList<Phonenumber> phonenumbers;
    public static ArrayList<Phonenumber> server_phonenumbers;
    public static ArrayList<Phonenumber> local_phonenumbers;

    PhonenumberAdaptor adapter;

    FloatingActionButton fab;
    FloatingActionButton itemFab;
    FloatingActionButton deleteFab;
    boolean isOpen=false;
    public static PhoneNumberFragment newInstance(int index) {
        PhoneNumberFragment fragment = new PhoneNumberFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        phonenumbers = new ArrayList<>();
        local_phonenumbers = new ArrayList<>();
        server_phonenumbers = new ArrayList<>();
        View view = inflater.inflate(R.layout.contact_fragment, container, false);

        ListView listView = (ListView) view.findViewById(R.id.listview1);
        adapter = new PhonenumberAdaptor(getContext(), phonenumbers);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout hidden = (LinearLayout) view.findViewById(R.id.hidden);
                if (!hidden.isShown()) {
                    hidden.setVisibility(view.VISIBLE);
                }else {
                    hidden.setVisibility(view.GONE);
                }

            }
        });

        Button btn1 = (Button)view.findViewById(R.id.u_server);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new JSONTask1().execute("http://192.249.18.166:3000/post");//AsyncTask 시작시킴
            }
        });

        Button btn2 = (Button)view.findViewById(R.id.l_server);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new JSONTask2().execute("http://192.249.18.166:3000/users");//AsyncTask 시작시킴
            }
        });

        Button btn3 = (Button)view.findViewById(R.id.l_local);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                read_contact();
            }
        });

        while (!CheckPermission(getContext(), permissons[0]) || !CheckPermission(getContext(), permissons[1]))
            RequestPermission(getActivity(), permissons, REQUEST_RUNTIME_PERMISSION);

        fab = (FloatingActionButton) view.findViewById(R.id.mainFab);
        itemFab = (FloatingActionButton) view.findViewById(R.id.insertfab);
        deleteFab = (FloatingActionButton) view.findViewById(R.id.deletefab);
        fab.setOnClickListener(this::onClick);
        itemFab.setOnClickListener(this::onClick);
        deleteFab.setOnClickListener(this::onClick);

        return view;
    }

    public void onClick(View v) {
        switch (v.getId()){
            case R.id.mainFab:
                if(!isOpen){
                    ObjectAnimator.ofFloat(itemFab, "translationY", -400f).start();
                    ObjectAnimator.ofFloat(deleteFab, "translationY", -200f).start();
                    isOpen = true;
                }
                else{
                    ObjectAnimator.ofFloat(itemFab, "translationY", 0f).start();
                    ObjectAnimator.ofFloat(deleteFab, "translationY", 0f).start();
                    isOpen = false;
                }
                break;
            case R.id.deletefab:
                new JSONTask1().execute("http://192.249.18.166:3000/post");
            case R.id.insertfab:
                new JSONTask2().execute("http://192.249.18.166:3000/users");
                break;
        }
    }

    /* put contact server */
    public class JSONTask1 extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            for(int i=0; i<phonenumbers.size(); i++) {
                phonenumbers.get(i).setAtServer(true);
            }
            try {
                JSONObject jsonObject = new JSONObject();
                JSONArray newArray0 = new JSONArray();
                try {
                    for (int i = 0; i < phonenumbers.size(); i++) {
                        JSONObject jsonObject1 = new JSONObject();
                        try {
                            jsonObject1.put("name", phonenumbers.get(i).getName());
                            jsonObject1.put("number", phonenumbers.get(i).getNumber());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        newArray0.put(jsonObject1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    jsonObject.put("Contacts", newArray0);
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

    /* get contacts from server */
    public class JSONTask2 extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                HttpURLConnection con = null;
                BufferedReader reader = null;
                try{
                    URL url = new URL(urls[0]);
                    //연결을 함
                    con = (HttpURLConnection) url.openConnection();
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
            JSONParser parser = new JSONParser();
            try {
                org.json.simple.JSONObject jsonObj = (org.json.simple.JSONObject)parser.parse(result);
                org.json.simple.JSONArray contactArray = (org.json.simple.JSONArray) jsonObj.get("Contacts");
                for (int i = 0; i < contactArray.size(); i++) {
                    jsonObj =  (org.json.simple.JSONObject)contactArray.get(i);
                    //System.out.println(jsonObj.getString("name")+ jsonObj.getString("number"));
                    server_phonenumbers.add(new Phonenumber((String)jsonObj.get("name"), (String)jsonObj.get("number"), false, true));
                    adapter.notifyDataSetChanged();
                }
                boolean dup=false;
                for (int i=0; i<server_phonenumbers.size(); i++) {
                    dup=false;
                    for (int j=0;  j<phonenumbers.size(); j++) {
                        if (server_phonenumbers.get(i).getName().equalsIgnoreCase(phonenumbers.get(j).getName()) && server_phonenumbers.get(i).getNumber().equalsIgnoreCase(phonenumbers.get(j).getNumber())) {
                            dup=true;
                            phonenumbers.get(j).setAtServer(true);
                        }
                    }
                    if (!dup) {
                        phonenumbers.add(new Phonenumber(server_phonenumbers.get(i).getName(), server_phonenumbers.get(i).getNumber(), false, true));
                    }
                }
                adapter.notifyDataSetChanged();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            super.onPostExecute(result);
        }
    }

    /* permission for access local contact */
    public boolean CheckPermission(Context context, String Permission) {
        if (ContextCompat.checkSelfPermission(context,
                Permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
    public void RequestPermission(Activity thisActivity, String[] Permission, int Code) {
        if (ContextCompat.checkSelfPermission(thisActivity,
                Permission[0])
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                    Permission[0])) {
            } else {
                ActivityCompat.requestPermissions(thisActivity, Permission,
                        Code);
            }
        }
    }

    /* get contacts from local */
    private void read_contact() {

        ContentResolver cr = getActivity().getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phoneNo = phoneNo.replaceAll("-", "");
                        local_phonenumbers.add(new Phonenumber(name, phoneNo, true, false));
                        adapter.notifyDataSetChanged();
                    }
                    boolean dup=false;
                    for (int i=0; i<local_phonenumbers.size(); i++) {
                        dup=false;
                        for (int j=0;  j<phonenumbers.size(); j++) {
                            if (local_phonenumbers.get(i).getName().equalsIgnoreCase(phonenumbers.get(j).getName()) && local_phonenumbers.get(i).getNumber().equalsIgnoreCase(phonenumbers.get(j).getNumber())) {
                                dup=true;
                                phonenumbers.get(j).setAtLocal(true);
                            }
                        }
                        if (!dup) {
                            phonenumbers.add(new Phonenumber(local_phonenumbers.get(i).getName(), local_phonenumbers.get(i).getNumber(), true, false));
                        }
                    }
                    adapter.notifyDataSetChanged();
                    pCur.close();
                }
            }
        }
        if(cur!=null){
            cur.close();
        }
        /*JSONObject jsonObject50 = new JSONObject();
        JSONArray newArray0 = new JSONArray();
        try {
            for (int i = 0; i < phonenumbers.size(); i++) {
                JSONObject jsonObject1 = new JSONObject();
                try {
                    jsonObject1.put("name", phonenumbers.get(i).getName());
                    jsonObject1.put("number", phonenumbers.get(i).getNumber());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                newArray0.put(jsonObject1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            jsonObject50.put("Contacts", newArray0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String filename0 = "Phonenumbers.json";
        try {
            try (FileOutputStream fos = getContext().openFileOutput(filename0, Context.MODE_PRIVATE)) {
                fos.write(jsonObject50.toString().getBytes());
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}

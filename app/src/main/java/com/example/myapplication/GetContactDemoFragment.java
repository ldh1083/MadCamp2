package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONObject;

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

public class GetContactDemoFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    public static ArrayList<Phonenumber> phonenumbers = new ArrayList<>();

    PhonenumberAdaptor adapter;
    public static GetContactDemoFragment newInstance(int index) {
        GetContactDemoFragment fragment = new GetContactDemoFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contact_fragment, container, false);
        Button btn = (Button)view.findViewById(R.id.l_server);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new JSONTask().execute("http://192.249.18.166:3000/users");//AsyncTask 시작시킴
            }
        });

        ListView listView = (ListView) view.findViewById(R.id.listview1);
        adapter = new PhonenumberAdaptor(getContext(), phonenumbers);
        listView.setAdapter(adapter);

        return view;
    }

    public class JSONTask extends AsyncTask<String, String, String> {

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
                JSONObject jsonObj = (JSONObject)parser.parse(result);
                System.out.println(jsonObj);
                JSONArray contactArray = (JSONArray) jsonObj.get("Contacts");
                for (int i = 0; i < contactArray.size(); i++) {
                    jsonObj =  (JSONObject)contactArray.get(i);
                    //System.out.println(jsonObj.getString("name")+ jsonObj.getString("number"));
                    phonenumbers.add(new Phonenumber((String)jsonObj.get("name"), (String)jsonObj.get("number")));
                    adapter.notifyDataSetChanged();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            super.onPostExecute(result);
        }
    }
}

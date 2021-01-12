package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class galleryFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int PICK_IMAGES = 2;
    public static final int STORAGE_PERMISSION = 100;
    String temp=null;
    String imageFileName;
    public static ArrayList<ImageModel> imageList;
    public static ArrayList<String> selectedImageList;
    RecyclerView imageRecyclerView, selectedImageRecyclerView;
    int[] resImg = {R.drawable.ic_camera_white_30dp};
    String[] title = {"Camera"};
    String mCurrentPhotoPath;
    // SelectedImageAdapter selectedImageAdapter;
    public static ImageAdapter imageAdapter;
    public String[] projection = {MediaStore.MediaColumns.DATA};
    File image;
    Button delete;
    Button done;
    Button get;
    Uri uri;
    public static galleryFragment fragment;
    public static galleryFragment newInstance(int index) {
        fragment = new galleryFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
            init(view);
            getAllImages();
            setImageList();
            setSelectedImageList();
        return view;
    }

    @Override
    public void onResume() {
        getAllImages();
        setImageList();
        setSelectedImageList();
        imageAdapter.notifyDataSetChanged();
        super.onResume();
    }

    public void init(View view) {
        imageRecyclerView = view.findViewById(R.id.recycler_view);
        selectedImageRecyclerView = view.findViewById(R.id.selected_recycler_view);
        done = view.findViewById(R.id.done);
        selectedImageList = new ArrayList<>();
        imageList = new ArrayList<>();

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < selectedImageList.size(); i++) {
                    Bitmap bitmap = BitmapFactory.decodeFile(selectedImageList.get(i));
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] buffer=baos.toByteArray();
                    temp = Base64.encodeToString(buffer, Base64.DEFAULT);
                    //System.out.println(temp);
                    if (MainActivity.islogin)
                        new galleryFragment.JSONTask3().execute("http://192.249.18.166:3000/upimg");
                    else
                        Toast.makeText(getContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                    //System.out.println(selectedImageList.get(i));
                }
            }
        });

        get = view.findViewById(R.id.get);
        get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.islogin)
                    new galleryFragment.JSONTask4().execute("http://192.249.18.166:3000/downimg");
                else
                    Toast.makeText(getContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        });

        delete = view.findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i=0; i<selectedImageList.size(); i++) {
                    System.out.println(selectedImageList.get(i));
                    File d_file = new File(selectedImageList.get(i));
                    d_file.delete();
                    if (!d_file.exists()) {
                        Cursor cursor = getContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
                        while (cursor.moveToNext()) {
                            String absolutePathOfImage = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                            //System.out.println(absolutePathOfImage);
                            if (absolutePathOfImage.equalsIgnoreCase(selectedImageList.get(i))) {
                                //System.out.println("hello");
                                //long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                                int id = cursor.getInt(0);
                                Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                                getContext().getContentResolver().delete(deleteUri, null, null);
                            }
                        }
                        cursor.close();
                    }
                }
                getAllImages();
                setImageList();
                setSelectedImageList();
                imageAdapter.notifyDataSetChanged();
            }
        });
    }

    public void setImageList() {
        imageRecyclerView.setLayoutManager(new GridLayoutManager(getContext().getApplicationContext(), 4));
        imageAdapter = new ImageAdapter(getContext().getApplicationContext(), imageList);
        imageRecyclerView.setAdapter(imageAdapter);

        imageAdapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                if (position == 0) {
                    takePicture();
//                } else if (position == 1) {
//                    getPickImageIntent();
                } else {
                    try {
                        if (!imageList.get(position).isSelected) {
                            selectImage(position);
                        } else {
                            unSelectImage(position);
                        }
                    } catch (ArrayIndexOutOfBoundsException ed) {
                        ed.printStackTrace();
                    }
                }
            }
        });
        setImagePickerList();
    }

    public void setSelectedImageList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        selectedImageRecyclerView.setLayoutManager(layoutManager);
        /*FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();*/
        //selectedImageAdapter = new SelectedImageAdapter(getActivity(), selectedImageList);
        //selectedImageRecyclerView.setAdapter(selectedImageAdapter);
    }

    // Add Camera and Folder in ArrayList
    public void setImagePickerList() {
        for (int i = 0; i < resImg.length; i++) {
            ImageModel imageModel = new ImageModel();
            imageModel.setResImg(resImg[i]);
            imageModel.setTitle(title[i]);
            imageList.add(i, imageModel);
        }
        imageAdapter.notifyDataSetChanged();
    }

    // get all images from external storage
    public void getAllImages() {
        selectedImageList.clear();
        imageList.clear();
        Cursor cursor = getContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
        while (cursor.moveToNext()) {
            String absolutePathOfImage = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
            ImageModel ImageModel = new ImageModel();
            ImageModel.setImage(absolutePathOfImage);
            imageList.add(ImageModel);
        }
        cursor.close();
        //imageAdapter.notifyDataSetChanged();
    }

    // start the image capture Intent
    public void takePicture() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Continue only if the File was successfully created;
        File photoFile = createImageFile();
        if (photoFile != null) {
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            getContext().sendBroadcast(new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(photoFile)));
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void getPickImageIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGES);

    }

    // Add image in SelectedArrayList
    public void selectImage(int position) {
        System.out.println(imageList.get(position).getImage());
        // Check before add new item in ArrayList;
        if (!selectedImageList.contains(imageList.get(position).getImage())) {
            imageList.get(position).setSelected(true);
            selectedImageList.add(0, imageList.get(position).getImage());
            //selectedImageAdapter.notifyDataSetChanged();
            imageAdapter.notifyDataSetChanged();

            String fileName= imageList.get(position).getImage().toString();
            Uri fileUri = Uri.parse("file:"+fileName);
            String filePath = fileUri.getPath();

            /*Cursor c = getContext().getContentResolver().query( MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, "_data = '" + filePath + "'", null, null );
            c.moveToNext();

            int id = c.getInt( c.getColumnIndex( "_id" ) );
            uri = ContentUris.withAppendedId( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id );
            //String Image_uri = uri.toString();
            imageAdapter.uri = uri.toString();*/

        }
    }

    public interface senduri{
        void onItemSelected(String key, String value);
    }

    // Remove image from selectedImageList
    public void unSelectImage(int position) {
        for (int i = 0; i < selectedImageList.size(); i++) {
            if (imageList.get(position).getImage() != null) {
                if (selectedImageList.get(i).equals(imageList.get(position).getImage())) {
                    imageList.get(position).setSelected(false);
                    selectedImageList.remove(i);
                    //selectedImageAdapter.notifyDataSetChanged();
                    imageAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    public File createImageFile() {
        // Create an image file name
        String dateTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "IMG_" + dateTime + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM+"/Camera");
        try {
            image = File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        //System.out.println("11" +mCurrentPhotoPath);
        return image;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            try {
                MediaStore.Images.Media.insertImage(getContext().getContentResolver(), mCurrentPhotoPath, imageFileName, null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (mCurrentPhotoPath != null) {
                    //System.out.println(mCurrentPhotoPath);
                    addImage(mCurrentPhotoPath);
                }
            } else if (requestCode == PICK_IMAGES) {
                if (data.getClipData() != null) {
                    ClipData mClipData = data.getClipData();
                    for (int i = 0; i < mClipData.getItemCount(); i++) {
                        ClipData.Item item = mClipData.getItemAt(i);
                        Uri uri = item.getUri();
                        getImageFilePath(uri);
                    }
                } else if (data.getData() != null) {
                    Uri uri = data.getData();
                    getImageFilePath(uri);
                }
            }
            else {
                File file = new File(mCurrentPhotoPath);
                file.delete();
                if (!file.exists()) {
                    Cursor cursor = getContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
                    while (cursor.moveToNext()) {
                        String absolutePathOfImage = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                        //System.out.println(absolutePathOfImage);
                        if (absolutePathOfImage.equalsIgnoreCase(mCurrentPhotoPath)) {
                            System.out.println("hello");
                            //long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                            int id = cursor.getInt(0);
                            Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                            getContext().getContentResolver().delete(deleteUri, null, null);
                        }
                    }
                    cursor.close();
                }
            }
        }
    }

    // Get image file path
    public void getImageFilePath(Uri uri) {
        Cursor cursor = getContext().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String absolutePathOfImage = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                if (absolutePathOfImage != null) {
                    checkImage(absolutePathOfImage);
                } else {
                    checkImage(String.valueOf(uri));
                }
            }
        }
    }

    // add image in selectedImageList and imageList
    public void checkImage(String filePath) {
        // Check before adding a new image to ArrayList to avoid duplicate images
        if (!selectedImageList.contains(filePath)) {
            for (int pos = 0; pos < imageList.size(); pos++) {
                if (imageList.get(pos).getImage() != null) {
                    if (imageList.get(pos).getImage().equalsIgnoreCase(filePath)) {
                        imageList.remove(pos);
                    }
                }
            }
            addImage(filePath);
        }
    }

    public void addImage(String filePath) {
        ImageModel imageModel = new ImageModel();
        imageModel.setImage(filePath.substring(6));
        imageModel.setSelected(true);
        imageList.add(imageList.size(), imageModel);
        //System.out.println(filePath.substring(6));
        selectedImageList.add(0, filePath.substring(5));
        //selectedImageAdapter.notifyDataSetChanged();
        imageAdapter.notifyDataSetChanged();
    }

    public boolean isStoragePermissionGranted() {
        int ACCESS_EXTERNAL_STORAGE = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if ((ACCESS_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
            return false;
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //init();
            getAllImages();
            setImageList();
            setSelectedImageList();
        }
    }

    public class JSONTask3 extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                HttpURLConnection con = null;
                BufferedReader reader = null;
                DataOutputStream dos = null;

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
                    writer.write(MainActivity.userid+temp);
                    writer.flush();
                    writer.close();//버퍼를 받아줌
                    /*bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                    outStream.flush();
                    outStream.close();*/

                    /*dos = new DataOutputStream(con.getOutputStream());
                    dos.write(buffer, 0, buffer.length);
                    dos.flush();
                    dos.close();*/

                    //서버로 부터 데이터를 받음
                    InputStream stream = con.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuffer buffer1 = new StringBuffer();
                    String line = null;
                    int size=0;
                    while((line = reader.readLine()) != null){
                        buffer1.append(line);
                    }
                    return buffer1.toString();//서버로 부터 받은 값을 리턴해줌 아마 OK!!가 들어올것임
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

    public class JSONTask4 extends AsyncTask<String, String, String> {
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
                    writer.write(MainActivity.userid);
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
            if (result.length() == 2){
                Toast.makeText(getContext(), "데이터가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
            else {
                System.out.println("{\"image\":" + result + "}");
                result = "{\"image\":" + result + "}";
                JSONParser parser = new JSONParser();
                ArrayList<String> arr = new ArrayList<>();
                try {
                    org.json.simple.JSONObject jsonObj = (org.json.simple.JSONObject) parser.parse(result);
                    org.json.simple.JSONArray contactArray = (org.json.simple.JSONArray) jsonObj.get("image");
                    for (int i = 0; i < contactArray.size(); i++) {
                        jsonObj = (org.json.simple.JSONObject) contactArray.get(i);
                        //System.out.println(jsonObj.getString("name")+ jsonObj.getString("number"));
                        arr.add((String) jsonObj.get("body"));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //System.out.println(arr.size());
                for (int i = 0; i < arr.size(); i++) {
                    byte[] encodeByte = Base64.decode(arr.get(i), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                    //System.out.println(Environment.getExternalStorageDirectory().toString());


                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    String path1 = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmap, "Title", null);
                    getAllImages();
                    setImageList();
                    setSelectedImageList();
                    imageAdapter.notifyDataSetChanged();
                }
            }
            super.onPostExecute(result);
        }
    }

    public void refresh () {
        getAllImages();
        setImageList();
        setSelectedImageList();
        imageAdapter.notifyDataSetChanged();
    }
}

package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    public static boolean islogin=false;
    public static String userid = null;
    CallbackManager callbackManager;
    CustomViewPager viewPager;
    TabsAdaptor sectionsPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sectionsPagerAdapter = new TabsAdaptor(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setPagingEnabled(true);

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("CONTACT"));
        tabs.addTab(tabs.newTab().setText("GALLERY"));
        tabs.addTab(tabs.newTab().setText("NONE"));
        tabs.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
        tabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        callbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");

        // Callback registration
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
                System.out.println(accessToken.getUserId());
                userid = accessToken.getUserId();
                GraphRequest req = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        //System.out.println(response.toString());
                        try {
                            Toast.makeText(getApplicationContext(),"안녕하세요 "+object.getString("name")+"님", Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                req.executeAsync();
                System.out.println("hi");
            }

            @Override
            public void onCancel() {
                System.out.println("cancel");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {

            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
                AccessToken.setCurrentAccessToken(newAccessToken);
                AccessToken accessToken2 = AccessToken.getCurrentAccessToken();
                boolean isLoggedIn1 = accessToken2 != null && !accessToken2.isExpired();
                if (isLoggedIn1) {
                    islogin = true;
                    userid = accessToken2.getUserId();
                    GraphRequest req = GraphRequest.newMeRequest(accessToken2, new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            try {
                                Toast.makeText(getApplicationContext(),"안녕하세요 "+object.getString("name")+"님", Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    req.executeAsync();
                }
                else {
                    islogin = false;
                    userid = null;
                }
            }
        };
        AccessToken token;
        token = AccessToken.getCurrentAccessToken();

        if (token == null) {
            islogin=false;
        }
        else {
            GraphRequest req = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                    try {
                        Toast.makeText(getApplicationContext(),"안녕하세요 "+object.getString("name")+"님", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            req.executeAsync();
            islogin = true;
            userid = token.getUserId();
            System.out.println(islogin+userid);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void refresh() {
        ((galleryFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + sectionsPagerAdapter.getItemId(1))).refresh();
    }
}
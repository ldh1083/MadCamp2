package com.example.myapplication;

import com.example.myapplication.PhoneNumberFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.ArrayList;

public class PhonenumberAdaptor extends ArrayAdapter<Phonenumber> {
    private Context context;
    private ArrayList<Phonenumber> phonenumbers;

    public PhonenumberAdaptor(Context context, ArrayList<Phonenumber> phonenumbers) {
        super(context,-1,phonenumbers);
        this.context = context;
        this.phonenumbers = phonenumbers;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_contact, parent, false);

        TextView nameTextView = (TextView) view.findViewById(R.id.txt_name);
        nameTextView.setText(phonenumbers.get(position).getName());
        final TextView numberTextView = (TextView) view.findViewById(R.id.number);
        numberTextView.setText(phonenumbers.get(position).getNumber());
        LinearLayout hidden = (LinearLayout) view.findViewById(R.id.hidden);
        hidden.setVisibility(view.GONE);

        //Button close_hidden = (Button) view.findViewById(R.id.close_hidden);
        Button call = (Button) view.findViewById(R.id.call);
        Button msg = (Button) view.findViewById(R.id.msg);
        Button update = (Button) view.findViewById(R.id.update);
        call.setBackgroundColor(Color.WHITE);
        msg.setBackgroundColor(Color.WHITE);
        update.setBackgroundColor(Color.WHITE);
        //Button update = (Button) view.findViewById(R.id.update);



        call.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+phonenumbers.get(position).getNumber()));
                context.startActivity(call);
            }
        });

        msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent msg = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"+phonenumbers.get(position).getNumber()));
                context.startActivity(msg);
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return view;
    }

}

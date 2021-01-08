package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

        return view;
    }
}

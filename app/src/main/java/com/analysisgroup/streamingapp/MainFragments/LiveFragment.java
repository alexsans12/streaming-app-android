package com.analysisgroup.streamingapp.MainFragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.analysisgroup.streamingapp.LiveVideoBroadcaster.LiveVideoBroadcasterActivity;
import com.analysisgroup.streamingapp.R;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

public class LiveFragment extends Fragment {

    EditText liveStreamTitle, liveStreamDescription;
    Button readyLiveStream;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    DatabaseReference DATABASE_USERS;

    String keyUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_live, container, false);

        liveStreamTitle = view.findViewById(R.id.liveStreamTitle);
        liveStreamDescription = view.findViewById(R.id.liveStreamDescription);

        readyLiveStream = view.findViewById(R.id.readyLiveStream);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        DATABASE_USERS = FirebaseDatabase.getInstance().getReference("DATABASE USERS");

        DATABASE_USERS.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                keyUser = ""+snapshot.child("SecretKey").getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        readyLiveStream.setOnClickListener(click -> {
            updateStream(keyUser,liveStreamTitle.getText().toString(), liveStreamDescription.getText().toString());

            Intent intent = new Intent(getActivity(), LiveVideoBroadcasterActivity.class);
            intent.putExtra("keyLiveStream", keyUser);
            startActivity(intent);
        });

        return view;
    }

    private void updateStream(String key, String title, String description) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", title);
            jsonObject.put("description", description);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.PUT,"http://20.25.25.216:5080/LiveApp/rest/v2/broadcasts/"+key, jsonObject,
                response -> {
                    try {
                        JSONObject objectResponse = new JSONObject(response.toString());
                        Toast.makeText(getActivity(), objectResponse.getString("status"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                },
                error -> {
                    if(error instanceof ServerError)
                        Log.i("TAG", "SERVER ERROR");
                    if(error instanceof NoConnectionError)
                        Log.i("TAG", "There is no internet connection");
                    if(error instanceof NetworkError)
                        Log.i("TAG", "Network");
                }
        );
        Volley.newRequestQueue(requireActivity()).add(postRequest);
    }
}
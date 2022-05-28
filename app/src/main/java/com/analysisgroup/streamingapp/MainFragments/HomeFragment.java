package com.analysisgroup.streamingapp.MainFragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.analysisgroup.streamingapp.Models.LiveStream;
import com.analysisgroup.streamingapp.R;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    DatabaseReference DATABASE_USERS;
    RecyclerView recyclerView;
    LiveStreamAdapter adapter;
    private static final String URL_JSON = "http://20.25.25.216:5080/LiveApp/rest/v2/broadcasts/list/0/10";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DATABASE_USERS = FirebaseDatabase.getInstance().getReference("DATABASE USERS");

        extractLiveStreams(Volley.newRequestQueue(requireContext()));

        return view;
    }

    private void extractLiveStreams(RequestQueue queue) {
        @SuppressLint("NotifyDataSetChanged") JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL_JSON, null, response -> {

            List<LiveStream> liveStreamList = new ArrayList<>();

            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject jsonObject = response.getJSONObject(i);
                    LiveStream liveStream = new LiveStream();
                    liveStream.setName(jsonObject.getString("name"));
                    liveStream.setUsername(jsonObject.getString("username"));
                    liveStream.setStatus(jsonObject.getString("status"));
                    liveStream.setHlsViewerCount(jsonObject.getInt("hlsViewerCount"));
                    liveStream.setDescription(jsonObject.getString("description"));
                    liveStream.setStreamId(jsonObject.getString("streamId"));
                    liveStream.setStreamUrl(jsonObject.getString("streamUrl"));

                    DATABASE_USERS.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot user : snapshot.getChildren()) {
                                String key = ""+user.child("SecretKey").getValue();
                                if (key.equals(liveStream.getStreamId()))
                                    liveStream.setImage(""+user.child("Image").getValue());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    if (liveStream.getStatus().equals("broadcasting"))
                        liveStreamList.add(liveStream);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            adapter = new LiveStreamAdapter(getContext(), liveStreamList);
            adapter.notifyDataSetChanged();
            recyclerView.setAdapter(adapter);
        }, error -> Log.d("tag", "onErrorResponse: " + error.getMessage()));

        queue.add(jsonArrayRequest);
    }
}
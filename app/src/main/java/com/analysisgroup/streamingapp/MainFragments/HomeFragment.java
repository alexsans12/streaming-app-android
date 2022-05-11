package com.analysisgroup.streamingapp.MainFragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    RecyclerView recyclerView;
    LiveStreamAdapter adapter;
    private static final String URL_JSON = "http://20.124.2.54:5080/LiveApp/rest/v2/broadcasts/list/0/10";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        extractLiveStreams(Volley.newRequestQueue(getContext()));
        /*List<LiveStream> liveStreams = new ArrayList<>();
        LiveStream stream = new LiveStream();
        stream.setStatus("created");
        stream.setName("Jugando chill Minecraft");
        stream.setUsername("Joshgamer777");
        stream.setHlsViewerCount(1500);
        liveStreams.add(stream);

        LiveStream stream2 = new LiveStream();
        stream2.setStatus("created");
        stream2.setName("Venta de ropa");
        stream2.setUsername("Juan");
        stream2.setHlsViewerCount(3500);
        liveStreams.add(stream2);

        LiveStream stream3 = new LiveStream();
        stream3.setStatus("created");
        stream3.setName("Si te ries memeperdonas");
        stream3.setUsername("Vicio One More Time");
        stream3.setHlsViewerCount(10000);
        liveStreams.add(stream3);

        adapter = new LiveStreamAdapter(getContext(),liveStreams);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);*/

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

                    Log.i("info",liveStream.getStreamId());
                    Log.i("info",liveStream.getName());
                    Log.i("info",liveStream.getUsername());
                    Log.i("info",liveStream.getStatus());
                    Log.i("info",String.valueOf(liveStream.getHlsViewerCount()));
                    Log.i("info",liveStream.getDescription());
                    Log.i("info",liveStream.getStreamUrl());

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
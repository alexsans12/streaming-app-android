package com.analysisgroup.streamingapp.MainFragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.analysisgroup.streamingapp.LiveVideoPlayer.LiveVideoPlayerActivity;
import com.analysisgroup.streamingapp.R;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    Button btnView;
    Button btnInfo;
    TextView info;

    RequestQueue requestQueue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        requestQueue = Volley.newRequestQueue(getContext());

        btnView = view.findViewById(R.id.button_live);
        btnInfo = view.findViewById(R.id.button_stream);
        info = view.findViewById(R.id.text_stream);

        btnView.setOnClickListener(viewClick ->  {
                Intent intent = new Intent(getActivity(), LiveVideoPlayerActivity.class);
                startActivity(intent);
        });

        btnInfo.setOnClickListener(viewClick -> {
            getStreams();
        });

        return view;
    }

    private void getStreams() {
        JsonArrayRequest postRequest = new JsonArrayRequest(Request.Method.GET,"http://20.124.2.54:5080/LiveApp/rest/v2/broadcasts/list/0/10", null,
            response -> {
                int size = response.length();
                for (int i = 0; i < size; i++) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.get(i).toString());
                        info.append(jsonObject.getString("streamId") + "\n");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }, error -> {
                if(error instanceof ServerError)
                    Log.i("TAG", "SERVER ERROR");
                if(error instanceof NoConnectionError)
                    Log.i("TAG", "There is no internet connection");
                if(error instanceof NetworkError)
                    Log.i("TAG", "Network");
            }
        );
        requestQueue.add(postRequest);
    }

}
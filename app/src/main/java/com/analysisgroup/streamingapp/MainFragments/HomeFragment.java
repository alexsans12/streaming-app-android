package com.analysisgroup.streamingapp.MainFragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.analysisgroup.streamingapp.LiveVideoPlayer.LiveVideoPlayerActivity;
import com.analysisgroup.streamingapp.R;

public class HomeFragment extends Fragment {

    Button btnView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        btnView = view.findViewById(R.id.button_live);

        btnView.setOnClickListener(viewClick ->  {
                Intent intent = new Intent(getActivity(), LiveVideoPlayerActivity.class);
                startActivity(intent);
        });

        return view;
    }
}
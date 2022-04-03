package com.analysisgroup.streamingapp.MainFragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.analysisgroup.streamingapp.LoginActivity;
import com.analysisgroup.streamingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private Button btnLogout;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        btnLogout = view.findViewById(R.id.logout_btn);

        btnLogout.setOnClickListener(viewClick -> {
            logout();
        });

        return view;
    }

    private void logout() {
        firebaseAuth.signOut();
        startActivity(new Intent(getContext(), LoginActivity.class));
        getActivity().finish();
        Toast.makeText(getContext(), "Logout Successfully", Toast.LENGTH_SHORT).show();
    }
}
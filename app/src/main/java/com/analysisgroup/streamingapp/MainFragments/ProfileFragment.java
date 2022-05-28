package com.analysisgroup.streamingapp.MainFragments;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.CLIPBOARD_SERVICE;
import static com.google.firebase.storage.FirebaseStorage.getInstance;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.analysisgroup.streamingapp.LoginActivity;
import com.analysisgroup.streamingapp.MainActivity;
import com.analysisgroup.streamingapp.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    ImageView profilePic;
    TextView profileName, profileLastName, profileEmail, profileAge, streamKey;
    Button btnLogout, btnKey;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    DatabaseReference DATABASE_USERS;

    StorageReference storageReference;
    String UrlStorage = "PICTURES_PROFILE_USERS/*";

    private Uri uri_image;
    private String profile_image;
    private ProgressDialog progressDialog;
    private String secretKey;
    private boolean showKey;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profilePic = view.findViewById(R.id.profilePic);
        profileName = view.findViewById(R.id.profileName);
        profileLastName = view.findViewById(R.id.profileLastName);
        profileEmail = view.findViewById(R.id.profileEmail);
        profileAge = view.findViewById(R.id.profileAge);
        streamKey = view.findViewById(R.id.streamKey);

        btnLogout = view.findViewById(R.id.logout_btn);
        btnKey = view.findViewById(R.id.showStreamKey);

        showKey = false;

        streamKey.setOnLongClickListener(v -> {
            if (showKey) {
                ClipboardManager clipboardManager = (ClipboardManager) requireActivity().getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("label", secretKey);
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getActivity(), R.string.copyKey, Toast.LENGTH_SHORT).show();
            }
            return false;
        });

        btnLogout.setOnClickListener(viewClick -> logout());

        btnKey.setOnClickListener(viewClick -> {
            if (!showKey) {
                streamKey.setText(secretKey);
                btnKey.setText(R.string.btnKeyShow);
                showKey = true;
            } else {
                streamKey.setText("*************");
                btnKey.setText(R.string.btnKey);
                showKey = false;
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        storageReference = getInstance().getReference();

        progressDialog =  new ProgressDialog(getActivity());

        DATABASE_USERS = FirebaseDatabase.getInstance().getReference("DATABASE USERS");

        DATABASE_USERS.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = ""+snapshot.child("FirstName").getValue();
                    String lastname = ""+snapshot.child("LastName").getValue();
                    String email = ""+snapshot.child("Email").getValue();
                    String age = ""+snapshot.child("Birthday").getValue();
                    String image = ""+snapshot.child("Image").getValue();
                    secretKey = ""+snapshot.child("SecretKey").getValue();

                    profileName.setText(name);
                    profileLastName.setText(lastname);
                    profileEmail.setText(email);
                    profileAge.setText(age);
                    streamKey.setText("*************");

                    try {
                        // If image exists
                        Picasso.get().load(image).placeholder(R.drawable.profile_ico).into(profilePic);
                    } catch (Exception e) {
                        //If not exists image
                        Picasso.get().load("https://cdn.onlinewebfonts.com/svg/img_24787.png").into(profilePic);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        profilePic.setOnClickListener(v -> ChangeProfilePicture());

        return view;
    }

    private void ChangeProfilePicture() {
        String [] options = {getString(R.string.optionsAlert)};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.titleAlert);
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                profile_image = "Image";
                ChooseImage();
            }
        });
        builder.create().show();
    }

    private void ChooseImage() {
        String [] options = {getString(R.string.optionCamera), getString(R.string.optionGallery)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.secondTitleAlert);
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                    ChooseCamera();
                else {
                    CameraPermitRequest.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                }
            } else if (which == 1) {
                if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    ChooseFromGallery();
                else
                    GalleryPermitRequest.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        });

        builder.create().show();
    }

    private void ChooseFromGallery() {
        Intent intentGallery = new Intent(Intent.ACTION_PICK);
        intentGallery.setType("image/*");
        GetGalleryImage.launch(intentGallery);
    }

    private void ChooseCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temporary photo");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temporary Description");
        uri_image = (requireActivity()).getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, uri_image);
        GetCameraImage.launch(intentCamera);
    }

    private final ActivityResultLauncher<Intent> GetCameraImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        UpdateImageInDatabase(uri_image);
                        progressDialog.setTitle(R.string.progressTitleUpload);
                        progressDialog.setMessage(getString(R.string.progressMsgUpload));
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                    } else {
                        Toast.makeText(getActivity(), R.string.cancellationMsg, Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> GetGalleryImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        assert data != null;
                        uri_image = data.getData();
                        UpdateImageInDatabase(uri_image);
                        progressDialog.setTitle(R.string.progressTitleUpload);
                        progressDialog.setMessage(getString(R.string.progressMsgUpload));
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                    } else {
                        Toast.makeText(getActivity(), R.string.cancellationMsg, Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<String []> CameraPermitRequest = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            (Map<String, Boolean> grantStates) ->  {
                boolean isGranted = false;

                for (Map.Entry<String, Boolean> grantState : grantStates.entrySet()) {
                    if (grantState.getValue())
                        isGranted = true;
                    else
                        isGranted = false;

                }

                if (isGranted)
                    ChooseCamera();
                else
                    Toast.makeText(getActivity(), "permission denied", Toast.LENGTH_SHORT).show();
            });

    private final ActivityResultLauncher<String> GalleryPermitRequest = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted)
            ChooseFromGallery();
        else
            Toast.makeText(getActivity(), R.string.deniedPermission, Toast.LENGTH_SHORT).show();
    });

    private void UpdateImageInDatabase(Uri uri) {
        String filePathAndName = UrlStorage + "" + profile_image + "_" + firebaseUser.getUid();
        StorageReference storageReference2 = storageReference.child(filePathAndName);
        storageReference2.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful());
            Uri downloadUri = uriTask.getResult();

            if (uriTask.isSuccessful()) {
                HashMap<String, Object> results = new HashMap<>();
                results.put(profile_image, downloadUri.toString());
                DATABASE_USERS.child(firebaseUser.getUid()).updateChildren(results)
                        .addOnSuccessListener(unused -> {
                            startActivity(new Intent(getActivity(), MainActivity.class));
                            requireActivity().finish();
                            Toast.makeText(getActivity(), R.string.imageUpdated, Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show());

            } else {
                Toast.makeText(getActivity(), R.string.errorServer, Toast.LENGTH_SHORT).show();
            }

        }).addOnFailureListener(e -> Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void logout() {
        firebaseAuth.signOut();
        startActivity(new Intent(getContext(), LoginActivity.class));
        requireActivity().finish();
        Toast.makeText(getContext(), R.string.logoutMsg, Toast.LENGTH_SHORT).show();
    }
}
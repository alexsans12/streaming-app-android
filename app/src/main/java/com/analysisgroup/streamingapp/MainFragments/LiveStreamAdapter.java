package com.analysisgroup.streamingapp.MainFragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.analysisgroup.streamingapp.LiveVideoPlayer.LiveVideoPlayerActivity;
import com.analysisgroup.streamingapp.Models.LiveStream;
import com.analysisgroup.streamingapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class LiveStreamAdapter extends RecyclerView.Adapter<LiveStreamAdapter.ViewHolderLive> {

    LayoutInflater inflater;
    List<LiveStream> list;

    public LiveStreamAdapter(Context context, List<LiveStream> list) {
        this.inflater = LayoutInflater.from(context);
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolderLive onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.stream_item, parent, false);
        return new ViewHolderLive(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderLive holder, @SuppressLint("RecyclerView") int position) {
        holder.titleStream.setText(list.get(position).getName());
        holder.usernameStream.setText(list.get(position).getUsername());
        holder.viewersStream.setText(String.valueOf(list.get(position).getHlsViewerCount()));

        try {
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/appstreaming-f76e1.appspot.com/o/Miniatura%20de%20Youtube%20m%C3%BAsica%20%20pop%20degradado%20azul%20y%20rojo.png?alt=media&token=fea0ab53-fd1e-4037-8f46-681bc1b4792a")
                       .into(holder.streamImage);

        } catch (Exception e) {
            Log.d("Error", e.getMessage());
        }

        holder.livestream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(inflater.getContext(), LiveVideoPlayerActivity.class);

                Bundle bundle = new Bundle();
                bundle.putString("streamUrl", list.get(position).getStreamUrl());

                intent.putExtras(bundle);
                inflater.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolderLive extends RecyclerView.ViewHolder {
        ImageView streamImage;
        TextView titleStream, usernameStream, viewersStream;
        RelativeLayout livestream;

        public ViewHolderLive(View itemView) {
            super(itemView);

            streamImage = itemView.findViewById(R.id.thumbnail);
            titleStream = itemView.findViewById(R.id.stream_title);
            usernameStream = itemView.findViewById(R.id.stream_username);
            viewersStream = itemView.findViewById(R.id.stream_spectators);
            livestream = itemView.findViewById(R.id.livestream);

        }
    }

}

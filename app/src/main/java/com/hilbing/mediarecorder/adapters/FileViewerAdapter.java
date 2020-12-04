package com.hilbing.mediarecorder.adapters;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hilbing.mediarecorder.R;
import com.hilbing.mediarecorder.database.DBHelper;
import com.hilbing.mediarecorder.fragments.PlaybackFragment;
import com.hilbing.mediarecorder.interfaces.OnDatabaseChangedListener;
import com.hilbing.mediarecorder.models.RecordingItem;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileViewerAdapter extends RecyclerView.Adapter<FileViewerAdapter.FileViewerViewHolder> implements OnDatabaseChangedListener {

    Context context;
    ArrayList<RecordingItem> arrayList;
    LinearLayoutManager linearLayoutManager;

    DBHelper dbHelper;

    public FileViewerAdapter(Context context, ArrayList<RecordingItem> arrayList, LinearLayoutManager linearLayoutManager){
        this.context = context;
        this.arrayList = arrayList;
        this.linearLayoutManager = linearLayoutManager;
        dbHelper = new DBHelper(context);

        dbHelper.setOnDatabaseChangedListener(this);
    }

    @NonNull
    @Override
    public FileViewerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_view,parent, false);
        return new FileViewerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewerViewHolder holder, int position) {
        RecordingItem recordingItem = arrayList.get(position);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(recordingItem.getLength());
        long seconds = TimeUnit.MILLISECONDS.toSeconds(recordingItem.getLength())- TimeUnit.MINUTES.toSeconds(minutes);

        holder.vName.setText(recordingItem.getName());
        holder.vLength.setText(String.format("%02d:#02d", minutes, seconds));
        holder.vTimeAdded.setText(DateUtils.formatDateTime(context, recordingItem.getTime_added(), DateUtils.FORMAT_SHOW_DATE |
                DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR));
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public void onNewDatabaseEntryAdded(RecordingItem recordingItem) {
        arrayList.add(recordingItem);
        notifyItemInserted(arrayList.size()-1);
    }

    public class FileViewerViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_file_name)
        TextView vName;
        @BindView(R.id.txt_file_length)
        TextView vLength;
        @BindView(R.id.txt_time_added)
        TextView vTimeAdded;
        @BindView(R.id.cardView)
        CardView cardView;


        public FileViewerViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PlaybackFragment playbackFragment = new PlaybackFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("item", arrayList.get(getAdapterPosition()));
                    playbackFragment.setArguments(bundle);

                    FragmentTransaction fragmentTransaction = ((FragmentActivity)context)
                            .getSupportFragmentManager()
                            .beginTransaction();
                    playbackFragment.show(fragmentTransaction, "dialog_playback");
                }
            });
        }
    }
}

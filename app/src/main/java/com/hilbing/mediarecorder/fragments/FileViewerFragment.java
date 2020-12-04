package com.hilbing.mediarecorder.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hilbing.mediarecorder.R;
import com.hilbing.mediarecorder.adapters.FileViewerAdapter;
import com.hilbing.mediarecorder.database.DBHelper;
import com.hilbing.mediarecorder.models.RecordingItem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileViewerFragment extends Fragment {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    DBHelper dbHelper;
    ArrayList<RecordingItem> arrayList;
    private FileViewerAdapter fileViewerAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_viewer, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DBHelper(getContext());
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        arrayList = dbHelper.getAllAudios();
        if(arrayList == null){
            Toast.makeText(getContext(), "No audio files", Toast.LENGTH_LONG).show();
        }
        else {
            fileViewerAdapter = new FileViewerAdapter(getActivity(), arrayList, linearLayoutManager);
            recyclerView.setAdapter(fileViewerAdapter);

        }


    }
}

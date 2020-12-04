package com.hilbing.mediarecorder.interfaces;

import com.hilbing.mediarecorder.models.RecordingItem;

public interface OnDatabaseChangedListener {
    void onNewDatabaseEntryAdded(RecordingItem recordingItem);

}

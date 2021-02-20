package com.example.backupmanagerexample;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.app.backup.BackupManager;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class BackupManagerExample extends AppCompatActivity {
    static final String TAG = "BRActivity";

    static final Object[] sDataLock = new Object[0];

    static final String DATA_FILE_NAME = "saved_data";

    RadioGroup mFillingGroup;
    CheckBox mAddMayoCheckBox;
    CheckBox mAddTomatoCheckBox;

    File mDataFile;
    BackupManager mBackupManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFillingGroup = findViewById(R.id.filling_group);
        mAddMayoCheckBox = findViewById(R.id.mayo);
        mAddTomatoCheckBox = findViewById(R.id.tomato);

        mDataFile = new File(getFilesDir(), BackupManagerExample.DATA_FILE_NAME);

        mBackupManager = new BackupManager(this);

        populateUI();
    }

    private void populateUI() {
        RandomAccessFile file;

        int whichFilling = R.id.pastrami;
        boolean addMayo = false;
        boolean addTomato = false;

        synchronized (BackupManagerExample.sDataLock){
            boolean exists = mDataFile.exists();
            try {
                file = new RandomAccessFile(mDataFile, "rw");
                if (exists){
                    Log.v(TAG, "datafile exists");
                    whichFilling = file.readInt();
                    addMayo = file.readBoolean();
                    addTomato = file.readBoolean();
                    Log.v(TAG, " mayo=" + addMayo
                    + " tomato=" + addTomato
                    + " filling=" + whichFilling);
                }else {
                    Log.v(TAG, "creating default datafile");
                    writeDataToFileLocked(file, addMayo,
                            addTomato, whichFilling);
                    mBackupManager.dataChanged();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mFillingGroup.check(whichFilling);
        mAddMayoCheckBox.setChecked(addMayo);
        mAddTomatoCheckBox.setChecked(addTomato);

        mFillingGroup.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        Log.v(TAG, "New radio item selected: " + checkedId);
                        recordNewUIState();
                    }
                }
        );

        CompoundButton.OnCheckedChangeListener checkedChangeListener =
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Log.v(TAG, "Checked toggled: " + buttonView);
                        recordNewUIState();
                    }
                };
        mAddMayoCheckBox.setOnCheckedChangeListener(checkedChangeListener);
        mAddTomatoCheckBox.setOnCheckedChangeListener(checkedChangeListener);
    }

    private void recordNewUIState() {
        boolean addMayo = mAddMayoCheckBox.isChecked();
        boolean addTomato = mAddTomatoCheckBox.isChecked();
        int whichFilling = mFillingGroup.getCheckedRadioButtonId();
        try {
            synchronized (BackupManagerExample.sDataLock){
                RandomAccessFile file = new RandomAccessFile(mDataFile, "rw");
                writeDataToFileLocked(file, addMayo, addTomato, whichFilling);
            }
        } catch (IOException e){
            Log.e(TAG, "Unable to record new UI state");
        }
        mBackupManager.dataChanged();
    }

    private void writeDataToFileLocked(RandomAccessFile file,
                                       boolean addMayo, boolean addTomato, int whichFilling) throws IOException {
        file.setLength(0L);
        file.writeInt(whichFilling);
        file.writeBoolean(addMayo);
        file.writeBoolean(addTomato);
        Log.v(TAG, "NEW STATE mayo=" + addMayo
                + " tomato=" + addTomato
                + " filling-" + whichFilling);
    }
}
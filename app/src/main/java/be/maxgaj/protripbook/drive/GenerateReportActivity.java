package be.maxgaj.protripbook.drive;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityOptions;
import com.google.android.gms.tasks.Task;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import be.maxgaj.protripbook.R;
import be.maxgaj.protripbook.ReportFragment;
import be.maxgaj.protripbook.models.Report;
import be.maxgaj.protripbook.models.Trip;

//https://github.com/gsuitedevs/android-samples/blob/021e756d763614d1f57c953083f8392f7189b686/drive/demos/app/src/main/java/com/google/android/gms/drive/sample/demo/CreateFileWithCreatorActivity.java
public class GenerateReportActivity extends BaseDriveActivity {
    private static final String TAG = "GenerateReportActivity";
    private static final int REQUEST_CODE_CREATE_FILE = 2;

    private Report report;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent.hasExtra(ReportFragment.REPORT_EXTRA))
            this.report = intent.getParcelableExtra(ReportFragment.REPORT_EXTRA);
    }

    @Override
    protected void onDriveClientReady() {
        createFileWithIntent();
    }

    private void createFileWithIntent() {
        Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();
        createContentsTask
                .continueWithTask(task -> {
                    DriveContents contents = task.getResult();
                    OutputStream outputStream = contents.getOutputStream();
                    try (Writer writer = new OutputStreamWriter(outputStream)) {
                        writer.write("Report from "+this.report.getFirstDate()+ " to "+this.report.getLastDate()+"\n");
                        writer.write("Total trip distance: "+String.valueOf(this.report.getTripDistance())+" "+this.report.getUnit()+"\n");
                        writer.write("Total odometer distance: "+String.valueOf(this.report.getOdometerDistance())+" "+this.report.getUnit()+"\n");
                        writer.write("Ratio: "+String.valueOf(this.report.getRatio())+" %\n\n");
                        for(Trip trip : this.report.getTripList()){
                            String round = trip.isRoundTrip()?"Round trip":"Simple trip";
                            writer.write(trip.getDate()+ " From "+trip.getStartingLocation()+" to "+trip.getDestinationLocation()+" ("+round+") "+String.valueOf(trip.getDistance()+" "+this.report.getUnit()+"\n"));
                        }
                    }

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle("New file")
                            .setMimeType("text/plain")
                            .setStarred(true)
                            .build();

                    CreateFileActivityOptions createOptions =
                            new CreateFileActivityOptions.Builder()
                                    .setInitialDriveContents(contents)
                                    .setInitialMetadata(changeSet)
                                    .build();
                    return getDriveClient().newCreateFileActivityIntentSender(createOptions);
                })
                .addOnSuccessListener(this,
                        intentSender -> {
                            try {
                                startIntentSenderForResult(
                                        intentSender, REQUEST_CODE_CREATE_FILE, null, 0, 0, 0);
                            } catch (IntentSender.SendIntentException e) {
                                Log.e(TAG, "createFileWithIntent: Unable to create file", e);
                                showMessage(getString(R.string.file_create_error));
                                finish();
                            }
                        })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "createFileWithIntent: Unable to create file", e);
                    showMessage(getString(R.string.file_create_error));
                    finish();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CREATE_FILE){
            if (resultCode != RESULT_OK){
                Log.e(TAG, "onActivityResult: Unable to create file");
                showMessage(getString(R.string.file_create_error));
            }
            else {
                DriveId driveId = data.getParcelableExtra(OpenFileActivityOptions.EXTRA_RESPONSE_DRIVE_ID);
                showMessage(getString(R.string.file_created, "File created with ID: "+driveId));
            }
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

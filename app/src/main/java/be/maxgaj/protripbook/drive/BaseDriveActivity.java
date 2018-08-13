package be.maxgaj.protripbook.drive;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.OpenFileActivityOptions;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.HashSet;
import java.util.Set;

import be.maxgaj.protripbook.R;

// https://github.com/gsuitedevs/android-samples/blob/021e756d763614d1f57c953083f8392f7189b686/drive/demos/app/src/main/java/com/google/android/gms/drive/sample/demo/BaseDemoActivity.java
public abstract class BaseDriveActivity extends Activity {
    private static final String TAG = "BaseDriveAcivity";
    protected static final int REQUEST_CODE_SIGN_IN = 0;
    protected static final int REQUEST_CODE_OPEN_ITEM = 1;

    private DriveClient driveClient;
    private DriveResourceClient driveResourceClient;
    private TaskCompletionSource<DriveId> openItemTaskSource;

    @Override
    protected void onStart() {
        super.onStart();
        signIn();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_CODE_SIGN_IN:
                if (resultCode!=RESULT_OK){
                    Log.e(TAG, "onActivityResult: Sign-in failed");
                    finish();
                    return;
                }
                Task<GoogleSignInAccount> getAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
                if (getAccountTask.isSuccessful()){
                    initializeDriveClient(getAccountTask.getResult());
                }
                else {
                    Log.e(TAG, "onActivityResult: Sign-in failed");
                    finish();
                }
                break;
            case REQUEST_CODE_OPEN_ITEM:
                if (resultCode == RESULT_OK){
                    DriveId driveId = data.getParcelableExtra(OpenFileActivityOptions.EXTRA_RESPONSE_DRIVE_ID);
                    this.openItemTaskSource.setResult(driveId);
                }
                else {
                    this.openItemTaskSource.setException(new RuntimeException("Unable to open file"));
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void signIn(){
        Set<Scope> requiredScopes = new HashSet<>(2);
        requiredScopes.add(Drive.SCOPE_FILE);
        requiredScopes.add(Drive.SCOPE_APPFOLDER);
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (signInAccount != null && signInAccount.getGrantedScopes().containsAll(requiredScopes)){
            initializeDriveClient(signInAccount);
        }
        else {
            GoogleSignInOptions signInOptions =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(Drive.SCOPE_FILE)
                    .requestScopes(Drive.SCOPE_APPFOLDER)
                    .build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, signInOptions);
            startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
        }
    }

    private void initializeDriveClient(GoogleSignInAccount signInAccount){
        this.driveClient = Drive.getDriveClient(getApplicationContext(), signInAccount);
        this.driveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), signInAccount);
        onDriveClientReady();
    }

    protected Task<DriveId> pickTextFile(){
        OpenFileActivityOptions openOptions =
                new OpenFileActivityOptions.Builder()
                .setSelectionFilter(Filters.eq(SearchableField.MIME_TYPE, "text/plain"))
                .setActivityTitle(getString(R.string.select_file))
                .build();
        return pickItem(openOptions);
    }

    protected Task<DriveId> pickFolder() {
        OpenFileActivityOptions openOptions =
                new OpenFileActivityOptions.Builder()
                .setSelectionFilter(Filters.eq(SearchableField.MIME_TYPE, DriveFolder.MIME_TYPE))
                .setActivityTitle(getString(R.string.select_folder))
                .build();
        return pickItem(openOptions);
    }

    private Task<DriveId> pickItem(OpenFileActivityOptions openOptions) {
        this.openItemTaskSource = new TaskCompletionSource<>();
        getDriveClient().newOpenFileActivityIntentSender(openOptions)
                        .continueWith((Continuation<IntentSender, Void>) task -> {
                            startIntentSenderForResult(task.getResult(), REQUEST_CODE_OPEN_ITEM, null, 0, 0, 0);
                            return null;
                        });
        return this.openItemTaskSource.getTask();
    }

    protected void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    protected abstract void onDriveClientReady();

    protected DriveClient getDriveClient() {
        return this.driveClient;
    }

    protected DriveResourceClient getDriveResourceClient() {
        return this.driveResourceClient;
    }
}

package com.ivigilate.android.app.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ivigilate.android.app.AppContext;
import com.ivigilate.android.app.BuildConfig;
import com.ivigilate.android.app.R;
import com.ivigilate.android.app.interfaces.IProfileQuery;
import com.ivigilate.android.app.utils.Logger;
import com.ivigilate.android.library.IVigilateManager;
import com.ivigilate.android.library.classes.User;
import com.ivigilate.android.library.interfaces.IVigilateApiCallback;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity implements LoaderCallbacks<Cursor> {
    private static final int PERMISSIONS_REQUEST = 1;

    // UI references.
    private ScrollView mSvLogin;
    private ProgressBar mPbLogin;

    private EditText mServerView;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.d("Started...");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_activity);

        bindControls();

        showHideViews();

        checkRequiredPermissions();

        Logger.d("Finished.");
    }

    private IVigilateManager getIVigilateManager() {
        return ((AppContext)getApplicationContext()).getIVigilateManager();
    }

    private void bindControls() {
        ImageView ivLogo = (ImageView) findViewById(R.id.ivLogo);
        ivLogo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Logger.d("Opening website...");
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(getIVigilateManager().getServerAddress()));
                startActivity(i);
            }
        });

        mPbLogin = (ProgressBar) findViewById(R.id.pbLogin);
        mSvLogin = (ScrollView) findViewById(R.id.svLogin);

        // Set up the login form.
        mServerView = (EditText) findViewById(R.id.etServer);
        mServerView.setText(getIVigilateManager().getServerAddress());
        if (BuildConfig.DEBUG) {
            mServerView.setVisibility(View.VISIBLE);
        } else {
            mServerView.setVisibility(View.GONE);
        }

        mEmailView = (AutoCompleteTextView) findViewById(R.id.etEmail);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.etPassword);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        if (BuildConfig.DEBUG) {
            mEmailView.setText("a@b.com");
            mPasswordView.setText("123");
        }

        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    private void showHideViews() {

        if (getIVigilateManager().getUser() != null) {
            Logger.d("User is already logged in.");

            mSvLogin.setVisibility(View.GONE);

            mEmailView.setText(getIVigilateManager().getUser().email);

            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
            finish();

        } else {
            Logger.d("User is not logged in.");

            mSvLogin.setVisibility(View.VISIBLE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Logger.d("Coarse location permission granted");
                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                } else if (grantResults[1] == PackageManager.PERMISSION_DENIED) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("This app needs to be able to access the device's IMEI to be able to function properly.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void populateAutoComplete() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            getLoaderManager().initLoader(0, null, this);
        }
    }

    public void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String server = mServerView.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and perform the login attempt asynchronously.
            showProgress(true);
            doLogin(server, email, password);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with more proper logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.trim().length() > 0;
    }

    public void showProgress(final boolean show) {
        mSvLogin.setVisibility(show ? View.GONE : View.VISIBLE);

        mPbLogin.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return new CursorLoader(this,
                    // Retrieve data rows for the device user's 'profile' contact.
                    Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                            ContactsContract.Contacts.Data.CONTENT_DIRECTORY), IProfileQuery.PROJECTION,
                    // Select only email addresses.
                    ContactsContract.Contacts.Data.MIMETYPE +
                            " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                    .CONTENT_ITEM_TYPE},
                    // Show primary email addresses first. Note that there won't be
                    // a primary email address if the user hasn't specified one.
                    ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                emails.add(cursor.getString(IProfileQuery.ADDRESS));
                cursor.moveToNext();
            }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    private void doLogin(final String serverAddress, String email, String password) {
        try {
            Logger.d("Started...");

            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);

            getIVigilateManager().setServerAddress(serverAddress);
            getIVigilateManager().login(new User(email, password), new IVigilateApiCallback<User>() {
                @Override
                public void success(User user) {
                    showProgress(false);
                    if (user != null) {
                        showHideViews();
                    } else {
                        mPasswordView.setError(getString(R.string.error_incorrect_password));
                        mPasswordView.requestFocus();
                    }
                }

                @Override
                public void failure(String errorMsg) {
                    mPasswordView.setError(errorMsg);
                    mPasswordView.requestFocus();
                    showProgress(false);
                }
            });

        } catch (Exception e) {
            Logger.e("Failed with exception: " + e.getMessage());
        }
    }
}
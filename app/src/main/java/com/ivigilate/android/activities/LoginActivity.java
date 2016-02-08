package com.ivigilate.android.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ivigilate.android.AppContext;
import com.ivigilate.android.BuildConfig;
import com.ivigilate.android.interfaces.IProfileQuery;
import com.ivigilate.android.interfaces.IVigilateApi;
import com.ivigilate.android.R;
import com.ivigilate.android.classes.User;
import com.ivigilate.android.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class LoginActivity extends Activity implements LoaderCallbacks<Cursor> {
    // UI references.
    private EditText mServerView;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.d("Started...");
        super.onCreate(savedInstanceState);

        AppContext appContext = ((AppContext)getApplication());
        if (appContext.settings.getUser() != null) {
            Logger.d("Skipping login screen as the user is already logged in.");
            gotoMainActivity();
        } else {
            setContentView(R.layout.login_activity);

            // Set up the login form.
            mServerView = (EditText) findViewById(R.id.server);
            mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
            populateAutoComplete();

            mPasswordView = (EditText) findViewById(R.id.password);
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

            Button emailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
            emailSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });

            mLoginFormView = findViewById(R.id.login_form);
            mProgressView = findViewById(R.id.login_progress);

            if (BuildConfig.DEBUG) {
                mServerView.setText(appContext.settings.getDebugServerAddress());

                if (mServerView.getText().toString().equals("")) mServerView.setText(AppContext.SERVER_BASE_URL);
                mEmailView.setText("a@b.com");
                mPasswordView.setText("1");
            } else {
                mServerView.setText(AppContext.SERVER_BASE_URL);
                mServerView.setVisibility(View.GONE);
            }
            Logger.d("Finished.");
        }
    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
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
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.trim().length() > 0;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
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

            TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            String imei = telephonyManager.getDeviceId();

            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(serverAddress)
                    .build();
            IVigilateApi api = restAdapter.create(IVigilateApi.class);
            Logger.d("Api created, making login call...");

            String metadata = String.format("{\"device\": {\"imei\": \"%s\"}}", imei);
            api.login(new User(email, password, metadata), new Callback<User>() {
                @Override
                public void success(User user, Response response) {
                    Logger.d("Login successful!");
                    if (user != null) {
                        AppContext appContext = (AppContext)getApplication();
                        appContext.settings.setUser(user);
                        appContext.settings.setDebugServerAddress(serverAddress);
                        gotoMainActivity();
                    } else {
                        showProgress(false);
                        mPasswordView.setError(getString(R.string.error_incorrect_password));
                        mPasswordView.requestFocus();
                    }
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    if (retrofitError.getResponse() != null) {
                        String errorMsg = new String(((TypedByteArray) retrofitError.getResponse().getBody()).getBytes());
                        Logger.e("Login failed with error: " + errorMsg);
                        mPasswordView.setError(errorMsg);
                        mPasswordView.requestFocus();
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.error_network_error), Toast.LENGTH_LONG).show();
                        Logger.e("Login failed with error: " + retrofitError.getKind().toString() +
                                " - " + retrofitError.getMessage());
                    }
                    showProgress(false);
                }
            });
        } catch (Exception e) {
            Logger.e("Failed with exception: " + e.getMessage());
        }
    }

    private void gotoMainActivity() {
        Logger.d("Replacing current activity...");
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}


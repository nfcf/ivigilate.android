package com.ivigilate.android.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivigilate.android.BuildConfig;
import com.ivigilate.android.core.IVigilateManager;
import com.ivigilate.android.core.classes.User;
import com.ivigilate.android.core.interfaces.IVigilateApiCallback;
import com.ivigilate.android.interfaces.IProfileQuery;
import com.ivigilate.android.R;
import com.ivigilate.android.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity implements LoaderCallbacks<Cursor> {
    // UI references.
    private LinearLayout mLLLogin;

    private EditText mServerView;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Button mBtnStartStop;

    private IVigilateManager mIVigilateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.d("Started...");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        mIVigilateManager = IVigilateManager.getInstance(this);

        ImageView ivLogo = (ImageView) findViewById(R.id.ivLogo);
        ivLogo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Logger.d("Opening website...");
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(mIVigilateManager.getSettings().getServerAddress()));
                startActivity(i);
            }
        });

        mLLLogin = (LinearLayout) findViewById(R.id.llLogin);
        mBtnStartStop = (Button) findViewById(R.id.btnStartStop);

        if (mIVigilateManager.getSettings().getUser() != null) {
            Logger.d("User is already logged in.");

            mLLLogin.setVisibility(View.GONE);
            mBtnStartStop.setVisibility(View.VISIBLE);
        } else {
            Logger.d("User is not logged in.");

            mLLLogin.setVisibility(View.VISIBLE);
            mBtnStartStop.setVisibility(View.GONE);

            // Set up the login form.
            mServerView = (EditText) findViewById(R.id.etServer);
            mServerView.setText(mIVigilateManager.getSettings().getServerAddress());

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

            Button btnLogin = (Button) findViewById(R.id.btnLogin);
            btnLogin.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });


            mBtnStartStop.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    //attemptLogin();
                }
            });

            mLoginFormView = findViewById(R.id.llLogin);
            mProgressView = findViewById(R.id.pbLogin);

            if (BuildConfig.DEBUG) {
                mEmailView.setText("nuno.freire@ivigilate.com");
                mPasswordView.setText("123");
            } else {
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
        //TODO: Replace this with more proper logic
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

            mIVigilateManager.getSettings().setServerAddress(serverAddress);
            mIVigilateManager.login(new User(email, password), new IVigilateApiCallback<User>() {
                @Override
                public void success(User user) {
                    showProgress(false);
                    if (user != null) {
                        mLLLogin.setVisibility(View.GONE);

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


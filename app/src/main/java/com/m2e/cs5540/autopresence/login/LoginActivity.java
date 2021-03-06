package com.m2e.cs5540.autopresence.login;

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.m2e.cs5540.autopresence.R;
import com.m2e.cs5540.autopresence.base.AsyncLoaderStatus;
import com.m2e.cs5540.autopresence.base.BaseActivity;
import com.m2e.cs5540.autopresence.context.AppContext;
import com.m2e.cs5540.autopresence.professors.ProfessorActivity;
import com.m2e.cs5540.autopresence.professors.home.ProfessorHomeActivity;
import com.m2e.cs5540.autopresence.register.RegisterActivity;
import com.m2e.cs5540.autopresence.service.LocationUpdateService;
import com.m2e.cs5540.autopresence.students.StudentCoursesActivity;
import com.m2e.cs5540.autopresence.students.StudentHomeActivity;
import com.m2e.cs5540.autopresence.util.AppUtil;
import com.m2e.cs5540.autopresence.vao.User;
import com.m2e.cs5540.autopresence.vao.UserRole;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by maeswara on 7/8/2017.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener,
      LoaderManager.LoaderCallbacks<AsyncLoaderStatus> {

   private static final String TAG = LoginActivity.class.getName();
   private ProgressDialog progressDialog;
   private EditText usernameEditText;
   private EditText passwordEditText;
   private Button loginButton;
   private TextView register;

   @Override protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.login_layout);
      this.usernameEditText = (EditText) findViewById(R.id.usernameText);
      this.passwordEditText = (EditText) findViewById(R.id.passwordText);
      progressDialog = new ProgressDialog(this);

      this.register = (TextView) findViewById(R.id.btn_signup);
      this.register.setOnClickListener(new View.OnClickListener() {

         @Override public void onClick(View v) {
            // Start the Signup activity
            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivityForResult(intent, 0);
         }
      });

      addButtonClickListener();
   }

   private void addButtonClickListener() {
      loginButton = (Button) findViewById(R.id.loginButton);
      loginButton.setOnClickListener(this);
   }

   @Override public void onClick(View v) {

      String username = usernameEditText.getText().toString();
      String password = passwordEditText.getText().toString();

      Log.i(TAG, "$$$$$$ Login process start... ");

      if (username == null || username.isEmpty() || password == null ||
            password.isEmpty()) {
         Toast.makeText(this, "Enter a valid " + "username and password!",
               Toast.LENGTH_SHORT).show();
      } else {
         Log.i(TAG, "$$$$$$ LoadManager.initLoader called");
         if (getLoaderManager().getLoader(102) == null) {
            getLoaderManager().initLoader(102, null, this);
         } else {

            getLoaderManager().restartLoader(102, null, this).forceLoad();
         }
        // showProgressDialog();
         progressDialog.setIndeterminate(true);
         progressDialog.setMessage("Authenticating...");
         progressDialog.show();

      }
   }

   @Override
   public Loader<AsyncLoaderStatus> onCreateLoader(int id, Bundle args) {
      Log.i(TAG, "$$$$$$ LoginActivity.onCreateLoader called");
      return new LoginAsyncTaskLoader(this, usernameEditText);
   }

   @Override public void onLoadFinished(Loader<AsyncLoaderStatus> loader,
         AsyncLoaderStatus loaderStatus) {
     // hideProgressDialog();
      if (progressDialog != null && progressDialog.isShowing()) {
         progressDialog.dismiss();
      }

      Log.i(TAG, "$$$$$$ LoginActivity.onLoadFinished called");
      if (loaderStatus.hasException()) {
         Toast.makeText(this, "Error " + loaderStatus.getExceptionMessage(),
               Toast.LENGTH_LONG).show();
      } else {
         User user = (User) loaderStatus.getResult();
         String password = passwordEditText.getText().toString();
         String username = (user != null) ? user.getName() : null;
         String encryptedPassword = (user != null) ? user.getPassword() : null;
         Log.i(TAG, "$$$ encryptedPassword from DB = " + encryptedPassword);
         Log.i(TAG, "$$$ encryptedPassword from login = " +
               AppUtil.encryptPassword(password));
         if (user != null && encryptedPassword != null &&
               encryptedPassword.equals(AppUtil.encryptPassword(password))) {
            Toast.makeText(this, "Logged in user is " + username,
                  Toast.LENGTH_LONG).show();
            startLocationService(loaderStatus);
            showLandingPageForUser(user);
         } else {
            Toast.makeText(this, "Invalid username/password. Try again!",
                  Toast.LENGTH_LONG).show();
         }
         //new LocationServiceAsyncTask().execute(
         //      "893b671d-d09e-428f-a251-8b21887d76a4");
      }
   }

   private void showLandingPageForUser(User user) {
      if (user != null) {
         if (user.getRole() == UserRole.STUDENT) {
            Intent studentLandingPageIntent = new Intent(this,
                  StudentHomeActivity.class);
            startActivity(studentLandingPageIntent);
         } else if (user.getRole() == UserRole.PROFESSOR) {
            Intent professorLandingPageIntent = new Intent(this,
                  ProfessorHomeActivity.class);
            startActivity(professorLandingPageIntent);
         }
      }
   }

   private void startLocationService(AsyncLoaderStatus loaderStatus) {
      User user = (User) loaderStatus.getResult();
      Intent locationServiceIntent = new Intent(this,
            LocationUpdateService.class);
      locationServiceIntent.putExtra("userId", user.getId());
      startService(locationServiceIntent);
   }

   @Override public void onLoaderReset(Loader<AsyncLoaderStatus> loader) {

   }

   @Override public void onBackPressed() {
      if (!AppContext.isUserLoggedIn()) {
         super.onBackPressed();
      }else{
         Intent a = new Intent(Intent.ACTION_MAIN);
         a.addCategory(Intent.CATEGORY_HOME);
         a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         startActivity(a);
      }
   }
}

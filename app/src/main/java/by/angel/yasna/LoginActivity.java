package by.angel.yasna;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.koushikdutta.ion.Ion;
import org.jsoup.nodes.Document;

public class LoginActivity extends AppCompatActivity implements Auth.onAuthListener {

    private EditText login, password, capcha;
    private ImageView capcha_image;
    private Button login_btn;

    private String loginInput, passwordInput, capchaInput;
    private Auth auth;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = (EditText) findViewById(R.id.login);
        password = (EditText) findViewById(R.id.password);
        capcha = (EditText) findViewById(R.id.capcha_text);

        capcha_image = (ImageView) findViewById(R.id.capcha_image);

        auth = new Auth(this);
        auth.setAuthListener(this);

        resetCache();

        /**
         * Get capcha image from server
         */
        Ion.with(capcha_image)
                .placeholder(null)
                .error(null)
                .load(Constants.CAPCHA_IMAGE);

        login_btn = (Button) findViewById(R.id.button_login);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkInput();
            }
        });

    }

    /**
     * Set negative result if back button pressed. This will close app.
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (auth!=null) {
            auth.setAuthListener(null);
        }

    }

    @Override
    public void onStop(){
        super.onStop();

        if (auth!=null) {
            auth.setAuthListener(null);
        }

    }

    private void checkInput() {

        /**
         * Check the availability of the Internet
         */
        if (!isConnected()){
            Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        loginInput = login.getText().toString().trim();
        passwordInput = password.getText().toString().trim();
        capchaInput = capcha.getText().toString().trim();

        /**
         * Check for empty fields (login, password, capcha)
         */
        if (TextUtils.isEmpty(loginInput)) {
            login.requestFocus();
            login.setSelection(login.getText().length());
            Toast.makeText(this, R.string.please_enter_login, Toast.LENGTH_SHORT).show();
            showIme(true, login);
            return;
        }

        if (TextUtils.isEmpty(passwordInput)) {
            password.requestFocus();
            password.setSelection(password.getText().length());
            Toast.makeText(this, R.string.please_enter_password, Toast.LENGTH_SHORT).show();
            showIme(true, password);
            return;
        }

        if (TextUtils.isEmpty(capchaInput)) {
            capcha.requestFocus();
            capcha.setSelection(capcha.getText().length());
            Toast.makeText(this, R.string.please_enter_capcha, Toast.LENGTH_SHORT).show();
            showIme(true, capcha);
            return;
        }

        progress = new ProgressDialog(this);
        progress.setMessage(getResources().getString(R.string.loading_body));
        progress.show();
        auth.postRequest(loginInput, passwordInput, capchaInput);
    }

    /**
     * Reset login form
     */
    private void resetInputForm (){
        resetCache();

        login.setText("");
        password.setText("");
        capcha.setText("");

        Ion.with(capcha_image)
                .placeholder(null)
                .error(null)
                .load(Constants.CAPCHA_IMAGE);
    }

    /**
     * Check the availability of the Internet method
     */
    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnected();
    }

    /**
     * Show IME
     */
    private void showIme(boolean show, View view){

        InputMethodManager inputMethodManager=(InputMethodManager)this.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        if (show){
            inputMethodManager.toggleSoftInputFromWindow(view.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
        } else {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Alert dialog builder method for errors etc.
     *
     * @param error - Error text, which you need to show to user
     */
    public void errorDialog (String error) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(getApplicationContext().getString(R.string.errortitle))
                .setMessage(error)
                .setCancelable(false)
                .setNegativeButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void resetCache() {

        Ion.getDefault(getApplicationContext()).getBitmapCache().clear();
        Ion.getDefault(getApplicationContext()).configure().getResponseCache().clear();
        Ion.getDefault(getApplicationContext()).getCookieMiddleware().clear();
        Ion.getDefault(getApplicationContext()).getCache().clear();

    }

    /**
     * Overrided method for onAuthListener. This method will be called if authorization successful
     * @param result - received HTML page in response after successful authorization
     */
    @Override
    public void onSuccessfulAuth(Document result) {
        progress.dismiss();

        Credentials.getInstance(this).setLogin(login.getText().toString().trim());
        Credentials.getInstance(this).setPassword(password.getText().toString().trim());
        Settings.getInstance(this).setLogined(true);

        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();

    }

    /**
     * Overrided method for onAuthListener. This method will be called if authorization unsuccessful
     * @param error - error text for Toast or log
     * @param errorDialog - boolean flag - if true - dialog will be displayed with @param error as text
     */
    @Override
    public void onFailedAuth(String error, Boolean errorDialog) {
        progress.dismiss();

        resetInputForm();

        if (!errorDialog) {
            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
        } else {
            errorDialog(error);
        }

    }

    /**
     * Overrided method for onAuthListener. This method will be called if server not response.
     */
    @Override
    public void onServerError() {
        progress.dismiss();
        Toast.makeText(getApplicationContext(), R.string.server_not_response, Toast.LENGTH_SHORT).show();
    }
}

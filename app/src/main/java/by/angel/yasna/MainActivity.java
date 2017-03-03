package by.angel.yasna;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.CardProvider;
import com.dexafree.materialList.view.MaterialListView;
import com.koushikdutta.ion.Ion;

import org.jsoup.nodes.Document;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class MainActivity extends AppCompatActivity implements Auth.onAuthListener{

    final int REQUEST_CODE_LOGIN = 1;
    private Auth auth;
    private UserData userData;
    private SwipeRefreshLayout swipeContainer;
    AlertDialog.Builder exitDialog;

    LinearLayout error;
    private TextView errorText;
    LinearLayout progress;

    private MaterialListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(myToolbar);
        getSupportActionBar().hide();


        mListView = (MaterialListView) findViewById(R.id.material_listview);

        mListView.setItemAnimator(new SlideInUpAnimator());
        mListView.getItemAnimator().setAddDuration(300);
        mListView.getItemAnimator().setRemoveDuration(300);

        error = (LinearLayout) findViewById(R.id.error);
        errorText = (TextView) findViewById(R.id.errorText);
        progress = (LinearLayout) findViewById(R.id.progress);

        /**
         * Prepare logout dialog
         */
        exitDialog = new AlertDialog.Builder(this);
        exitDialog.setMessage(R.string.dialogExit);
        exitDialog.setPositiveButton(R.string.yes_btn, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                logout();
            }
        });
        exitDialog.setNegativeButton(R.string.no_btn, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                return;
            }
        });
        exitDialog.setCancelable(false);
        /***/

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setColorSchemeResources(R.color.brand1,
                R.color.brand2,
                R.color.brand3);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mListView.getAdapter().clearAll();
                checkAccount();
            }
        });

        auth = new Auth(this);

        checkAccount();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            exitDialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            switch (requestCode) {
                case REQUEST_CODE_LOGIN:
                    checkAccount();
                    break;
            }

        } else {
            finish();
        }
    }

    /**
     *  Check for an authorized account
     */
    private void checkAccount() {

        if (!Settings.getInstance(this).isLogined()) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivityForResult(intent, REQUEST_CODE_LOGIN);
        } else {
            authenticate();
        }

    }

    /**
     *  Authentication using the stored data
     */
    private void authenticate() {

        if (isConnected()) {

            progress.setVisibility(View.VISIBLE);

            String login = Credentials.getInstance(this).getLogin();
            String password = Credentials.getInstance(this).getPassword();

            /**
             * Clear cache before connect to server
             */
            Ion.getDefault(getApplicationContext()).getBitmapCache().clear();
            Ion.getDefault(getApplicationContext()).configure().getResponseCache().clear();
            Ion.getDefault(getApplicationContext()).getCookieMiddleware().clear();
            Ion.getDefault(getApplicationContext()).getCache().clear();

            auth.setAuthListener(this);
            auth.postRequest(login, password, null);

        } else {
            errorText.setText(getResources().getString(R.string.no_connection));
            error.setVisibility(View.VISIBLE);

        }

    }

    /**
     * Clear shared preferences and restart activity
     * FixMe restart activity code
     */
    private void logout() {
        if (Settings.getInstance(this).isLogined()) {

            Settings.getInstance(this).setLogined(false);
            Credentials.getInstance(this).setLogin(null);
            Credentials.getInstance(this).setPassword(null);

            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

    /**
     * Parse resulted page and filling data cards
     * @param mainPage
     */
    private void getUserData(Document mainPage) {

        Parser parser = new Parser(this);
        userData = parser.parseData(mainPage);

        addCard(getResources().getString(R.string.item_login), userData.login);
        addCard(getResources().getString(R.string.item_status), userData.status);
        addCard(getResources().getString(R.string.item_address), userData.phoneAddress);
        addCard(getResources().getString(R.string.item_activationdate), userData.acitvationDate);
        addCard(getResources().getString(R.string.item_appnumber), userData.appNum);
        addCard(getResources().getString(R.string.item_balance), userData.currentBalance + " " + getResources().getString(R.string.byn));
        addCard(getResources().getString(R.string.item_uname), userData.userName);

        setActionBar(userData.tarif);

        swipeContainer.setRefreshing(false);
    }

    /**
     * Method for adding card with account information.
     * @param param - mean field name
     * @param value - field value
     *              (Example: Current balance - 100 BYN)
     */
    private void addCard(String param, String value){

        Card card = new Card.Builder(this)
                .withProvider(new CardProvider())
                .setLayout(R.layout.cardlayout)
                .setTitle(param)
                .setDescription(value)
                .endConfig()
                .build();

        mListView.getAdapter().addAll(card);

    }

    /**
     * Set background color and title for ActionBar
     * Background color is taken from the official site of Beltelecom - Yasna
     * @param tarif - contains name of current user tariff
     */
    private void setActionBar(String tarif){

        if (getSupportActionBar()!=null) {

            getSupportActionBar().setTitle(getResources().getString(R.string.tariffplan) + " " + tarif);

            if (tarif.contains("10")) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.brand1)));
            }

            if (tarif.contains("25")) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.brand2)));
            }

            if (tarif.contains("50")) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.brand3)));
            }

            getSupportActionBar().show();

        }

    }


    /**
     * Interface implementation for Auth.class
     * START
     */
    @Override
    public void onSuccessfulAuth(Document result) {
        progress.setVisibility(View.INVISIBLE);
        error.setVisibility(View.INVISIBLE);
        getUserData(result);
    }

    @Override
    public void onFailedAuth(String errorString, Boolean errorDialog) {
        progress.setVisibility(View.INVISIBLE);
        errorText.setText(errorString);
        error.setVisibility(View.VISIBLE);
    }

    @Override
    public void onServerError() {
        progress.setVisibility(View.INVISIBLE);
        errorText.setText(getResources().getString(R.string.server_not_response));
        error.setVisibility(View.VISIBLE);
    }
    /** END */

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
}

package by.angel.yasna;

import android.content.Context;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.nio.charset.Charset;

public class Auth {

    private Context context;

    public interface onAuthListener {
        void onSuccessfulAuth(Document result);
        void onFailedAuth(String error, Boolean errorDialog);
        void onServerError();
    }

    private onAuthListener listener;

    public Auth(Context context){
        this.context=context;
        this.listener = null;
    }

    public void setAuthListener(onAuthListener listener) {
        this.listener = listener;
    }

    /**
     * We send the entered data to the server
     *
     * @param login
     * @param password
     * @param capcha (null if already logined user (SIGN IN)). If capcha !=null - this is registration POST request (LOGIN).
     */
    public void postRequest(String login, String password, String capcha){

        /** LOGIN */
        if (capcha!=null) {
            Ion.with(context)
                    .load(Constants.BASE_URL)
                    .setBodyParameter("oper_user", login)
                    .setBodyParameter("passwd", password)
                    .setBodyParameter("cap_field", capcha)
                    .asString()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<String>>() {

                        @Override
                        public void onCompleted(Exception e, Response<String> result) {

                            Document resultPage;

                            if (result!=null){
                                resultPage = Jsoup.parse(result.getResult());
                                checkCredentials(resultPage);
                            } else {
                                if (listener!=null) listener.onServerError();
                            }

                        }

                    });
        /** SIGN IN */
        } else {
            Ion.with(context)
                    .load(Constants.BASE_URL)
                    .setBodyParameter("oper_user", login)
                    .setBodyParameter("passwd", password)
                    .asString()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<String>>() {

                        @Override
                        public void onCompleted(Exception e, Response<String> result) {

                            if (result!=null){
                                Document resultPage = Jsoup.parse(result.getResult());
                                checkCredentials(resultPage);
                            } else {
                                if (listener!=null) listener.onServerError();
                            }

                        }

                    });
        }

    }

    /**
     * Analysis of the resulting HTML pages for login errors.
     *
     * @param resultPage - the resulting HTML page after sending the request to the server.
     */
    private void checkCredentials (Document resultPage) {

        /** Login or Password or Capcha incorrect */
        if (resultPage.getElementsByClass("alert").toString()!="") {
            if (listener!=null) listener.onFailedAuth(resultPage.getElementsByClass("alert").text(), false);
            return;
        }

        /** Too many login attemps */
        if (resultPage.getElementsByTag("center").text().equals(context.getResources().getString(R.string.error))){
            if (listener!=null) listener.onFailedAuth(resultPage.getElementsByTag("center").text(), true);
            return;
        }

        /** Successful login */
        if (resultPage.getElementsByTag("title").text().equals(context.getResources().getString(R.string.correctTitle))) {

            /** This request need, because after successful login server redirect to common page, which not content actual user information (balance, username, app number etc.)*/
            Ion.with(context)
                    .load(Constants.BASE_URL)
                    .asString(Charset.forName("UTF-8"))
                    .withResponse()
                    .setCallback(new FutureCallback<Response<String>>() {
                        @Override
                        public void onCompleted(Exception e, Response<String> result) {
                            if (result!=null){
                                Document mainPage = Jsoup.parse(result.getResult());
                                if (listener!=null) listener.onSuccessfulAuth(mainPage);
                            } else {
                                if (listener!=null) listener.onServerError();
                            }

                        }
                    });

        }

    }


}

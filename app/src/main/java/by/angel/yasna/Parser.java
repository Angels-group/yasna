package by.angel.yasna;

import android.content.Context;

import org.jsoup.nodes.Document;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private Context mContext;
    private UserData userData;

     Parser (Context context) {
        mContext = context;
        userData = new UserData();
    }

    public UserData parseData(Document mainPage) {
        String source = mainPage.toString();

        userData.currentBalance = findData(source, mContext.getResources().getString(R.string.regex_balance));
        userData.userName = findData(source, mContext.getResources().getString(R.string.regex_umane));
        userData.login = findData(source, mContext.getResources().getString(R.string.regex_login));
        userData.appNum = findData(source, mContext.getResources().getString(R.string.regex_appnum));
        userData.status = findData(source, mContext.getResources().getString(R.string.regex_status));
        userData.tarif = findData(source, mContext.getResources().getString(R.string.regex_tarif));
        userData.type = findData(source, mContext.getResources().getString(R.string.regex_type));
        userData.billingGroup = findData(source, mContext.getResources().getString(R.string.regex_billinggroup));
        userData.calcMethod = findData(source, mContext.getResources().getString(R.string.regex_calcmethod));
        userData.payMethod = findData(source, mContext.getResources().getString(R.string.regex_paymethod));
        userData.payList = findData(source, mContext.getResources().getString(R.string.regex_paylist));
        userData.acitvationDate = findData(source, mContext.getResources().getString(R.string.regex_activationdate));
        userData.phoneAddress = findData(source, mContext.getResources().getString(R.string.regex_phoneaddress));

        return userData;
    }

    private static String findData(String source, String regex) {
        Matcher balanceMatcher = Pattern.compile(regex).matcher(source);
        return balanceMatcher.find() ? balanceMatcher.group(1) : null;
    }

}

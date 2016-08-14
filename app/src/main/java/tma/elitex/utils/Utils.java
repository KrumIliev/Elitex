package tma.elitex.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.app.ActionBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import tma.elitex.R;

/**
 * Created by Krum Iliev
 */
public class Utils {

    public static void setupActionBar(Context context, String title, ActionBar bar) {
        TextView textView = new TextView(context);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(lp);
        textView.setText(title);
        textView.setTextSize(25);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setTextColor(context.getResources().getColor(R.color.colorTextLite));
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        bar.setCustomView(textView);
    }

    public static String getCurrentDate() {
        DateFormat df = new SimpleDateFormat(getDateFormat(),  Locale.ENGLISH);
        return df.format(Calendar.getInstance().getTime());
    }

    public static String getDateFormat() {
        return "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    }
}

package com.icecream.coronacoc;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Implementation of App Widget functionality.
 */
public class FeedWidget extends AppWidgetProvider {

    public static String WIDGET_BUTTON = "com.icecream.coronacoc.action.WIDGET_BUTTON";
    public static Context context_save;
    public static AppWidgetManager appWidgetManager_save;
    public static int appWidgetId_save;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        context_save = context;
        appWidgetManager_save = appWidgetManager;
        appWidgetId_save = appWidgetId;


        getApi(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.feed_widget);
        Intent configIntent = new Intent(context, MainActivity.class);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, PendingIntent.FLAG_MUTABLE);
        views.setOnClickPendingIntent(R.id.widget_layout, configPendingIntent);

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(WIDGET_BUTTON.equals(intent.getAction())) { //새로고침 버튼 눌렸을 때
            Toast.makeText(context, "위젯을 새로고침 했어요.", Toast.LENGTH_SHORT).show();
            getApi(context_save, appWidgetManager_save, appWidgetId_save);
        }

        super.onReceive(context, intent);
    }

    public static void getApi(Context context, AppWidgetManager appWidgetManager,
                              int appWidgetId) {
        new Thread() {
            public void run() {

                        try {
                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .url("https://apiv2.corona-live.com/domestic-init.json")
                                    .build(); //GET Request

                            //동기 처리시 execute함수 사용
                            Response response = client.newCall(request).execute();
                            String message = response.body().string();
                            JSONObject jsonObject = new JSONObject(message);

                            String stats = jsonObject.getString("stats");

                            JSONObject subJsonObject = new JSONObject(stats);
                            String cases[] = subJsonObject.getString("cases").split(",");
                            String deaths[] = subJsonObject.getString("deaths").split(",");
                            String servere[]  = subJsonObject.getString("patientsWithSevereSymptons").split(",");


                            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.feed_widget);
                            views.setTextViewText(R.id.confirmCases_txt, cases[0].replaceAll("\\[", "").replaceAll("]", "").replaceAll("\\B(?=(\\d{3})+(?!\\d))", ","));
                            views.setTextViewText(R.id.severe_txt, servere[0].replaceAll("\\[", "").replaceAll("]", "").replaceAll("\\B(?=(\\d{3})+(?!\\d))", ","));
                            views.setTextViewText(R.id.death_txt, deaths[0].replaceAll("\\[", "").replaceAll("]", "").replaceAll("\\B(?=(\\d{3})+(?!\\d))", ","));

                            views.setTextViewText(R.id.newcases_txt, "↑ "+cases[1].replaceAll("]", "").replaceAll("\\B(?=(\\d{3})+(?!\\d))", ","));
                            if(servere[1].replaceAll("]", "").indexOf('-') != -1) {
                                views.setTextViewText(R.id.newsevere_txt, "↓ "+servere[1].replaceAll("]", "").replaceAll("-", "").replaceAll("\\B(?=(\\d{3})+(?!\\d))", ","));
                            } else {
                                views.setTextViewText(R.id.newsevere_txt, "↑ "+servere[1].replaceAll("]", "").replaceAll("\\B(?=(\\d{3})+(?!\\d))", ","));
                            }

                            views.setTextViewText(R.id.newdeath_txt, "↑ "+deaths[1].replaceAll("]", "").replaceAll("\\B(?=(\\d{3})+(?!\\d))", ","));
                            appWidgetManager.updateAppWidget(appWidgetId, views);

                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }


            }
        }.start();

    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null) && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
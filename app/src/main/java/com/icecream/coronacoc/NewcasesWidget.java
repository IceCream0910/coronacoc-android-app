package com.icecream.coronacoc;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Shader;
import android.net.ConnectivityManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Implementation of App Widget functionality.
 */
public class NewcasesWidget extends AppWidgetProvider {

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

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.newcases_widget);



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

                //신규 확진자수 가져오기
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("https://api.corona-19.kr/korea/country/new/?serviceKey=5d4143bd958c16e18abe1acef5386c12d")
                            .build(); //GET Request

                    //동기 처리시 execute함수 사용
                    Response response = client.newCall(request).execute();
                    String message = response.body().string();
                    String newCase = message.substring(message.indexOf("newCase"), (message.indexOf("totalCase"))).replaceAll("\"", "").replaceAll(",", "").replaceAll(" ", "").replaceAll("newCase:", "");

                    //업데이트 일자 가져오기
                    try {
                        OkHttpClient client2 = new OkHttpClient();
                        Request request2 = new Request.Builder()
                                .url("https://api.corona-19.kr/korea/?serviceKey=5d4143bd958c16e18abe1acef5386c12d")
                                .build(); //GET Request

                        //동기 처리시 execute함수 사용
                        Response response2 = client2.newCall(request2).execute();
                        String message2 = response2.body().string();
                        String whenUpdate = message2.substring(message2.indexOf("updateTime"), (message2.indexOf("resultMessage"))).replaceAll("\"", "").replaceAll(",", "").replaceAll(" ", "").replaceAll("updateTime:", "").replaceAll("코로나바이러스감염증-19국내발생현황", "").replaceAll("\\)", "").replaceAll("\\(", "").replaceAll("시기준", "시 기준");

                        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.newcases_widget);
                        views.setTextViewText(R.id.newcases_txt, newCase);
                        views.setTextViewText(R.id.update_txt, whenUpdate);
                        appWidgetManager.updateAppWidget(appWidgetId, views);

                    } catch (Exception e) {
                        Log.e("taein", e.toString());
                    }



                } catch (Exception e) {
                    Log.e("taein", e.toString());
                }



            }
        }.start();

    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null) && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
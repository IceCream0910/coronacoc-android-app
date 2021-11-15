package com.icecream.coronacoc;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;

import static android.content.Context.MODE_PRIVATE;

import org.json.JSONException;
import org.json.JSONObject;


public class AlarmReceiver extends BroadcastReceiver {
    public String newCase, newDeath, newSevere ="";
    public int apiStatus = 0;
    public Context ct;

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    @Override
    public void onReceive(Context context, Intent intent) {

ct = context;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(context, MainActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingI = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_MUTABLE);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");


        //OREO API 26 이상에서는 채널 필요
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            builder.setSmallIcon(R.drawable.ic_launcher_foreground); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남


            String channelName ="매일 알람 채널";
            String description = "매일 정해진 시간에 알람합니다.";
            int importance = NotificationManager.IMPORTANCE_HIGH; //소리와 알림메시지를 같이 보여줌

            NotificationChannel channel = new NotificationChannel("default", channelName, importance);
            channel.setDescription(description);

            if (notificationManager != null) {
                // 노티피케이션 채널을 시스템에 등록
                notificationManager.createNotificationChannel(channel);
            }
        }else builder.setSmallIcon(R.mipmap.coronacoc_noti_foreground); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남



        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isNotice= prefs.getBoolean("notice", true);

        if(isNotice == true) {
            Log.e("taein", Boolean.toString(isNetworkConnected(ct))); //////////네트워크 상태 잘 안됨 210713 수정필요
            if (isNetworkConnected(ct) == false) {

            Log.e("taein", "인터넷 연결되어 있지 않음");
            builder.setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.coronacoc_noti_foreground)
                    .setTicker("{Time to watch some cool stuff!}")
                    .setContentTitle("인터넷에 연결되어 있지 않아요. ")
                    .setContentText("현황 정보를 가져오지 못했어요. 앱에서 확인해주세요.")
                    .setContentInfo("INFO")
                    .setContentIntent(pendingI);
        } else {


            getApi();

                while(apiStatus == 0) {
                    Log.e("taein", "알림 보낼 때 newCase : " + newCase);
                    builder.setAutoCancel(true)
                            .setDefaults(NotificationCompat.DEFAULT_ALL)
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(R.mipmap.coronacoc_noti_foreground)
                            .setTicker("{Time to watch some cool stuff!}")
                            .setContentTitle("코로나19 매일 브리핑 도착")
                            .setContentText("신규 확진 " + newCase + "명, 사망자 "+newDeath+"명 추가, 위중증 "+newSevere+"\n자세한 정보는 앱에서 확인해보세요.")
                            .setContentInfo("INFO")
                            .setContentIntent(pendingI);
                }
            }
        } else {Log.e("taein", "알림 꺼져있음");}

        Log.e("taein", "알림 전송 메커니즘 완료");

        if (notificationManager != null && isNotice == true) {

            // 노티피케이션 동작시킴
            notificationManager.notify(1234, builder.build());

            Calendar nextNotifyTime = Calendar.getInstance();

            // 내일 같은 시간으로 알람시간 결정
            nextNotifyTime.add(Calendar.DATE, 1);

            //  Preference에 설정한 값 저장
            SharedPreferences.Editor editor = context.getSharedPreferences("daily alarm", MODE_PRIVATE).edit();
            editor.putLong("nextNotifyTime", nextNotifyTime.getTimeInMillis());
            editor.apply();

            Date currentDateTime = nextNotifyTime.getTime();
            String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(currentDateTime);
            //Toast.makeText(context.getApplicationContext(),"다음 알람은 " + date_text + "으로 알람이 설정되었습니다!", Toast.LENGTH_SHORT).show();
        }

    }

    public void getApi() {
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

                    newCase = cases[1].replaceAll("]", "").replaceAll("\\B(?=(\\d{3})+(?!\\d))", ",");
                    if(servere[1].replaceAll("]", "").indexOf('-') != -1) {
                        newSevere = servere[1].replaceAll("]", "").replaceAll("-", "").replaceAll("\\B(?=(\\d{3})+(?!\\d))", ",") + "명 감소";
                    } else {
                        newSevere = servere[1].replaceAll("]", "").replaceAll("-", "").replaceAll("\\B(?=(\\d{3})+(?!\\d))", ",") + "명 증가";
                    }

                    newDeath = deaths[1].replaceAll("]", "").replaceAll("\\B(?=(\\d{3})+(?!\\d))", ",");
                    apiStatus = 1;

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


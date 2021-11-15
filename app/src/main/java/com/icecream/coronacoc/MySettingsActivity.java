package com.icecream.coronacoc;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.onesignal.OneSignal;

import java.io.File;

public class MySettingsActivity extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

       String userId = OneSignal.getDeviceState().getUserId();

        Preference customPref = (Preference) findPreference("onesignal_id");
        customPref.setSummary("사용자 ID : \n"+userId);
    }




}

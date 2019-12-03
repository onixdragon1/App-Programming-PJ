package com.example.reminderapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Java Class which is managing schedule.
 * It has 3 function (Save, Delete, cancel)
 * But it can conduct basic Select, Insert, Update, Delete (SIUD!)
 * @date 2019.12.01
 * @author Shin Juwon (github profile name : onixdragon1)
 * @version 1.0
 */

public class ManageSchedule extends Activity implements View.OnClickListener {
    ManageDB mDBHelper;
    int mId;
    String today;
    String this_hour, this_min;
    EditText editDate, editTitle, editMemo;
    TimePicker picker;
    int hour, min;
    public static Context MSActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_schedule);

        MSActivity = this;

        editDate = (EditText) findViewById(R.id.inputDate);
        editTitle = (EditText) findViewById(R.id.inputTitle);
        editMemo = (EditText) findViewById(R.id.inputMemo);
        picker = (TimePicker)findViewById(R.id.timePicker);

        Intent intent = getIntent();
        mId = intent.getIntExtra("ParamID", -1);
        today = intent.getStringExtra("ParamDate");

        mDBHelper = new ManageDB(getApplicationContext(), "Today.db", null, 1);

        if (mId == -1) {
            editDate.setText(today);
        } else {
            SQLiteDatabase db = mDBHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM today WHERE _id='" + mId + "'", null);
            String dateTime_hour = "", dateTime_min = "";
            String title_schedule="", memo_schedule="";
            if (cursor.moveToNext()) {
                title_schedule = cursor.getString(1);
                editTitle.setText(title_schedule);
                editDate.setText(cursor.getString(2));
                String timeGet = cursor.getString(3);
                dateTime_hour = timeGet.split(":")[0];
                dateTime_min = timeGet.split(":")[1];
                // cursor.getString(3).replace(":", "");
                if (Build.VERSION.SDK_INT >= 23 ){
                    picker.setHour(Integer.parseInt(dateTime_hour));
                    picker.setMinute(Integer.parseInt(dateTime_min));
                }
                else{
                    picker.setCurrentHour(Integer.parseInt(dateTime_hour));
                    picker.setCurrentMinute(Integer.parseInt(dateTime_min));
                }
                memo_schedule = cursor.getString(4);
                editMemo.setText(memo_schedule);
            }
            mDBHelper.close();
        }

        Button btn1 = (Button) findViewById(R.id.btnSave);
        btn1.setOnClickListener(this);
        Button btn2 = (Button) findViewById(R.id.btnDelete);
        btn2.setOnClickListener(this);
        Button btn3 = (Button) findViewById(R.id.btnCancel);
        btn3.setOnClickListener(this);

        if (mId == -1) {
            btn2.setVisibility(View.INVISIBLE);
        }
    }

    public String getEditMemo() {
        return editMemo.getText().toString();
    }

    public String getEditTitle() {
        return editTitle.getText().toString();
    }

    @Override
    public void onClick(View v) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        picker.setIs24HourView(true);

        switch (v.getId()) {
            case R.id.btnSave:
                if (mId != -1) {
                    if (Build.VERSION.SDK_INT >= 23 ){
                        hour = picker.getHour();
                        min = picker.getMinute();
                    } else {
                        hour = picker.getCurrentHour();
                        min = picker.getCurrentMinute();
                    }
                    Log.i("hour", hour+"");
                    Log.i("min", min+"");
                    db.execSQL("UPDATE today SET title='"
                            + editTitle.getText().toString() + "',date='"
                            + editDate.getText().toString() + "', time='"
                            + hour+"" + ":" + min+"" + "', memo='"
                            + editMemo.getText().toString() + "' WHERE _id='" + mId
                            + "';");
                    /* timepicker를 통해 정해진 시간에 알람을 설정하는 부분 */
                    /*SharedPreferences sharedPreferences = getSharedPreferences("daily alarm", MODE_PRIVATE);
                    long millis = sharedPreferences.getLong("nextNotifyTime", Calendar.getInstance().getTimeInMillis());
                    Calendar nextNotifyTime = new GregorianCalendar();
                    nextNotifyTime.setTimeInMillis(millis);

                    Date nextDate = nextNotifyTime.getTime();
                    String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(nextDate);
                    Toast.makeText(getApplicationContext(),"[처음 실행시] 다음 알람은 " + date_text + "으로 알람이 설정되었습니다!", Toast.LENGTH_SHORT).show();

                    Date currentTime = nextNotifyTime.getTime();
                    SimpleDateFormat HourFormat = new SimpleDateFormat("kk", Locale.getDefault());
                    SimpleDateFormat MinuteFormat = new SimpleDateFormat("mm", Locale.getDefault());

                    int pre_hour = Integer.parseInt(HourFormat.format(currentTime));
                    int pre_minute = Integer.parseInt(MinuteFormat.format(currentTime));*/

                    if (Build.VERSION.SDK_INT >= 23 ){
                        picker.setHour(hour);
                        picker.setMinute(min);
                    } else {
                        picker.setCurrentHour(hour);
                        picker.setCurrentMinute(min);
                    }

                    String alarm_time = picker.getCurrentHour() + ":" + picker.getCurrentMinute();

                    int this_hour, hour_24, this_min;
                    String am_pm;
                    if (Build.VERSION.SDK_INT >= 23 ){
                        hour_24 = picker.getHour();
                        this_min = picker.getMinute();
                    } else {
                        hour_24 = picker.getCurrentHour();
                        this_min = picker.getCurrentMinute();
                    }
                    if(hour_24 > 12) {
                        am_pm = "PM";
                        this_hour = hour_24 - 12;
                    } else {
                        this_hour = hour_24;
                        am_pm="AM";
                    }

                    // 현재 지정된 시간으로 알람 시간 설정
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.HOUR_OF_DAY, hour_24);
                    calendar.set(Calendar.MINUTE, this_min);
                    calendar.set(Calendar.SECOND, 0);

                    // 이미 지난 시간을 지정했다면 다음날 같은 시간으로 설정
                    if (calendar.before(Calendar.getInstance())) {
                        calendar.add(Calendar.DATE, 1);
                    }

                    Date currentDateTime = calendar.getTime();
                    String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(currentDateTime);
                    Toast.makeText(getApplicationContext(),date_text + "으로 알람이 설정되었습니다!", Toast.LENGTH_SHORT).show();

                    // Preference에 설정한 값 저장
                    SharedPreferences.Editor editor = getSharedPreferences("daily alarm", MODE_PRIVATE).edit();
                    editor.putLong("nextNotifyTime", (long)calendar.getTimeInMillis());
                    editor.apply();

                    diaryNotification(calendar);
                    /* timepicker 알람 설정 부분의 끝(DB가 끝나기 전에 할당해줌) */
                    //Toast.makeText(getApplicationContext(), "일정이 갱신되었습니다!", Toast.LENGTH_LONG).show();
                } else {
                    if (Build.VERSION.SDK_INT >= 23 ){
                        hour = picker.getHour();
                        min = picker.getMinute();
                    }
                    else{
                        hour = picker.getCurrentHour();
                        min = picker.getCurrentMinute();
                    }
                    db.execSQL("INSERT INTO today VALUES(null, '"
                            + editTitle.getText().toString() + "', '"
                            + editDate.getText().toString() + "', '"
                            + hour+"" + ":" + min+"" + "', '"
                            + editMemo.getText().toString() + "');");
                    /* timepicker를 통해 정해진 시간에 알람을 설정하는 부분 */
                    /*SharedPreferences sharedPreferences = getSharedPreferences("daily alarm", MODE_PRIVATE);
                    long millis = sharedPreferences.getLong("nextNotifyTime", Calendar.getInstance().getTimeInMillis());
                    Log.i("sharedPref", sharedPreferences+"");
                    Log.i("calendar component", Calendar.getInstance().getTimeInMillis()+"");
                    Log.i("time", millis+"");
                    Calendar nextNotifyTime = new GregorianCalendar();
                    nextNotifyTime.setTimeInMillis(millis);

                    Date nextDate = nextNotifyTime.getTime();
                    String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(nextDate);
                    Toast.makeText(getApplicationContext(),"[처음 실행시] 다음 알람은 " + date_text + "으로 알람이 설정되었습니다!", Toast.LENGTH_SHORT).show();

                    Log.i("this_date", nextDate+"");

                    Date currentTime = nextNotifyTime.getTime();
                    SimpleDateFormat HourFormat = new SimpleDateFormat("kk", Locale.getDefault());
                    SimpleDateFormat MinuteFormat = new SimpleDateFormat("mm", Locale.getDefault());

                    int pre_hour = Integer.parseInt(HourFormat.format(currentTime));
                    int pre_minute = Integer.parseInt(MinuteFormat.format(currentTime));

                    Log.i("this_hour", pre_hour+"시");
                    Log.i("this_minute", pre_minute+"분");*/

                    if (Build.VERSION.SDK_INT >= 23 ){
                        picker.setHour(hour);
                        picker.setMinute(min);
                    } else {
                        picker.setCurrentHour(hour);
                        picker.setCurrentMinute(min);
                    }

                    String alarm_time = picker.getCurrentHour() + ":" + picker.getCurrentMinute();

                    int this_hour, hour_24, this_min;
                    String am_pm;
                    if (Build.VERSION.SDK_INT >= 23) {
                        hour_24 = picker.getHour();
                        this_min = picker.getMinute();
                    } else {
                        hour_24 = picker.getCurrentHour();
                        this_min = picker.getCurrentMinute();
                    }

                    if(hour_24 > 12) {
                        am_pm = "PM";
                        this_hour = hour_24 - 12;
                    } else {
                        this_hour = hour_24;
                        am_pm="AM";
                    }

                    // 현재 지정된 시간으로 알람 시간 설정
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.HOUR_OF_DAY, hour_24);
                    calendar.set(Calendar.MINUTE, this_min);
                    calendar.set(Calendar.SECOND, 0);

                    // 이미 지난 시간을 지정했다면 다음날 같은 시간으로 설정
                    if (calendar.before(Calendar.getInstance())) {
                        calendar.add(Calendar.DATE, 1);
                    }

                    Date currentDateTime = calendar.getTime();
                    Log.i("this_time", currentDateTime+"");
                    String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(currentDateTime);
                    Toast.makeText(getApplicationContext(),date_text + "으로 알람이 설정되었습니다!", Toast.LENGTH_SHORT).show();

                    // Preference에 설정한 값 저장
                    SharedPreferences.Editor editor = getSharedPreferences("daily alarm", MODE_PRIVATE).edit();
                    editor.putLong("nextNotifyTime", (long)calendar.getTimeInMillis());
                    Log.i("AlarmTime", calendar.getTimeInMillis()+"");
                    editor.apply();

                    diaryNotification(calendar);
                    /* timepicker 알람 설정 부분의 끝(DB가 끝나기 전에 할당해줌) */
                    //Toast.makeText(getApplicationContext(), "일정이 추가되었습니다!", Toast.LENGTH_LONG).show();
                }
                mDBHelper.close();
                setResult(RESULT_OK);
                break;
            case R.id.btnDelete:
                if (mId != -1) {
                    db.execSQL("DELETE FROM today WHERE _id='" + mId + "';");
                    Toast.makeText(getApplicationContext(), "일정이 삭제되었습니다!", Toast.LENGTH_LONG).show();
                    mDBHelper.close();
                }
                setResult(RESULT_OK);
                break;
            case R.id.btnCancel:
                setResult(RESULT_CANCELED);
                break;
        }
        finish();
    }
    void diaryNotification(Calendar calendar){
        Boolean dailyNotify = true; // 무조건 알람을 사용

        PackageManager pm = this.getPackageManager();
        ComponentName receiver = new ComponentName(this.getApplicationContext(), DeviceBootReceiver.class);
        Intent alarmIntent = new Intent(this.getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // 사용자가 매일 알람을 허용했다면
        if (dailyNotify) {
            if (alarmManager != null) {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, pendingIntent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }
            // 부팅 후 실행되는 리시버 사용가능하게 설정
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        } else { //Disable Daily Notifications
            if (PendingIntent.getBroadcast(this, 0, alarmIntent, 0) != null && alarmManager != null) {
                alarmManager.cancel(pendingIntent);
                //Toast.makeText(this,"Notifications were disabled",Toast.LENGTH_SHORT).show();
            }
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }
}



package com.example.reminderapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.reminderapp.decorators.EventDecorator;
import com.example.reminderapp.decorators.OneDayDecorator;
import com.example.reminderapp.decorators.SaturdayDecorator;
import com.example.reminderapp.decorators.SundayDecorator;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Java Class which affects all of other Java Class
 * These codes may be disorderly at some point of view........
 * @date 2019.12.01
 * @author Shin Juwon (github profile name : onixdragon1)
 * @version 1.0
 */

public class MainActivity extends AppCompatActivity{
    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    Cursor cursor;
    MaterialCalendarView materialCalendarView;
    private PopupWindow popupWindow;
    ManageDB mDBHelper;
    SimpleCursorAdapter adapter;
    ListView list;
    Button addBtn;
    TextView dateTextView;
    String shot_Day;
    int popupLayout_Width, popupLayout_Height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        materialCalendarView = (MaterialCalendarView)findViewById(R.id.calendarView);

        materialCalendarView.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setMinimumDate(CalendarDay.from(2018, 0, 1)) // 달력의 시작
                .setMaximumDate(CalendarDay.from(2020, 12, 31)) // 달력의 끝
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();

        materialCalendarView.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator(),
            oneDayDecorator);

        String[] result = {"2017,03,18","2017,04,18","2017,05,18","2017,06,18"};

        // 싱글 스레드 작업
        // 백그라운드에서 이벤트 동작과 현재 시간을 체크하여 날짜가 바뀔 시 Decorator 이벤트 적용
        new ApiSimulator(result).executeOnExecutor(Executors.newSingleThreadExecutor());
        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                int Year = date.getYear();
                int Month = date.getMonth() + 1;
                int Day = date.getDay();

                shot_Day = Year + "/" + Month + "/" + Day;

                Log.i("shot_Day test", shot_Day + "");
                materialCalendarView.clearSelection();

                // 날짜 클릭 시 fragment를 이용해 일정 관리 영역을 달력 아래 띄워줌
                /*FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction  = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.listOfSchedule,  Fragment_scheduleList.newInstance(shot_Day));
                fragmentTransaction.commit();*/

                // 날짜 클릭 시 popupWindow를 이용해 일정 관리 영역을 달력 위에 오버래핑하여 띄워줌
                DisplayMetrics metric = getApplicationContext().getResources().getDisplayMetrics();
                popupLayout_Width = (int)(metric.widthPixels*0.75);
                popupLayout_Height = (int)(metric.heightPixels*0.7);

                View popupView = getLayoutInflater().inflate(R.layout.show_listofschedule, null);
                popupWindow = new PopupWindow(popupView, popupLayout_Width, popupLayout_Height);

                popupWindow.setFocusable(true);
                // 외부 영역 선택 시 PopUp 종료
                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

                addBtn = (Button)popupView.findViewById(R.id.btnScheduleAdd);
                dateTextView = (TextView)popupView.findViewById(R.id.todaySchedule);
                dateTextView.setText(shot_Day);

                mDBHelper = new ManageDB(getApplicationContext(), "Today.db", null, 1);
                SQLiteDatabase db = mDBHelper.getWritableDatabase();

                cursor = db.rawQuery(
                        "SELECT * FROM today WHERE date = '" + shot_Day + "'", null);

                // getActivity().getApplicationContext()를 원래는 Intent 호출을 통해 this 객체로 받았지만,
                // 여기는 fragment이기 때문에 먼저 액티비티를 받아와서(getActivity() 메서드)
                // 받아온 액티비티에서 실행할 것이다(getApplicationContext() 메서드)라고 사용해준다.
                // 여타 메서드들도 그렇게 사용한다.
                adapter = new SimpleCursorAdapter(getApplicationContext(),
                        android.R.layout.simple_list_item_2, cursor, new String[] {
                        "title", "time" }, new int[] { android.R.id.text1,
                        android.R.id.text2 });

                list = (ListView)popupView.findViewById(R.id.scheduleList);
                list.setAdapter(adapter);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(getApplicationContext(), ManageSchedule.class);
                        cursor.moveToPosition(position);
                        intent.putExtra("ScheduleID", cursor.getInt(0));
                        startActivityForResult(intent, 0);
                    }
                });

                mDBHelper.close();

                addBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), ManageSchedule.class);
                        intent.putExtra("ScheduleDate", shot_Day);
                        startActivityForResult(intent, 1);
                    }
                });
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
            case 1:
                if (resultCode == RESULT_OK) {
                    // adapter.notifyDataSetChanged();
                    SQLiteDatabase db = mDBHelper.getWritableDatabase();
                    cursor = db.rawQuery("SELECT * FROM today WHERE date = '"
                            + shot_Day + "'", null);
                    adapter.changeCursor(cursor);
                    mDBHelper.close();
                }
                break;
        }
    }

    // ApiSimulator라는 MainActivity 내부 클래스를 선언하여 싱글 스레드 비동기작업을 수행할 수 있는
    // 메서드와 리소스를 받아와서 시간별로 Background에서 시간을 체크해주고
    // addDecorator를 사용하여 토요일, 일요일, 오늘 날짜, 클릭 이벤트 등을 받아와 수행할 수 있도록 받아온다
    private class ApiSimulator extends AsyncTask<Void, Void, List<CalendarDay>> {
        String[] Time_Result;
        ApiSimulator(String[] Time_Result){
            this.Time_Result = Time_Result;
        }

        @Override
        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Calendar calendar = Calendar.getInstance();
            ArrayList<CalendarDay> dates = new ArrayList<>();

            /*특정날짜 달력에 점표시해주는곳*/
            /*월은 0이 1월 년,일은 그대로*/
            // string 문자열인 Time_Result 을 받아와서 ,를 기준으로짜르고 string을 int 로 변환
            for(int i = 0 ; i < Time_Result.length ; i ++){
                CalendarDay day = CalendarDay.from(calendar);
                String[] time = Time_Result[i].split(",");
                int year = Integer.parseInt(time[0]);
                int month = Integer.parseInt(time[1]);
                int days = Integer.parseInt(time[2]);

                dates.add(day);
                calendar.set(year,month-1,days);
            }
            return dates;
        }

        @Override
        protected void onPostExecute(@NonNull List<CalendarDay> calendarDays) {
            super.onPostExecute(calendarDays);
            if (isFinishing()) {
                return;
            }
            materialCalendarView.addDecorator(new EventDecorator(Color.RED, calendarDays,MainActivity.this));
        }
    }
}

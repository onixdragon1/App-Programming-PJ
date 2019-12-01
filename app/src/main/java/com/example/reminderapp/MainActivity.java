package com.example.reminderapp;

import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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

public class MainActivity extends AppCompatActivity{

    String time,kcal,menu;
    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    Cursor cursor;
    MaterialCalendarView materialCalendarView;
    LinearLayout showList;
    // private PopupWindow mPopupWindow ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        materialCalendarView = (MaterialCalendarView)findViewById(R.id.calendarView);
        showList = (LinearLayout)findViewById(R.id.listOfSchedule) ;

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

        new ApiSimulator(result).executeOnExecutor(Executors.newSingleThreadExecutor());
        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                int Year = date.getYear();
                int Month = date.getMonth() + 1;
                int Day = date.getDay();

                Log.i("Year test", Year + "");
                Log.i("Month test", Month + "");
                Log.i("Day test", Day + "");

                String shot_Day = Year + "/" + Month + "/" + Day;


                Log.i("shot_Day test", shot_Day + "");
                materialCalendarView.clearSelection();
                showList.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                // 날짜 클릭 시 fragment를 이용해 일정 관리 영역을 달력 아래 띄워줌
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction  = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.listOfSchedule,  Fragment_scheduleList.newInstance(shot_Day));
                fragmentTransaction.commit();
            }
        });
    }

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
            //string 문자열인 Time_Result 을 받아와서 ,를 기준으로짜르고 string을 int 로 변환
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

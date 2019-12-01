package com.example.reminderapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;

public class Fragment_scheduleList extends Fragment {
    ManageDB mDBHelper;
    Cursor cursor;
    SimpleCursorAdapter adapter;
    ListView list;
    String curDate;
    Button addBtn;
    TextView dateTextView;
    public Fragment_scheduleList() {}

    /*public interface OnFragmentButtonClickedListener{
        public void onButtonClicked(String date);
    }*/
    //private OnFragmentButtonClickedListener btnListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            curDate = getArguments().getString("날짜");
            Log.i("date", curDate);
        }
    }

    public static Fragment_scheduleList newInstance(String date){
        Fragment_scheduleList fragment = new Fragment_scheduleList();
        Bundle args = new Bundle(1);
        args.putString("날짜", date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.show_listofschedule, container, false);

        addBtn = (Button)view.findViewById(R.id.btnadd);
        dateTextView = (TextView)view.findViewById(R.id.texttoday);
        dateTextView.setText(curDate);

        mDBHelper = new ManageDB(getActivity(), "Today.db", null, 1);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        // 진짜 코드임
        /*cursor = db.rawQuery(
                "SELECT * FROM today WHERE date = '" + today + "'", null);*/

        cursor = db.rawQuery(
                "SELECT * FROM today WHERE date = '" + curDate + "'", null);

        // getActivity().getApplicationContext()를 원래는 Intent 호출을 통해 this 객체로 받았지만,
        // 여기는 fragment이기 때문에 먼저 액티비티를 받아와서(getActivity() 메서드)
        // 여기에서 실행할 것이다(getApplicationContext() 메서드)라고 사용해준다.
        // 여타 메서드들도 그렇게 사용한다.
        adapter = new SimpleCursorAdapter(getActivity().getApplicationContext(),
                android.R.layout.simple_list_item_2, cursor, new String[] {
                "title", "time" }, new int[] { android.R.id.text1,
                android.R.id.text2 });

        list = (ListView)view.findViewById(R.id.list1);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // getActivity().getApplicationContext()를 원래는 Intent 호출을 통해 this 객체로 받았지만,
                // 여기는 fragment이기 때문에 먼저 액티비티를 받아와서(getActivity() 메서드)
                // 여기에서 실행할 것이다(getApplicationContext() 메서드)라고 사용해준다.
                // 여타 메서드들도 그렇게 사용한다.
                Intent intent = new Intent(getActivity().getApplicationContext(), ManageSchedule.class);
                cursor.moveToPosition(position);
                intent.putExtra("ParamID", cursor.getInt(0));
                startActivityForResult(intent, 0);
            }
        });

        mDBHelper.close();

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // getActivity().getApplicationContext()를 원래는 Intent 호출을 통해 this 객체로 받았지만,
                // 여기는 fragment이기 때문에 먼저 액티비티를 받아와서(getActivity() 메서드)
                // 여기에서 실행할 것이다(getApplicationContext() 메서드)라고 사용해준다.
                // 여타 메서드들도 그렇게 사용한다.
                Intent intent = new Intent(getActivity().getApplicationContext(), ManageSchedule.class);
                intent.putExtra("ParamDate", curDate);
                startActivityForResult(intent, 1);
            }
        });
        return view;
    }

    // 새 창 열기(DB에 일정 추가) (리스트의 특정 아이템을 클릭한 경우)
    /*public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        Intent intent = new Intent(getActivity().getApplicationContext(), ManageSchedule.class);
        cursor.moveToPosition(position);
        intent.putExtra("ParamID", cursor.getInt(0));
        startActivityForResult(intent, 0);
    }*/

    // 새 창 열기(DB에 일정 추가) (추가 버튼 눌렀을 경우)
    /*public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent intent = new Intent(getActivity().getApplicationContext(), ManageSchedule.class);
        intent.putExtra("ParamDate", today);
        startActivityForResult(intent, 1);
    }*/


    /*protected*/
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        // super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
            case 1:
                if (resultCode == RESULT_OK) {
                    // adapter.notifyDataSetChanged();
                    SQLiteDatabase db = mDBHelper.getWritableDatabase();
                    cursor = db.rawQuery("SELECT * FROM today WHERE date = '"
                            + curDate + "'", null);
                    adapter.changeCursor(cursor);
                    mDBHelper.close();
                }
                break;
        }
    }
}

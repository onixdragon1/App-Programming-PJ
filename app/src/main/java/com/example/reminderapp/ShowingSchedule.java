package com.example.reminderapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ShowingSchedule extends Activity implements OnItemClickListener, OnClickListener {
    ManageDB mDBHelper;
    String today;
    Cursor cursor;
    SimpleCursorAdapter adapter;
    ListView list;
    TextView todayTv;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_listofschedule);

        todayTv = (TextView)findViewById(R.id.texttoday);
        today = todayTv.getText().toString();
        //Log.i("hi", today);

        mDBHelper = new ManageDB(this, "Today.db", null, 1);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        cursor = db.rawQuery(
                "SELECT * FROM today WHERE date = '" + today + "'", null);

        adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, cursor, new String[] {
                "title", "time" }, new int[] { android.R.id.text1,
                android.R.id.text2 });

        list = (ListView) findViewById(R.id.list1);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);

        mDBHelper.close();

        Button btn = (Button) findViewById(R.id.btnadd);
        btn.setOnClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        Intent intent = new Intent(this, ManageSchedule.class);
        cursor.moveToPosition(position);
        intent.putExtra("ParamID", cursor.getInt(0));
        startActivityForResult(intent, 0);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent intent = new Intent(this, ManageSchedule.class);
        intent.putExtra("ParamDate", today);
        startActivityForResult(intent, 1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        // super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
            case 1:
                if (resultCode == RESULT_OK) {
                    // adapter.notifyDataSetChanged();
                    SQLiteDatabase db = mDBHelper.getWritableDatabase();
                    cursor = db.rawQuery("SELECT * FROM today WHERE date = '"
                            + today + "'", null);
                    adapter.changeCursor(cursor);
                    mDBHelper.close();
                }
                break;
        }
    }
}

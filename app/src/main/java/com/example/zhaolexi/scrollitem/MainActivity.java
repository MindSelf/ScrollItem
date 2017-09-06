package com.example.zhaolexi.scrollitem;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        RecyclerView rv = (RecyclerView) findViewById(R.id.list);
        ArrayList<String> datas = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            datas.add("name " + i);
        }
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(datas,rv);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        rv.setAdapter(adapter);
        rv.setLayoutManager(manager);
    }
}

package com.example.yeomanda;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.yeomanda.ListView.FavoriteTeamListViewAdapter;
import com.example.yeomanda.Retrofit.ResponseDto.MyFavoriteListResponseDto;
import com.example.yeomanda.Retrofit.ResponseDto.ProfileResponseDto;
import com.example.yeomanda.Retrofit.ResponseDto.WithoutDataResponseDto;
import com.example.yeomanda.Retrofit.RetrofitClient;

public class MyFavoriteList extends AppCompatActivity {
    ListView myFavoriteTeamListView;
    String myToken;
    private Context context=this;
    RetrofitClient retrofitClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_favorite_list);
        Intent intent=getIntent();
        myToken=intent.getStringExtra("token");

        init();
    }

    public void init(){
        Log.d("tokenCheck",myToken);

        retrofitClient=new RetrofitClient();

        MyFavoriteListResponseDto myFavoriteListResponseDto=retrofitClient.showMyFavoriteTeamList(myToken);

        while(myFavoriteListResponseDto==null){
            Log.d("error","myFavoriteListResponseDto is null");
        }
        if(myFavoriteListResponseDto.getData()!=null) {
            myFavoriteTeamListView = findViewById(R.id.myFavoriteTeamListView);
            FavoriteTeamListViewAdapter adapter = new FavoriteTeamListViewAdapter();
            myFavoriteTeamListView.setAdapter(adapter);
            for (int i=0;i<myFavoriteListResponseDto.getData().size();i++){
                adapter.addItem(myFavoriteListResponseDto.getData().get(i).getTeamName(),myFavoriteListResponseDto.getData().get(i).getMember());
            }
        }
        myFavoriteTeamListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), MyFavoriteTeamProfile.class);
                intent.putExtra("teamName", myFavoriteListResponseDto.getData().get(position).getTeamName());
                intent.putExtra("token", myToken);
                startActivity(intent);
            }
        });
        //true????????? longclicklistener??? ??????, false????????? ?????? clicklistener??? ??????
        myFavoriteTeamListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                TextView textView=new TextView(getApplicationContext());
                textView.setText("\""+myFavoriteListResponseDto.getData().get(position).getTeamName()+"\"??????\n?????????????????? ?????? ???????????????????");
                textView.setGravity(Gravity.CENTER);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                textView.setTextColor(R.color.back);

                builder.setView(textView);
                //builder.setMessage(myFavoriteListResponseDto.getData().get(position).getTeamName()+"??????\n??????????????? ?????? ???????????????????");
                builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button

                        retrofitClient=new RetrofitClient();
                        WithoutDataResponseDto withoutDataResponseDto =retrofitClient.deleteFavoriteTeam(myToken,myFavoriteListResponseDto.getData().get(position).getTeamNum());
                        while(withoutDataResponseDto ==null){
                            Log.d("error", " withoutDataResponseDto is null");
                        }
                        if(withoutDataResponseDto.getSuccess()) {
                            init();
                        }
                    }
                });
                builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            }
        });

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        init();
    }

}
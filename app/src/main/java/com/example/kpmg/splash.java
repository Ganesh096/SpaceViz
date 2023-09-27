package com.example.kpmg;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;

public class splash extends AppCompatActivity {
    Handler handler;
    Animation topAnim;
    Animation bottomAnim;
    Animation textAnim;
    ImageView image1;
    TextView image2;
    TextView textView;
    Timer timer;
    //View splash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //splash = findViewById(R.id.splashScreen);

        /*splash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Login.class));

            }
        });*/

        handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(splash.this, Login.class);
                startActivity(intent);
                finish();
            }

        },2500);

        topAnim= AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottomAnim=AnimationUtils.loadAnimation(this,R.anim.bottom_animation);
        textAnim=AnimationUtils.loadAnimation(this,R.anim.text);
        image2=findViewById(R.id.textView6);
        image2.setAnimation(bottomAnim);
        image1=findViewById(R.id.imageView7);
        image1.setAnimation(topAnim);
        textView=findViewById(R.id.textView20);
        textView.setAnimation(textAnim);


    }
}
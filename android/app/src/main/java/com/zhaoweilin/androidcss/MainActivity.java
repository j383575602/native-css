package com.zhaoweilin.androidcss;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.zhaoweilin.css.CssDrawable;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.mImage);
        CssDrawable.sDensity = 3;
        //String css = "{shape:rect;fill:#878987;stroke-color:#123122;stroke-width:1px;stroke-style:dash;corner:10\\,0\\,0\\,10;width:100;height:100}";
        //String css = "{shape:rect;fill:grad(#878987,#878221,3);stroke-color:#123122;stroke-width:1px;stroke-style:dash;corner:10 10 10 10;width:100;height:100}";
        //String css = "{shape:rect;fill:sl(grad(#abcd21,#78723a,1)@3,grad(#123432,#898783,1)@1);stroke-color:sl(grad(#123122,#989823,1)@3,grad(#123122,#909892,1)@1);stroke-width:1px;stroke-style:dash(2,3,1);corner:2 2 2 2}";
        //String css = "{shape:rect;fill:sl(grad(#abcd21,#78723a,1)@3,grad(#123432,#898783,1)@1);stroke-color:#00FF00;stroke-width:1;stroke-style:dash(2,10,0);corner:5 5 5 5}";
        String css = "{shape:oval;fill:sl(grad(#abcd21,#78723a,1)@3,grad(#123432,#898783,1)@1);stroke-color:#00FF00;stroke-width:1;stroke-style:dash(2,10,0)}";
        CssDrawable drawable = new CssDrawable(css);
        imageView.setBackgroundDrawable(drawable);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"",Toast.LENGTH_SHORT).show();
            }
        });
    }
}

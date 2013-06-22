package com.romanblack.example.imagegallery;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import com.romanblack.android.widget.imagegallery.ImageGallery;
import java.util.ArrayList;

public class MainActivity extends Activity implements OnClickListener{
    private ImageGallery imgGallery = null;
    private ImageView leftEndButton = null;
    private ImageView rightEndButton = null;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.main);
        
        imgGallery = (ImageGallery)findViewById(R.id.img_gallery);
        
        ArrayList<String> paths = new ArrayList<String>();
        paths.add(Environment.getExternalStorageDirectory() + "/img/1");
        paths.add(Environment.getExternalStorageDirectory() + "/img/2");
        paths.add(Environment.getExternalStorageDirectory() + "/img/3");
        
        imgGallery.setPosition(2);
        
        imgGallery.setImagePaths(paths);
        
        leftEndButton = (ImageView)findViewById(R.id.left_end_button);
        leftEndButton.setOnClickListener(this);
        
        rightEndButton = (ImageView)findViewById(R.id.right_end_button);
        rightEndButton.setOnClickListener(this);
    }

    public void onClick(View arg0) {
        if(arg0 == leftEndButton){
            imgGallery.setPosition(0);
        }else if(arg0 == rightEndButton){
            imgGallery.setPosition(2);
        }
    }
}

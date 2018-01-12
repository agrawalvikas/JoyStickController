package app.akexorcist.joystickcontroller;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Main extends ActionBarActivity {
	RelativeLayout layout_joystick;
	TextView textView1, textView2, textView3, textView4, textView5;
	JoyStickClass js;
    Toolbar toolbar;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        textView1 = (TextView)findViewById(R.id.textView1);
        textView2 = (TextView)findViewById(R.id.textView2);
        textView3 = (TextView)findViewById(R.id.textView3);
        textView4 = (TextView)findViewById(R.id.textView4);
        textView5 = (TextView)findViewById(R.id.textView5);
        
	    layout_joystick = (RelativeLayout)findViewById(R.id.layout_joystick);

        js = new JoyStickClass(getApplicationContext()
        		, layout_joystick, R.drawable.image_button);
	    //js.setStickSize(200, 200);
	    js.setLayoutSize(650, 650);
	    js.setLayoutAlpha(150);
	    //js.setStickAlpha(100);
	    js.setOffset(90);
	    js.setMinimumDistance(50);

        textView1.setText("X : 0 " );
        textView2.setText("Y : 0" );
        textView3.setText("Angle : 0");
        textView4.setText("Distance : 0");
        textView5.setText("Direction : Center");
        //js.initDrawStick();

	    layout_joystick.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View arg0, MotionEvent arg1) {
				js.drawStick(arg1);
				if(arg1.getAction() == MotionEvent.ACTION_DOWN
						|| arg1.getAction() == MotionEvent.ACTION_MOVE) {
					textView1.setText("X : " + String.valueOf(js.getX()));
					textView2.setText("Y : " + String.valueOf(js.getY()));
					textView3.setText("Angle : " + String.valueOf(js.getAngle()));
					textView4.setText("Distance : " + String.valueOf(js.getDistance()));
					
					int direction = js.get8Direction();
					if(direction == JoyStickClass.STICK_UP) {
						textView5.setText("Direction : Up");
					} else if(direction == JoyStickClass.STICK_UPRIGHT) {
						textView5.setText("Direction : Up Right");
					} else if(direction == JoyStickClass.STICK_RIGHT) {
						textView5.setText("Direction : Right");
					} else if(direction == JoyStickClass.STICK_DOWNRIGHT) {
						textView5.setText("Direction : Down Right");
					} else if(direction == JoyStickClass.STICK_DOWN) {
						textView5.setText("Direction : Down");
					} else if(direction == JoyStickClass.STICK_DOWNLEFT) {
						textView5.setText("Direction : Down Left");
					} else if(direction == JoyStickClass.STICK_LEFT) {
						textView5.setText("Direction : Left");
					} else if(direction == JoyStickClass.STICK_UPLEFT) {
						textView5.setText("Direction : Up Left");
					} else if(direction == JoyStickClass.STICK_NONE) {
						textView5.setText("Direction : Center");
					}
				} else if(arg1.getAction() == MotionEvent.ACTION_UP) {
					textView1.setText("X : 0 ");
					textView2.setText("Y : 0");
					textView3.setText("Angle : 0");
					textView4.setText("Distance : 0");
					textView5.setText("Direction : Center");
                    //js.initDrawStick();
				}
				return true;
			}
        });
    } 	
}

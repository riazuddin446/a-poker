package com.zorrozua.asier;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class aPokerCustomButton extends Activity {

	public void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		super.onCreate(savedInstanceState);

//		// Create and set the Main layout
//		LinearLayout linLayoutMain = new LinearLayout(this);
//		linLayoutMain.setOrientation(LinearLayout.VERTICAL);
//		linLayoutMain.setGravity(Gravity.BOTTOM);
//
//		// Create and set the button's layout
//		LinearLayout linLayoutButtons = new LinearLayout(this);
//		linLayoutButtons.setOrientation(LinearLayout.HORIZONTAL);
//
//		// Create buttons
//		CustomButton btn1 = new CustomButton(this);
//		CustomButton btn2 = new CustomButton(this);
//
//		// Add button to the button's layout
//		linLayoutButtons.addView(btn1, new LinearLayout.LayoutParams(72, 72));
//		linLayoutButtons.addView(btn2, new LinearLayout.LayoutParams(72, 72));
//
//
//		// Add button's layout to the Main Layout
//		linLayoutMain.addView(linLayoutButtons, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		setContentView(new GameView(getApplicationContext()));
	}
	

}
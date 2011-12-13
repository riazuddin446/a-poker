package com.zorrozua.asier;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class GameViewCustomButton extends CustomButton {

	CustomButton btn1;
	CustomButton btn2;

	public GameViewCustomButton(Context context) {
		super(context);

		setBackgroundResource(R.drawable.menubackground);

		// Create and set the Main layout
		LinearLayout linLayoutMain = new LinearLayout(getContext());
		linLayoutMain.setOrientation(LinearLayout.VERTICAL);
		linLayoutMain.setGravity(Gravity.BOTTOM);

		// Create and set the button's layout
		LinearLayout linLayoutButtons = new LinearLayout(getContext());
		linLayoutButtons.setOrientation(LinearLayout.HORIZONTAL);

		// Create buttons
		btn1 = new CustomButton(getContext());
		btn2 = new CustomButton(getContext());

		// Add button to the button's layout
		linLayoutButtons.addView(btn1, new LinearLayout.LayoutParams(72, 72));
		linLayoutButtons.addView(btn2, new LinearLayout.LayoutParams(144, 144));


		// Add button's layout to the Main Layout
		linLayoutMain.addView(linLayoutButtons, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}

}

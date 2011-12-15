package com.zorrozua.asier;

import com.zorrozua.asier.pruebas.CustomButton;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class GameView extends CustomButton {

	public GameView(Context context) {
		super(context);

		setBackgroundResource(R.drawable.menubackground);

	}

}

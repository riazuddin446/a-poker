package com.zorrozua.asier;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class CustomButton extends Button {

	static final int StateDefault = 0;
	static final int StatePressed = 1;

	private int mState = StateDefault;
	private Bitmap mBitmapDefault;
	private Bitmap mBitmapPressed;

	public CustomButton(Context context) {

		super(context);

		setClickable(true);

		// Create a Bitmap for each state
		mBitmapDefault =  Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
		mBitmapPressed = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.a));

		// define OnClickListener for the Button
		setOnClickListener(onClickListener);
	}

	@Override

	protected void onDraw(Canvas canvas) {

		switch (mState) {

		case StateDefault:
			canvas.drawBitmap(mBitmapDefault, 0, 0, null);
			break;

		case StatePressed:
			canvas.drawBitmap(mBitmapPressed, 0, 0, null);
			break;
		}
	}



	@Override
	protected void drawableStateChanged() {

		if (isPressed()) {
			mState = StatePressed;
			Log.i("CustomButton", "BOTON PRESIONADO");
		} else {
			mState = StateDefault;
		}

		// force the redraw of the Image
		// onDraw will be called!
		invalidate();

	}

	private OnClickListener onClickListener = new OnClickListener() {
		public void onClick(View arg0) {
			
		}
	};

}

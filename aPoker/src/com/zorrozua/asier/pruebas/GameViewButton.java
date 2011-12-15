package com.zorrozua.asier.pruebas;

import com.zorrozua.asier.R;
import com.zorrozua.asier.R.drawable;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class GameViewButton extends View {

	Button mButton;

	public GameViewButton(Context context) {
		super(context);

		setBackgroundResource(R.drawable.menubackground);

		mButton = new Button(context);
		mButton.setText("Boton");
		mButton.setClickable(true);
		mButton.measure(MeasureSpec.getSize(this.getMeasuredWidth()), MeasureSpec.getSize(this.getMeasuredHeight()));
		mButton.layout(0, 0, MeasureSpec.getSize(mButton.getMeasuredWidth()), MeasureSpec.getSize(mButton.getMeasuredHeight()));

		mButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.i("GameView", "Button Pressed!");
			}
		});
	}

	public GameViewButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public GameViewButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher), 0, 0, null);
		mButton.draw(canvas);
		invalidate();
	}

}

package com.zorrozua.asier;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class aPoker extends BaseGameActivity {

	public void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.gamelayout);
	}

	@Override
	public Engine onLoadEngine() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onLoadResources() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Scene onLoadScene() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onLoadComplete() {
		// TODO Auto-generated method stub
		
	}
	

}
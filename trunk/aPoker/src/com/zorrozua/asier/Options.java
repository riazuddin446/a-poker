package com.zorrozua.asier;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class Options extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		
		Log.i("Options","Options started");
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.optionslayout);
		
		final Button soundButton = (Button) findViewById(R.id.soundButton);
		soundButton.setText("Sound ON");
		soundButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i("Options","Sound button clicked");
				if(soundButton.getText()=="Sound ON"){
					soundButton.setText("Sound OFF");
					//TODO Desactivar sonido
				}
				else{
					soundButton.setText("Sound ON");
					//TODO Activar sonido
				}
			}
		});
		
		//TODO About
		final Button aboutButton = (Button) findViewById(R.id.aboutButton);
		aboutButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i("Options","About button clicked");

			}
		});
		
		final Button backButton = (Button) findViewById(R.id.backButton);
		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i("Options","Back button clicked");
				finish();
			}
		});
	}

}

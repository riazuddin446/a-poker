package client;

import com.zorrozua.asier.APokerServer;
import com.zorrozua.asier.R;
import com.zorrozua.asier.R.id;
import com.zorrozua.asier.R.layout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class CreateTable extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		super.onCreate(savedInstanceState);

		setContentView(R.layout.createlayout);
		
		final Intent aPokerClient = new Intent(this, APokerServer.class);

		Button createButton = (Button) findViewById(R.id.createButton);
		createButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i("CreateTable","Create button clicked");
				startActivity(aPokerClient);
			}
		});
		
		Button backButton = (Button) findViewById(R.id.backButton);
		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i("CreateTable","Back button clicked");
				finish();
			}
		});
	}

}

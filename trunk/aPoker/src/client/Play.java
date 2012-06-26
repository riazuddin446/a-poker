package client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.zorrozua.asier.R;

public class Play extends Activity {

	public void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.joinorcreatelayout);

		Button joinButton = (Button) findViewById(R.id.joinButton);
		joinButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				accederPantallaJoin();
			}
		});

		Button createButton = (Button) findViewById(R.id.createButton);
		createButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				accederPantallaCreate();
			}
		});

		Button backButton = (Button) findViewById(R.id.backButton);
		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				volverPantallaAnterior();
			}
		});
	}
	
	private void accederPantallaJoin()
	{
		showMssg();
	}
	
	private void accederPantallaCreate()
	{
		final Intent createTable = new Intent(this, CreateTable.class);
		startActivity(createTable);
	}
	
	private void volverPantallaAnterior()
	{
		finish();
	}
	
	private void showMssg()
	{
		Toast.makeText(getApplicationContext(), "Coming soon...", 2).show();
	}

}

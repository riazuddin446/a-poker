package client;

import server.PServer;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.zorrozua.asier.R;

public class CreateTable extends Activity {

	public void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.createlayout);
		Toast.makeText(getApplicationContext(), "Changing settings will not affect the next game!", 2).show();

		Button createButton = (Button) findViewById(R.id.createButton);
		createButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				iniciarPartida();
			}
		});

		Button backButton = (Button) findViewById(R.id.backButton);
		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				volverPantallaAnterior();
			}
		});
	}

	private void iniciarPartida()
	{
		final Intent aPokerClient = new Intent(this, PServer.class);
		startActivity(aPokerClient);
	}

	private void volverPantallaAnterior()
	{
		finish();
	}

}

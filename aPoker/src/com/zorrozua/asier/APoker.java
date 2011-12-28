package com.zorrozua.asier;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

public class APoker extends BaseGameActivity {

	//	public void onCreate(Bundle savedInstanceState) {
	//
	//		requestWindowFeature(Window.FEATURE_NO_TITLE);
	//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	//
	//		super.onCreate(savedInstanceState);
	//
	//		setContentView(R.layout.gamelayout);
	//	}

	//	@Override
	//	public Engine onLoadEngine() {
	//		// TODO Auto-generated method stub
	//		return null;
	//	}
	//
	//	@Override
	//	public void onLoadResources() {
	//		// TODO Auto-generated method stub
	//		
	//	}
	//
	//	@Override
	//	public Scene onLoadScene() {
	//		// TODO Auto-generated method stub
	//		return null;
	//	}
	//
	//	@Override
	//	public void onLoadComplete() {
	//		// TODO Auto-generated method stub
	//		
	//	}

	Display display;   

	private static int CAMERA_WIDTH = 0;          
	private static int CAMERA_HEIGHT = 0;            

	private Camera mCamera;

	private BitmapTextureAtlas textura;          
	private TextureRegion pelotaTextura;

	//En onLoadEngine vamos a inicializar el motor del programa
	@Override
	public Engine onLoadEngine() {
		// TODO Auto-generated method stub

		//Capturamos la pantalla por defecto
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		display = wm.getDefaultDisplay();

		//Capturamos el ancho y alto de nuestra pantalla para configurar la camara
		CAMERA_WIDTH = display.getWidth();
		CAMERA_HEIGHT = display.getHeight();

		//Creamos la camara
		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);		

		//Creamos el motor del programa
		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera));

	}

	//En onLoadResources vamos a cargar los recursos necesarios para la aplicación, imagenes, musica, etc...
	@Override
	public void onLoadResources() {
		// TODO Auto-generated method stub

		//Creamos una nueva textura de 64x64, recuerda que debe ser potencia de 2
		this.textura = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);                  

		//Seleccionamos la carpeta assets/gfx/ como contenedor de nuestros recursos
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		//Cargamos la textura de la pelota en la textura
		this.pelotaTextura = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.textura, this, "a.png", 0, 0);

		//Cargamos la textura en memoria
		this.mEngine.getTextureManager().loadTexture(this.textura); 

	}


	//En onLoadScene preparamos la escena de nuestra app
	@Override
	public Scene onLoadScene() {
		//Creamos una nueva escena                   
		final Scene scene = new Scene(1);

		//Le ponemos un color de fondo verde
		scene.setBackground(new ColorBackground(0.0f, 255.0f, 0.0f));

		//Buscamos el centro de la pantalla
		final int centerX = (CAMERA_WIDTH - this.pelotaTextura.getWidth()) / 2;                  
		final int centerY = (CAMERA_HEIGHT - this.pelotaTextura.getHeight()) / 2;

		//Creamos nuestra pelota y la colocamos en el centro de la pantalla
		final Sprite pelota = new Sprite(centerX, centerY, this.pelotaTextura);                  

		//Añadimos la pelota a nuestra escena
		scene.getLastChild().attachChild(pelota);

		//Creamos el bucle de nuestra escena, aunque en este ejemplo no va a hacer nada
		scene.registerUpdateHandler(new IUpdateHandler() {
			@Override
			public void onUpdate(float arg0) {

				//Vacio
			}

			@Override
			public void reset() {
				//Vacio
			}
		});



		return scene;		

	}



	@Override
	public void onLoadComplete() {
		// TODO Auto-generated method stub

	}
}
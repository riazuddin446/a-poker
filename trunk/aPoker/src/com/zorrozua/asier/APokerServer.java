package com.zorrozua.asier;

import java.util.HashMap;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.scene.background.SpriteBackground;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.BaseSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.BaseTextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.util.Log;
import android.view.Display;
import android.widget.Toast;

public class APokerServer extends BaseGameActivity{

	// ===========================================================
	// Constants
	// ===========================================================

	//	private static final int CAMERA_WIDTH = 720;
	//	private static final int CAMERA_HEIGHT = 480;

	// ===========================================================
	// Fields
	// ===========================================================

	private static int cameraWidth;
	private static int cameraHeight;

	private Camera mCamera;
	private Scene mMainScene;

	private BitmapTextureAtlas mCardDeckTextureAtlas;
	private HashMap<Card, TextureRegion> mCardTotextureRegionMap;

	//Background
	private BitmapTextureAtlas mBackgroundTextureAtlas;
	private TextureRegion mBackgroundTexureRegion;
	Sprite backgroundSpriteSprite;
	SpriteBackground backgroundSprite;

	//Buttons
	private BitmapTextureAtlas mButtonsTextureAtlas;
	private HashMap<Button, TextureRegion> mButtonsTextureRegionMap;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public void setCameraWidth(int cameraWidth) {
		APokerServer.cameraWidth = cameraWidth;
	}

	public static int getCameraWidth() {
		return cameraWidth;
	}

	public void setCameraHeight(int cameraHeight) {
		APokerServer.cameraHeight = cameraHeight;
	}

	public static int getCameraHeight() {
		return cameraHeight;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public Engine onLoadEngine() {

		final Display display = getWindowManager().getDefaultDisplay();
		this.setCameraWidth(display.getWidth());
		this.setCameraHeight(display.getHeight());

		this.mCamera = new Camera(0, 0, getCameraWidth(), getCameraHeight());
		final Engine engine = new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(getCameraWidth(), getCameraHeight()), this.mCamera));

		Log.i("APokerCLient", "onLoadEngine correcto");
		return engine;
	}

	@Override
	public void onLoadResources() {

		//Set the path for graphics
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		//Load the background texture
		mBackgroundTextureAtlas = new BitmapTextureAtlas(1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mBackgroundTexureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBackgroundTextureAtlas, this,"gamebackground.png", 0, 0);
		mEngine.getTextureManager().loadTexture(mBackgroundTextureAtlas);

		// Extract the textures of each button
		this.mButtonsTextureAtlas = new BitmapTextureAtlas(1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		this.mButtonsTextureRegionMap = new HashMap<Button, TextureRegion>();

		int i = 0;

		for(final Button button : Button.values()){
			final TextureRegion buttonTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mButtonsTextureAtlas, this, button.name()+".png", i*button.BUTTON_HEIGHT, i*button.BUTTON_WIDTH);
			this.mButtonsTextureRegionMap.put(button, buttonTextureRegion);
			i++;
		}	

		this.mEngine.getTextureManager().loadTexture(this.mButtonsTextureAtlas);

		//Load the card deck into a TextureAtlas
		this.mCardDeckTextureAtlas = new BitmapTextureAtlas(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mCardDeckTextureAtlas, this, "carddeck_tiled.png", 0, 0);

		//Load each card texture into a HashMap
		this.mCardTotextureRegionMap = new HashMap<Card, TextureRegion>();

		/* Extract the TextureRegion of each card in the whole deck. */
		for(final Card card : Card.values()) {
			final TextureRegion cardTextureRegion = TextureRegionFactory.extractFromTexture(this.mCardDeckTextureAtlas, card.getTexturePositionX(), card.getTexturePositionY(), Card.CARD_WIDTH, Card.CARD_HEIGHT, true);
			this.mCardTotextureRegionMap.put(card, cardTextureRegion);
		}

		this.mEngine.getTextureManager().loadTexture(this.mCardDeckTextureAtlas);

		Log.i("APokerCLient", "onLoadResources correcto");
	}

	@Override
	public Scene onLoadScene() {

		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mMainScene = new Scene();
		this.mMainScene.setOnAreaTouchTraversalFrontToBack();

		backgroundSpriteSprite = new Sprite(0, 0, mBackgroundTexureRegion);
		backgroundSprite = new SpriteBackground(backgroundSpriteSprite);

		mMainScene.setBackground(backgroundSprite);

		//		this.addCard(Card.CLUB_ACE, 200, 100);
		//		this.addCard(Card.HEART_ACE, 200, 260);
		//		this.addCard(Card.DIAMOND_ACE, 440, 100);
		//		this.addCard(Card.SPADE_ACE, 440, 260);

		this.addButtons();

		mMainScene.setBackground(backgroundSprite);

		this.mMainScene.setTouchAreaBindingEnabled(true);

		Log.i("APokerCLient", "onLoadScene correcto");
		return this.mMainScene;
	}

	@Override
	public void onLoadComplete() {
		Log.i("APokerCLient", "onLoadComplete correcto");
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private void addCard(final Card pCard, final int pX, final int pY) {
		final Sprite sprite = new Sprite(pX, pY, this.mCardTotextureRegionMap.get(pCard));
		//		{
		//			boolean mGrabbed = false;
		//
		//			@Override
		//			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		//				switch(pSceneTouchEvent.getAction()) {
		//				case TouchEvent.ACTION_DOWN:
		//					this.setScale(1.25f);
		//					this.mGrabbed = true;
		//					break;
		//				case TouchEvent.ACTION_MOVE:
		//					if(this.mGrabbed) {
		//						this.setPosition(pSceneTouchEvent.getX() - Card.CARD_WIDTH / 2, pSceneTouchEvent.getY() - Card.CARD_HEIGHT / 2);
		//					}
		//					break;
		//				case TouchEvent.ACTION_UP:
		//					if(this.mGrabbed) {
		//						this.mGrabbed = false;
		//						this.setScale(1.0f);
		//					}
		//					break;
		//				}
		//				return true;
		//			}
		//		};

		this.mMainScene.attachChild(sprite);
		this.mMainScene.registerTouchArea(sprite);
	}

	private void addFoldButton(final int pX, final int pY){
		final Sprite sprite = new Sprite(pX, pY, this.mButtonsTextureRegionMap.get(Button.FOLD)){
			boolean mGrabbed = false;

			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch(pSceneTouchEvent.getAction()) {
				case TouchEvent.ACTION_DOWN:
					this.setScale(1.25f);
					this.mGrabbed = true;
					break;
				case TouchEvent.ACTION_UP:
					if(this.mGrabbed) {
						this.mGrabbed = false;
						this.setScale(1.0f);
					}
					break;
				}
				return true;
			}
		};
		this.mMainScene.attachChild(sprite);
		this.mMainScene.registerTouchArea(sprite);
	}

	private void addCheckButton(final int pX, final int pY){
		final Sprite sprite = new Sprite(pX, pY, this.mButtonsTextureRegionMap.get(Button.CHECK)){
			boolean mGrabbed = false;

			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch(pSceneTouchEvent.getAction()) {
				case TouchEvent.ACTION_DOWN:
					this.setScale(1.25f);
					this.mGrabbed = true;
					break;
				case TouchEvent.ACTION_UP:
					if(this.mGrabbed) {
						this.mGrabbed = false;
						this.setScale(1.0f);
					}
					break;
				}
				return true;
			}
		};
		this.mMainScene.attachChild(sprite);
		this.mMainScene.registerTouchArea(sprite);
	}

	private void addCallButton(final int pX, final int pY){
		final Sprite sprite = new Sprite(pX, pY, this.mButtonsTextureRegionMap.get(Button.CALL)){
			boolean mGrabbed = false;

			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch(pSceneTouchEvent.getAction()) {
				case TouchEvent.ACTION_DOWN:
					this.setScale(1.25f);
					this.mGrabbed = true;
					break;
				case TouchEvent.ACTION_UP:
					if(this.mGrabbed) {
						this.mGrabbed = false;
						this.setScale(1.0f);
					}
					break;
				}
				return true;
			}
		};
		this.mMainScene.attachChild(sprite);
		this.mMainScene.registerTouchArea(sprite);
	}

	private void addRaiseButton(final int pX, final int pY){
		final Sprite sprite = new Sprite(pX, pY, this.mButtonsTextureRegionMap.get(Button.RAISE)){
			boolean mGrabbed = false;

			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch(pSceneTouchEvent.getAction()) {
				case TouchEvent.ACTION_DOWN:
					this.setScale(1.25f);
					this.mGrabbed = true;
					break;
				case TouchEvent.ACTION_UP:
					if(this.mGrabbed) {
						this.mGrabbed = false;
						this.setScale(1.0f);
					}
					break;
				}
				return true;
			}
		};
		this.mMainScene.attachChild(sprite);
		this.mMainScene.registerTouchArea(sprite);
	}

	private void addExitButton(final int pX, final int pY){
		final Sprite sprite = new Sprite(pX, pY, this.mButtonsTextureRegionMap.get(Button.EXIT)){
			boolean mGrabbed = false;

			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch(pSceneTouchEvent.getAction()) {
				case TouchEvent.ACTION_DOWN:
					this.setScale(1.25f);
					this.mGrabbed = true;
					break;
				case TouchEvent.ACTION_UP:
					if(this.mGrabbed) {
						this.mGrabbed = false;
						this.setScale(1.0f);
						finish();
					}
					break;
				}
				return true;
			}
		};
		this.mMainScene.attachChild(sprite);
		this.mMainScene.registerTouchArea(sprite);
	}

	private void addButtons() {

		this.addFoldButton(0, cameraWidth - this.mButtonsTextureRegionMap.get(Button.FOLD).getWidth());
		this.addCheckButton(this.mButtonsTextureRegionMap.get(Button.CHECK).getHeight(), cameraWidth - this.mButtonsTextureRegionMap.get(Button.CHECK).getWidth()+10);
		this.addCallButton( cameraHeight/2 + this.mButtonsTextureRegionMap.get(Button.CALL).getHeight(),cameraWidth - this.mButtonsTextureRegionMap.get(Button.CALL).getWidth());
		this.addRaiseButton(cameraHeight - this.mButtonsTextureRegionMap.get(Button.RAISE).getHeight(), cameraWidth/2 + this.mButtonsTextureRegionMap.get(Button.RAISE).getWidth()*2 + 10);
		this.addExitButton(0, cameraWidth - this.mButtonsTextureRegionMap.get(Button.EXIT).getWidth());

	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

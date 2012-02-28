package com.zorrozua.asier;

import java.util.HashMap;

import logic.Card;
import logic.Player;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.SpriteBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import client.Button;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Display;

public class APokerServer extends BaseGameActivity{

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static int cameraWidth;
	private static int cameraHeight;

	private Camera mCamera;
	private Scene mMainScene;

	//Background
	private BitmapTextureAtlas mBackgroundTextureAtlas;
	private TextureRegion mBackgroundTexureRegion;
	Sprite backgroundSpriteSprite;
	SpriteBackground backgroundSprite;

	//Font
	private Font mFont;
	private Texture mFontTexture;

	//Buttons
	private BitmapTextureAtlas mButtonsTextureAtlas;
	private HashMap<Button, TextureRegion> mButtonsTextureRegionMap;

	//Card deck
	private BitmapTextureAtlas mCardDeckTextureAtlas;
	private HashMap<Card, TextureRegion> mCardTotextureRegionMap;

	//Player
	public Player mPlayer;

	//Player Name
	private ChangeableText mPlayerNameText;

	//Chip counter
	private ChangeableText mChipCounterText;

	//Ingame player list
	HashMap<Integer, Player> players;


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

		mPlayer = new Player("Asier", 4000, 1);

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

		//Load the font for texts
		this.mFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mFont = new Font(this.mFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 28, true, Color.WHITE);
		this.mEngine.getTextureManager().loadTexture(this.mFontTexture);
		this.mEngine.getFontManager().loadFont(this.mFont);

		// Extract and load the textures of each button
		this.mButtonsTextureAtlas = new BitmapTextureAtlas(1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mButtonsTextureRegionMap = new HashMap<Button, TextureRegion>();
		int i = 0;
		for(final Button button : Button.values()){
			final TextureRegion buttonTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mButtonsTextureAtlas, this, button.name()+".png", i*button.BUTTON_HEIGHT, i*button.BUTTON_WIDTH);
			this.mButtonsTextureRegionMap.put(button, buttonTextureRegion);
			i++;
		}	
		this.mEngine.getTextureManager().loadTexture(this.mButtonsTextureAtlas);

		//Extract and load the card deck textures
		this.mCardDeckTextureAtlas = new BitmapTextureAtlas(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mCardDeckTextureAtlas, this, "carddeck_tiled.png", 0, 0);
		this.mCardTotextureRegionMap = new HashMap<Card, TextureRegion>();
		for(final Card card : Card.values()) {
			final TextureRegion cardTextureRegion = TextureRegionFactory.extractFromTexture(this.mCardDeckTextureAtlas, card.getTexturePositionX(), card.getTexturePositionY(), Card.CARD_WIDTH, Card.CARD_HEIGHT, true);
			this.mCardTotextureRegionMap.put(card, cardTextureRegion);
		}
		this.mEngine.getTextureManager().loadTexture(this.mCardDeckTextureAtlas);



	}

	@Override
	public Scene onLoadScene() {

		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mMainScene = new Scene();
		this.mMainScene.setOnAreaTouchTraversalFrontToBack();

		//Setting the game background
		backgroundSpriteSprite = new Sprite(0, 0, mBackgroundTexureRegion);
		backgroundSprite = new SpriteBackground(backgroundSpriteSprite);
		mMainScene.setBackground(backgroundSprite);

		//Adding the player name to the screen
		this.mPlayerNameText =  new ChangeableText(0, 0, this.mFont, this.mPlayer.getPlayerName());
		this.mPlayerNameText.setPosition(getCameraWidth()/2 - mPlayerNameText.getWidth()/2, getCameraHeight() - mPlayerNameText.getHeight());
		mMainScene.attachChild(mPlayerNameText);

		//Adding the chip counter
		this.mChipCounterText = new ChangeableText(10, 10, this.mFont, Integer.toString(this.mPlayer.getStake()));
		mMainScene.attachChild(mChipCounterText);

		//Update handler para manejar los turnos
		this.mMainScene.registerUpdateHandler(new IUpdateHandler() {

			public void onUpdate(float pSecondsElapsed) {
				// TODO Auto-generated method stub
				//Your code to run here!
			}


			public void reset() {
				// TODO Auto-generated method stub

			}

		});
		
		this.paintCard(Card.CLUB_ACE, 200, 100);
		this.paintCard(Card.HEART_ACE, 200, 260);
		this.paintCard(Card.DIAMOND_ACE, 440, 100);
		this.paintCard(Card.SPADE_ACE, 440, 260);
		

		this.addButtons();

		this.mMainScene.setTouchAreaBindingEnabled(true);

		return this.mMainScene;
	}

	@Override
	public void onLoadComplete() {

	}

	// ===========================================================
	// Methods
	// ===========================================================

	private void paintCard(final Card pCard, final int pX, final int pY) {

		final Sprite sprite = new Sprite(pX, pY, this.mCardTotextureRegionMap.get(pCard));

		this.mMainScene.attachChild(sprite);
		this.mMainScene.registerTouchArea(sprite);
	}

	private void paintTurnedCard(final Card pCard, final int pX, final int pY) {

		final Sprite sprite = new Sprite(pX, pY, this.mCardTotextureRegionMap.get(pCard));

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

	private void addPlayer(final int position){

	}

	//This function adds the following buttons: Fold, Check, Call, Raise and Exit
	private void addButtons() {

		this.addFoldButton(0, getCameraHeight() - this.mButtonsTextureRegionMap.get(Button.FOLD).getHeight());
		this.addCheckButton(this.mButtonsTextureRegionMap.get(Button.FOLD).getWidth() + 15, getCameraHeight() - this.mButtonsTextureRegionMap.get(Button.CHECK).getHeight());
		this.addCallButton(getCameraWidth() - 2*(this.mButtonsTextureRegionMap.get(Button.RAISE).getWidth()) - 15, getCameraHeight() - this.mButtonsTextureRegionMap.get(Button.CALL).getHeight());
		this.addRaiseButton(getCameraWidth() - this.mButtonsTextureRegionMap.get(Button.RAISE).getWidth(), getCameraHeight() - this.mButtonsTextureRegionMap.get(Button.RAISE).getHeight());
		this.addExitButton(getCameraWidth() - this.mButtonsTextureRegionMap.get(Button.EXIT).getWidth(), 0);

	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

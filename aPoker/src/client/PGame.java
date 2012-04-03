package client;

import java.util.HashMap;

import logic.Card;
import logic.Player;
import logic.Player.Action;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
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

import server.GameController;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Display;

public class PGame extends BaseGameActivity{

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
	private TextureRegion mBackgroundTextureRegion;

	//Buttons
	private BitmapTextureAtlas mButtonsTextureAtlas;
	private HashMap<Button, TextureRegion> mButtonsTextureRegionMap;

	//Card deck
	private BitmapTextureAtlas mCardDeckTextureAtlas;
	private HashMap<Card, TextureRegion> mCardTotextureRegionMap;

	//Seat
	private BitmapTextureAtlas mSeatTextureAtlas;
	private TextureRegion mSeatTextureRegion;

	//Table related
	private ChangeableText mBettingRoundText;

	//Font
	private Font mFont;
	private Texture mFontTexture;

	//Player related
	private ChangeableText mPlayerNameText;
	private ChangeableText mPlayerStakeText;

	GameController mGameController;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public void setCameraWidth(int cameraWidth) {
		PGame.cameraWidth = cameraWidth;
	}

	public static int getCameraWidth() {
		return cameraWidth;
	}

	public void setCameraHeight(int cameraHeight) {
		PGame.cameraHeight = cameraHeight;
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

		return engine;
	}

	@Override
	public void onLoadResources() {

		//Set the path for graphics
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		//Load the background texture
		this.mBackgroundTextureAtlas = new BitmapTextureAtlas(1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mBackgroundTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBackgroundTextureAtlas, this,"game_table_background.png", 0, 0);

		// Extract and load the textures of each button
		this.mButtonsTextureAtlas = new BitmapTextureAtlas(1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mButtonsTextureRegionMap = new HashMap<Button, TextureRegion>();
		int i = 0;
		for(final Button button : Button.values()){
			final TextureRegion buttonTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mButtonsTextureAtlas, this, button.name()+".png", i*button.BUTTON_HEIGHT, i*button.BUTTON_WIDTH);
			this.mButtonsTextureRegionMap.put(button, buttonTextureRegion);
			i++;
		}	

		//Extract and load the card deck textures
		this.mCardDeckTextureAtlas = new BitmapTextureAtlas(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mCardDeckTextureAtlas, this, "carddeck_tiled.png", 0, 0);
		this.mCardTotextureRegionMap = new HashMap<Card, TextureRegion>();
		for(final Card card : Card.values()) {
			final TextureRegion cardTextureRegion = TextureRegionFactory.extractFromTexture(this.mCardDeckTextureAtlas, card.getTexturePositionX(), card.getTexturePositionY(), Card.CARD_WIDTH, Card.CARD_HEIGHT, true);
			this.mCardTotextureRegionMap.put(card, cardTextureRegion);
		}

		//Load the texture of seats
		this.mSeatTextureAtlas = new BitmapTextureAtlas(512, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mSeatTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mSeatTextureAtlas, this,"seat.png", 0, 0);

		//Load the font for texts
		this.mFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mFont = new Font(this.mFontTexture, Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC), 28, true, Color.BLACK);

		//Load the textures into the engine
		mEngine.getTextureManager().loadTextures(mBackgroundTextureAtlas,
				this.mButtonsTextureAtlas,
				this.mCardDeckTextureAtlas,
				this.mSeatTextureAtlas,
				this.mFontTexture);

		this.mEngine.getFontManager().loadFont(this.mFont);

	}

	@Override
	public Scene onLoadScene()
	{
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mMainScene = new Scene();
		this.mMainScene.setOnAreaTouchTraversalFrontToBack();

		//Setting the game background
		Sprite backgroundSprite = new Sprite(0, 0, mBackgroundTextureRegion);
		SpriteBackground backgroundSpriteBackgroudn = new SpriteBackground(backgroundSprite);
		this.mMainScene.setBackground(backgroundSpriteBackgroudn);

		this.addButtons();
		this.addSeats();

		this.addComunnityCard(Card.CLUB_ACE, 262, 175);
		this.addComunnityCard(Card.DIAMOND_THREE, 262 + 50+5, 175);
		this.addComunnityCard(Card.CLUB_EIGHT, 262 + (50+5)*2, 175); 
		//this.addComunnityCard(Card.CLUB_EIGHT, 262 + (50+5)*3, 175); 
		//this.addComunnityCard(Card.CLUB_EIGHT, 262 + (50+5)*4, 175); 

		this.initializeGame();

		this.addDebugPlayers();

		//Adding the player name to the screen
		this.mPlayerNameText =  new ChangeableText(0, 0, this.mFont, mGameController.players.get(mGameController.getOwner()).name);
		this.mPlayerNameText.setPosition(getCameraWidth()/2 - mPlayerNameText.getWidth()/2, getCameraHeight() - mPlayerNameText.getHeight());
		mMainScene.attachChild(mPlayerNameText);

		//Adding the chip counter
		this.mPlayerStakeText = new ChangeableText(10, 10, this.mFont, Integer.toString(mGameController.players.get(mGameController.getOwner()).stake));
		mMainScene.attachChild(mPlayerStakeText);

		this.mMainScene.registerUpdateHandler(new IUpdateHandler() {

			public void onUpdate(float pSecondsElapsed) {
				//updateInterface();
			}

			@Override
			public void reset() {

			}
		});

		//FIXME Hacer tick cuando sea necesario
		this.mMainScene.registerUpdateHandler(new IUpdateHandler() {

			public void onUpdate(float pSecondsElapsed) {
				//mGameController.tick();
			}

			@Override
			public void reset() {

			}
		});

		this.mMainScene.registerUpdateHandler(new TimerHandler(5f, true, new ITimerCallback() {
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler)
			{
				if(mGameController.getOwner() == 4)
					mGameController.setOwner(0);
				else
					mGameController.setOwner(mGameController.getOwner()+1);

				mPlayerNameText.setText(mGameController.players.get(mGameController.getOwner()).name);

				System.out.println("Owner: "+mGameController.getOwner()+" Name: "+mPlayerNameText.getText());
			}

		}));

		this.mMainScene.setTouchAreaBindingEnabled(true);

		return this.mMainScene;
	}

	@Override
	public void onLoadComplete()
	{	
		//this.gameLoop();
	}

	// ===========================================================
	// Methods
	// ===========================================================



	private void addComunnityCard(final Card pCard, final int pX, final int pY) 
	{
		final Sprite sprite = new Sprite(pX, pY, this.mCardTotextureRegionMap.get(pCard));

		this.mMainScene.attachChild(sprite);

		sprite.setScale(0.7f);
	}

	private void addSeat(final int pX, final int pY)
	{
		final Sprite sprite = new Sprite(pX, pY, this.mSeatTextureRegion);

		this.mMainScene.attachChild(sprite);
	}

	private void addSeats()
	{
		this.addSeat(15, 120);
		this.addSeat(15, 120 + 150);
		this.addSeat(getCameraWidth()-15-150, 120);
		this.addSeat(getCameraWidth()-15-150, 120 + 150);
	}

	//This function adds the following buttons: Fold, Check, Call, Raise and Exit
	private void addButtons() {

		this.addFoldButton(0, getCameraHeight() - this.mButtonsTextureRegionMap.get(Button.FOLD).getHeight());
		this.addCheckButton(this.mButtonsTextureRegionMap.get(Button.FOLD).getWidth() + 15, getCameraHeight() - this.mButtonsTextureRegionMap.get(Button.CHECK).getHeight());
		this.addCallButton(getCameraWidth() - 2*(this.mButtonsTextureRegionMap.get(Button.RAISE).getWidth()) - 15, getCameraHeight() - this.mButtonsTextureRegionMap.get(Button.CALL).getHeight());
		this.addRaiseButton(getCameraWidth() - this.mButtonsTextureRegionMap.get(Button.RAISE).getWidth(), getCameraHeight() - this.mButtonsTextureRegionMap.get(Button.RAISE).getHeight());
		this.addExitButton(getCameraWidth() - this.mButtonsTextureRegionMap.get(Button.EXIT).getWidth(), 0);

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

	private void initializeGame()
	{
		mGameController = new GameController();
		mGameController.setName("Prueba"); //FIXME Recibir el nombre del activity anterior
		mGameController.setMaxPlayers(5); //FIXME Recibir el numero maximo de jugadores del activity anterior
		mGameController.setPlayerStakes(4000);
		mGameController.setRestart(true);
		mGameController.setOwner(-1);
	}

	private void gameLoop()
	{
		if(mGameController.tick() < 0)
		{
			//Replicate game if "restart" is set
			if(mGameController.getRestart())
			{
				GameController newgame = new GameController();

				newgame.setName(mGameController.getName());
				newgame.setMaxPlayers(mGameController.getMaxPlayers());
				newgame.setPlayerStakes(mGameController.getPlayerStakes());
				newgame.setRestart(true);
				newgame.setOwner(mGameController.getOwner());

				mGameController = newgame;
			}
		}
	}

	private void updateInterface()
	{

	}

	/**
	 * Establece la acción que el jugador ha presionado
	 * 
	 * @param pid Id del jugador que realiza la acción (En nuestro caso el jugador actual)
	 * @param action La acción que desea realizar el jugador
	 * @param amount En caso necesario, la cantidad de fichas que gasta el jugador
	 */
	private void doSetAction(int pid, Player.Action action, int amount)
	{
		Player auxPlayer = this.mGameController.players.get(pid);

		Player.SchedAction auxSchedAction = auxPlayer.new SchedAction();
		auxSchedAction.valid = true;
		auxSchedAction.action = action;
		if(action == Action.Call || action == Action.Raise)
		{
			auxSchedAction.amount = amount;
		} else
			auxSchedAction.amount = 0;

		auxPlayer.setNextAction(auxSchedAction);

		this.mGameController.players.put(pid, auxPlayer);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	private void addDebugPlayers()
	{
		for(int i=0; i<5; i++)
		{
			//Add debug player
			Player degubPlayer = new Player("Asier"+i, this.mGameController.getPlayerStakes());
			this.mGameController.players.put(i, degubPlayer);
		}

		this.mGameController.setOwner(0);
		
		System.out.println("Players.size(): "+this.mGameController.players.size());
	}
}

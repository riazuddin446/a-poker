package client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

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
import org.anddev.andengine.entity.sprite.TiledSprite;
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
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import server.GameController;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Display;

public class PGame extends BaseGameActivity
{
	// ===========================================================
	// Constants
	// ===========================================================

	private HashMap<Integer, Integer> seats_pX = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> seats_pY = new HashMap<Integer, Integer>();

	// ===========================================================
	// Fields
	// ===========================================================

	private static int cameraWidth;
	private static int cameraHeight;

	private Camera mCamera;
	private Scene mMainScene;

	//Font
	private Font mFont;
	private Texture mFontTexture;

	//Background
	private BitmapTextureAtlas mBackgroundTextureAtlas;
	private TextureRegion mBackgroundTextureRegion;

	//Buttons
	private BitmapTextureAtlas mButtonsTextureAtlas;
	private HashMap<Button, TextureRegion> mButtonsTextureRegionMap;

	//Card deck
	private BitmapTextureAtlas mCardDeckTextureAtlas;
	private HashMap<Card, TextureRegion> mCardTotextureRegionMap;

	//Seat related
	private BitmapTextureAtlas mSeatTextureAtlas;
	private TiledTextureRegion mSeatTextureRegion;
	private HashMap<Integer, TiledSprite> mSeatSprites;

	//Table related
	private ChangeableText mBettingRoundText;

	//Game related
	private ChangeableText mTableStateText;

	//Community Cards
	private HashMap<Integer, Sprite> mCommunityCards;

	//Player related
	private HashMap<Integer, ChangeableText> mPlayerNamesText;
	private HashMap<Integer, ChangeableText> mPlayerStakesText;
	private HashMap<Integer, ChangeableText> mSeatBetText;

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
	public Engine onLoadEngine()
	{
		final Display display = getWindowManager().getDefaultDisplay();
		this.setCameraWidth(display.getWidth());
		this.setCameraHeight(display.getHeight());

		this.mCamera = new Camera(0, 0, getCameraWidth(), getCameraHeight());
		final Engine engine = new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(getCameraWidth(), getCameraHeight()), this.mCamera));

		//Set the reference position of the Seats in the view

		//Top left
		seats_pX.put(0, 15);
		seats_pY.put(0, 120);

		//Bottom left
		seats_pX.put(1, 15);
		seats_pY.put(1, 270);

		//Center
		seats_pX.put(2, getCameraWidth()/2-75);
		seats_pY.put(2, getCameraHeight()-85);

		//Top rigth
		seats_pX.put(3, getCameraWidth()-165);
		seats_pY.put(3, 120);

		//Bottom rigth
		seats_pX.put(4, getCameraWidth()-165);
		seats_pY.put(4, 270);

		return engine;
	}

	public void onLoadResources()
	{
		//Set the path for graphics
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		//Load the background texture
		this.mBackgroundTextureAtlas = new BitmapTextureAtlas(1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mBackgroundTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBackgroundTextureAtlas, this,"game_table_background.png", 0, 0);

		//Extract and load the textures of each button
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

		//Load the texture for seats
		this.mSeatTextureAtlas = new BitmapTextureAtlas(512, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mSeatTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mSeatTextureAtlas, this,"seat.png", 0, 0, 1, 2);


		//Load the font for texts
		this.mFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mFont = new Font(this.mFontTexture, Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC), 20, true, Color.BLACK);

		//Load the textures into the engine
		mEngine.getTextureManager().loadTextures(mBackgroundTextureAtlas,
				this.mButtonsTextureAtlas,
				this.mCardDeckTextureAtlas,
				this.mSeatTextureAtlas,
				this.mFontTexture);

		this.mEngine.getFontManager().loadFont(this.mFont);
	}

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

		this.initializeGameController();

		mCommunityCards = new HashMap<Integer, Sprite>();
		for(int i=0; i<5; i++)
		{
			Sprite aux = null;
			mCommunityCards.put(i, aux);
		}

		this.mBettingRoundText = new ChangeableText(0, 30, this.mFont, "Betting round: " + this.mGameController.table.betround.name());
		mMainScene.attachChild(mBettingRoundText);

		this.mTableStateText = new ChangeableText(0, 0, this.mFont, "Table state: " + this.mGameController.table.state.name());
		mMainScene.attachChild(mTableStateText);

		this.addDebugPlayers();

		this.mPlayerNamesText = new HashMap<Integer, ChangeableText>();
		for(int i=0; i<this.mGameController.table.seats.size(); i++)
		{
			ChangeableText aux = new ChangeableText(seats_pX.get(i)+5, seats_pY.get(i)+2, this.mFont, this.mGameController.table.seats.get(i).player.name);
			this.mPlayerNamesText.put(i, aux);
			mMainScene.attachChild(aux);
		}

		this.mPlayerStakesText = new HashMap<Integer, ChangeableText>();
		for(int i=0; i<this.mGameController.table.seats.size(); i++)
		{
			ChangeableText aux = new ChangeableText(seats_pX.get(i)+5, seats_pY.get(i)+20, this.mFont, Integer.toString(this.mGameController.table.seats.get(i).player.stake));
			this.mPlayerStakesText.put(i, aux);
			mMainScene.attachChild(aux);
		}

		this.mSeatBetText = new HashMap<Integer, ChangeableText>();
		for(int i=0; i<this.mGameController.table.seats.size(); i++)
		{
			ChangeableText aux = new ChangeableText(seats_pX.get(i)+5, seats_pY.get(i)+40, this.mFont, Integer.toString(this.mGameController.table.seats.get(i).bet));
			this.mSeatBetText.put(i, aux);
			mMainScene.attachChild(aux);
		}

		this.mMainScene.registerUpdateHandler(new IUpdateHandler() {

			public void onUpdate(float pSecondsElapsed) {
				updateInterface();
			}

			@Override
			public void reset() {

			}
		});

		this.mMainScene.registerUpdateHandler(new TimerHandler(5f, true, new ITimerCallback() {

			boolean flag = true;

			@Override
			public void onTimePassed(final TimerHandler pTimerHandler)
			{
				if(flag)
				{
					if(mGameController.table.communitycards.size() == 0)
					{
						mGameController.table.communitycards.setFlop(Card.CLUB_ACE, Card.CLUB_EIGHT, Card.CLUB_FIVE);
					}

					flag = false;
				}
				else
				{
					mGameController.table.communitycards.clear();
					flag = true;
				}
			}

		}));

		//FIXME Hacer tick cuando sea necesario
		this.mMainScene.registerUpdateHandler(new IUpdateHandler() {

			public void onUpdate(float pSecondsElapsed) {
				//mGameController.tick();
			}

			@Override
			public void reset() {

			}
		});

		this.mMainScene.setTouchAreaBindingEnabled(true);

		return this.mMainScene;
	}

	@Override
	public void onLoadComplete()
	{	
		//		System.out.println(mGameController.players.get(0).name);
		//		Player aux = mGameController.players.get(0);
		//		aux.name = "Palomo!";
		//		System.out.println(mGameController.players.get(0).name);

		//this.gameLoop();
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private void initializeGameController()
	{
		mGameController = new GameController();
		mGameController.setName("Prueba"); //FIXME Recibir el nombre del activity anterior
		mGameController.setMaxPlayers(5); //FIXME Recibir el numero maximo de jugadores del activity anterior
		mGameController.setPlayerStakes(4000);
		mGameController.setRestart(true);
		mGameController.setOwner(-1);
	}

	//This function adds the following buttons: Fold, Check, Call, Raise and Exit
	private void addButtons()
	{
		this.addFoldButton(0, getCameraHeight() - this.mButtonsTextureRegionMap.get(Button.FOLD).getHeight());
		this.addCheckButton(this.mButtonsTextureRegionMap.get(Button.FOLD).getWidth() + 15, getCameraHeight() - this.mButtonsTextureRegionMap.get(Button.CHECK).getHeight());
		this.addCallButton(getCameraWidth() - 2*(this.mButtonsTextureRegionMap.get(Button.RAISE).getWidth()) - 15, getCameraHeight() - this.mButtonsTextureRegionMap.get(Button.CALL).getHeight());
		this.addRaiseButton(getCameraWidth() - this.mButtonsTextureRegionMap.get(Button.RAISE).getWidth(), getCameraHeight() - this.mButtonsTextureRegionMap.get(Button.RAISE).getHeight());
		this.addExitButton(getCameraWidth() - this.mButtonsTextureRegionMap.get(Button.EXIT).getWidth(), 0);

	}

	private void addSeats()
	{
		mSeatSprites = new HashMap<Integer, TiledSprite>();

		for(int i=0; i<5; i++)
		{
			this.addSeat(seats_pX.get(i), seats_pY.get(i), i);
		}

		System.out.println("SEATSPRITES.SIZE(): "+mSeatSprites.size());
	}

	private void addDebugPlayers()
	{
		for(int i=0; i<5; i++)
		{
			//Add debug player
			Player debugPlayer = new Player("Asier"+i, i);
			this.mGameController.addPlayer(i, debugPlayer);
		}

		this.mGameController.setOwner(0);

		System.out.println("Players.size(): "+this.mGameController.players.size());
		System.out.println("Seats.size(): "+this.mGameController.table.seats.size());
	}

	private void addSeat(final int pX, final int pY, final int pos)
	{
		final TiledSprite sprite = new TiledSprite(pX, pY, this.mSeatTextureRegion);

		this.mMainScene.attachChild(sprite);

		mSeatSprites.put(pos, sprite);
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

	private void addComunnityCard(final Card pCard, final int pX, final int pY) 
	{
		final Sprite sprite = new Sprite(pX, pY, this.mCardTotextureRegionMap.get(pCard));

		this.mMainScene.attachChild(sprite);

		sprite.setScale(0.7f);
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
		//Draw current game state
		mTableStateText.setText(mGameController.table.state.name());

		//Draw current game betting round
		mBettingRoundText.setText(mGameController.table.betround.name());

		//Draw player names
		for(int i=0; i<mGameController.players.size(); i++)
		{
			mPlayerNamesText.get(i).setText(mGameController.players.get(i).getPlayerName());
		}

		//Draw player stakes
		for(int i=0; i<mGameController.players.size(); i++)
		{
			mPlayerStakesText.get(i).setText(String.valueOf(mGameController.players.get(i).getStake()));
		}

		//Draw seat bet
		for(int i=0; i<mGameController.players.size(); i++)
		{
			mSeatBetText.get(i).setText(String.valueOf(mGameController.table.seats.get(i).bet));
		}

		//Draw current player indicator (seat)
		for(int i=0; i<mSeatSprites.size(); i++)
		{
			int owner = mGameController.getOwner();

			if(i == owner)
				mSeatSprites.get(i).setCurrentTileIndex(1);
			else 
				mSeatSprites.get(i).setCurrentTileIndex(0);
		}

		//Draw Community cards
		ArrayList<Card> cmcards = mGameController.table.communitycards.cards;
		int cmsize = mGameController.table.communitycards.size();
		for(int i=0; i<5;i++)
		{
			if(i<cmsize)
			{
				System.out.println("cmsize" + cmsize);
				System.out.println("i" + i);

				Sprite aux = mCommunityCards.get(i);
				aux = new Sprite(262+55*i, 175, mCardTotextureRegionMap.get(cmcards.get(i)));
				aux.setScale(0.7f);
				mMainScene.attachChild(aux);
			}
			else if(cmsize == 0)
				mMainScene.detachChild(mCommunityCards.get(i));
		}
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



}

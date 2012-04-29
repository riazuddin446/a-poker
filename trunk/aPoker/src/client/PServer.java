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

public class PServer extends BaseGameActivity
{
	boolean debugFlag = true;
	Sprite debugSprite = null;

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

	private Camera camera;
	private Scene mainScene;

	//Font
	private Font font;
	private Texture fontTexture;

	//Background
	private BitmapTextureAtlas backgroundTextureAtlas;
	private TextureRegion backgroundTextureRegion;

	//Buttons
	private BitmapTextureAtlas buttonsTextureAtlas;
	private HashMap<Button, TextureRegion> buttonToTextureRegionMap;

	//Card deck
	private BitmapTextureAtlas cardDeckTextureAtlas;
	private HashMap<Card, TextureRegion> cardTotextureRegionMap;

	//Seat related
	private BitmapTextureAtlas seatTextureAtlas;
	private TiledTextureRegion seatTiledTextureRegion;
	private HashMap<Integer, TiledSprite> seatSprites;

	//Game related
	private ChangeableText tableStateText;

	//Table related
	private ChangeableText bettingRoundText;

	//Community Cards
	private HashMap<Integer, Sprite> communityCardsSprites;

	//Player related
	private HashMap<Integer, ChangeableText> playerNamesText;
	private HashMap<Integer, ChangeableText> playerStakesText;
	private HashMap<Integer, ChangeableText> seatBetText;

	GameController mGameController;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public void setCameraWidth(int cameraWidth) {
		PServer.cameraWidth = cameraWidth;
	}

	public static int getCameraWidth() {
		return cameraWidth;
	}

	public void setCameraHeight(int cameraHeight) {
		PServer.cameraHeight = cameraHeight;
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

		this.camera = new Camera(0, 0, getCameraWidth(), getCameraHeight());
		final Engine engine = new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(getCameraWidth(), getCameraHeight()), this.camera));

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
		this.backgroundTextureAtlas = new BitmapTextureAtlas(1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.backgroundTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.backgroundTextureAtlas, this,"game_table_background.png", 0, 0);

		//Extract and load the textures of each button
		this.buttonsTextureAtlas = new BitmapTextureAtlas(1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.buttonToTextureRegionMap = new HashMap<Button, TextureRegion>();
		int i = 0;
		for(final Button button : Button.values()){
			final TextureRegion buttonTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.buttonsTextureAtlas, this, button.name()+".png", i*button.BUTTON_HEIGHT, i*button.BUTTON_WIDTH);
			this.buttonToTextureRegionMap.put(button, buttonTextureRegion);
			i++;
		}	

		//Extract and load the card deck textures
		this.cardDeckTextureAtlas = new BitmapTextureAtlas(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.cardDeckTextureAtlas, this, "carddeck_tiled.png", 0, 0);
		this.cardTotextureRegionMap = new HashMap<Card, TextureRegion>();
		for(final Card card : Card.values()) {
			final TextureRegion cardTextureRegion = TextureRegionFactory.extractFromTexture(this.cardDeckTextureAtlas, card.getTexturePositionX(), card.getTexturePositionY(), Card.CARD_WIDTH, Card.CARD_HEIGHT, true);
			this.cardTotextureRegionMap.put(card, cardTextureRegion);
		}

		//Load the texture for seats
		this.seatTextureAtlas = new BitmapTextureAtlas(512, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.seatTiledTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.seatTextureAtlas, this,"seat.png", 0, 0, 1, 2);


		//Load the font for texts
		this.fontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.font = new Font(this.fontTexture, Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC), 20, true, Color.BLACK);

		//Load the textures into the engine
		mEngine.getTextureManager().loadTextures(backgroundTextureAtlas,
				this.buttonsTextureAtlas,
				this.cardDeckTextureAtlas,
				this.seatTextureAtlas,
				this.fontTexture);

		this.mEngine.getFontManager().loadFont(this.font);
	}

	public Scene onLoadScene()
	{
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mainScene = new Scene();
		this.mainScene.setOnAreaTouchTraversalFrontToBack();

		//Setting the game background
		Sprite backgroundSprite = new Sprite(0, 0, backgroundTextureRegion);
		SpriteBackground backgroundSpriteBackgroudn = new SpriteBackground(backgroundSprite);
		this.mainScene.setBackground(backgroundSpriteBackgroudn);

		this.addButtons();

		this.addSeats();

		this.initializeGameController();

		communityCardsSprites = new HashMap<Integer, Sprite>();
		for(int i=0; i<5; i++)
		{
			Sprite aux = null;
			communityCardsSprites.put(i, aux);
		}

		this.bettingRoundText = new ChangeableText(0, 30, this.font, "Betting round: " + this.mGameController.table.betround.name());
		mainScene.attachChild(bettingRoundText);

		this.tableStateText = new ChangeableText(0, 0, this.font, "Table state: " + this.mGameController.table.state.name());
		mainScene.attachChild(tableStateText);

		this.addDebugPlayers();

		this.playerNamesText = new HashMap<Integer, ChangeableText>();
		for(int i=0; i<this.mGameController.table.seats.size(); i++)
		{
			ChangeableText aux = new ChangeableText(seats_pX.get(i)+5, seats_pY.get(i)+2, this.font, this.mGameController.table.seats.get(i).player.name);
			this.playerNamesText.put(i, aux);
			mainScene.attachChild(aux);
		}

		this.playerStakesText = new HashMap<Integer, ChangeableText>();
		for(int i=0; i<this.mGameController.table.seats.size(); i++)
		{
			ChangeableText aux = new ChangeableText(seats_pX.get(i)+5, seats_pY.get(i)+20, this.font, Integer.toString(this.mGameController.table.seats.get(i).player.stake));
			this.playerStakesText.put(i, aux);
			mainScene.attachChild(aux);
		}

		this.seatBetText = new HashMap<Integer, ChangeableText>();
		for(int i=0; i<this.mGameController.table.seats.size(); i++)
		{
			ChangeableText aux = new ChangeableText(seats_pX.get(i)+5, seats_pY.get(i)+40, this.font, Integer.toString(this.mGameController.table.seats.get(i).bet));
			this.seatBetText.put(i, aux);
			mainScene.attachChild(aux);
		}

		this.mainScene.registerUpdateHandler(new IUpdateHandler() {

			public void onUpdate(float pSecondsElapsed) {
				updateInterface();
			}

			@Override
			public void reset() {

			}
		});

		//FIXME Hacer tick cuando sea necesario
		this.mainScene.registerUpdateHandler(new IUpdateHandler() {

			public void onUpdate(float pSecondsElapsed) {
				//mGameController.tick();
			}

			@Override
			public void reset() {

			}
		});

		//		this.mMainScene.registerUpdateHandler(new TimerHandler(2f, true, new ITimerCallback() {
		//
		//			int flag = 0;
		//
		//			@Override
		//			public void onTimePassed(final TimerHandler pTimerHandler)
		//			{
		//
		//				if(flag==0)
		//				{
		//					System.out.println("SET FLOP");
		//					if(mGameController.table.communitycards.size() == 0)
		//					{
		//						mGameController.table.communitycards.setFlop(Card.CLUB_ACE, Card.CLUB_EIGHT, Card.CLUB_FIVE);
		//					}
		//
		//					flag = 1;
		//				}
		//				else if(flag==1)
		//				{
		//					System.out.println("SET TURN");
		//
		//					mGameController.table.communitycards.setTurn(Card.CLUB_ACE);
		//
		//					flag = 2;
		//				}
		//				else if(flag==2)
		//				{
		//					System.out.println("SET RIVER");
		//
		//					mGameController.table.communitycards.setRiver(Card.CLUB_ACE);
		//
		//					flag = 3;
		//				}
		//				else if(flag==3)
		//				{					
		//					System.out.println("CLEAR CARDS");
		//					mGameController.table.communitycards.clear();
		//					flag = 0;
		//				}
		//			}
		//
		//		}));

		this.mainScene.setTouchAreaBindingEnabled(true);

		return this.mainScene;
	}

	@Override
	public void onLoadComplete()
	{	
		gameLoop();
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
		tableStateText.setText("Table state: " + mGameController.table.state.name());

		//Draw current game betting round
		bettingRoundText.setText("Betting round: " + mGameController.table.betround.name());

		//Draw player names
		for(int i=0; i<mGameController.players.size(); i++)
		{
			playerNamesText.get(i).setText(mGameController.players.get(i).getPlayerName());
		}

		//Draw player stakes
		for(int i=0; i<mGameController.players.size(); i++)
		{
			playerStakesText.get(i).setText(String.valueOf(mGameController.players.get(i).getStake()));
		}

		//Draw seat bet
		for(int i=0; i<mGameController.players.size(); i++)
		{
			seatBetText.get(i).setText(String.valueOf(mGameController.table.seats.get(i).bet));
		}

		//Draw current player indicator (seat)
		for(int i=0; i<seatSprites.size(); i++)
		{
			int owner = mGameController.getOwner();

			if(i == owner){
				System.out.println("THE OWNER: " + i);
				seatSprites.get(i).setCurrentTileIndex(1);
			}
			else if(i != owner) {
				seatSprites.get(i).setCurrentTileIndex(0);
			}
		}

		//Draw/Undraw Community cards
		ArrayList<Card> cmcards = mGameController.table.communitycards.cards; //Get community cards
		int cmsize = mGameController.table.communitycards.size(); //Get the number of cards

		for(int i=0; i<5;i++)
		{
			if(i<cmsize) //Add sprite
			{
				//System.out.println("ADD SPRITE at pos: " + i);

				//System.out.println(mCommunityCardsSprites.get(i));

				if(communityCardsSprites.get(i) == null) //If the sprite is not created and attached yet
				{
					//Create new Sprite with the needed card texture
					Sprite aux = new Sprite(262+55*i, 175, cardTotextureRegionMap.get(cmcards.get(i)));
					aux.setScale(0.7f);

					//Add it to the Array who saves the sprites of the Community Cards
					communityCardsSprites.put(i, aux);

					//Attach it to the scene
					//System.out.println("CARD TO BE ATTACHED: " + aux);
					mainScene.attachChild(communityCardsSprites.get(i));
				}
			}
			else //Delete sprite
			{
				//System.out.println("DELETE SPRITE at pos: " + i + " | Sprite: " + mCommunityCardsSprites.get(i));

				if(communityCardsSprites.get(i) != null)
				{
					//System.out.println("Delete? " + mMainScene.detachChild(mCommunityCardsSprites.get(i)));
					communityCardsSprites.put(i, null);
				}
			}
		}


	}

	private void addSeats()
	{
		seatSprites = new HashMap<Integer, TiledSprite>();

		for(int i=0; i<5; i++)
		{
			this.addSeat(seats_pX.get(i), seats_pY.get(i), i);
		}

		System.out.println("SEATSPRITES.SIZE(): "+seatSprites.size());
	}

	private void addDebugPlayers()
	{
		for(int i=0; i<5; i++)
		{
			//Add debug player
			Player debugPlayer = new Player("Asier"+i, i);
			this.mGameController.addPlayer(i, debugPlayer);
		}

		this.mGameController.setOwner(3);

		System.out.println("Players.size(): "+this.mGameController.players.size());
		System.out.println("Seats.size(): "+this.mGameController.table.seats.size());
	}

	private void addSeat(final int pX, final int pY, final int pos)
	{
		final TiledSprite sprite = new TiledSprite(pX, pY, this.seatTiledTextureRegion);

		this.mainScene.attachChild(sprite);

		seatSprites.put(pos, sprite);
	}

	private void addComunnityCard(final Card pCard, final int pX, final int pY) 
	{
		final Sprite sprite = new Sprite(pX, pY, this.cardTotextureRegionMap.get(pCard));

		this.mainScene.attachChild(sprite);

		sprite.setScale(0.7f);
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
		//Player auxPlayer = this.mGameController.players.get(pid);

		Player.SchedAction auxSchedAction = this.mGameController.players.get(pid).new SchedAction();
		auxSchedAction.valid = true;
		auxSchedAction.action = action;
		if(action == Action.Call || action == Action.Raise)
		{
			auxSchedAction.amount = amount;
		} else
			auxSchedAction.amount = 0;

		this.mGameController.players.get(pid).setNextAction(auxSchedAction);

		//this.mGameController.players.put(pid, auxPlayer);
	}

	private void addFoldButton(final int pX, final int pY){
		final Sprite sprite = new Sprite(pX, pY, this.buttonToTextureRegionMap.get(Button.FOLD)){
			boolean mGrabbed = false;

			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch(pSceneTouchEvent.getAction()) {
				case TouchEvent.ACTION_DOWN:
					this.setScale(1.25f);
					this.mGrabbed = true;

					doSetAction(mGameController.getOwner(), Player.Action.Fold, 0);

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
		this.mainScene.attachChild(sprite);
		this.mainScene.registerTouchArea(sprite);
	}

	private void addCheckButton(final int pX, final int pY){
		final Sprite sprite = new Sprite(pX, pY, this.buttonToTextureRegionMap.get(Button.CHECK)){
			boolean mGrabbed = false;

			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch(pSceneTouchEvent.getAction()) {
				case TouchEvent.ACTION_DOWN:
					this.setScale(1.25f);
					this.mGrabbed = true;

					doSetAction(mGameController.getOwner(), Player.Action.Check, 0);

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
		this.mainScene.attachChild(sprite);
		this.mainScene.registerTouchArea(sprite);
	}

	private void addCallButton(final int pX, final int pY){
		final Sprite sprite = new Sprite(pX, pY, this.buttonToTextureRegionMap.get(Button.CALL)){
			boolean mGrabbed = false;

			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch(pSceneTouchEvent.getAction()) {
				case TouchEvent.ACTION_DOWN:
					this.setScale(1.25f);
					this.mGrabbed = true;

					doSetAction(mGameController.getOwner(), Player.Action.Call, 0); //TODO Pop up para insertar la cantidad

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
		this.mainScene.attachChild(sprite);
		this.mainScene.registerTouchArea(sprite);
	}

	private void addRaiseButton(final int pX, final int pY){
		final Sprite sprite = new Sprite(pX, pY, this.buttonToTextureRegionMap.get(Button.RAISE)){
			boolean mGrabbed = false;

			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch(pSceneTouchEvent.getAction()) {
				case TouchEvent.ACTION_DOWN:
					this.setScale(1.25f);
					this.mGrabbed = true;

					doSetAction(mGameController.getOwner(), Player.Action.Raise, 0); //TODO Pop up para insertar la cantidad

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
		this.mainScene.attachChild(sprite);
		this.mainScene.registerTouchArea(sprite);
	}

	private void addExitButton(final int pX, final int pY){
		final Sprite sprite = new Sprite(pX, pY, this.buttonToTextureRegionMap.get(Button.EXIT)){
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
		this.mainScene.attachChild(sprite);
		this.mainScene.registerTouchArea(sprite);
	}

	//This function adds the following buttons: Fold, Check, Call, Raise and Exit
	private void addButtons()
	{
		this.addFoldButton(0, getCameraHeight() - this.buttonToTextureRegionMap.get(Button.FOLD).getHeight());
		this.addCheckButton(this.buttonToTextureRegionMap.get(Button.FOLD).getWidth() + 15, getCameraHeight() - this.buttonToTextureRegionMap.get(Button.CHECK).getHeight());
		this.addCallButton(getCameraWidth() - 2*(this.buttonToTextureRegionMap.get(Button.RAISE).getWidth()) - 15, getCameraHeight() - this.buttonToTextureRegionMap.get(Button.CALL).getHeight());
		this.addRaiseButton(getCameraWidth() - this.buttonToTextureRegionMap.get(Button.RAISE).getWidth(), getCameraHeight() - this.buttonToTextureRegionMap.get(Button.RAISE).getHeight());
		this.addExitButton(getCameraWidth() - this.buttonToTextureRegionMap.get(Button.EXIT).getWidth(), 0);

	}
}

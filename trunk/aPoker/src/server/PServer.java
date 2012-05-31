package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import logic.Card;
import logic.Player;
import logic.Player.Action;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
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

import server.Table.Pot;
import server.Table.Seat;
import server.Table.State;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Display;
import android.widget.EditText;
import android.widget.Toast;
import client.Button;

public class PServer extends BaseGameActivity
{
	// ===========================================================
	// Elements
	// ===========================================================

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
	private Font blackFont;
	private Texture blackFontTexture;

	private Font whiteFont;
	private Texture whiteFontTexture;

	//Background
	private BitmapTextureAtlas backgroundTextureAtlas;
	private TextureRegion backgroundTextureRegion;

	//Buttons
	private BitmapTextureAtlas buttonsTextureAtlas;
	private HashMap<Button, TiledTextureRegion> buttonToTextureRegionMap;

	//Card deck
	private BitmapTextureAtlas cardDeckTextureAtlas;
	private HashMap<Card, TextureRegion> cardTotextureRegionMap;

	//Seat related
	private BitmapTextureAtlas seatTextureAtlas;
	private TextureRegion seatTextureRegion;
	private TextureRegion currentSeatTextureRegion;
	private ArrayList<Sprite> seatSprites;

	//Dealer and blind buttons
	private BitmapTextureAtlas dealerAndBlindTextureAtlas;
	private TextureRegion dealerTextureRegion;
	private TextureRegion smallBlindTextureRegion;
	private TextureRegion bigBlindTextureRegion;
	private Sprite dealerButton;
	private Sprite smallBlindButton;
	private Sprite bigBlindButton;

	//Game related
	private ChangeableText tableStateText;

	//Table related
	private ChangeableText bettingRoundText;

	//Community Cards
	private ArrayList<Sprite> communityCardSprites;

	//Hole Cards
	private ArrayList< ArrayList<Sprite> > holeCardSprites;

	//Player related
	int dealer, sb, bb;
	private ArrayList<ChangeableText> playerNameTexts;
	private ArrayList<ChangeableText> playerStakeTexts;
	private ArrayList<ChangeableText> seatBetText;

	//Pot related
	private ArrayList<ChangeableText> potsText;

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

		return engine;
	}

	@Override
	public void onLoadResources()
	{
		//Set the path for graphics
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		//Load the BACKGROUND texture
		this.backgroundTextureAtlas = new BitmapTextureAtlas(1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.backgroundTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.backgroundTextureAtlas, this,"game_table_background.png", 0, 0);

		//Extract and load the textures of each BUTTON
		this.buttonsTextureAtlas = new BitmapTextureAtlas(2048, 2048, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.buttonToTextureRegionMap = new HashMap<Button, TiledTextureRegion>();
		int i = 0;
		for(final Button button : Button.values()){
			final TiledTextureRegion buttonTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.buttonsTextureAtlas, this, button.name()+".png", i*button.BUTTON_HEIGHT, i*button.BUTTON_WIDTH, 1, 2);
			this.buttonToTextureRegionMap.put(button, buttonTextureRegion);
			i++;
		}	

		//Extract and load the CARD DECK textures
		this.cardDeckTextureAtlas = new BitmapTextureAtlas(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.cardDeckTextureAtlas, this, "carddeck_tiled.png", 0, 0);
		this.cardTotextureRegionMap = new HashMap<Card, TextureRegion>();
		for(final Card card : Card.values()) {
			final TextureRegion cardTextureRegion = TextureRegionFactory.extractFromTexture(this.cardDeckTextureAtlas, card.getTexturePositionX(), card.getTexturePositionY(), Card.CARD_WIDTH, Card.CARD_HEIGHT, true);
			this.cardTotextureRegionMap.put(card, cardTextureRegion);
		}

		//Load the texture for SEATS
		this.seatTextureAtlas = new BitmapTextureAtlas(512, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.seatTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.seatTextureAtlas, this,"seat.png", 0, 0);
		this.currentSeatTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.seatTextureAtlas, this,"current_seat.png", 0, 85);

		//Load the textures for the DEALER and BLINDS buttons
		this.dealerAndBlindTextureAtlas = new BitmapTextureAtlas(512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.dealerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.dealerAndBlindTextureAtlas, this,"dealer.png", 0, 0);
		this.smallBlindTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.dealerAndBlindTextureAtlas, this,"smallblind.png", 0, 30);
		this.bigBlindTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.dealerAndBlindTextureAtlas, this,"bigblind.png", 0, 60);

		//Load the font for TEXT
		this.blackFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.blackFont = new Font(this.blackFontTexture, Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC), 20, true, Color.BLACK);

		this.whiteFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.whiteFont = new Font(this.whiteFontTexture, Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC), 20, true, Color.WHITE);

		//Load the textures into the engine
		mEngine.getTextureManager().loadTextures(this.backgroundTextureAtlas,
				this.buttonsTextureAtlas,
				this.cardDeckTextureAtlas,
				this.seatTextureAtlas,
				this.dealerAndBlindTextureAtlas,
				this.blackFontTexture,
				this.whiteFontTexture);
		//Load the fonts into the engine
		this.mEngine.getFontManager().loadFonts(this.blackFont,
				this.whiteFont);
	}

	@Override
	public Scene onLoadScene()
	{
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mainScene = new Scene();
		this.mainScene.setOnAreaTouchTraversalFrontToBack();

		//Setting the game background
		Sprite backgroundSprite = new Sprite(0, 0, backgroundTextureRegion);
		SpriteBackground backgroundSpriteBackgroudn = new SpriteBackground(backgroundSprite);
		this.mainScene.setBackground(backgroundSpriteBackgroudn);

		addButtons();

		addSeats();

		initializeGameController();

		addDebugPlayers();

		initializeGUIElements();

		stateUpdater();
		bettingRoundUpdater();
		currentPlayerUpdater();
		playerNameUpdater();
		playerStakeUpdater();
		seatBetUpdater();
		potUpdater();
		buttonUpdater();
		communityCardUpdater();
		holeCardUpdater();

		this.mainScene.registerUpdateHandler(new IUpdateHandler() {
			@Override
			public void onUpdate(float pSecondsElapsed) {
				gameLoop();
			}

			@Override
			public void reset() {
				// TODO Auto-generated method stub

			}
		});

		this.mainScene.setTouchAreaBindingEnabled(true);

		return this.mainScene;
	}

	@Override
	public void onLoadComplete()
	{	
		Toast.makeText(getApplicationContext(), "This is just a Poker game emulation", 2).show();
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private void initializeGameController()
	{
		mGameController = new GameController();
		mGameController.setName("Prueba"); //FIXME Recibir el nombre del activity anterior
		mGameController.setMaxPlayers(5); //FIXME Recibir el numero maximo de jugadores del activity anterior
		mGameController.setPlayerStakes(2000);
		mGameController.setRestart(false);
		mGameController.setOwner(-1);
	}

	/**
	 * Encargado de inicializar todos los elementos que irán en la interfaz gráfica: Textos e imagenes
	 * Así como sus contenedores.
	 */
	private void initializeGUIElements()
	{
		//Texto donde se mostrará el estado de la mesa
		bettingRoundText = new ChangeableText(0, 30, blackFont, mGameController.table.betround.name());
		mainScene.attachChild(bettingRoundText);

		//Texto donde se mostrará la ronda de apuestas de la mesa
		tableStateText = new ChangeableText(0, 0, blackFont, mGameController.table.state.name());
		mainScene.attachChild(tableStateText);

		//Nombres de los jugadores
		playerNameTexts = new ArrayList<ChangeableText>();
		for(int i=0; i<5; i++)
		{
			ChangeableText aux = new ChangeableText(seats_pX.get(i)+7, seats_pY.get(i)+5, blackFont, "        ");
			aux.setVisible(false);

			playerNameTexts.add(i, aux);
			mainScene.attachChild(aux);
		}

		//Fichas de los jugadores
		playerStakeTexts = new ArrayList<ChangeableText>();
		for(int i=0; i<5; i++)
		{
			ChangeableText aux = new ChangeableText(seats_pX.get(i)+7, seats_pY.get(i)+25, blackFont, "        ");
			aux.setVisible(false);

			playerStakeTexts.add(i, aux);
			mainScene.attachChild(aux);
		}

		//Apuestas de los jugadores
		seatBetText = new ArrayList<ChangeableText>();
		for(int i=0; i<5; i++)
		{
			ChangeableText aux = new ChangeableText(seats_pX.get(i)+7, seats_pY.get(i)+45, blackFont, "         ");
			aux.setVisible(false);

			seatBetText.add(i, aux);
			mainScene.attachChild(aux);
		}

		//Dealer and blind buttons related
		dealerButton = new Sprite(230, 0, dealerTextureRegion);
		dealerButton.setVisible(false);
		mainScene.attachChild(dealerButton);

		smallBlindButton = new Sprite(0, 0, smallBlindTextureRegion);
		smallBlindButton.setVisible(false);
		mainScene.attachChild(smallBlindButton);

		bigBlindButton = new Sprite(0, 0, bigBlindTextureRegion);
		bigBlindButton.setVisible(false);
		mainScene.attachChild(bigBlindButton);

		//Botes en juego
		potsText = new ArrayList<ChangeableText>();
		for(int i=0; i<4; i++)
		{
			ChangeableText aux = new ChangeableText(280+15*i, 100, whiteFont, "Pot"+i+": "+"        ");
			aux.setVisible(false);

			potsText.add(i, aux);
			mainScene.attachChild(aux);
		}

		//Cartas que comparten todos los jugadores
		communityCardSprites = new ArrayList<Sprite>();
		for(int i=0; i<5; i++)
		{
			Sprite aux = new Sprite(262+55*i, 175, cardTotextureRegionMap.get(Card.CLUB_ACE));
			aux.setScale(0.7f);
			aux.setVisible(false);

			communityCardSprites.add(i, aux);
			mainScene.attachChild(aux);
		}

		//Cartas de las manos de los jugadores
		holeCardSprites = new ArrayList< ArrayList<Sprite> >();
		for(int i=0; i<5; i++)
		{
			//Cartas de la mano de un jugador
			ArrayList<Sprite> subArray = new ArrayList<Sprite>();
			for(int j=0; j<2; j++)
			{
				Sprite aux = new Sprite(seats_pX.get(i)+65+52*j, seats_pY.get(i)-20, cardTotextureRegionMap.get(Card.CLUB_ACE));
				aux.setScale(0.7f);
				aux.setVisible(false);

				subArray.add(j, aux);
				mainScene.attachChild(aux);
			}

			holeCardSprites.add(i, subArray);
		}
	}

	private void gameLoop()
	{
		if(mGameController.tick() < 0)
		{
			System.out.println("¡Tick() < 0!" + "Restart? "+mGameController.getRestart());

			//Replicate game if "restart" is set
			if(mGameController.getRestart())
			{
				System.out.println("REPLICATE GAME!");
				GameController newgame = new GameController();

				newgame.setName(mGameController.getName());
				newgame.setMaxPlayers(mGameController.getMaxPlayers());
				newgame.setPlayerStakes(mGameController.getPlayerStakes());
				newgame.setRestart(true);
				newgame.setOwner(mGameController.getOwner());

				mGameController = newgame;
			}
			else
			{
				//FIXME Toast.makeText(this, "Partida finalizada", 3).show();
				finish();
			}
		}
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

	/**
	 * At first, this sets the positions of each seat.
	 * Then adds five sprites, one per each seat.
	 */
	private void addSeats()
	{
		//Set the reference position of each seat

		//Seat #1 - Top left
		seats_pX.put(0, 15);
		seats_pY.put(0, 120);

		//Seat #2 - Bottom left
		seats_pX.put(1, 15);
		seats_pY.put(1, 270);

		//Seat #3 - Center
		seats_pX.put(2, getCameraWidth()/2-75);
		seats_pY.put(2, getCameraHeight()-165);

		//Seat #4 - Top rigth
		seats_pX.put(3, getCameraWidth()-175);
		seats_pY.put(3, 270);

		//Seat #5 - Bottom rigth
		seats_pX.put(4, getCameraWidth()-175);
		seats_pY.put(4, 120);

		seatSprites = new ArrayList<Sprite>();

		//Add seat sprites
		for(int i=0; i<5; i++)
		{
			this.addSeat(seats_pX.get(i), seats_pY.get(i), i);
		}
	}

	private void addSeat(final int pX, final int pY, final int pos)
	{
		final Sprite sprite = new Sprite(pX, pY, this.seatTextureRegion);
		seatSprites.add(pos, sprite);

		this.mainScene.attachChild(sprite);
	}

	private void removeText(final ChangeableText _text, Iterator it) {
		runOnUpdateThread(new Runnable() {
			@Override
			public void run() {
				mainScene.detachChild(_text);
			}
		});

		it.remove();
	}

	/**
	 * Encargado de mantener actualizado en pantalla el texto con elestado de la mesa
	 */
	private void stateUpdater()
	{
		IUpdateHandler stateUpdater = new IUpdateHandler() {
			@Override
			public void reset() {
			}

			@Override
			public void onUpdate(float pSecondsElapsed) {
				if(tableStateText.getText() != mGameController.table.state.name())
				{
					tableStateText.setText(mGameController.table.state.name());
				}
			}	
		};

		mainScene.registerUpdateHandler(stateUpdater);
	}

	/**
	 * Encargado de mantener actualizado en pantalla la ronda de apuestas
	 */
	private void bettingRoundUpdater()
	{
		IUpdateHandler bettingRoundUpdater = new IUpdateHandler() {
			@Override
			public void reset() {		
			}

			@Override
			public void onUpdate(float pSecondsElapsed) {
				if(bettingRoundText.getText() != mGameController.table.betround.name())
				{
					bettingRoundText.setText(mGameController.table.betround.name());
				}
			}	
		};

		mainScene.registerUpdateHandler(bettingRoundUpdater);
	}

	/**
	 * Encargado mantener actualizada la imagen del seat del current player
	 */
	private void currentPlayerUpdater()
	{
		IUpdateHandler currentPlayerIndicatorUpdater = new IUpdateHandler() {
			@Override
			public void reset() {		
			}

			@Override
			public void onUpdate(float pSecondsElapsed) {

				int currentPlayer = mGameController.table.currentPlayer;

				for(int i=0; i<seatSprites.size(); i++)
				{
					Sprite _seatSprite = seatSprites.get(i);

					if(i == currentPlayer) //Current player, check his texture
					{
						if(_seatSprite.getTextureRegion() != currentSeatTextureRegion)
							_seatSprite.setTextureRegion(currentSeatTextureRegion);
					}
					else if(_seatSprite.getTextureRegion() != seatTextureRegion)
						_seatSprite.setTextureRegion(seatTextureRegion);
				}
			}	
		};

		mainScene.registerUpdateHandler(currentPlayerIndicatorUpdater);
	}

	/**
	 * Encargado mantener los nombres de los jugadores en pantalla
	 */
	private void playerNameUpdater()
	{
		IUpdateHandler playerNameUpdater = new IUpdateHandler() {
			@Override
			public void reset() {		
			}

			@Override
			public void onUpdate(float pSecondsElapsed) {

				ArrayList<Seat> _seats = mGameController.table.seats; //Referencia a los asientos

				if(_seats.isEmpty())
					return;

				for(int i=0; i<5;i++)
				{
					Seat _seat = _seats.get(i);

					if(_seat.occupied) //Si el asiento esta ocupado por un jugador
					{
						ChangeableText _name = playerNameTexts.get(i);

						if(_seat.player.name != _name.getText())
							_name.setText(_seat.player.name);

						if(!_name.isVisible())
							_name.setVisible(true);
					}
					else //"Borrar" ChangeableText
					{
						ChangeableText _name = playerNameTexts.get(i);

						_name.setVisible(false);
					}
				}
			}	
		};
		mainScene.registerUpdateHandler(playerNameUpdater);
	}

	/**
	 * Encargado de mantener actualizadas las fichas de los jugadores en pantalla
	 */
	private void playerStakeUpdater()
	{
		IUpdateHandler playerStakeUpdater = new IUpdateHandler() {
			@Override
			public void reset() {		
			}

			@Override
			public void onUpdate(float pSecondsElapsed) {

				ArrayList<Seat> _seats = mGameController.table.seats; //Get seats

				if(_seats.isEmpty())
					return;

				for(int i=0; i<5;i++)
				{
					Seat _seat = _seats.get(i);

					if(_seat.occupied)
					{
						ChangeableText _stake = playerStakeTexts.get(i);

						if(Integer.toString(_seat.player.stake) != _stake.getText())
							_stake.setText(Integer.toString(_seat.player.stake));

						if(!_stake.isVisible())
							_stake.setVisible(true);
					}
					else //"Borrar" ChangeableText
					{
						ChangeableText _stake = playerStakeTexts.get(i);
						_stake.setVisible(false);
					}

				}
			}	
		};

		mainScene.registerUpdateHandler(playerStakeUpdater);
	}

	/**
	 * Encargado mantener actualizadas las apuestas de los jugadores
	 */
	private void seatBetUpdater()
	{
		IUpdateHandler seatBetUpdater = new IUpdateHandler() {
			@Override
			public void reset() {		
			}

			@Override
			public void onUpdate(float pSecondsElapsed) {

				ArrayList<Seat> _seats = mGameController.table.seats; //Get seats

				if(_seats.isEmpty())
					return;

				for(int i=0; i<5;i++)
				{
					Seat _seat = _seats.get(i);

					if(_seat.occupied)
					{
						ChangeableText _bet = seatBetText.get(i);

						if(Integer.toString(_seats.get(i).bet) != _bet.getText())
							_bet.setText(Integer.toString(_seats.get(i).bet));

						if(!_bet.isVisible())
							_bet.setVisible(true);
					}
					else //"Borrar" ChangeableText
					{
						ChangeableText _bet = seatBetText.get(i);
						_bet.setVisible(false);
					}

				}
			}	
		};
		mainScene.registerUpdateHandler(seatBetUpdater);
	}

	/**
	 * Encargado de crear y añadir en pantalla las apuestas de los jugadores
	 */
	private void potUpdater()
	{
		IUpdateHandler potUpdater = new IUpdateHandler() {
			@Override
			public void reset() {		
			}

			@Override
			public void onUpdate(float pSecondsElapsed) {

				ArrayList<Pot> _pots = mGameController.table.pots; //Get pots
				int potssize = _pots.size(); //Get the number pots

				for(int i=0; i<_pots.size();i++)
				{
					if(i<potssize)
					{
						ChangeableText _pot = potsText.get(i);

						if(Integer.toString(_pots.get(i).amount) != _pot.getText())
							_pot.setText("Pot"+i+": "+Integer.toString(_pots.get(i).amount));

						if(!_pot.isVisible())
							_pot.setVisible(true);
					}
					else //Ocultar ChangeableText
					{
						ChangeableText _pot = potsText.get(i);
						_pot.setVisible(false);
					}

				}
			}	
		};
		mainScene.registerUpdateHandler(potUpdater);
	}

	private void buttonUpdater()
	{
		IUpdateHandler buttonUpdater = new IUpdateHandler() {
			@Override
			public void reset() {		
			}

			@Override
			public void onUpdate(float pSecondsElapsed) {

				if(!mGameController.isEnded())
				{
					/*
					 * Dealer button
					 */
					if(!dealerButton.isVisible()) //First time
					{
						dealer = mGameController.table.dealer;
						dealerButton.setPosition(seats_pX.get(dealer), seats_pY.get(dealer)-30);
						dealerButton.setVisible(true);
					}
					else if(dealer != mGameController.table.dealer)
					{
						dealer = mGameController.table.dealer;
						dealerButton.setPosition(seats_pX.get(dealer), seats_pY.get(dealer)-30);
					}

					/*
					 * Small blind button
					 */
					if(!smallBlindButton.isVisible()) //First time
					{
						sb = mGameController.table.sb;
						smallBlindButton.setPosition(seats_pX.get(sb), seats_pY.get(sb)-30);
						smallBlindButton.setVisible(true);
					}
					else if(sb != mGameController.table.sb)
					{
						sb = mGameController.table.sb;
						smallBlindButton.setPosition(seats_pX.get(sb), seats_pY.get(sb)-30);
					}

					/*
					 * Big blind button
					 */
					if(!bigBlindButton.isVisible()) //First time
					{
						bb = mGameController.table.bb;
						bigBlindButton.setPosition(seats_pX.get(bb), seats_pY.get(bb)-30);
						bigBlindButton.setVisible(true);
					}
					else if(bb != mGameController.table.bb)
					{
						bb = mGameController.table.bb;
						bigBlindButton.setPosition(seats_pX.get(bb), seats_pY.get(bb)-30);
					}
				}
			}
		};
		mainScene.registerUpdateHandler(buttonUpdater);
	}

	/**
	 * Encargado de crear los sprites de las community cards que aun no esten creadas
	 */
	private void communityCardUpdater()
	{
		IUpdateHandler communityCardUpdater = new IUpdateHandler() {
			@Override
			public void reset() {		
			}

			@Override
			public void onUpdate(float pSecondsElapsed) {

				ArrayList<Card> _communitycards = mGameController.table.communitycards.cards; //Get community cards
				int cmsize = _communitycards.size(); //Get the number of cards

				for(int i=0; i<5;i++)
				{
					if(i<cmsize) //Existe la carta, tratar de actualizar el sprite asociado
					{
						Card _card = _communitycards.get(i);
						Sprite _sprite = communityCardSprites.get(i);

						TextureRegion _texture = cardTotextureRegionMap.get(_card); //Textura del sprite actualmente

						if(_sprite.getTextureRegion() != _texture) //Nueva textura = Nueva carta
							_sprite.setTextureRegion(_texture);


						if(!_sprite.isVisible())
							_sprite.setVisible(true);
					}
					else if(i>= cmsize) //No existe la carta, ocultar sprite asociado
					{
						Sprite _sprite = communityCardSprites.get(i);
						_sprite.setVisible(false);
					}
				}
			}	
		};

		mainScene.registerUpdateHandler(communityCardUpdater);
	}

	/**
	 * Encargado de crear los sprites de las community cards que aun no esten creadas
	 */
	private void holeCardUpdater()
	{
		IUpdateHandler holeCardUpdater = new IUpdateHandler() {
			@Override
			public void reset() {		
			}

			@Override
			public void onUpdate(float pSecondsElapsed) {

				ArrayList<Seat> _seats = mGameController.table.seats; //Cogemos la referencia a los asientos

				for(int j = 0; j < _seats.size(); j++) //Por cada asiento de la mesa
				{
					Seat _seat = _seats.get(j);

					if(_seat.occupied && _seat.in_round) //Comprobamos si esta ocupado por un jugador
					{
						ArrayList<Card> _holecards = _seat.player.holecards.cards; //Referencia a sus holecards
						ArrayList<Sprite> _sprites = holeCardSprites.get(j); //Referencia a los sprites asociados a esos holecards

						int hlsize = _holecards.size(); //Numero de cartas en la mano

						for(int i=0; i<2;i++)
						{
							if(i<hlsize) //Existe la carta, tratar de actualizar el sprite asociado
							{
								Card _card = _holecards.get(i);
								Sprite _sprite = _sprites.get(i);

								TextureRegion _texture = cardTotextureRegionMap.get(_card); //Textura del sprite actualmente

								if(_sprite.getTextureRegion() != _texture) //Nueva carta = Nueva textura
									_sprite.setTextureRegion(_texture);

								if(!_sprite.isVisible())
									_sprite.setVisible(true);
							}
							else //No existe la carta, ocultar sprite asociado
							{
								Sprite _sprite = _sprites.get(i);
								_sprite.setVisible(false);
							}
						}
					}
					else //Asiento vacio, ocultar cartas
					{
						ArrayList<Sprite> _sprites = holeCardSprites.get(j); //Referencia a los sprites asociados a esos holecards

						for(int i=0; i<_sprites.size(); i++)
							_sprites.get(i).setVisible(false);
					}
				}
			}	
		};

		mainScene.registerUpdateHandler(holeCardUpdater);
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
		Player.SchedAction auxSchedAction = this.mGameController.players.get(pid).new SchedAction();
		auxSchedAction.valid = true;
		auxSchedAction.action = action;
		if(action == Action.Bet || action == Action.Call || action == Action.Raise)
		{
			auxSchedAction.amount = amount;
		} else
			auxSchedAction.amount = 0;

		this.mGameController.players.get(pid).setNextAction(auxSchedAction);
	}

	//This function adds the following buttons: Fold, Check, Call, Raise and Exit
	private void addButtons()
	{
		this.addFoldButton(5, getCameraHeight() - this.buttonToTextureRegionMap.get(Button.FOLD).getHeight()/2 -5);
		this.addCheckButton(this.buttonToTextureRegionMap.get(Button.FOLD).getWidth() + 20, getCameraHeight() - this.buttonToTextureRegionMap.get(Button.CHECK).getHeight()/2-5);
		this.addBetButton(getCameraWidth() - 3*(this.buttonToTextureRegionMap.get(Button.RAISE).getWidth()) - 30, getCameraHeight() - this.buttonToTextureRegionMap.get(Button.CALL).getHeight()/2-5);
		this.addCallButton(getCameraWidth() - 2*(this.buttonToTextureRegionMap.get(Button.RAISE).getWidth()) - 15, getCameraHeight() - this.buttonToTextureRegionMap.get(Button.CALL).getHeight()/2-5);
		this.addRaiseButton(getCameraWidth() - this.buttonToTextureRegionMap.get(Button.RAISE).getWidth(), getCameraHeight() - this.buttonToTextureRegionMap.get(Button.RAISE).getHeight()/2-5);
		this.addExitButton(getCameraWidth() - this.buttonToTextureRegionMap.get(Button.EXIT).getWidth()-5, 5);

	}

	private void addFoldButton(final int pX, final int pY){
		final TiledSprite sprite = new TiledSprite(pX, pY, this.buttonToTextureRegionMap.get(Button.FOLD)){
			boolean mGrabbed = false;

			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch(pSceneTouchEvent.getAction()) {
				case TouchEvent.ACTION_DOWN:
					this.setCurrentTileIndex(1);					
					this.mGrabbed = true;
					doSetAction(mGameController.table.currentPlayer, Player.Action.Fold, 0);

					break;
				case TouchEvent.ACTION_UP:
					if(this.mGrabbed) {
						this.mGrabbed = false;
						this.setCurrentTileIndex(0);					
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
		final TiledSprite sprite = new TiledSprite(pX, pY, this.buttonToTextureRegionMap.get(Button.CHECK)){
			boolean mGrabbed = false;

			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch(pSceneTouchEvent.getAction()) {
				case TouchEvent.ACTION_DOWN:
					this.setCurrentTileIndex(1);					
					this.mGrabbed = true;

					//Allowed check?
					if(mGameController.table.seats.get(mGameController.table.currentPlayer).bet < mGameController.table.bet_amount)
					{
						Toast.makeText(getApplicationContext(), "You can't check dude! Try call ;D", 3).show();
					}

					doSetAction(mGameController.table.currentPlayer, Player.Action.Check, 0);

					break;
				case TouchEvent.ACTION_UP:
					if(this.mGrabbed) {
						this.setCurrentTileIndex(0);					
					}
					break;
				}
				return true;
			}
		};
		this.mainScene.attachChild(sprite);
		this.mainScene.registerTouchArea(sprite);
	}

	private void addBetButton(final int pX, final int pY){
		final TiledSprite sprite = new TiledSprite(pX, pY, this.buttonToTextureRegionMap.get(Button.BET)){
			boolean mGrabbed = false;

			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch(pSceneTouchEvent.getAction()) {
				case TouchEvent.ACTION_DOWN:
					this.setCurrentTileIndex(1);					
					this.mGrabbed = true;

					if(mGameController.table.state == State.Betting) //Solo permitir apostar cuando nos encontremos en ronda de apuestas
					{
						if(mGameController.table.bet_amount > 0){
							Toast.makeText(getApplicationContext(), "You can't bet dude! There already was a bet, try call or raise.", 3).show();
						}
						else
							betDialog();
					}
					else
						Toast.makeText(getApplicationContext(), "You can't bet. This is not a betting round.", 2).show();

					break;
				case TouchEvent.ACTION_UP:
					if(this.mGrabbed) {
						this.setCurrentTileIndex(0);					
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
		final TiledSprite sprite = new TiledSprite(pX, pY, this.buttonToTextureRegionMap.get(Button.CALL)){
			boolean mGrabbed = false;

			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch(pSceneTouchEvent.getAction()) {
				case TouchEvent.ACTION_DOWN:
					this.setCurrentTileIndex(1);					
					this.mGrabbed = true;

					doSetAction(mGameController.table.currentPlayer, Player.Action.Call, 0);

					break;
				case TouchEvent.ACTION_UP:
					if(this.mGrabbed) {
						this.setCurrentTileIndex(0);					
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
		final TiledSprite sprite = new TiledSprite(pX, pY, this.buttonToTextureRegionMap.get(Button.RAISE)){
			boolean mGrabbed = false;

			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch(pSceneTouchEvent.getAction()) {
				case TouchEvent.ACTION_DOWN:
					this.setCurrentTileIndex(1);					
					this.mGrabbed = true;

					if(mGameController.table.state == State.Betting) //Solo permitir apostar cuando nos encontremos en ronda de apuestas
					{
						raiseDialog();
					}
					else
					{
						Toast.makeText(getApplicationContext(), "You can't raise. This is not a betting round.", 2).show();
					}

					break;
				case TouchEvent.ACTION_UP:
					if(this.mGrabbed) {
						this.mGrabbed = false;
						this.setCurrentTileIndex(0);					
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
		final TiledSprite sprite = new TiledSprite(pX, pY, this.buttonToTextureRegionMap.get(Button.EXIT)){
			boolean mGrabbed = false;

			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch(pSceneTouchEvent.getAction()) {
				case TouchEvent.ACTION_DOWN:
					this.setCurrentTileIndex(1);					
					this.mGrabbed = true;
					break;
				case TouchEvent.ACTION_UP:
					if(this.mGrabbed) {
						this.mGrabbed = false;
						this.setCurrentTileIndex(0);					
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

	private void betDialog()
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Make your bet:");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		alert.setView(input);

		alert.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				String betValue = input.getText().toString();

				if(Integer.parseInt(betValue) < mGameController.minimun_bet)
				{	
					Toast.makeText(getApplicationContext(), "You can't bet this amount, the minimun bet is: " + mGameController.minimun_bet, 3).show();
				}

				doSetAction(mGameController.table.currentPlayer, Player.Action.Bet, Integer.parseInt(betValue));
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});

		alert.show();
	}

	private void raiseDialog()
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Make your raise:");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		alert.setView(input);

		alert.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				String raiseValue = input.getText().toString();

				if(Integer.parseInt(raiseValue) < mGameController.minimun_bet)
				{	
					Toast.makeText(getApplicationContext(), "You can't raise this amount, the minimun raise is: " + mGameController.minimun_bet, 3).show();
				}

				doSetAction(mGameController.table.currentPlayer, Player.Action.Raise, Integer.parseInt(raiseValue));
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});

		alert.show();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}

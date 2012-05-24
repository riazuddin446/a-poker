package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import logic.Card;
import logic.HoleCards;
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
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Display;
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
	private Font font;
	private Texture fontTexture;

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
	private TiledTextureRegion seatTiledTextureRegion;
	private ArrayList<TiledSprite> seatSprites;

	//Dealer and blind buttons
	private BitmapTextureAtlas dealerAndBlindTextureAtlas;
	private ArrayList<TextureRegion> dealerAndBlindToTextureRegionList;
	private ArrayList<Sprite> dealerAndBlindButtons;

	//Game related
	private ChangeableText tableStateText;

	//Table related
	private ChangeableText bettingRoundText;

	//Community Cards
	private ArrayList<Sprite> communityCardSprites;

	//Hole Cards
	private ArrayList< ArrayList<Sprite> > holeCardSprites;

	//Player related
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
		this.seatTiledTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.seatTextureAtlas, this,"seat.png", 0, 0, 1, 2);

		//Load the textures for the DEALER and BLINDS buttons
		dealerAndBlindTextureAtlas = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		dealerAndBlindToTextureRegionList = new ArrayList<TextureRegion>();
		for(int j=0; i<3; j++){
			TextureRegion buttonTextureRegion = TextureRegionFactory.extractFromTexture(dealerAndBlindTextureAtlas, 0, i*25, 25, 25, true);
			dealerAndBlindToTextureRegionList.add(i, buttonTextureRegion);
		}

		//Load the font for TEXT
		this.fontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.font = new Font(this.fontTexture, Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC), 20, true, Color.BLACK);

		//Load the textures into the engine
		mEngine.getTextureManager().loadTextures(backgroundTextureAtlas,
				this.buttonsTextureAtlas,
				this.cardDeckTextureAtlas,
				this.seatTextureAtlas,
				this.fontTexture);
		//Load the fonts into the engine
		this.mEngine.getFontManager().loadFont(this.font);
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
		//FIXME createCurrentPlayerIndicatorHandler();
		playerNameUpdater();
		playerStakeUpdater();
		seatBetUpdater();
		potUpdater();
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

	}

	// ===========================================================
	// Methods
	// ===========================================================

	private void initializeGameController()
	{
		mGameController = new GameController();
		mGameController.setName("Prueba"); //FIXME Recibir el nombre del activity anterior
		mGameController.setMaxPlayers(5); //FIXME Recibir el numero maximo de jugadores del activity anterior
		mGameController.setPlayerStakes(500); //(4000);
		mGameController.setRestart(true);
		mGameController.setOwner(-1);
	}

	/**
	 * Encargado de inicializar todos los elementos que irán en la interfaz gráfica: Textos e imagenes
	 * Así como sus contenedores.
	 */
	private void initializeGUIElements()
	{
		//Texto donde se mostrará el estado de la mesa
		bettingRoundText = new ChangeableText(0, 30, font, mGameController.table.betround.name());
		mainScene.attachChild(bettingRoundText);

		//Texto donde se mostrará la ronda de apuestas de la mesa
		tableStateText = new ChangeableText(0, 0, font, mGameController.table.state.name());
		mainScene.attachChild(tableStateText);

		//Cartas que comparten todos los jugadores
		communityCardSprites = new ArrayList<Sprite>();
		for(int i=0; i<5; i++)
		{
			Sprite aux = null; //FIXME ¿Crear el sprite?
			communityCardSprites.add(i, aux);
		}

		//Cartas de las manos de los jugadores
		holeCardSprites = new ArrayList< ArrayList<Sprite> >();
		for(int i=0; i<5; i++)
		{
			//Cartas de la mano de un jugador
			ArrayList<Sprite> auxArray = new ArrayList<Sprite>();
			for(int j=0; j<2; j++)
			{
				Sprite aux = null;
				auxArray.add(j, aux);
			}

			holeCardSprites.add(i, auxArray);
		}

		//Nombres de los jugadores
		playerNameTexts = new ArrayList<ChangeableText>();
		for(int i=0; i<5; i++)
		{
			ChangeableText aux = new ChangeableText(seats_pX.get(i)+5, seats_pY.get(i)+2, font, "");
			playerNameTexts.add(i, aux);
		}

		//Fichas de los jugadores
		playerStakeTexts = new ArrayList<ChangeableText>();
		for(int i=0; i<5; i++)
		{
			ChangeableText aux = new ChangeableText(seats_pX.get(i)+5, seats_pY.get(i)+20, font, "");
			playerStakeTexts.add(i, aux);
		}

		//Apuestas de los jugadores
		seatBetText = new ArrayList<ChangeableText>();
		for(int i=0; i<5; i++)
		{
			ChangeableText aux = new ChangeableText(seats_pX.get(i)+5, seats_pY.get(i)+40, font, "");
			seatBetText.add(i, aux);
		}

		//Botes en juego
		potsText = new ArrayList<ChangeableText>();
		for(int i=0; i<5; i++)
		{
			ChangeableText aux = new ChangeableText(280+15*i, 100, font, "Pot"+i+": "+"");
			potsText.add(i, aux);
		}
	}

	private void gameLoop()
	{
		if(mGameController.tick() < 0)
		{
			System.out.println("¡Tick() < 0!");
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

		seatSprites = new ArrayList<TiledSprite>();

		//Add seat sprites
		for(int i=0; i<5; i++)
		{
			this.addSeat(seats_pX.get(i), seats_pY.get(i), i);
		}
	}

	private void addSeat(final int pX, final int pY, final int pos)
	{
		final TiledSprite sprite = new TiledSprite(pX, pY, this.seatTiledTextureRegion);
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
	private void currentPlayerIndicatorUpdater()
	{
		IUpdateHandler currentPlayerIndicatorUpdater = new IUpdateHandler() {
			@Override
			public void reset() {		
			}

			@Override
			public void onUpdate(float pSecondsElapsed) {

				int currentPlayer = mGameController.table.currentPlayer;
				System.out.println("Current player :"+currentPlayer);

				for(int i=0; i<seatSprites.size(); i++)
				{
					TiledSprite _seatSprite = seatSprites.get(i);

					if(i == currentPlayer){
						System.out.println("¡Current player!: "+mGameController.players.get(i).name);
						System.out.println("Textura actual: "+_seatSprite.getCurrentTileIndex());
						if(_seatSprite.getCurrentTileIndex() != 1)
							_seatSprite.setCurrentTileIndex(1);
						System.out.println("Textura posterior: "+_seatSprite.getCurrentTileIndex());
					}
					else if(i != currentPlayer){
						System.out.println("NORMAL player: "+mGameController.players.get(i).name);
						System.out.println("Textura actual: "+_seatSprite.getCurrentTileIndex());
						if(_seatSprite.getCurrentTileIndex() != 0)
							_seatSprite.setCurrentTileIndex(0);
						System.out.println("Textura posterior: "+_seatSprite.getCurrentTileIndex());
					}
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
				int namessize = playerNameTexts.size();

				for(int i=0; i<5;i++)
				{
					Seat _seat = _seats.get(i);

					if(_seat.occupied) //Si el asiento esta ocupado por un jugador
					{
						if(i>=namessize) //Añadir ChangeableText
						{
							//Create new ChangeableText
							ChangeableText aux = new ChangeableText(seats_pX.get(i)+5, seats_pY.get(i)+2, font, _seat.player.name);

							//Add it to the Array who saves the ChangeableTexts of the names of the players in seats
							playerNameTexts.add(i, aux);

							//Attach it to the scene
							mainScene.attachChild(playerNameTexts.get(i));
						}
						else if(i<namessize) //Actualizar ChangeableText
						{
							if(_seat.player.name != playerNameTexts.get(i).getText())
								playerNameTexts.get(i).setText(_seat.player.name);
						}
					}
					else //"Borrar" ChangeableText
					{
						mainScene.detachChild(playerNameTexts.get(i));
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
				int textsize = playerStakeTexts.size();

				for(int i=0; i<5;i++)
				{
					Seat _seat = _seats.get(i);

					if(_seat.occupied)
					{
						if(i>=textsize) //Add ChangeableText
						{
							//Create new ChangeableText
							ChangeableText aux = new ChangeableText(seats_pX.get(i)+5, seats_pY.get(i)+20, font, Integer.toString(_seats.get(i).player.stake));

							//Add it to the Array who saves the ChangeableTexts of the names of the players in seats
							playerStakeTexts.add(i, aux);

							//Attach it to the scene
							mainScene.attachChild(playerStakeTexts.get(i));
						}
						else if(i<textsize) //Update text
						{
							String _stake = Integer.toString(_seat.player.stake);
							if(_stake != playerStakeTexts.get(i).getText())
								playerStakeTexts.get(i).setText(_stake);
						}
						else //"Borrar" ChangeableText
						{
							mainScene.detachChild(playerStakeTexts.get(i));
						}
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
				int textssize = seatBetText.size();

				for(int i=0; i<5;i++)
				{
					Seat _seat = _seats.get(i);

					if(_seat.occupied)
					{
						if(i>=textssize) //Add ChangeableText
						{
							//Create new ChangeableText
							ChangeableText aux = new ChangeableText(seats_pX.get(i)+5, seats_pY.get(i)+40, font, Integer.toString(_seats.get(i).bet));

							//Add it to the Array who saves the ChangeableTexts of the bets of the players in seats
							seatBetText.add(i, aux);

							//Attach it to the scene
							mainScene.attachChild(seatBetText.get(i));
						}
						else if(i<textssize) //Update text
						{
							String _bet = Integer.toString(_seat.bet);

							if(_bet != seatBetText.get(i).getText())
								seatBetText.get(i).setText(_bet);
						}
						else //"Borrar" ChangeableText
						{
							mainScene.detachChild(seatBetText.get(i));
						}
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

				ArrayList<Pot> _pots = mGameController.table.pots; //Get seats
				int potssize = _pots.size(); //Get the number seats
				int textssize = potsText.size();

				for(int i=0; i<_pots.size();i++)
				{
					if(i<potssize)
					{
						if(i>=textssize) //Add ChangeableText
						{
							ChangeableText aux = new ChangeableText(280+15*i, 100, font, "Pot"+i+": "+Integer.toString(_pots.get(i).amount));

							potsText.add(i, aux);

							mainScene.attachChild(potsText.get(i));
						}
						else if(i<textssize) //Update ChangeableText
						{
							String amount = Integer.toString(_pots.get(i).amount);

							if(amount != potsText.get(i).getText())
								potsText.get(i).setText("Pot"+i+": "+amount);
						}
						else //"Borrar" ChangeableText
						{
							mainScene.detachChild(potsText.get(i));
						}
					}
				}
			}	
		};
		mainScene.registerUpdateHandler(potUpdater);
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
				int cmspritesize = communityCardSprites.size();

				for(int i=0; i<5;i++)
				{
					if(i>=cmspritesize && i<cmsize) //Add sprite
					{
						//Create new Sprite with the needed card texture
						Sprite aux = new Sprite(262+55*i, 175, cardTotextureRegionMap.get(_communitycards.get(i)));
						aux.setScale(0.7f);

						//Add it to the Array who saves the sprites of the Community Cards
						communityCardSprites.add(i, aux);

						//Attach it to the scene
						mainScene.attachChild(communityCardSprites.get(i));
					}
					else if(i<cmspritesize && i<cmsize) //Update sprite
					{
						Sprite _sprite = communityCardSprites.get(i);
						TextureRegion _texture = cardTotextureRegionMap.get(_communitycards.get(i));

						if(_sprite.getTextureRegion() != _texture) //Nueva carta = Nueva textura
						{
							mainScene.detachChild(_sprite);
							_sprite = new Sprite(262+55*i, 175, _texture);
							_sprite.setScale(0.7f);
							mainScene.attachChild(_sprite);
						}
					}
					else if(i<cmspritesize && i>= cmsize) //Detach sprite
					{
						Sprite _sprite = communityCardSprites.get(i);
						mainScene.detachChild(_sprite);
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

					if(_seat.occupied) //Comprobamos si esta ocupado por un jugador
					{
						ArrayList<Card> _holecards = _seat.player.holecards.cards; //Referencia a sus holecards
						ArrayList<Sprite> _sprites = holeCardSprites.get(j); //Referencia a los sprites asociados a esos holecards

						int hlsize = _holecards.size(); //Numero de cartas en la mano
						int spritesize = _sprites.size(); //Numero de Sprites

						for(int i=0; i<2;i++)
						{
							if(i>=spritesize && i<hlsize) //Añadir Sprite
							{
								Sprite _sprite = new Sprite(seats_pX.get(j)+60+52*i, seats_pY.get(j)-20, cardTotextureRegionMap.get(_holecards.get(i)));
								_sprite.setScale(0.7f);

								_sprites.add(i, _sprite);

								mainScene.attachChild(_sprites.get(i)); //Añadirlo a la escena para que se muestre
							}
							else if(i<spritesize && i<hlsize) //Actualizar Sprite
							{
								Sprite _sprite = _sprites.get(i);
								TextureRegion _texture = cardTotextureRegionMap.get(_holecards.get(i));

								if(_sprite.getTextureRegion() != _texture) //Nueva carta = Nueva textura
								{
									mainScene.detachChild(_sprite);
									_sprite = new Sprite(seats_pX.get(j)+60+52*i, seats_pY.get(j)-20, _texture);
									_sprite.setScale(0.7f);
									mainScene.attachChild(_sprite);
								}
							}
						}
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
		if(action == Action.Call || action == Action.Raise)
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

					doSetAction(mGameController.table.currentPlayer, Player.Action.Check, 0);

					break;
				case TouchEvent.ACTION_UP:
					if(this.mGrabbed) {
						this.setCurrentTileIndex(0);					
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

	private void addBetButton(final int pX, final int pY){
		final TiledSprite sprite = new TiledSprite(pX, pY, this.buttonToTextureRegionMap.get(Button.BET)){
			boolean mGrabbed = false;

			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch(pSceneTouchEvent.getAction()) {
				case TouchEvent.ACTION_DOWN:
					this.setCurrentTileIndex(1);					
					this.mGrabbed = true;

					doSetAction(mGameController.table.currentPlayer, Player.Action.Bet, 0); //TODO Pop up para insertar la cantidad

					break;
				case TouchEvent.ACTION_UP:
					if(this.mGrabbed) {
						this.setCurrentTileIndex(0);					
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
		final TiledSprite sprite = new TiledSprite(pX, pY, this.buttonToTextureRegionMap.get(Button.CALL)){
			boolean mGrabbed = false;

			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch(pSceneTouchEvent.getAction()) {
				case TouchEvent.ACTION_DOWN:
					this.setCurrentTileIndex(1);					
					this.mGrabbed = true;

					doSetAction(mGameController.table.currentPlayer, Player.Action.Call, 0); //TODO Pop up para insertar la cantidad

					break;
				case TouchEvent.ACTION_UP:
					if(this.mGrabbed) {
						this.setCurrentTileIndex(0);					
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
		final TiledSprite sprite = new TiledSprite(pX, pY, this.buttonToTextureRegionMap.get(Button.RAISE)){
			boolean mGrabbed = false;

			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				switch(pSceneTouchEvent.getAction()) {
				case TouchEvent.ACTION_DOWN:
					this.setCurrentTileIndex(1);					
					this.mGrabbed = true;

					doSetAction(mGameController.table.currentPlayer, Player.Action.Raise, 0); //TODO Pop up para insertar la cantidad

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

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}

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
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Display;
import client.Button;

public class PServer extends BaseGameActivity
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
	private ArrayList<ChangeableText> playerNamesText;
	private ArrayList<ChangeableText> playerStakesText;
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

		initializeSpriteContainers();
		initializeStaticSprites();
		initializeStaticTexts();

		createStateHandler();
		createBettingRoundHandler();

		createCurrentPlayerIndicatorHandler();

		createPlayerNameAddHandler();
		createPlayerNameRemoveHandler();

		createPlayerStakeAddAndUpdateHandler();
		createPlayerStakeRemoveHandler();

		createSeatBetAddAndUpdaterHandler();
		createSeatBetRemoveHandler();

		createPotAddTimeHandler();
		createPotRemoveTimeHandler();

		createCommunityCardAddTimeHandler();
		createCommunityCardRemoveTimeHandler();

		createHoleCardAddTimeHandler();
		createHoleCardRemoveTimeHandler();

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

		//		this.mainScene.registerUpdateHandler(new IUpdateHandler() {
		//			@Override
		//			public void onUpdate(float pSecondsElapsed) {
		//				System.out.println("Current player: "+mGameController.table.currentPlayer);
		//			}
		//
		//			@Override
		//			public void reset() {
		//				// TODO Auto-generated method stub
		//
		//			}
		//		});

		this.mainScene.setTouchAreaBindingEnabled(true);

		return this.mainScene;
	}

	@Override
	public void onLoadComplete()
	{	

	}

	private void initializeGameController()
	{
		mGameController = new GameController();
		mGameController.setName("Prueba"); //FIXME Recibir el nombre del activity anterior
		mGameController.setMaxPlayers(5); //FIXME Recibir el numero maximo de jugadores del activity anterior
		mGameController.setPlayerStakes(4000);
		mGameController.setRestart(true);
		mGameController.setOwner(-1);
	}

	private void initializeSpriteContainers()
	{
		communityCardSprites = new ArrayList<Sprite>();
		holeCardSprites = new ArrayList< ArrayList<Sprite> >();
		for(int i=0; i<5; i++)
		{
			ArrayList<Sprite> auxArray = new ArrayList<Sprite>();

			holeCardSprites.add(i, auxArray);
		}
		playerNamesText = new ArrayList<ChangeableText>();
		playerStakesText = new ArrayList<ChangeableText>();
		seatBetText = new ArrayList<ChangeableText>();
		potsText = new ArrayList<ChangeableText>();
	}

	private void initializeStaticSprites()
	{
		for(int i=0; i<dealerAndBlindToTextureRegionList.size(); i++)
		{
			Sprite _sprite = new Sprite(-1, -1, dealerAndBlindToTextureRegionList.get(i));
			dealerAndBlindButtons.add(_sprite);
			mainScene.attachChild(_sprite);
		}
	}

	private void initializeStaticTexts()
	{
		//Crear el texto donde se mostrará el estado de la mesa
		bettingRoundText = new ChangeableText(0, 30, font, "Betting round: " + mGameController.table.betround.name());
		mainScene.attachChild(bettingRoundText);

		//Crear el texto donde se mostrará la ronda de apuestas de la mesa
		tableStateText = new ChangeableText(0, 0, font, "Table state: " + mGameController.table.state.name());
		mainScene.attachChild(tableStateText);
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

	private void removeSprite(final Sprite _sprite, Iterator it) {
		runOnUpdateThread(new Runnable() {
			@Override
			public void run() {
				mainScene.detachChild(_sprite);
			}
		});

		it.remove();
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
	 * Encargado de mantener actualizado en pantalla el estado de la mesa
	 */
	private void createStateHandler()
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
	private void createBettingRoundHandler()
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
	private void createCurrentPlayerIndicatorHandler()
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
	 * Encargado de crear y añadir en pantalla los nombres de los jugadores que aun no esten creados
	 */
	private void createPlayerNameAddHandler()
	{
		IUpdateHandler playerNameAdder = new IUpdateHandler() {
			@Override
			public void reset() {		
			}

			@Override
			public void onUpdate(float pSecondsElapsed) {

				ArrayList<Seat> seats = mGameController.table.seats; //Get seats
				int seatssize = seats.size(); //Get the number seats
				int nametextsize = playerNamesText.size();

				for(int i=0; i<5;i++)
				{
					if(i<seatssize && i>=nametextsize) //Add ChangeableText
					{
						//Create new ChangeableText
						ChangeableText aux = new ChangeableText(seats_pX.get(i)+5, seats_pY.get(i)+2, font, seats.get(i).player.name);

						//Add it to the Array who saves the ChangeableTexts of the names of the players in seats
						playerNamesText.add(i, aux);

						//Attach it to the scene
						mainScene.attachChild(playerNamesText.get(i));
					}
				}
			}	
		};
		mainScene.registerUpdateHandler(playerNameAdder);
	}

	/**
	 * Encargado de eliminar de la pantalla los nombres de los jugadores que ya no esten en la partida
	 */
	private void createPlayerNameRemoveHandler()
	{
		IUpdateHandler playerNameRemover = new IUpdateHandler() {
			@Override
			public void reset() {		
			}

			@Override
			public void onUpdate(float pSecondsElapsed) {

				Iterator<ChangeableText> names = playerNamesText.iterator();
				ChangeableText _name;		 

				while (names.hasNext()) {
					_name = names.next();
					int pos = playerNamesText.indexOf(_name);

					if (pos+1 > mGameController.table.seats.size()) {
						removeText(_name, names);		
					}	
				}
			}	
		};

		mainScene.registerUpdateHandler(playerNameRemover);
	}

	/**
	 * Encargado de crear y añadir en pantalla las fichas de los jugadores que aun no esten creados
	 */
	private void createPlayerStakeAddAndUpdateHandler()
	{
		IUpdateHandler playerStakeAdderAndUpdater = new IUpdateHandler() {
			@Override
			public void reset() {		
			}

			@Override
			public void onUpdate(float pSecondsElapsed) {

				ArrayList<Seat> seats = mGameController.table.seats; //Get seats
				int seatssize = seats.size(); //Get the number seats
				int staketextsize = playerStakesText.size();

				for(int i=0; i<5;i++)
				{
					if(i<seatssize && i>=staketextsize) //Add ChangeableText
					{
						//Create new ChangeableText
						ChangeableText aux = new ChangeableText(seats_pX.get(i)+5, seats_pY.get(i)+20, font, Integer.toString(seats.get(i).player.stake));

						//Add it to the Array who saves the ChangeableTexts of the names of the players in seats
						playerStakesText.add(i, aux);

						//Attach it to the scene
						mainScene.attachChild(playerStakesText.get(i));
					}
					else if(playerStakesText.get(i).getText() != Integer.toString(seats.get(i).player.stake)) //Update text
					{
						playerStakesText.get(i).setText(Integer.toString(seats.get(i).player.stake));
					}
				}
			}	
		};
		mainScene.registerUpdateHandler(playerStakeAdderAndUpdater);
	}

	/**
	 * Encargado de eliminar de la pantalla las fichas de los jugadores que ya no esten en la partida
	 */
	private void createPlayerStakeRemoveHandler()
	{
		IUpdateHandler playerStakeRemover = new IUpdateHandler() {
			@Override
			public void reset() {		
			}

			@Override
			public void onUpdate(float pSecondsElapsed) {

				Iterator<ChangeableText> stakes = playerStakesText.iterator();
				ChangeableText _stake;		 

				while (stakes.hasNext()) {
					_stake = stakes.next();
					int pos = playerStakesText.indexOf(_stake);

					if (pos+1 > mGameController.table.seats.size()) {
						removeText(_stake, stakes);		
					}	
				}
			}	
		};

		mainScene.registerUpdateHandler(playerStakeRemover);
	}

	/**
	 * Encargado de crear y añadir en pantalla las apuestas de los jugadores
	 */
	private void createSeatBetAddAndUpdaterHandler()
	{
		IUpdateHandler seatBetAdderAndUpdater = new IUpdateHandler() {
			@Override
			public void reset() {		
			}

			@Override
			public void onUpdate(float pSecondsElapsed) {

				ArrayList<Seat> seats = mGameController.table.seats; //Get seats
				int seatssize = seats.size(); //Get the number seats
				int bettextsize = seatBetText.size();

				for(int i=0; i<5;i++)
				{
					String bet = Integer.toString(seats.get(i).bet);

					if(i<seatssize && i>=bettextsize) //Add ChangeableText
					{
						//Create new ChangeableText
						ChangeableText aux = new ChangeableText(seats_pX.get(i)+5, seats_pY.get(i)+40, font, bet);

						//Add it to the Array who saves the ChangeableTexts of the bets of the players in seats
						seatBetText.add(i, aux);

						//Attach it to the scene
						mainScene.attachChild(seatBetText.get(i));
					}
					else if(seatBetText.get(i).getText() != bet) //Update text
					{
						seatBetText.get(i).setText(bet);
					}
				}
			}	
		};
		mainScene.registerUpdateHandler(seatBetAdderAndUpdater);
	}

	/**
	 * Encargado de eliminar de la pantalla las apuestas de los jugadores que ya no esten en la partida
	 */
	private void createSeatBetRemoveHandler()
	{
		IUpdateHandler seatBetRemover = new IUpdateHandler() {
			@Override
			public void reset() {		
			}

			@Override
			public void onUpdate(float pSecondsElapsed) {

				Iterator<ChangeableText> bets = seatBetText.iterator();
				ChangeableText _bet;		 

				while (bets.hasNext()) {
					_bet = bets.next();
					int pos = seatBetText.indexOf(_bet);

					if (pos+1 > mGameController.table.seats.size()) {
						removeText(_bet, bets);		
					}	
				}
			}	
		};

		mainScene.registerUpdateHandler(seatBetRemover);
	}

	/**
	 * Encargado de crear y añadir en pantalla las apuestas de los jugadores
	 */
	private void createPotAddTimeHandler()
	{
		IUpdateHandler potAdder = new IUpdateHandler() {
			@Override
			public void reset() {		
			}

			@Override
			public void onUpdate(float pSecondsElapsed) {

				ArrayList<Pot> pots = mGameController.table.pots; //Get seats
				int potssize = pots.size(); //Get the number seats
				int potstextsize = potsText.size();

				for(int i=0; i<pots.size();i++)
				{
					String amount = Integer.toString(pots.get(i).amount);
					//System.out.println("Amount: "+amount);

					if(i<potssize && i>=potstextsize) //Add ChangeableText
					{
						//Create new ChangeableText
						ChangeableText aux = new ChangeableText(280+15*i, 100, font, "Pot"+i+": "+amount);

						//Add it to the Array who saves the ChangeableTexts of the bets of the players in seats
						potsText.add(i, aux);

						//Attach it to the scene
						mainScene.attachChild(potsText.get(i));
					}
					else if(potsText.get(i).getText() != amount) //Update text
					{
						potsText.get(i).setText("Pot"+i+": "+amount);
					}
				}
			}	
		};
		mainScene.registerUpdateHandler(potAdder);
	}

	/**
	 * Encargado de eliminar de la pantalla las apuestas de los jugadores que ya no esten en la partida
	 */
	private void createPotRemoveTimeHandler()
	{
		IUpdateHandler potRemover = new IUpdateHandler() {
			@Override
			public void reset() {		
			}

			@Override
			public void onUpdate(float pSecondsElapsed) {

				Iterator<ChangeableText> pots = potsText.iterator();
				ChangeableText _pot;		 

				while (pots.hasNext()) {
					_pot = pots.next();
					int pos = potsText.indexOf(_pot);

					if (pos+1 > mGameController.table.seats.size()) {
						removeText(_pot, pots);		
					}	
				}
			}	
		};

		mainScene.registerUpdateHandler(potRemover);
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	/**
	 * Encargado de crear los sprites de las community cards que aun no esten creadas
	 */
	private void createCommunityCardAddTimeHandler()
	{
		IUpdateHandler communityCardAdder = new IUpdateHandler() {
			@Override
			public void reset() {		
			}

			@Override
			public void onUpdate(float pSecondsElapsed) {

				ArrayList<Card> cmcards = mGameController.table.communitycards.cards; //Get community cards
				int cmsize = cmcards.size(); //Get the number of cards
				int cmspritesize = communityCardSprites.size();

				for(int i=0; i<5;i++)
				{
					if(i<cmsize && i>=cmspritesize) //Add sprite
					{
						//Create new Sprite with the needed card texture
						Sprite aux = new Sprite(262+55*i, 175, cardTotextureRegionMap.get(cmcards.get(i)));
						aux.setScale(0.7f);

						//Add it to the Array who saves the sprites of the Community Cards
						communityCardSprites.add(i, aux);

						//Attach it to the scene
						mainScene.attachChild(communityCardSprites.get(i));
					}
				}
			}	
		};

		mainScene.registerUpdateHandler(communityCardAdder);
	}

	/**
	 * Encargado de eliminar los sprites de las community cards que ya no existen
	 */
	private void createCommunityCardRemoveTimeHandler()
	{
		IUpdateHandler communityCardRemover = new IUpdateHandler() {
			@Override
			public void reset() {		
			}

			@Override
			public void onUpdate(float pSecondsElapsed) {

				Iterator<Sprite> cards = communityCardSprites.iterator();
				Sprite _card;		 

				while (cards.hasNext()) {
					_card = cards.next();
					int pos = communityCardSprites.indexOf(_card);

					if (pos+1 > mGameController.table.communitycards.cards.size()) {
						removeSprite(_card, cards);		
					}	
				}
			}	
		};

		mainScene.registerUpdateHandler(communityCardRemover);
	}

	/**
	 * Encargado de crear los sprites de las community cards que aun no esten creadas
	 */
	private void createHoleCardAddTimeHandler()
	{
		IUpdateHandler holeCardCardAdder = new IUpdateHandler() {
			@Override
			public void reset() {		
			}

			@Override
			public void onUpdate(float pSecondsElapsed) {

				ArrayList<Seat> _seats = mGameController.table.seats; //Cogemos la referencia a los asientos

				for(int j = 0; j < _seats.size(); j++) //Por cada uno de ellos
				{
					if(_seats.get(j).occupied) //Primero comprobamos si esta ocupado por un jugador
					{
						Player _player = _seats.get(j).player; //Cogemos la referencia al jugador que ocupa ese asiento
						ArrayList<Card> hlcards = _player.holecards.cards; //Cogemos la referencia a sus holecards
						int hlsize = hlcards.size(); //Get the number of cards
						int hlspritesize = holeCardSprites.get(j).size();

						for(int i=0; i<2;i++)
						{
							if(i<hlsize && i>=hlspritesize) //Add sprite
							{
								//System.out.println((seats_pX.get(j)+60+52*i)+","+(seats_pY.get(j)-20));
								//Create new Sprite with the needed card texture
								Sprite aux = new Sprite(seats_pX.get(j)+60+52*i, seats_pY.get(j)-20, cardTotextureRegionMap.get(hlcards.get(i)));
								aux.setScale(0.7f);

								//Add it to the Array who saves the sprites of the Community Cards
								holeCardSprites.get(j).add(i, aux);

								//Attach it to the scene
								mainScene.attachChild(holeCardSprites.get(j).get(i));
							}
						}
					}
				}
			}	
		};

		mainScene.registerUpdateHandler(holeCardCardAdder);
	}

	/**
	 * Encargado de eliminar los sprites de las community cards que ya no existen
	 */
	private void createHoleCardRemoveTimeHandler()
	{
		IUpdateHandler holeCardRemover = new IUpdateHandler() {
			@Override
			public void reset() {		
			}

			@Override
			public void onUpdate(float pSecondsElapsed) {

				for(int j = 0; j < holeCardSprites.size(); j++) //Por cada uno de ellos
				{
					ArrayList<Sprite> aux = holeCardSprites.get(j);
					Iterator<Sprite> cards = aux.iterator();
					Sprite _card;		 

					while (cards.hasNext()) {
						_card = cards.next();
						int pos = aux.indexOf(_card);

						if (pos+1 > mGameController.table.seats.get(j).player.holecards.size()) {
							removeSprite(_card, cards);		
						}	
					}
				}
			}	
		};

		mainScene.registerUpdateHandler(holeCardRemover);
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
		this.addFoldButton(0, getCameraHeight() - this.buttonToTextureRegionMap.get(Button.FOLD).getHeight()/2);
		this.addCheckButton(this.buttonToTextureRegionMap.get(Button.FOLD).getWidth() + 15, getCameraHeight() - this.buttonToTextureRegionMap.get(Button.CHECK).getHeight()/2);
		this.addBetButton(getCameraWidth() - 3*(this.buttonToTextureRegionMap.get(Button.RAISE).getWidth()) - 30, getCameraHeight() - this.buttonToTextureRegionMap.get(Button.CALL).getHeight()/2);
		this.addCallButton(getCameraWidth() - 2*(this.buttonToTextureRegionMap.get(Button.RAISE).getWidth()) - 15, getCameraHeight() - this.buttonToTextureRegionMap.get(Button.CALL).getHeight()/2);
		this.addRaiseButton(getCameraWidth() - this.buttonToTextureRegionMap.get(Button.RAISE).getWidth(), getCameraHeight() - this.buttonToTextureRegionMap.get(Button.RAISE).getHeight()/2);
		this.addExitButton(getCameraWidth() - this.buttonToTextureRegionMap.get(Button.EXIT).getWidth(), 0);

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
}

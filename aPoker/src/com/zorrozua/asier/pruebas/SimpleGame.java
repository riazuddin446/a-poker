package com.zorrozua.asier.pruebas;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.modifier.MoveXModifier;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.view.Display;

public class SimpleGame extends BaseGameActivity implements
IOnSceneTouchListener{

	private Camera mCamera;
	private Scene mMainScene;

	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TextureRegion mPlayerTextureRegion;
	private Sprite player;

	private TextureRegion mTargetTextureRegion;
	private LinkedList targetLL;
	private LinkedList TargetsToBeAdded;

	private LinkedList projectileLL;
	private LinkedList projectilesToBeAdded;
	private TextureRegion mProjectileTextureRegion;

	@Override
	public Engine onLoadEngine() {

		final Display display = getWindowManager().getDefaultDisplay();
		int cameraWidth = display.getWidth();
		int cameraHeight = display.getHeight();

		mCamera = new Camera(0, 0, cameraWidth, cameraHeight);

		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(cameraWidth, cameraHeight), mCamera));
	}

	@Override
	public void onLoadResources() {

		mBitmapTextureAtlas = new BitmapTextureAtlas(512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory
		.createFromAsset(this.mBitmapTextureAtlas, this, "a.png",
				0, 0);

		mTargetTextureRegion = BitmapTextureAtlasTextureRegionFactory
		.createFromAsset(this.mBitmapTextureAtlas, this, "a.png",
				128, 0);

		mProjectileTextureRegion = BitmapTextureAtlasTextureRegionFactory
		.createFromAsset(this.mBitmapTextureAtlas, this,
				"a.png", 64, 0);

		mEngine.getTextureManager().loadTexture(mBitmapTextureAtlas);

	}

	@Override
	public Scene onLoadScene() {

		mEngine.registerUpdateHandler(new FPSLogger());

		mMainScene = new Scene();
		mMainScene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));

		final int PlayerX = 0; //this.mPlayerTextureRegion.getWidth() / 2;
		final int PlayerY = (int) ((mCamera.getHeight() - mPlayerTextureRegion
				.getHeight()) / 2);

		player = new Sprite(PlayerX, PlayerY, mPlayerTextureRegion);

		mMainScene.attachChild(player);

		targetLL = new LinkedList();
		TargetsToBeAdded = new LinkedList();

		createSpriteSpawnTimeHandler();

		mMainScene.registerUpdateHandler(detect);

		projectileLL = new LinkedList();
		projectilesToBeAdded = new LinkedList();

		mMainScene.setOnSceneTouchListener(this);

		return mMainScene;
	}

	@Override
	public void onLoadComplete() {
		// TODO Auto-generated method stub

	}

	public void addTarget() {
		Random rand = new Random();

		int x = (int) mCamera.getWidth() + mTargetTextureRegion.getWidth();
		int minY = mTargetTextureRegion.getHeight();
		int maxY = (int) (mCamera.getHeight() - mTargetTextureRegion
				.getHeight());
		int rangeY = maxY - minY;
		int y = rand.nextInt(rangeY) + minY;

		Sprite target = new Sprite(x, y, mTargetTextureRegion.deepCopy());
		mMainScene.attachChild(target);

		int minDuration = 2;
		int maxDuration = 4;
		int rangeDuration = maxDuration - minDuration;
		int actualDuration = rand.nextInt(rangeDuration) + minDuration;

		MoveXModifier mod = new MoveXModifier(actualDuration, target.getX(),
				-target.getWidth());
		target.registerEntityModifier(mod.deepCopy());

		TargetsToBeAdded.add(target);

	}

	private void createSpriteSpawnTimeHandler() {
		TimerHandler spriteTimerHandler;
		float mEffectSpawnDelay = 1f;

		spriteTimerHandler = new TimerHandler(mEffectSpawnDelay, true,
				new ITimerCallback() {

			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				addTarget();
			}
		});

		getEngine().registerUpdateHandler(spriteTimerHandler);
	}

	public void removeSprite(final Sprite _sprite, Iterator it) {
		runOnUpdateThread(new Runnable() {

			@Override
			public void run() {
				mMainScene.detachChild(_sprite);
			}
		});
		it.remove();
	}

	IUpdateHandler detect = new IUpdateHandler() {
		@Override
		public void reset() {
		}

		@Override
		public void onUpdate(float pSecondsElapsed) {

			Iterator<Sprite> targets = targetLL.iterator();
			Sprite _target;

			while (targets.hasNext()) {
				_target = targets.next();
				if (_target.getX() <= -_target.getWidth()) {
					removeSprite(_target, targets);
				}
			}
			targetLL.addAll(TargetsToBeAdded);
			TargetsToBeAdded.clear();
		}
	};

	private void shootProjectile(final float pX, final float pY) {

		int offX = (int) (pX - player.getX());
		int offY = (int) (pY - player.getY());
		if (offX <= 0)
			return;

		final Sprite projectile;
		projectile = new Sprite(player.getX(), player.getY(),
				mProjectileTextureRegion.deepCopy());
		mMainScene.attachChild(projectile, 1);

		int realX = (int) (mCamera.getWidth() + projectile.getWidth() / 2.0f);
		float ratio = (float) offY / (float) offX;
		int realY = (int) ((realX * ratio) + projectile.getY());

		int offRealX = (int) (realX - projectile.getX());
		int offRealY = (int) (realY - projectile.getY());
		float length = (float) Math.sqrt((offRealX * offRealX)
				+ (offRealY * offRealY));
		float velocity = 480.0f / 1.0f; // 480 pixels / 1 sec
		float realMoveDuration = length / velocity;

		MoveModifier mod = new MoveModifier(realMoveDuration,
				projectile.getX(), realX, projectile.getY(), realY);
		projectile.registerEntityModifier(mod.deepCopy());

		projectilesToBeAdded.add(projectile);
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {

		if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
			final float touchX = pSceneTouchEvent.getX();
			final float touchY = pSceneTouchEvent.getY();
			shootProjectile(touchX, touchY);
			return true;
		}
		return false;

	}

}

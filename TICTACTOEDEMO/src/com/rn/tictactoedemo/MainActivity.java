package com.rn.tictactoedemo;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.rn.tictactoedemo.MainActivity.GameGridAdapter.GridItemContainer;

public class MainActivity extends Activity implements OnItemClickListener,
		OnClickListener {

	// ----------- DEFINING CONSTANTS------------------------
	private static final int PLAYER_1 = 1; // to denote cells played by player 1

	private static final int PLAYER_1_VICTORIOUS = 3; // victorious marks those
														// elements in the game
														// grid that are part of
														// winning combination
														// for player 1

	private static final int PLAYER_2 = 2;// to denote cells played by player 2

	private static final int PLAYER_2_VICTORIOUS = 4;// victorious marks those
														// elements in game grid
														// that are part of
														// winning combination
														// for player 2

	private static final int EMPTY = 0; // to denote empty/not played cells

	private static final int GAME_WON = 2;// someone won yay! :)
	private static final int GAME_ON = 1; // game in progress
	private static final int GAME_TIED = 0; // no result.
	private static final int NO_OF_COLS_DEFAULT = 3;
	private static final String PREFERNCE_NAME = "TICTACTOEPREF";

	// KEYS to get/put values from/into Shared Preferences. Also being used to
	// save instance of activity for handling orientation change
	private static final String KEY_GAMEARRAY = "gameArray";
	private static final String KEY_CURRPLAYER = "currPlayer";
	private static final String KEY_GAMESTAT = "gameStat";
	private static final String KEY_NUM_MOVES = "noOfMoves";
	private static final String KEY_NO_OF_COLS = "columns";
	private static final String KEY_PLAYER1_SCORE = "player1Score";
	private static final String KEY_PLAYER2_SCORE = "player2Score";
	private static final String KEY_PLAYER1_NAME = "player1name";
	private static final String KEY_PLAYER2_NAME = "player2name";

	// Default Player names.
	private static final String PLAYER_1_NAME_DEFAULT = "Player X";
	private static final String PLAYER_2_NAME_DEFAULT = "Player O";
	// ------------------------------------------------------

	// -----------DEFINING VARIABLES------------------------
	int currentPlayer = PLAYER_1; // starting with player 1 (X)
	int[] arr = new int[9]; // for default 3*3 grid

	int noOfMoves = 0;
	int minNoOfMoves = 5;
	int noOfCols = NO_OF_COLS_DEFAULT;
	int gameStatus = GAME_ON; // go play :)
	GridView mGridView;
	Animation growAndTurn; // the shake animation. earlier we were creating
							// object in
							// onClick... but this was loading resources
							// multiple
							// time. So moved it to class instance variable. So
							// only
							// one object of our Shake animation exists now :)

	ArrayList<Integer> arrWinningCols;// hack for showing//highlighting the
										// winning columns after Montu pointed
										// out we needded to show it to user
										// when he won. I
										// didnt find anything more elegant than
										// this :(

	// ------------------------------------------------------
	ImageView imgPlayerX, imgPlayerO;
	RelativeLayout rlRoot;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); // mandatory ..

		// set the layout for activity
		setContentView(R.layout.activity_main);

		// find the gridview we are playing with!
		mGridView = (GridView) findViewById(R.id.gvMain);

		ImageButton btnRefresh = (ImageButton) findViewById(R.id.btnRefresh);
		// button to refresh and restart the game

		// ImageButton btnSettings = (ImageButton)
		// findViewById(R.id.btnSettings);
		// (not really bwing used.)
		btnRefresh.setOnClickListener(this);

		rlRoot = (RelativeLayout) findViewById(R.id.rlRoot);
		imgPlayerX = (ImageView) findViewById(R.id.imgPlayerX);
		imgPlayerO = (ImageView) findViewById(R.id.imgPlayerO);

		// set event listener to the gridview.
		mGridView.setOnItemClickListener(this); // to handle clicks on grid.
												// That's what makes it play :)

		// our single reference to the animation object... alas.. it does get
		// created again when screen is rotated. Hopefuly the ealrier object
		// gets GC
		growAndTurn = AnimationUtils.loadAnimation(this, R.anim.grow);

		arrWinningCols = new ArrayList<Integer>();// the ugly hack arrayList :(

		// checking gameinstance for handling orientation change.
		if (savedInstanceState != null) {
			// set values into game variables from instance of previous
			// orientation.
			arr = savedInstanceState.getIntArray(KEY_GAMEARRAY);
			currentPlayer = savedInstanceState.getInt(KEY_CURRPLAYER);
			gameStatus = savedInstanceState.getInt(KEY_GAMESTAT);
			noOfMoves = savedInstanceState.getInt(KEY_NUM_MOVES);
			setUpGame(false); // not a new game, continue where we were...
		} else {
			setUpGame(true); // new game.
		}

		//Dialog mDialog = new Dialog(this);
		//mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//mDialog.setContentView(R.layout.dialog_layout);
		//mDialog.show();

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// Saving game variables in bundle which is used up in onCreate so as to
		// continue game when orientation is changed.
		outState.putIntArray(KEY_GAMEARRAY, arr);
		outState.putInt(KEY_CURRPLAYER, currentPlayer);
		outState.putInt(KEY_GAMESTAT, gameStatus);
		outState.putInt(KEY_NUM_MOVES, noOfMoves);
		super.onSaveInstanceState(outState); // dont forget this line!!!
	}

	/**
	 * initializes all game setttins, and binds the icons to the grid view. In
	 * case screen was rotated, set isNewGame as false, so that the state of the
	 * game before orientation change is restored. Be sure you have handled the
	 * previous game variables, before calling this functions in such a case.
	 * 
	 * @param isNewGame
	 *            : true if new game needs to be started,. false if we are to
	 *            conitnue from where we left off. (orientation change)
	 */
	void setUpGame(boolean isNewGame) {
		if (isNewGame) {
			// we need to get all values from shared preferences.
			SharedPreferences prefs = getSharedPreferences(PREFERNCE_NAME,
					MODE_PRIVATE);
			noOfMoves = prefs.getInt(KEY_NUM_MOVES, 0);
			currentPlayer = prefs.getInt(KEY_CURRPLAYER, PLAYER_1);
			gameStatus = prefs.getInt(KEY_GAMESTAT, GAME_ON);
			noOfCols = prefs.getInt(KEY_NO_OF_COLS, NO_OF_COLS_DEFAULT);
			mGridView.setNumColumns(noOfCols);
			// min no of moves = 2*n -1; (for 3 cols : 5, for 5 cols: 9
			// hoefully this holds true :)
			minNoOfMoves = 2 * noOfCols - 1;
			if (noOfCols != NO_OF_COLS_DEFAULT) {
				arr = Arrays.copyOf(arr, noOfCols * noOfCols); // in case the
																// size
																// of array was
																// changed from
																// default 9
			}
			for (int index = 0; index < arr.length; index++) {
				arr[index] = 0;
			}

		}
		switchPlayerIndicator();
		// aah., finally ready to start the game, present the game grid to the
		// user.
		mGridView.setAdapter(new GameGridAdapter(arr));

	}

	/**
	 * Checks possible combinations where game is won/tied.
	 * 
	 * @return gameStatus
	 */
	private int checkGameOver() {
		arrWinningCols.clear();
		// step 1 check diagonals. (this is done first because no of diagonals
		// will only be 2 and no of columns may be more.. we can avoid excessive
		// loops if we check diagonals first)
		// -------------------------------------------------------
		// check diagonal 1:
		// values in 0,(n+1), 2(n+1) ..... should be same.
		int factor = 0;
		int index = factor * (noOfCols + 1);
		int matchCount = 0;
		// arr[0] is empty means diagonal 1 is not valid for calculation.
		if (arr[0] != EMPTY) {
			while (index <= arr.length) {
				// System.out.println(arr[index]);

				if (arr[index] == arr[0] && arr[index] != EMPTY) {
					matchCount++;
					arrWinningCols.add(index);
					// System.out.println("match!" + matchCount);
				}
				factor++;
				index = factor * (noOfCols + 1);
			}
			if (matchCount == noOfCols) {
				Toast.makeText(getApplicationContext(),
						"D1:Player " + currentPlayer + " wins",
						Toast.LENGTH_SHORT).show();
				gameStatus = GAME_WON; // not really needed to do this here.
				for (int winningIndex : arrWinningCols) {
					arr[winningIndex] = currentPlayer + 2;
				}
				return GAME_WON;
			}
		}
		// --------------------------------------------------------
		// check diagonal 2 :
		// values in (n-1), 2(n-1),3(n-1)... should be same.
		factor = 1;
		matchCount = 0;
		index = factor * (noOfCols - 1);
		arrWinningCols.clear();
		// arr[numOfCols] is EMPTY means Diagonal 2 is not valid for calculation
		if (arr[noOfCols - 1] != EMPTY) {
			while (index <= ((noOfCols * noOfCols) - noOfCols)) {
				System.out.println("arr[" + index + "]=" + arr[index]);

				if (arr[index] == arr[noOfCols - 1] && arr[index] != EMPTY) {
					matchCount++;
					System.out.println("match!" + matchCount);
					arrWinningCols.add(index);
				}
				factor++;
				index = factor * (noOfCols - 1);
			}
			if (matchCount == noOfCols) {
				System.out.println("game over." + currentPlayer + "wins");
				Toast.makeText(getApplicationContext(),
						"D2: Player " + currentPlayer + " wins",
						Toast.LENGTH_SHORT).show();
				gameStatus = GAME_WON;
				for (int winningIndex : arrWinningCols) {
					arr[winningIndex] = currentPlayer + 2;
				}
				return GAME_WON;
			}
		}

		// --------------------------------------------------------
		// step 2 check all columns. (done later )
		// for each col, : (colindex+n),
		// 2(colindex+n),3(colindex+n),4(colindex+n) should be same.
		for (int i = 0; i < noOfCols; i++) {
			// System.out.println("checking col" + i);
			factor = 1;
			index = i + (factor * noOfCols);
			matchCount = 1;
			arrWinningCols.clear();
			arrWinningCols.add(i);
			if (arr[i] == EMPTY) {
				continue;
			}
			while (index < arr.length) {
				// System.out.println("arr[" + index + "]=" + arr[index] +
				// "/arr["
				// + i + "]=" + arr[i]);
				if (arr[index] == arr[i]) {
					matchCount++;
					// System.out.println("col" + i + ":match" + matchCount);
					arrWinningCols.add(index);
				} else {
					break;
				}
				factor++;
				index = i + (factor * noOfCols);
			}
			if (matchCount == noOfCols) {
				gameStatus = GAME_WON;
				Toast.makeText(getApplicationContext(),
						i + "V:Player " + currentPlayer + " wins",
						Toast.LENGTH_SHORT).show();
				// gameStatus = GAME_OVER;
				for (int winningIndex : arrWinningCols) {
					arr[winningIndex] = currentPlayer + 2;
				}
				return GAME_WON;
			}
		}

		// --------------------------------------------------------
		// for each row
		// rowindex, rowindex+1, rowindex+2, rowindex+3...rowIndex+n should be
		// same. loop from 0 to n^2 -n
		// factor = 1;
		// index = i + (factor * numOfCols);

		for (int i = 0; i <= ((noOfCols * noOfCols) - noOfCols); i += noOfCols) {
			arrWinningCols.clear();
			arrWinningCols.add(i);
			if (arr[i] == EMPTY) {
				continue;
			}
			matchCount = 0;
			for (int j = 0; j < noOfCols; j++) {
				if (arr[i + j] == arr[i]) {
					matchCount++;
					arrWinningCols.add(i + j);

				}
			}
			if (matchCount == noOfCols) {
				gameStatus = GAME_WON;
				Toast.makeText(getApplicationContext(),
						i + "H:Player " + currentPlayer + " wins",
						Toast.LENGTH_SHORT).show();
				for (int winningIndex : arrWinningCols) {
					arr[winningIndex] = currentPlayer + 2;
				}
				return GAME_WON;
			}
		}

		// We checked all columns, rows, diagonals, and no oF moves is exhasted!
		if (noOfMoves == noOfCols * noOfCols) {
			// if we dont have a winner yet: game TIED!!!
			return GAME_TIED;
		}
		return GAME_ON;

	}

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
	 * menu; this adds items to the action bar if it is present.
	 * getMenuInflater().inflate(R.menu.main, menu); return true; }
	 */

	/***
	 * Custom Adapter for binding the grid view. We need this to show Xs and Os
	 * 
	 * @author 157462
	 * 
	 */
	class GameGridAdapter extends BaseAdapter {

		// the gamearray which will store the state of each grid.
		int[] gameArray;
		Animation animFadeOut; // again, avoid creating multiple animation
								// objects, when we can do it with one.

		public GameGridAdapter(int[] array) {
			// TODO Auto-generated constructor stub
			gameArray = array;
			animFadeOut = AnimationUtils.loadAnimation(MainActivity.this,
					R.anim.fadeout);

		}

		@Override
		public int getCount() {
			// return count of items.
			return gameArray == null ? 0 : gameArray.length;
		}

		@Override
		public Object getItem(int position) {
			// return the item
			return gameArray == null ? null : gameArray[position];
		}

		@Override
		public long getItemId(int position) {
			// return the item
			return gameArray == null ? 0 : gameArray[position];
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// code for using the recycled convertview...
			GridItemContainer item = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(MainActivity.this).inflate(
						R.layout.grid_item, null);
				item = new GridItemContainer();
				item.img = (ImageView) convertView.findViewById(R.id.imgGrid);
				item.value = 0;
				convertView.setTag(item);
			} else {
				Object tagValue = convertView.getTag();
				if (tagValue != null) {
					item = (GridItemContainer) tagValue;
				}
			}
			item.value = gameArray[position];

			switch (item.value) {
			case PLAYER_1:
			case PLAYER_1_VICTORIOUS: {
				item.img.setImageResource(R.drawable.x);
				break;
			}
			case PLAYER_2:
			case PLAYER_2_VICTORIOUS: {
				item.img.setImageResource(R.drawable.o);
				break;
			}
			case EMPTY:
				item.img.setImageResource(R.drawable.emptycell);
				break;
			}
			if (gameStatus == GAME_WON) {
				// we need to highlight the winning combination of cells.
				if (arr[position] == PLAYER_1 || arr[position] == PLAYER_2) {
					item.img.setAnimation(animFadeOut);
					// fading out the items that were not part of winning
					// combination
				}
			} else if (gameStatus == GAME_TIED) {
				item.img.setAnimation(animFadeOut);// nothing was won. fade out
													// everything
			}
			return convertView;
		}

		// holder class for items in the grid. improves performance of gridview
		// this way
		class GridItemContainer {
			ImageView img;
			int value;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// handle click on cell, change current player if move is valid, and
		// calculate if game has ended
		Object tagValue = view.getTag(); // get the item
		if (tagValue != null) {
			GridItemContainer item = (GridItemContainer) tagValue;
			int state = item.value;
			// System.out.println("player is" + currentPlayer);
			// System.out.println("state:#####" + state + id);

			// perform all the logic only when game is on.
			if (state == 0 && gameStatus == GAME_ON) {
				item.img.setAnimation(growAndTurn); // introduce the Xs and Os
													// with a
													// small animation.
				item.value = currentPlayer;
				// System.out.println("initial value at" + position + "="
				// + arr[position]);
				// System.out.println("initial value at 0" + arr[0]);
				arr[position] = currentPlayer;

				// for (int index = 0; index < arr.length; index++) {
				// System.out.println("##" + arr[index]);
				// }
				// System.out.println(position + "statechanged to:#####"
				// + arr[position]);
				switch (item.value) {
				case PLAYER_1: {
					item.img.setImageResource(R.drawable.x);
					break;
				}
				case PLAYER_2: {
					item.img.setImageResource(R.drawable.o);
					break;
				}
				case EMPTY:
					item.img.setImageResource(R.drawable.emptycell);
					break;
				}
				view.setTag(item);
				noOfMoves++;
				if (noOfMoves >= minNoOfMoves) {
					gameStatus = checkGameOver();
				}
			}
			if (gameStatus == GAME_ON) {
				currentPlayer = currentPlayer == PLAYER_1 ? PLAYER_2 : PLAYER_1;
				switchPlayerIndicator();

			} else if (gameStatus == GAME_WON) {
				performGameWonEvents();

			} else if (gameStatus == GAME_TIED) {
				performGameTiedEvents();
			}
		}

	}

	private void switchPlayerIndicator() {

		// FlipAnimation flipAnimation = new FlipAnimation(imgPlayerX,
		// imgPlayerO);
		// if (imgPlayerX.getVisibility() == View.GONE) {
		// flipAnimation.reverse();
		// }
		// rlRoot.startAnimation(flipAnimation);
		if (currentPlayer == PLAYER_1) {
			imgPlayerX.setVisibility(View.VISIBLE);
			imgPlayerO.setVisibility(View.GONE);
			imgPlayerX.startAnimation(growAndTurn);
		} else if (currentPlayer == PLAYER_2) {
			imgPlayerO.setVisibility(View.VISIBLE);
			imgPlayerX.setVisibility(View.GONE);
			imgPlayerO.startAnimation(growAndTurn);
		}
	}

	private void performGameWonEvents() {

		// save preferences
		SharedPreferences prefs = getSharedPreferences(PREFERNCE_NAME,
				MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();

		int playerOneScore = prefs.getInt(KEY_PLAYER1_SCORE, 0);
		int playerTwoScore = prefs.getInt(KEY_PLAYER2_SCORE, 0);
		String winnerName = "";
		int winnerScore = 0;
		if (currentPlayer == PLAYER_1) {
			playerOneScore++;
			winnerName = prefs.getString(KEY_PLAYER1_NAME,
					PLAYER_1_NAME_DEFAULT);
			winnerScore = playerOneScore;
		} else if (currentPlayer == PLAYER_2) {
			playerTwoScore++;
			winnerName = prefs.getString(KEY_PLAYER2_NAME,
					PLAYER_2_NAME_DEFAULT);
			winnerScore = playerTwoScore;
		}
		editor.putInt(KEY_PLAYER1_SCORE, playerOneScore);
		editor.putInt(KEY_PLAYER2_SCORE, playerTwoScore);
		editor.putInt(KEY_CURRPLAYER, currentPlayer);
		// victorious player to start next game!
		editor.commit();
		// show message!
		System.out.println("Winner is " + winnerName + " with score "
				+ winnerScore);
		for (int item : arr) {
			System.out.println("won" + item);
		}
		mGridView.setAdapter(new GameGridAdapter(arr));
	}

	private void performGameTiedEvents() {
		// System.out.println("OMG! game tied");
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.btnRefresh) {
			setUpGame(true);
		}
	}

}
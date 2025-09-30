package com.dadm.tictactoe;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    static final int DIALOG_DIFFICULTY_ID = 0;
    static final int DIALOG_QUIT_ID = 1;
    private TicTacToeGame mGame;
    private BoardView mBoardView;
    private TextView mInfoTextView;
    private boolean mGameOver = false;
    // Sonidos
    private MediaPlayer mHumanMediaPlayer;
    private MediaPlayer mComputerMediaPlayer;
    private SharedPreferences mPrefs;

    private TextView mScoreTextView;


    // Marcadores
    private int mHumanWins = 0;
    private int mComputerWins = 0;
    private int mTies = 0;

    // Quién empieza la siguiente partida
    private char mGoFirst = TicTacToeGame.HUMAN_PLAYER;



    private final Handler mHandler = new Handler();
    // Listener para detectar toques
    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (mGameOver) return false;

            int col = (int) event.getX() / mBoardView.getBoardCellWidth();
            int row = (int) event.getY() / mBoardView.getBoardCellHeight();
            int pos = row * 3 + col;

            if (setMove(TicTacToeGame.HUMAN_PLAYER, pos)) {
                int winner = mGame.checkForWinner();

                if (winner == 0) {
                    mInfoTextView.setText("It's Android's turn.");

                    // Esperar 1 segundo antes de que Android juegue
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            int move = mGame.getComputerMove();
                            setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                            int winner = mGame.checkForWinner();
                            checkWinner(winner);
                        }
                    }, 1000);

                } else {
                    checkWinner(winner);
                }
            }

            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);
        mScoreTextView = findViewById(R.id.scores);


        // restaurar scores persistentes
        mHumanWins = mPrefs.getInt("mHumanWins", 0);
        mComputerWins = mPrefs.getInt("mComputerWins", 0);
        mTies = mPrefs.getInt("mTies", 0);

        displayScores();

        // inicializar vistas
        mInfoTextView = findViewById(R.id.information);
        mGame = new TicTacToeGame();
        mBoardView = findViewById(R.id.board);
        mBoardView.setGame(mGame);

        // Listener para detectar toques en el tablero
        mBoardView.setOnTouchListener(mTouchListener);

        // restaurar o empezar nuevo juego
        if (savedInstanceState == null) {
            startNewGame();
        } else {
            mGame.setBoardState(savedInstanceState.getCharArray("board"));
            mGameOver = savedInstanceState.getBoolean("mGameOver");
            mInfoTextView.setText(savedInstanceState.getCharSequence("info"));
            mHumanWins = savedInstanceState.getInt("mHumanWins");
            mComputerWins = savedInstanceState.getInt("mComputerWins");
            mTies = savedInstanceState.getInt("mTies");
            mGoFirst = savedInstanceState.getChar("mGoFirst");

            displayScores();
            mBoardView.invalidate(); // redibuja el tablero con el estado restaurado
        }
    }


    private void startNewGame() {
        mGame.clearBoard();
        mGameOver = false;
        mBoardView.invalidate();
        mInfoTextView.setText("You go first.");
    }

    private boolean setMove(char player, int location) {
        if (mGame.setMove(player, location)) {
            mBoardView.invalidate(); // Redibujar tablero

            // Reproducir sonido
            if (player == TicTacToeGame.HUMAN_PLAYER) {
                mHumanMediaPlayer.start();
            } else {
                mComputerMediaPlayer.start();
            }
            return true;
        }
        return false;
    }

    private void checkWinner(int winner) {
        if (winner == 0) {
            mInfoTextView.setText("It's your turn.");
        } else if (winner == 1) {
            mInfoTextView.setText("It's a tie!");
            mTies++;  // sumamos empate
            mGameOver = true;
            displayScores();
        } else if (winner == 2) {
            mInfoTextView.setText("You won!");
            mHumanWins++;  // sumamos victoria humano
            mGameOver = true;
            displayScores();
        } else if (winner == 3) {
            mInfoTextView.setText("Android won!");
            mComputerWins++;  // sumamos victoria Android
            mGameOver = true;
            displayScores();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt("mHumanWins", mHumanWins);
        ed.putInt("mComputerWins", mComputerWins);
        ed.putInt("mTies", mTies);
        ed.apply();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.new_game) {
            startNewGame();
            return true;
        } else if (id == R.id.ai_difficulty) {
            showDialog(DIALOG_DIFFICULTY_ID);
            return true;
        } else if (id == R.id.quit) {
            showDialog(DIALOG_QUIT_ID);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Dialog dialog = null;

        switch (id) {
            case DIALOG_DIFFICULTY_ID:
                builder.setTitle("Choose difficulty");

                final CharSequence[] levels = {"Easy", "Harder", "Expert"};
                int selected = mGame.getDifficultyLevel().ordinal();

                builder.setSingleChoiceItems(levels, selected,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                mGame.setDifficultyLevel(
                                        TicTacToeGame.DifficultyLevel.values()[item]);
                                Toast.makeText(getApplicationContext(),
                                        "Difficulty set to " + levels[item],
                                        Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
                dialog = builder.create();
                break;

            case DIALOG_QUIT_ID:
                builder.setMessage("Are you sure you want to quit?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                MainActivity.this.finish();
                            }
                        })
                        .setNegativeButton("No", null);
                dialog = builder.create();
                break;
        }

        return dialog;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cargar sonidos desde res/raw
        mHumanMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.move_human);
        mComputerMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.move_android);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Liberar recursos de audio
        mHumanMediaPlayer.release();
        mComputerMediaPlayer.release();
    }

    private void displayScores() {
        String scoreText = "Human: " + mHumanWins +
                "  Android: " + mComputerWins +
                "  Ties: " + mTies;
        mScoreTextView.setText(scoreText);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharArray("board", mGame.getBoardState());
        outState.putBoolean("mGameOver", mGameOver);
        outState.putCharSequence("info", mInfoTextView.getText());
        outState.putInt("mHumanWins", mHumanWins);
        outState.putInt("mComputerWins", mComputerWins);
        outState.putInt("mTies", mTies);
        outState.putChar("mGoFirst", mGoFirst);
    }

}

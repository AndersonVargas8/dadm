package com.dadm.tictactoe;

import java.util.Random;

public class TicTacToeGame {

    public static final char HUMAN_PLAYER = 'X';
    public static final char COMPUTER_PLAYER = 'O';
    public static final char OPEN_SPOT = ' ';

    public static final int BOARD_SIZE = 9;

    private char mBoard[];
    private Random mRand;

    public TicTacToeGame() {
        mBoard = new char[BOARD_SIZE];
        mRand = new Random();
        clearBoard();
    }

    /** Limpia el tablero */
    public void clearBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            mBoard[i] = OPEN_SPOT;
        }
    }

    /** Pone una jugada */
    public void setMove(char player, int location) {
        if (mBoard[location] == OPEN_SPOT) {
            mBoard[location] = player;
        }
    }

    /** Jugada de la computadora */
    public int getComputerMove() {
        // Primero: ¿puede ganar?
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (mBoard[i] == OPEN_SPOT) {
                mBoard[i] = COMPUTER_PLAYER;
                if (checkForWinner() == 3) {
                    mBoard[i] = OPEN_SPOT;
                    return i;
                }
                mBoard[i] = OPEN_SPOT;
            }
        }

        // Segundo: ¿puede bloquear?
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (mBoard[i] == OPEN_SPOT) {
                mBoard[i] = HUMAN_PLAYER;
                if (checkForWinner() == 2) {
                    mBoard[i] = OPEN_SPOT;
                    return i;
                }
                mBoard[i] = OPEN_SPOT;
            }
        }

        // Tercero: jugada aleatoria
        int move;
        do {
            move = mRand.nextInt(BOARD_SIZE);
        } while (mBoard[move] != OPEN_SPOT);

        return move;
    }

    /** Verifica si hay ganador */
    public int checkForWinner() {
        // Horizontal
        for (int i = 0; i <= 6; i += 3) {
            if (mBoard[i] == HUMAN_PLAYER &&
                    mBoard[i+1] == HUMAN_PLAYER &&
                    mBoard[i+2] == HUMAN_PLAYER) return 2;
            if (mBoard[i] == COMPUTER_PLAYER &&
                    mBoard[i+1] == COMPUTER_PLAYER &&
                    mBoard[i+2] == COMPUTER_PLAYER) return 3;
        }

        // Vertical
        for (int i = 0; i < 3; i++) {
            if (mBoard[i] == HUMAN_PLAYER &&
                    mBoard[i+3] == HUMAN_PLAYER &&
                    mBoard[i+6] == HUMAN_PLAYER) return 2;
            if (mBoard[i] == COMPUTER_PLAYER &&
                    mBoard[i+3] == COMPUTER_PLAYER &&
                    mBoard[i+6] == COMPUTER_PLAYER) return 3;
        }

        // Diagonales
        if ((mBoard[0] == HUMAN_PLAYER &&
                mBoard[4] == HUMAN_PLAYER &&
                mBoard[8] == HUMAN_PLAYER) ||
                (mBoard[2] == HUMAN_PLAYER &&
                        mBoard[4] == HUMAN_PLAYER &&
                        mBoard[6] == HUMAN_PLAYER)) return 2;

        if ((mBoard[0] == COMPUTER_PLAYER &&
                mBoard[4] == COMPUTER_PLAYER &&
                mBoard[8] == COMPUTER_PLAYER) ||
                (mBoard[2] == COMPUTER_PLAYER &&
                        mBoard[4] == COMPUTER_PLAYER &&
                        mBoard[6] == COMPUTER_PLAYER)) return 3;

        // Empate
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (mBoard[i] != HUMAN_PLAYER && mBoard[i] != COMPUTER_PLAYER) {
                return 0; // sigue en juego
            }
        }

        return 1; // empate
    }
}

package com.example.cis183_finalproject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.Array;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Game extends AppCompatActivity
{
    BoardView bv;
    Board board;

    Cell to;
    Cell from;
    Piece currentPiece;
    boolean canSelectMoveCell = false;

    boolean playerTurn = true;

    boolean botTurn = false;

    Piece capturedPiece = null;

    ArrayList<Piece> botPieces;

    ArrayList<Move> matchMoves;

    ImageButton img_j_backArrow;

    TextView tv_j_username;
    TextView tv_j_timePassed;
    TextView tv_j_userTurn;
    TextView tv_j_captureAlert;
    ConstraintLayout cons_j_gameOver;
    TextView tv_j_result;
    TextView tv_j_time;
    TextView tv_j_numTurns;

    Move currentMove;

    boolean gameOver = false;

    int turnCounter = 0;

    Match currentMatch;

    boolean userWon = false;
    boolean botWon = false;

    Timer timer;
    TimerTask timerTask;
    int time = 0;

    DatabaseHelper dbHelper;

    Button btn_j_retry;
    Button btn_j_quit;
    boolean chainStarted = false;

    boolean captureMade;
    boolean moveMade;

    TextView tv_j_uF;
    TextView tv_j_uT;
    TextView tv_j_bT;
    TextView tv_j_bF;

    boolean isAnimating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.boardView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        dbHelper = new DatabaseHelper(this);

        currentMatch = new Match();
        currentMove = new Move();
        timer = new Timer();
        howMuchTimeHasPassed();

        matchMoves = new ArrayList<>();

        botPieces = new ArrayList<>();

        bv = findViewById(R.id.boardView);
        board = bv.getBoard();

        bv.removeSelectionRing();

        tv_j_username = findViewById(R.id.tv_v_game_username);
        tv_j_userTurn = findViewById(R.id.tv_v_game_turnAlert);
        tv_j_captureAlert = findViewById(R.id.tv_v_game_captureAlert);
        tv_j_timePassed = findViewById(R.id.tv_v_game_timePassed);
        tv_j_time = findViewById(R.id.tv_v_game_time);
        tv_j_numTurns = findViewById(R.id.tv_v_game_turns);
        tv_j_result = findViewById(R.id.tv_v_game_result);
        tv_j_uF = findViewById(R.id.tv_v_game_userMoveF);
        tv_j_uT = findViewById(R.id.tv_v_game_userMoveT);
        tv_j_bF = findViewById(R.id.tv_v_game_botMoveF);
        tv_j_bT = findViewById(R.id.tv_v_game_botMoveT);
        img_j_backArrow = findViewById(R.id.img_v_game_backArrow);
        cons_j_gameOver = findViewById(R.id.cons_v_game_gameOver);

        btn_j_quit = findViewById(R.id.btn_v_game_quit);
        btn_j_retry = findViewById(R.id.btn_v_game_retry);

        onSelectedPiece();

        tv_j_username.setText(SessionData.getSignedInUser().getUsername());

        buttonClickListener();

        tv_j_uF.setText("No Move");
        tv_j_uT.setText("No Move");
        tv_j_bF.setText("No Move");
        tv_j_bT.setText("No Move");

    }

    private int getSquareNumber(int row, int col)
    {
        int num = 0;
        for (int r = 0; r <= row; r++)
        {
            for (int c = 0; c < 8; c++)
            {
                if ((r + c) % 2 == 1)
                {
                    num++;

                }

                if (r == row && c == col)
                {
                    return num;
                }
            }
        }
        return -1;
    }

    private void howMuchTimeHasPassed()
    {
        timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        time++;
                        tv_j_timePassed.setText(getTimerText());
                    }
                });

            }
        };

        timer.schedule(timerTask, 0, 1000);
    }

    private String getTimerText()
    {
        int rounded = Math.round(time);

        String timerText = String.valueOf(rounded);

        return timerText;
    }

    private void buttonClickListener()
    {
        img_j_backArrow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!gameOver)
                {
                    SessionData.easyModeSelected = false;
                    startActivity(new Intent(Game.this, HomePage.class));
                }

            }
        });

        btn_j_retry.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                recreate();
            }
        });

        btn_j_quit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(Game.this, HomePage.class));
            }
        });

    }

    private boolean canPieceMove(Piece piece, Cell from, Cell to)
    {
        //Check if move is diagonal
        if (Math.abs(from.getCol() - to.getCol()) != Math.abs(from.getRow() - to.getRow()))
        {
            return false;
        }

        if (!piece.isCrowned())
        {
            if (piece.getColor().equals("Light"))
            {
                //Light moves UP (row decreases)
                if (from.getRow() - to.getRow() == 1 && Math.abs(from.getCol() - to.getCol()) == 1 && !to.containsPiece())
                {
                    return true;
                }
            }
            else
            {
                //Dark moves DOWN (row increases)
                if (to.getRow() - from.getRow() == 1 && Math.abs(from.getCol() - to.getCol()) == 1 && !to.containsPiece())
                {
                    return true;
                }
            }
        }
        else
        {
            //Crowned can move one step diagonally in either direction
            if (Math.abs(from.getRow() - to.getRow()) == 1 && Math.abs(from.getCol() - to.getCol()) == 1 && !to.containsPiece())
            {
                return true;
            }
        }



        return false;

    }

    private boolean canCapturePiece(Piece piece, Cell to)
    {
        if (piece == null || piece.getCell() == null)
        {
            Log.d("Game: ", "failure to do anything");
            return false;
        }

        //Non crowned pieces

        //    0   1   2   3   4   5   6   7   <-- column numbers
        //   +---+---+---+---+---+---+---+---+
        //0  |   |   |   |   |   |   |   |   |
        //   +---+---+---+---+---+---+---+---+
        //1  |   |   |   |   |   |   |   |   |
        //   +---+---+---+---+---+---+---+---+
        //2  |   |   |   |   |   |   |   |   |
        //   +---+---+---+---+---+---+---+---+
        //3  |   |   |   |   |   |   |   |   |
        //   +---+---+---+---+---+---+---+---+
        //4  |   |   |   |   |   |   |   |   |
        //   +---+---+---+---+---+---+---+---+
        //5  |   |   |   |   |   |   |   |   |
        //   +---+---+---+---+---+---+---+---+
        //6  |   |   |   |   |   |   |   |   |
        //   +---+---+---+---+---+---+---+---+
        //7  |   |   |   |   |   |   |   |   |
        //   +---+---+---+---+---+---+---+---+
        //      ^ row numbers on the left

        //upper
        boolean leftContainsPiece = false;
        boolean rightContainsPiece = false;

        //lower
        boolean lowerLeftContainsPiece = false;
        boolean lowerRightContainsPiece = false;

        //upper
        boolean piecesTouchingLeft = false;
        boolean piecesTouchingRight = false;

        //lower
        boolean lowerPiecesTouchingLeft = false;
        boolean lowerPiecesTouchingRight = false;

        //upper
        boolean leftSpecial = false;
        boolean rightSpecial = false;

        //lower
        boolean lowerLeftSpecial = false;
        boolean lowerRightSpecial = false;

        boolean canCapture = false;


        if (!piece.isCrowned())
        {
            int rightCellRow = -1;
            int rightCellCol = -1;
            Cell rightCell;
            int leftCellRow = -1;
            int leftCellCol = -1;
            Cell leftCell;

            int rightCellRowPiece = -1;
            int rightCellColPiece = -1;
            Cell rightCellPiece;
            int leftCellRowPiece = -1;
            int leftCellColPiece = -1;
            Cell leftCellPiece;
            if (piece.getColor().equals("Light"))
            {
                //2 row and 2 col away
                rightCellRow = piece.getCell().getRow() - 2;
                rightCellCol = piece.getCell().getCol() + 2;
                rightCell = board.getCell(rightCellRow, rightCellCol);
                leftCellRow = piece.getCell().getRow() - 2;
                leftCellCol = piece.getCell().getCol() - 2;
                leftCell = board.getCell(leftCellRow, leftCellCol);

                //1 row and 1 col away
                rightCellRowPiece = piece.getCell().getRow() - 1;
                rightCellColPiece = piece.getCell().getCol() + 1;
                rightCellPiece = board.getCell(rightCellRowPiece, rightCellColPiece);
                leftCellRowPiece = piece.getCell().getRow() - 1;
                leftCellColPiece = piece.getCell().getCol() - 1;
                leftCellPiece = board.getCell(leftCellRowPiece, leftCellColPiece);
            }
            else
            {
                //2 rows and 2 cols away
                rightCellRow = piece.getCell().getRow() + 2;
                rightCellCol = piece.getCell().getCol() + 2;
                rightCell = board.getCell(rightCellRow, rightCellCol);

                leftCellRow = piece.getCell().getRow() + 2;
                leftCellCol = piece.getCell().getCol() - 2;
                leftCell = board.getCell(leftCellRow, leftCellCol);

                //1 row and 1 col away
                rightCellRowPiece = piece.getCell().getRow() + 1;
                rightCellColPiece = piece.getCell().getCol() + 1;
                rightCellPiece = board.getCell(rightCellRowPiece, rightCellColPiece);

                leftCellRowPiece = piece.getCell().getRow() + 1;
                leftCellColPiece = piece.getCell().getCol() - 1;
                leftCellPiece = board.getCell(leftCellRowPiece, leftCellColPiece);
            }


            if (rightCell != null && rightCell.containsPiece())
            {
                rightContainsPiece = true;
            }

            if (leftCell != null && leftCell.containsPiece())
            {
                leftContainsPiece = true;
            }


            //check opponent piece in between
            if (rightCellPiece != null && rightCellPiece.containsPiece())
            {
                String color = piece.getColor();
                String otherColor = "";

                if (color.equals("Light"))
                {
                    otherColor = "Dark";
                }

                if (color.equals("Dark"))
                {
                    otherColor = "Light";
                }

                if (rightCellPiece.getPiece().getColor().equals(otherColor))
                {
                    piecesTouchingRight = true;
                }
            }

            if (leftCellPiece != null && leftCellPiece.containsPiece())
            {
                String color = piece.getColor();
                String otherColor = "";

                if (color.equals("Light"))
                {
                    otherColor = "Dark";
                }

                if (color.equals("Dark"))
                {
                    otherColor = "Light";
                }

                if (leftCellPiece.getPiece().getColor().equals(otherColor))
                {
                    piecesTouchingLeft = true;
                }
            }

            if(!leftContainsPiece && piecesTouchingLeft)
            {
                leftSpecial = true;
                canCapture = true;
            }
            if(!rightContainsPiece && piecesTouchingRight)
            {
                rightSpecial = true;
                canCapture = true;
            }

            //actual movement
            if (canCapture)
            {




                if (leftCell != null && !leftCell.containsPiece() && leftCellPiece != null && leftCellPiece.containsPiece())
                {
                    if (to == leftCell && leftSpecial)
                    {
                        Piece p = leftCellPiece.getPiece();
                        if (p != null)
                        {
                            Log.d("Game: ", "got piece");
                            capturedPiece = p;
                            return true;
                        }
                    }
                }
                if (rightCell != null && !rightCell.containsPiece() && rightCellPiece != null && rightCellPiece.containsPiece())
                {
                    if (to == rightCell && rightSpecial)
                    {
                        Piece p = rightCellPiece.getPiece();
                        if (p != null)
                        {
                            Log.d("Game: ", "got piece");
                            capturedPiece = p;
                            return true;
                        }
                    }
                }



            }
            else
            {
                Log.d("Game:" , "tried to capture but couldn't");
                return false;
            }
        }
        else
        {
            //Crowned pieces

            //    0   1   2   3   4   5   6   7   <-- column numbers
            //   +---+---+---+---+---+---+---+---+
            //0  |   |   |   |   |   |   |   |   |
            //   +---+---+---+---+---+---+---+---+
            //1  |   |   |   |   |   |   |   |   |
            //   +---+---+---+---+---+---+---+---+
            //2  |   |   |   |   |   |   |   |   |
            //   +---+---+---+---+---+---+---+---+
            //3  |   |   |   |   |   |   |   |   |
            //   +---+---+---+---+---+---+---+---+
            //4  |   |   |   |   |   |   |   |   |
            //   +---+---+---+---+---+---+---+---+
            //5  |   |   |   |   |   |   |   |   |
            //   +---+---+---+---+---+---+---+---+
            //6  |   |   |   |   |   |   |   |   |
            //   +---+---+---+---+---+---+---+---+
            //7  |   |   |   |   |   |   |   |   |
            //   +---+---+---+---+---+---+---+---+
            //      ^ row numbers on the left

            //UPPER
            //2 row and 2 col away
            int rightCellRow = piece.getCell().getRow() - 2;
            int rightCellCol = piece.getCell().getCol() + 2;
            Cell rightCell = board.getCell(rightCellRow, rightCellCol);
            int leftCellRow = piece.getCell().getRow() - 2;
            int leftCellCol = piece.getCell().getCol() - 2;
            Cell leftCell = board.getCell(leftCellRow, leftCellCol);

            //1 row and 1 col away
            int rightCellRowPiece = piece.getCell().getRow() - 1;
            int rightCellColPiece = piece.getCell().getCol() + 1;
            Cell rightCellPiece = board.getCell(rightCellRowPiece, rightCellColPiece);
            int leftCellRowPiece = piece.getCell().getRow() - 1;
            int leftCellColPiece = piece.getCell().getCol() - 1;
            Cell leftCellPiece = board.getCell(leftCellRowPiece, leftCellColPiece);


            //LOWER
            //2 row and 2 col away
            int lowerRightCellRow = piece.getCell().getRow() + 2;
            int lowerRightCellCol = piece.getCell().getCol() + 2;
            Cell lowerRightCell = board.getCell(lowerRightCellRow, lowerRightCellCol);
            int lowerLeftCellRow = piece.getCell().getRow() + 2;
            int lowerLeftCellCol = piece.getCell().getCol() - 2;
            Cell lowerLeftCell = board.getCell(lowerLeftCellRow, lowerLeftCellCol);

            //1 row and 1 col away
            int lowerRightCellRowPiece = piece.getCell().getRow() + 1;
            int lowerRightCellColPiece = piece.getCell().getCol() + 1;
            Cell lowerRightCellPiece = board.getCell(lowerRightCellRowPiece, lowerRightCellColPiece);
            int lowerLeftCellRowPiece = piece.getCell().getRow() + 1;
            int lowerLeftCellColPiece = piece.getCell().getCol() - 1;
            Cell lowerLeftCellPiece = board.getCell(lowerLeftCellRowPiece, lowerLeftCellColPiece);


            //upper
            if (rightCell != null && rightCell.containsPiece())
            {
                rightContainsPiece = true;
            }

            if (leftCell != null && leftCell.containsPiece())
            {
                leftContainsPiece = true;
            }


            //lower
            if (lowerRightCell != null && lowerRightCell.containsPiece())
            {
                lowerRightContainsPiece = true;
            }

            if (lowerLeftCell != null && lowerLeftCell.containsPiece())
            {
                lowerLeftContainsPiece = true;
            }

            //upper
            if (leftCellPiece != null && leftCellPiece.containsPiece())
            {
                String color = piece.getColor();
                String otherColor = "";

                if (color.equals("Light"))
                {
                    otherColor = "Dark";
                }

                if (color.equals("Dark"))
                {
                    otherColor = "Light";
                }

                if (leftCellPiece.getPiece().getColor().equals(otherColor))
                {
                    piecesTouchingLeft = true;
                }

            }

            if (rightCellPiece != null && rightCellPiece.containsPiece())
            {
                String color = piece.getColor();
                String otherColor = "";

                if (color.equals("Light"))
                {
                    otherColor = "Dark";
                }

                if (color.equals("Dark"))
                {
                    otherColor = "Light";
                }

                if (rightCellPiece.getPiece().getColor().equals(otherColor))
                {
                    piecesTouchingRight = true;
                }

            }

            //lower
            if (lowerLeftCellPiece != null && lowerLeftCellPiece.containsPiece())
            {
                String color = piece.getColor();
                String otherColor = "";

                if (color.equals("Light"))
                {
                    otherColor = "Dark";
                }

                if (color.equals("Dark"))
                {
                    otherColor = "Light";
                }

                if (lowerLeftCellPiece.getPiece().getColor().equals(otherColor))
                {
                    lowerPiecesTouchingLeft = true;
                }

            }

            if (lowerRightCellPiece != null && lowerRightCellPiece.containsPiece())
            {
                String color = piece.getColor();
                String otherColor = "";

                if (color.equals("Light"))
                {
                    otherColor = "Dark";
                }

                if (color.equals("Dark"))
                {
                    otherColor = "Light";
                }

                if (lowerRightCellPiece.getPiece().getColor().equals(otherColor))
                {
                    lowerPiecesTouchingRight = true;
                }

            }


            if(!leftContainsPiece && piecesTouchingLeft)
            {
                leftSpecial = true;
                canCapture = true;
            }
            if(!rightContainsPiece && piecesTouchingRight)
            {
                rightSpecial = true;
                canCapture = true;
            }
            if(!lowerRightContainsPiece && lowerPiecesTouchingRight)
            {
                lowerRightSpecial = true;
                canCapture = true;
            }
            if(!lowerLeftContainsPiece && lowerPiecesTouchingLeft)
            {
                lowerLeftSpecial = true;
                canCapture = true;
            }


            //actual movement
            if (canCapture)
            {
                if (leftCell != null && !leftCell.containsPiece() && leftCellPiece != null && leftCellPiece.containsPiece())
                {
                    if (to == leftCell && leftSpecial)
                    {
                        Piece p = leftCellPiece.getPiece();
                        if (p != null)
                        {
                            capturedPiece = p;
                            return true;
                        }
                    }
                }
                if (rightCell != null && !rightCell.containsPiece() && rightCellPiece != null && rightCellPiece.containsPiece())
                {
                    if (to == rightCell && rightSpecial)
                    {
                        Piece p = rightCellPiece.getPiece();
                        if (p != null)
                        {
                            capturedPiece = p;
                            return true;
                        }
                    }
                }


                if (lowerRightCell != null && !lowerRightCell.containsPiece() && lowerRightCellPiece != null && lowerRightCellPiece.containsPiece())
                {
                    if (to == lowerRightCell && lowerRightSpecial)
                    {
                        Piece p = lowerRightCellPiece.getPiece();
                        if (p != null)
                        {
                            capturedPiece = p;
                            return true;
                        }
                    }
                }

                if (lowerLeftCell != null && !lowerLeftCell.containsPiece() && lowerLeftCellPiece != null && lowerLeftCellPiece.containsPiece())
                {
                    if (to == lowerLeftCell && lowerLeftSpecial)
                    {
                        Piece p = lowerLeftCellPiece.getPiece();
                        if (p != null)
                        {
                            capturedPiece = p;
                            return true;
                        }
                    }
                }

            }
            else
            {
                return false;
            }
        }

        return false;
    }


    private boolean crownPiece(Piece piece)
    {
        if (piece.isCrowned())
        {
            return false;
        }

        String color = piece.getColor();

        if (color.equals("Light"))
        {
            //    0   1   2   3   4   5   6   7   <-- column numbers
            //   +---+---+---+---+---+---+---+---+
            //0  |   |   |   |   |   |   |   |   |
            //   +---+---+---+---+---+---+---+---+
            //1  |   |   |   |   |   |   |   |   |
            //   +---+---+---+---+---+---+---+---+
            //2  |   |   |   |   |   |   |   |   |
            //   +---+---+---+---+---+---+---+---+
            //3  |   |   |   |   |   |   |   |   |
            //   +---+---+---+---+---+---+---+---+
            //4  |   |   |   |   |   |   |   |   |
            //   +---+---+---+---+---+---+---+---+
            //5  |   |   |   |   |   |   |   |   |
            //   +---+---+---+---+---+---+---+---+
            //6  |   |   |   |   |   |   |   |   |
            //   +---+---+---+---+---+---+---+---+
            //7  |   |   |   |   |   |   |   |   |
            //   +---+---+---+---+---+---+---+---+
            //      ^ row numbers on the left

            if (piece.getCell().getRow() == 0)
            {
                return true;
            }
            else
            {
                return false;
            }

        }
        else if (color.equals("Dark"))
        {
            if (piece.getCell().getRow() == 7)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    private boolean isMoveOrCaptureSafe(Piece piece, Cell potenialTo, boolean goingToMove, boolean goingToCapture, String color)
    {
        int rightCellRow = -1;
        int rightCellCol = -1;
        Cell rightCell;

        int leftCellRow = -1;
        int leftCellCol = -1;
        Cell leftCell;

        int rightCellRowCapture = -1;
        int rightCellColCapture = -1;
        Cell rightCellCapture;

        int leftCellRowCapture = -1;
        int leftCellColCapture = -1;
        Cell leftCellCapture;

        //2 row and 2 col away
        int rightCellRowUpperCapture = -1;
        int rightCellColUpperCapture = -1;
        Cell rightCellUpperCapture;
        int leftCellRowUpperCapture = -1;
        int leftCellColUpperCapture = -1;
        Cell leftCellUpperCapture;

        //capture check
        int rightCellRowUpperCC = -1;
        int rightCellColUpperCC = -1;
        Cell rightCellUpperCC;
        int leftCellRowUpperCC = -1;
        int leftCellColUpperCC = -1;
        Cell leftCellUpperCC;

        int rightCellRowCC = -1;
        int rightCellColCC = -1;
        Cell rightCellCC;
        int leftCellRowCC = -1;
        int leftCellColCC = -1;
        Cell leftCellCC;

        //1 row and 1 col away
        int rightCellRowUpper = -1;
        int rightCellColUpper = -1;
        Cell rightCellUpper;
        int leftCellRowUpper = -1;
        int leftCellColUpper = -1;
        Cell leftCellUpper;

        int lowerLeftCellRow = -1;
        int lowerLeftCellCol = -1;
        Cell lowerLeftCell;

        int lowerRightCellRow = -1;
        int lowerRightCellCol = -1;
        Cell lowerRightCell;


        Board tempBoard = board.clone();

        //UPPER
        //2 row and 2 col away
        rightCellRowUpperCapture = potenialTo.getRow() - 2;
        rightCellColUpperCapture = potenialTo.getCol() + 2;
        rightCellUpperCapture = tempBoard.getCell(rightCellRowUpperCapture, rightCellColUpperCapture);
        leftCellRowUpperCapture = potenialTo.getRow() - 2;
        leftCellColUpperCapture = potenialTo.getCol() - 2;
        leftCellUpperCapture = tempBoard.getCell(leftCellRowUpperCapture, leftCellColUpperCapture);

        //capture check upper
        if (rightCellUpperCapture != null)
        {
            rightCellRowUpperCC = rightCellUpperCapture.getRow() - 1;
            rightCellColUpperCC = rightCellUpperCapture.getCol() + 1;
            rightCellUpperCC = tempBoard.getCell(rightCellRowUpperCC, rightCellColUpperCC);
        }
        else
        {
            rightCellUpperCC = null;
        }

        if (leftCellUpperCapture != null)
        {
            leftCellRowUpperCC = leftCellUpperCapture.getRow() - 1;
            leftCellColUpperCC = leftCellUpperCapture.getCol() - 1;
            leftCellUpperCC = tempBoard.getCell(leftCellRowUpperCC, leftCellColUpperCC);
        }
        else
        {
            leftCellUpperCC = null;
        }

        //1 row and 1 col away
        rightCellRowUpper = potenialTo.getRow() - 1;
        rightCellColUpper = potenialTo.getCol() + 1;
        rightCellUpper = tempBoard.getCell(rightCellRowUpper, rightCellColUpper);
        leftCellRowUpper = potenialTo.getRow() - 1;
        leftCellColUpper = potenialTo.getCol() - 1;
        leftCellUpper = tempBoard.getCell(leftCellRowUpper, leftCellColUpper);


        //LOWER
        //2 rows and 2 cols away
        rightCellRowCapture = potenialTo.getRow() + 2;
        rightCellColCapture = potenialTo.getCol() + 2;
        rightCellCapture = tempBoard.getCell(rightCellRowCapture, rightCellColCapture);

        leftCellRowCapture = potenialTo.getRow() + 2;
        leftCellColCapture = potenialTo.getCol() - 2;
        leftCellCapture = tempBoard.getCell(leftCellRowCapture, leftCellColCapture);

        //capture check lower
        if (rightCellCapture != null)
        {
            rightCellRowCC = rightCellCapture.getRow() + 1;
            rightCellColCC = rightCellCapture.getCol() + 1;
            rightCellCC = tempBoard.getCell(rightCellRowCC, rightCellColCC);
        }
        else
        {
            rightCellCC = null;
        }

        if (leftCellCapture != null)
        {
            leftCellRowCC = leftCellCapture.getRow() + 1;
            leftCellColCC = leftCellCapture.getCol() - 1;
            leftCellCC = tempBoard.getCell(leftCellRowCC, leftCellColCC);
        }
        else
        {
            leftCellCC = null;
        }


        //1 row and 1 col away
        rightCellRow = potenialTo.getRow() + 1;
        rightCellCol = potenialTo.getCol() + 1;
        rightCell = tempBoard.getCell(rightCellRow, rightCellCol);

        leftCellRow = potenialTo.getRow() + 1;
        leftCellCol = potenialTo.getCol() - 1;
        leftCell = tempBoard.getCell(leftCellRow, leftCellCol);




        if (goingToMove)
        {
            if (leftCell != null)
            {
                if (leftCell.containsPiece() && !leftCell.getPiece().getColor().equals(color))
                {
                    //if left cell contains piece, make sure
                    //upper right cell is not open

                    // +--+--+--+--+--+
                    // |  |  |x |  |  |
                    // +--+--+--+--+--+
                    // |  |to|  |  |  |
                    // +--+--+--+--+--+
                    // |[]|  |  |  |  |
                    // +--+--+--+--+--+
                    // |  |  |  |  |  |
                    // +--+--+--+--+--+

                    if (rightCellUpper != null)
                    {
                        if (!rightCellUpper.containsPiece())
                        {
                            //not safe
                            return false;
                        }
                    }


                }

            }


            if (rightCell != null)
            {
                if (rightCell.containsPiece() && !rightCell.getPiece().getColor().equals(color))
                {
                    if (leftCellUpper != null)
                    {
                        if (!leftCellUpper.containsPiece())
                        {
                            return false;
                        }
                    }


                }


            }

            //reminder: ensure fixes below work
            if (piece.isCrowned())
            {

                if (rightCellUpper != null && rightCellUpper.containsPiece() && !rightCellUpper.getPiece().getColor().equals(color))
                {

                    if (leftCell != null && !leftCell.containsPiece())
                    {
                        return false;
                    }

                }


                if (leftCellUpper != null && leftCellUpper.containsPiece() && !leftCellUpper.getPiece().getColor().equals(color))
                {

                    if (rightCell != null && !rightCell.containsPiece())
                    {
                        return false;
                    }

                }
            }



        }

        if (goingToCapture)
        {
            //some tears were shed making this

            Cell originalCell = piece.getCell();
            int r = originalCell.getRow();
            int c = originalCell.getCol();

            Cell cloneBotCell = tempBoard.getCell(r, c);

            //first remove the piece to be captured from tempBoard
            if (leftCell != null && leftCell.containsPiece() && !leftCell.getPiece().getColor().equals(color))
            {

                if (leftCellCapture != null && leftCellCapture.containsPiece() && leftCellCapture.getPiece().getColor().equals(color) && leftCellCapture == cloneBotCell)
                {
                    leftCell.removePiece();
                }
            }
            if (rightCell != null && rightCell.containsPiece() && !rightCell.getPiece().getColor().equals(color))
            {
                if (rightCellCapture != null && rightCellCapture.containsPiece() && rightCellCapture.getPiece().getColor().equals(color) && rightCellCapture == cloneBotCell)
                {
                    rightCell.removePiece();
                }

            }
            if (leftCellUpper != null && leftCellUpper.containsPiece() && !leftCellUpper.getPiece().getColor().equals(color))
            {
                if (leftCellUpperCapture != null && leftCellUpperCapture.containsPiece() && leftCellUpperCapture.getPiece().getColor().equals(color) && leftCellUpperCapture == cloneBotCell)
                {
                    leftCellUpper.removePiece();
                }

            }

            if (rightCellUpper != null && rightCellUpper.containsPiece() && !rightCellUpper.getPiece().getColor().equals(color))
            {
                if (rightCellUpperCapture != null && rightCellUpperCapture.containsPiece() && rightCellUpperCapture.getPiece().getColor().equals(color) && rightCellUpperCapture == cloneBotCell)
                {
                    rightCellUpper.removePiece();
                }
            }



            //then safety check
            if (leftCell != null && leftCell.containsPiece() && !leftCell.getPiece().getColor().equals(color))
            {

                if (rightCellUpper != null && !rightCellUpper.containsPiece())
                {
                    return false;
                }

            }

            if (rightCell != null && rightCell.containsPiece() && !rightCell.getPiece().getColor().equals(color))
            {
                if (leftCellUpper != null && !leftCellUpper.containsPiece())
                {
                    return false;
                }
            }

            if (piece.isCrowned())
            {

                if (leftCellUpper != null && leftCellUpper.containsPiece() && !leftCellUpper.getPiece().getColor().equals(color))
                {

                    if (rightCell != null && !rightCell.containsPiece())
                    {
                        return false;
                    }

                }

                if (rightCellUpper != null && rightCellUpper.containsPiece() && !rightCellUpper.getPiece().getColor().equals(color))
                {

                    if (leftCell != null && !leftCell.containsPiece())
                    {
                        return false;
                    }

                }
            }


        }

        //if it makes it to here, it is safe
        return true;
    }


    private void continueBotCapture(Piece botPiece, Cell targetCell, Cell originalCell)
    {
        Cell origin = botPiece.getCell();
        botPiece.animatePiece(botPiece, botPiece.getCell(), targetCell, bv);

        botPiece.objectMoveAnimator.removeAllListeners();

        botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                board.movePiece(origin.getRow(), origin.getCol(),
                        targetCell.getRow(), targetCell.getCol());

                if (capturedPiece != null)
                {
                    capturedPiece.getCell().removePiece();
                    capturedPiece = null;
                }

                if (crownPiece(botPiece) && !botPiece.isCrowned())
                {
                    botPiece.makeCrowned();
                }

                bv.invalidate();

                ArrayList<Cell> nextCaptures = isAnotherCaptureAvailable(botPiece);
                if (!nextCaptures.isEmpty())
                {
                    Random random = new Random();
                    Cell nextCell = nextCaptures.get(random.nextInt(nextCaptures.size()));
                    if (canCapturePiece(botPiece, nextCell))
                    {
                        continueBotCapture(botPiece, nextCell, originalCell); // recurse
                        return;
                    }
                }

                currentMove.setFromSquareRowB(originalCell.getRow());
                currentMove.setFromSquareColB(originalCell.getCol());
                currentMove.setToSquareRowB(botPiece.getCell().getRow());
                currentMove.setToSquareColB(botPiece.getCell().getCol());
                currentMove.setTurnNumber(turnCounter);
                matchMoves.add(currentMove);

                int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                tv_j_uF.setText(String.valueOf(uFrom));
                tv_j_uT.setText(String.valueOf(uTo));
                tv_j_bF.setText(String.valueOf(bFrom));
                tv_j_bT.setText(String.valueOf(bTo));

                currentMove = new Move();

                tv_j_userTurn.setVisibility(View.VISIBLE);

                turnCounter++;
                playerTurn = true;
                botTurn = false;

                boolean isStuck = canNoLongerMoveOrNoMorePieces();

                if (isStuck)
                {
                    timerTask.cancel();
                    timer.cancel();
                    tv_j_time.setText(getTimerText());
                    int rounded = Math.round(time);

                    cons_j_gameOver.setVisibility(View.VISIBLE);
                    gameOver = true;
                    tv_j_numTurns.setText(String.valueOf(turnCounter));

                    Log.d("Game", "GAME OVER");

                    int diffId = 0;

                    if (SessionData.easyModeSelected)
                    {
                        diffId = 1;
                    }
                    else
                    {
                        diffId = 2;
                    }

                    currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                    currentMatch.setTime(rounded);
                    currentMatch.setDifficultyId(diffId);
                    currentMatch.setResult(tv_j_result.getText().toString());
                    dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                }
            }
        });
    }


    private void intermediateDifficultyBotTurn()
    {
        moveMade = false;
        captureMade = false;

        //can capture more than one piece at a time
        //will check to make sure it is safe before capturing or moving


        botPieces.clear();

        if (playerTurn || !botTurn)
        {
            return;
        }

        for (int row = 0; row < 8; row++)
        {
            for (int col = 0; col < 8; col++)
            {
                Cell cell = board.getCell(row, col);
                if (cell.containsPiece())
                {
                    Piece piece = cell.getPiece();
                    if (piece.getColor().equals("Dark"))
                    {
                        botPieces.add(piece);
                    }
                }
            }
        }

        if (botPieces != null && botTurn)
        {
            checkForPotentialCaptures();

            if (captureMade)
            {
                captureMade = false;
                return;
            }

            int rightCellRow = -1;
            int rightCellCol = -1;
            Cell rightCell;

            int leftCellRow = -1;
            int leftCellCol = -1;
            Cell leftCell;

            int rightCellRowCapture = -1;
            int rightCellColCapture = -1;
            Cell rightCellCapture;

            int leftCellRowCapture = -1;
            int leftCellColCapture = -1;
            Cell leftCellCapture;

            //2 row and 2 col away
            int rightCellRowUpperCapture = -1;
            int rightCellColUpperCapture = -1;
            Cell rightCellUpperCapture;
            int leftCellRowUpperCapture = -1;
            int leftCellColUpperCapture = -1;
            Cell leftCellUpperCapture;

            //1 row and 1 col away
            int rightCellRowUpper = -1;
            int rightCellColUpper = -1;
            Cell rightCellUpper;
            int leftCellRowUpper = -1;
            int leftCellColUpper = -1;
            Cell leftCellUpper;

            ArrayList<Piece> optimalPieces = new ArrayList<>();
            ArrayList<Piece> leastOptimalPieces = new ArrayList<>();

            //boolean canCapture = false;

            for(Piece botPiece : botPieces)
            {
                if (!botPiece.isCrowned())
                {

                    tryToCrown(botPiece);

                    if (moveMade)
                    {
                        moveMade = false;
                        return;
                    }

                    //LOWER
                    //2 rows and 2 cols away
                    rightCellRowCapture = botPiece.getCell().getRow() + 2;
                    rightCellColCapture = botPiece.getCell().getCol() + 2;
                    rightCellCapture = board.getCell(rightCellRowCapture, rightCellColCapture);

                    leftCellRowCapture = botPiece.getCell().getRow() + 2;
                    leftCellColCapture = botPiece.getCell().getCol() - 2;
                    leftCellCapture = board.getCell(leftCellRowCapture, leftCellColCapture);


                    //1 row and 1 col away
                    rightCellRow = botPiece.getCell().getRow() + 1;
                    rightCellCol = botPiece.getCell().getCol() + 1;
                    rightCell = board.getCell(rightCellRow, rightCellCol);

                    leftCellRow = botPiece.getCell().getRow() + 1;
                    leftCellCol = botPiece.getCell().getCol() - 1;
                    leftCell = board.getCell(leftCellRow, leftCellCol);

                    //piece, from, to
                    boolean canMoveLeft = false;
                    boolean canCaptureAndMoveLeft = false;
                    boolean canMoveRight = false;
                    boolean canCaptureAndMoveRight = false;
                    if (leftCell != null)
                    {
                        canMoveLeft = canPieceMove(botPiece, botPiece.getCell(), leftCell);
                    }

                    if (leftCellCapture != null)
                    {
                        canCaptureAndMoveLeft = canCapturePiece(botPiece, leftCellCapture);
                    }

                    if (canCaptureAndMoveLeft)
                    {
                        boolean isSafe = isMoveOrCaptureSafe(botPiece, leftCellCapture, false, true, "Dark");

                        if (isSafe)
                        {
                            optimalPieces.add(botPiece);
                        }
                       else
                        {
                            leastOptimalPieces.add(botPiece);
                        }

                    }
                    else
                    {
                        if (rightCellCapture != null)
                        {
                            canCaptureAndMoveRight = canCapturePiece(botPiece, rightCellCapture);
                        }

                        if (canCaptureAndMoveRight)
                        {
                            boolean isSafe = isMoveOrCaptureSafe(botPiece, rightCellCapture, false, true, "Dark");

                            if (isSafe)
                            {
                                optimalPieces.add(botPiece);
                            }
                            else
                            {
                                leastOptimalPieces.add(botPiece);
                            }
                        }

                    }

                    if (!canMoveLeft)
                    {
                        if (rightCell != null)
                        {
                            canMoveRight = canPieceMove(botPiece, botPiece.getCell(), rightCell);
                        }

                        if (canMoveRight)
                        {
                            boolean isSafe = isMoveOrCaptureSafe(botPiece, rightCell, true, false, "Dark");

                            if (isSafe)
                            {
                                optimalPieces.add(botPiece);
                            }
                            else
                            {
                                leastOptimalPieces.add(botPiece);
                            }
                        }
                    }
                    else
                    {
                        boolean isSafe = isMoveOrCaptureSafe(botPiece, leftCell, true, false, "Dark");

                        if (isSafe)
                        {
                            optimalPieces.add(botPiece);
                        }
                        else
                        {
                            leastOptimalPieces.add(botPiece);
                        }
                    }
                }
                else
                {
                    //UPPER
                    //2 row and 2 col away
                    rightCellRowUpperCapture = botPiece.getCell().getRow() - 2;
                    rightCellColUpperCapture = botPiece.getCell().getCol() + 2;
                    rightCellUpperCapture = board.getCell(rightCellRowUpperCapture, rightCellColUpperCapture);
                    leftCellRowUpperCapture = botPiece.getCell().getRow() - 2;
                    leftCellColUpperCapture = botPiece.getCell().getCol() - 2;
                    leftCellUpperCapture = board.getCell(leftCellRowUpperCapture, leftCellColUpperCapture);

                    //1 row and 1 col away
                    rightCellRowUpper = botPiece.getCell().getRow() - 1;
                    rightCellColUpper = botPiece.getCell().getCol() + 1;
                    rightCellUpper = board.getCell(rightCellRowUpper, rightCellColUpper);
                    leftCellRowUpper = botPiece.getCell().getRow() - 1;
                    leftCellColUpper = botPiece.getCell().getCol() - 1;
                    leftCellUpper = board.getCell(leftCellRowUpper, leftCellColUpper);

                    //LOWER
                    //2 rows and 2 cols away
                    rightCellRowCapture = botPiece.getCell().getRow() + 2;
                    rightCellColCapture = botPiece.getCell().getCol() + 2;
                    rightCellCapture = board.getCell(rightCellRowCapture, rightCellColCapture);

                    leftCellRowCapture = botPiece.getCell().getRow() + 2;
                    leftCellColCapture = botPiece.getCell().getCol() - 2;
                    leftCellCapture = board.getCell(leftCellRowCapture, leftCellColCapture);


                    //1 row and 1 col away
                    rightCellRow = botPiece.getCell().getRow() + 1;
                    rightCellCol = botPiece.getCell().getCol() + 1;
                    rightCell = board.getCell(rightCellRow, rightCellCol);

                    leftCellRow = botPiece.getCell().getRow() + 1;
                    leftCellCol = botPiece.getCell().getCol() - 1;
                    leftCell = board.getCell(leftCellRow, leftCellCol);

                    //piece, from, to
                    boolean canMoveLeft = false;
                    boolean canMoveLeftUp = false;
                    boolean canCaptureAndMoveLeft = false;
                    boolean canCaptureAndMoveLeftUp = false;
                    boolean canMoveRight = false;
                    boolean canMoveRightUp = false;
                    boolean canCaptureAndMoveRight = false;
                    boolean canCaptureAndMoveRightUp = false;

                    if (leftCell != null)
                    {
                        canMoveLeft = canPieceMove(botPiece, botPiece.getCell(), leftCell);
                    }

                    if (leftCellUpper != null)
                    {
                        canMoveLeftUp = canPieceMove(botPiece, botPiece.getCell(), leftCellUpper);
                    }

                    if (leftCellCapture != null)
                    {
                        canCaptureAndMoveLeft = canCapturePiece(botPiece, leftCellCapture);
                    }

                    if (leftCellUpperCapture != null)
                    {
                        canCaptureAndMoveLeftUp = canCapturePiece(botPiece, leftCellUpperCapture);
                    }

                    if (canCaptureAndMoveLeft)
                    {

                        boolean isSafe = isMoveOrCaptureSafe(botPiece, leftCellCapture, false, true, "Dark");

                        if (isSafe)
                        {
                            optimalPieces.add(botPiece);
                        }
                        else
                        {
                            leastOptimalPieces.add(botPiece);
                        }

                    }
                    else if (canCaptureAndMoveLeftUp)
                    {
                        boolean isSafe = isMoveOrCaptureSafe(botPiece, leftCellUpperCapture, false, true, "Dark");

                        if (isSafe)
                        {
                            optimalPieces.add(botPiece);
                        }
                        else
                        {
                            leastOptimalPieces.add(botPiece);
                        }
                    }
                    else
                    {
                        if (rightCellCapture != null)
                        {
                            canCaptureAndMoveRight = canCapturePiece(botPiece, rightCellCapture);
                        }

                        if (rightCellUpperCapture != null)
                        {
                            canCaptureAndMoveRightUp = canCapturePiece(botPiece, rightCellUpperCapture);
                        }

                        if (canCaptureAndMoveRight)
                        {
                            boolean isSafe = isMoveOrCaptureSafe(botPiece, rightCellCapture, false, true, "Dark");

                            if (isSafe)
                            {
                                optimalPieces.add(botPiece);
                            }
                            else
                            {
                                leastOptimalPieces.add(botPiece);
                            }
                        }
                        else if (canCaptureAndMoveRightUp)
                        {
                            boolean isSafe = isMoveOrCaptureSafe(botPiece, rightCellUpperCapture, false, true, "Dark");

                            if (isSafe)
                            {
                                optimalPieces.add(botPiece);
                            }
                            else
                            {
                                leastOptimalPieces.add(botPiece);
                            }
                        }

                    }

                    if (!canMoveLeft)
                    {
                        if (rightCell != null)
                        {
                            canMoveRight = canPieceMove(botPiece, botPiece.getCell(), rightCell);
                        }

                        if (rightCellUpper != null)
                        {
                            canMoveRightUp = canPieceMove(botPiece, botPiece.getCell(), rightCellUpper);
                        }

                        if (canMoveRight)
                        {
                            boolean isSafe = isMoveOrCaptureSafe(botPiece, rightCell, true, false, "Dark");

                            if (isSafe)
                            {
                                optimalPieces.add(botPiece);
                            }
                            else
                            {
                                leastOptimalPieces.add(botPiece);
                            }
                        }
                        else if (canMoveRightUp)
                        {
                            boolean isSafe = isMoveOrCaptureSafe(botPiece, rightCellUpper, true, false, "Dark");

                            if (isSafe)
                            {
                                optimalPieces.add(botPiece);
                            }
                            else
                            {
                                leastOptimalPieces.add(botPiece);
                            }
                        }
                    }
                    else if (canMoveLeftUp)
                    {
                        boolean isSafe = isMoveOrCaptureSafe(botPiece, leftCellUpper, true, false, "Dark");

                        if (isSafe)
                        {
                            optimalPieces.add(botPiece);
                        }
                        else
                        {
                            leastOptimalPieces.add(botPiece);
                        }
                    }
                    else
                    {
                        boolean isSafe = isMoveOrCaptureSafe(botPiece, leftCell, true, false, "Dark");

                        if (isSafe)
                        {
                            optimalPieces.add(botPiece);
                        }
                        else
                        {
                            leastOptimalPieces.add(botPiece);
                        }
                    }

                }

            }

            //Bot will prefer to move optimal pieces
            if (optimalPieces.isEmpty())
            {
                for (Piece botPiece : leastOptimalPieces)
                {
                    if (!botPiece.isCrowned())
                    {

                        //LOWER
                        //2 rows and 2 cols away
                        rightCellRowCapture = botPiece.getCell().getRow() + 2;
                        rightCellColCapture = botPiece.getCell().getCol() + 2;
                        rightCellCapture = board.getCell(rightCellRowCapture, rightCellColCapture);

                        leftCellRowCapture = botPiece.getCell().getRow() + 2;
                        leftCellColCapture = botPiece.getCell().getCol() - 2;
                        leftCellCapture = board.getCell(leftCellRowCapture, leftCellColCapture);


                        //1 row and 1 col away
                        rightCellRow = botPiece.getCell().getRow() + 1;
                        rightCellCol = botPiece.getCell().getCol() + 1;
                        rightCell = board.getCell(rightCellRow, rightCellCol);

                        leftCellRow = botPiece.getCell().getRow() + 1;
                        leftCellCol = botPiece.getCell().getCol() - 1;
                        leftCell = board.getCell(leftCellRow, leftCellCol);

                        //piece, from, to
                        boolean canMoveLeft = false;
                        boolean canCaptureAndMoveLeft = false;
                        boolean canMoveRight = false;
                        boolean canCaptureAndMoveRight = false;
                        if (leftCell != null)
                        {
                            canMoveLeft = canPieceMove(botPiece, botPiece.getCell(), leftCell);
                        }

                        if (leftCellCapture != null)
                        {
                            canCaptureAndMoveLeft = canCapturePiece(botPiece, leftCellCapture);
                        }

                        if (canCaptureAndMoveLeft)
                        {
                            Cell origin = botPiece.getCell();
                            botPiece.animatePiece(botPiece, botPiece.getCell(), leftCellCapture, bv);

                            botPiece.objectMoveAnimator.removeAllListeners();

                            Cell finalLeftCellCapture = leftCellCapture;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(origin.getRow(), origin.getCol(), finalLeftCellCapture.getRow(), finalLeftCellCapture.getCol());

                                    if (capturedPiece != null)
                                    {
                                        capturedPiece.getCell().removePiece();
                                        capturedPiece = null;
                                    }

                                    if (crownPiece(botPiece) && !botPiece.isCrowned())
                                    {
                                        botPiece.makeCrowned();
                                    }

                                    bv.invalidate();

                                    ArrayList<Cell> possibleCells = isAnotherCaptureAvailable(botPiece);

                                    if (!possibleCells.isEmpty())
                                    {
                                        Random random = new Random();
                                        int randIndex = random.nextInt(possibleCells.size());
                                        Cell pCell = possibleCells.get(randIndex);

                                        if (canCapturePiece(botPiece, pCell))
                                        {
                                            continueBotCapture(botPiece, pCell, origin);

                                            return;
                                        }


                                    }

                                    currentMove.setFromSquareRowB(origin.getRow());
                                    currentMove.setFromSquareColB(origin.getCol());
                                    currentMove.setToSquareRowB(finalLeftCellCapture.getRow());
                                    currentMove.setToSquareColB(finalLeftCellCapture.getCol());
                                    currentMove.setTurnNumber(turnCounter);
                                    matchMoves.add(currentMove);

                                    int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                                    int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                                    int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                                    int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                                    tv_j_uF.setText(String.valueOf(uFrom));
                                    tv_j_uT.setText(String.valueOf(uTo));
                                    tv_j_bF.setText(String.valueOf(bFrom));
                                    tv_j_bT.setText(String.valueOf(bTo));

                                    currentMove = new Move();

                                    tv_j_userTurn.setVisibility(View.VISIBLE);

                                    turnCounter++;
                                    playerTurn = true;
                                    botTurn = false;

                                    boolean isStuck = canNoLongerMoveOrNoMorePieces();
                                    if (isStuck)
                                    {
                                        timerTask.cancel();
                                        timer.cancel();
                                        tv_j_time.setText(getTimerText());
                                        int rounded = Math.round(time);

                                        cons_j_gameOver.setVisibility(View.VISIBLE);
                                        gameOver = true;
                                        tv_j_numTurns.setText(String.valueOf(turnCounter));

                                        Log.d("Game", "GAME OVER");

                                        int diffId = 0;

                                        if (SessionData.easyModeSelected)
                                        {
                                            diffId = 1;
                                        }
                                        else
                                        {
                                            diffId = 2;
                                        }

                                        currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                        currentMatch.setTime(rounded);currentMatch.setDifficultyId(diffId);
                                        currentMatch.setResult(tv_j_result.getText().toString());
                                        dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                    }



                                }


                            });


                            return;
                        }
                        else
                        {
                            if (rightCellCapture != null)
                            {
                                canCaptureAndMoveRight = canCapturePiece(botPiece, rightCellCapture);
                            }

                            if (canCaptureAndMoveRight)
                            {
                                Cell origin = botPiece.getCell();
                                botPiece.animatePiece(botPiece, botPiece.getCell(), rightCellCapture, bv);

                                botPiece.objectMoveAnimator.removeAllListeners();

                                Cell finalRightCellCapture = rightCellCapture;
                                botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                                {
                                    @Override
                                    public void onAnimationEnd(Animator animation)
                                    {
                                        Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                        //Update board state AFTER animation completes
                                        board.movePiece(origin.getRow(), origin.getCol(), finalRightCellCapture.getRow(), finalRightCellCapture.getCol());

                                        if (capturedPiece != null)
                                        {
                                            capturedPiece.getCell().removePiece();
                                            capturedPiece = null;
                                        }

                                        if (crownPiece(botPiece) && !botPiece.isCrowned())
                                        {
                                            botPiece.makeCrowned();
                                        }

                                        bv.invalidate();

                                        ArrayList<Cell> possibleCells = isAnotherCaptureAvailable(botPiece);

                                        if (!possibleCells.isEmpty())
                                        {
                                            Random random = new Random();
                                            int randIndex = random.nextInt(possibleCells.size());
                                            Cell pCell = possibleCells.get(randIndex);

                                            if (canCapturePiece(botPiece, pCell))
                                            {
                                                continueBotCapture(botPiece, pCell, origin);

                                                return;
                                            }


                                        }

                                        currentMove.setFromSquareRowB(origin.getRow());
                                        currentMove.setFromSquareColB(origin.getCol());
                                        currentMove.setToSquareRowB(finalRightCellCapture.getRow());
                                        currentMove.setToSquareColB(finalRightCellCapture.getCol());
                                        currentMove.setTurnNumber(turnCounter);
                                        matchMoves.add(currentMove);

                                        int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                                        int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                                        int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                                        int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                                        tv_j_uF.setText(String.valueOf(uFrom));
                                        tv_j_uT.setText(String.valueOf(uTo));
                                        tv_j_bF.setText(String.valueOf(bFrom));
                                        tv_j_bT.setText(String.valueOf(bTo));

                                        currentMove = new Move();

                                        tv_j_userTurn.setVisibility(View.VISIBLE);

                                        turnCounter++;
                                        playerTurn = true;
                                        botTurn = false;

                                        boolean isStuck = canNoLongerMoveOrNoMorePieces();
                                        if (isStuck)
                                        {
                                            timerTask.cancel();
                                            timer.cancel();
                                            tv_j_time.setText(getTimerText());
                                            int rounded = Math.round(time);

                                            cons_j_gameOver.setVisibility(View.VISIBLE);
                                            gameOver = true;
                                            tv_j_numTurns.setText(String.valueOf(turnCounter));

                                            Log.d("Game", "GAME OVER");

                                            int diffId = 0;

                                            if (SessionData.easyModeSelected)
                                            {
                                                diffId = 1;
                                            }
                                            else
                                            {
                                                diffId = 2;
                                            }

                                            currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                            currentMatch.setTime(rounded);currentMatch.setDifficultyId(diffId);
                                            currentMatch.setResult(tv_j_result.getText().toString());
                                            dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                        }



                                    }


                                });

                                return;

                            }

                        }

                        if (!canMoveLeft)
                        {
                            if (rightCell != null)
                            {
                                canMoveRight = canPieceMove(botPiece, botPiece.getCell(), rightCell);
                            }

                            if (canMoveRight)
                            {
                                Cell origin = botPiece.getCell();
                                botPiece.animatePiece(botPiece, botPiece.getCell(), rightCell, bv);

                                currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                                currentMove.setFromSquareColB(botPiece.getCell().getCol());
                                currentMove.setToSquareRowB(rightCell.getRow());
                                currentMove.setToSquareColB(rightCell.getCol());
                                currentMove.setTurnNumber(turnCounter);
                                matchMoves.add(currentMove);

                                int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                                int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                                int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                                int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                                tv_j_uF.setText(String.valueOf(uFrom));
                                tv_j_uT.setText(String.valueOf(uTo));
                                tv_j_bF.setText(String.valueOf(bFrom));
                                tv_j_bT.setText(String.valueOf(bTo));

                                currentMove = new Move();

                                turnCounter++;

                                Cell finalRightCell = rightCell;
                                botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                                {
                                    @Override
                                    public void onAnimationEnd(Animator animation)
                                    {
                                        Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalRightCell.getRow() + "," + finalRightCell.getCol());
                                        //Update board state AFTER animation completes
                                        board.movePiece(origin.getRow(), origin.getCol(), finalRightCell.getRow(), finalRightCell.getCol());

                                        if (crownPiece(botPiece) && !botPiece.isCrowned())
                                        {
                                            botPiece.makeCrowned();
                                        }

                                        bv.invalidate();

                                        tv_j_userTurn.setVisibility(View.VISIBLE);


                                        playerTurn = true;
                                        botTurn = false;

                                        boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                        if (isStuck)
                                        {
                                            timerTask.cancel();
                                            timer.cancel();
                                            tv_j_time.setText(getTimerText());
                                            int rounded = Math.round(time);

                                            cons_j_gameOver.setVisibility(View.VISIBLE);
                                            gameOver = true;
                                            tv_j_numTurns.setText(String.valueOf(turnCounter));

                                            Log.d("Game", "GAME OVER");

                                            int diffId = 0;

                                            if (SessionData.easyModeSelected)
                                            {
                                                diffId = 1;
                                            }
                                            else
                                            {
                                                diffId = 2;
                                            }

                                            currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                            currentMatch.setTime(rounded);
                                            currentMatch.setDifficultyId(diffId);
                                            currentMatch.setResult(tv_j_result.getText().toString());
                                            dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                        }


                                    }
                                });

                                return;
                            }
                        }
                        else
                        {
                            Cell origin = botPiece.getCell();
                            botPiece.animatePiece(botPiece, botPiece.getCell(), leftCell, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(leftCell.getRow());
                            currentMove.setToSquareColB(leftCell.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                            int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                            int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                            int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                            tv_j_uF.setText(String.valueOf(uFrom));
                            tv_j_uT.setText(String.valueOf(uTo));
                            tv_j_bF.setText(String.valueOf(bFrom));
                            tv_j_bT.setText(String.valueOf(bTo));

                            currentMove = new Move();

                            turnCounter++;

                            Cell finalLeftCell = leftCell;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalLeftCell.getRow() + "," + finalLeftCell.getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(origin.getRow(), origin.getCol(), finalLeftCell.getRow(), finalLeftCell.getCol());

                                    if (crownPiece(botPiece) && !botPiece.isCrowned())
                                    {
                                        botPiece.makeCrowned();
                                    }

                                    bv.invalidate();

                                    tv_j_userTurn.setVisibility(View.VISIBLE);


                                    playerTurn = true;
                                    botTurn = false;

                                    boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                    if (isStuck)
                                    {
                                        timerTask.cancel();
                                        timer.cancel();
                                        tv_j_time.setText(getTimerText());
                                        int rounded = Math.round(time);

                                        cons_j_gameOver.setVisibility(View.VISIBLE);
                                        gameOver = true;
                                        tv_j_numTurns.setText(String.valueOf(turnCounter));

                                        Log.d("Game", "GAME OVER");

                                        int diffId = 0;

                                        if (SessionData.easyModeSelected)
                                        {
                                            diffId = 1;
                                        }
                                        else
                                        {
                                            diffId = 2;
                                        }

                                        currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                        currentMatch.setTime(rounded);
                                        currentMatch.setDifficultyId(diffId);
                                        currentMatch.setResult(tv_j_result.getText().toString());
                                        dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                    }


                                }
                            });

                            return;
                        }
                    }
                    else
                    {
                        //UPPER
                        //2 row and 2 col away
                        rightCellRowUpperCapture = botPiece.getCell().getRow() - 2;
                        rightCellColUpperCapture = botPiece.getCell().getCol() + 2;
                        rightCellUpperCapture = board.getCell(rightCellRowUpperCapture, rightCellColUpperCapture);
                        leftCellRowUpperCapture = botPiece.getCell().getRow() - 2;
                        leftCellColUpperCapture = botPiece.getCell().getCol() - 2;
                        leftCellUpperCapture = board.getCell(leftCellRowUpperCapture, leftCellColUpperCapture);

                        //1 row and 1 col away
                        rightCellRowUpper = botPiece.getCell().getRow() - 1;
                        rightCellColUpper = botPiece.getCell().getCol() + 1;
                        rightCellUpper = board.getCell(rightCellRowUpper, rightCellColUpper);
                        leftCellRowUpper = botPiece.getCell().getRow() - 1;
                        leftCellColUpper = botPiece.getCell().getCol() - 1;
                        leftCellUpper = board.getCell(leftCellRowUpper, leftCellColUpper);

                        //LOWER
                        //2 rows and 2 cols away
                        rightCellRowCapture = botPiece.getCell().getRow() + 2;
                        rightCellColCapture = botPiece.getCell().getCol() + 2;
                        rightCellCapture = board.getCell(rightCellRowCapture, rightCellColCapture);

                        leftCellRowCapture = botPiece.getCell().getRow() + 2;
                        leftCellColCapture = botPiece.getCell().getCol() - 2;
                        leftCellCapture = board.getCell(leftCellRowCapture, leftCellColCapture);


                        //1 row and 1 col away
                        rightCellRow = botPiece.getCell().getRow() + 1;
                        rightCellCol = botPiece.getCell().getCol() + 1;
                        rightCell = board.getCell(rightCellRow, rightCellCol);

                        leftCellRow = botPiece.getCell().getRow() + 1;
                        leftCellCol = botPiece.getCell().getCol() - 1;
                        leftCell = board.getCell(leftCellRow, leftCellCol);

                        //piece, from, to
                        boolean canMoveLeft = false;
                        boolean canMoveLeftUp = false;
                        boolean canCaptureAndMoveLeft = false;
                        boolean canCaptureAndMoveLeftUp = false;
                        boolean canMoveRight = false;
                        boolean canMoveRightUp = false;
                        boolean canCaptureAndMoveRight = false;
                        boolean canCaptureAndMoveRightUp = false;

                        if (leftCell != null)
                        {
                            canMoveLeft = canPieceMove(botPiece, botPiece.getCell(), leftCell);
                        }

                        if (leftCellUpper != null)
                        {
                            canMoveLeftUp = canPieceMove(botPiece, botPiece.getCell(), leftCellUpper);
                        }

                        if (leftCellCapture != null)
                        {
                            canCaptureAndMoveLeft = canCapturePiece(botPiece, leftCellCapture);
                        }

                        if (leftCellUpperCapture != null)
                        {
                            canCaptureAndMoveLeftUp = canCapturePiece(botPiece, leftCellUpperCapture);
                        }

                        if (canCaptureAndMoveLeft)
                        {
                            Cell origin = botPiece.getCell();
                            botPiece.animatePiece(botPiece, botPiece.getCell(), leftCellCapture, bv);

                            botPiece.objectMoveAnimator.removeAllListeners();

                            Cell finalLeftCellCapture = leftCellCapture;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(origin.getRow(), origin.getCol(), finalLeftCellCapture.getRow(), finalLeftCellCapture.getCol());

                                    if (capturedPiece != null)
                                    {
                                        capturedPiece.getCell().removePiece();
                                        capturedPiece = null;
                                    }

                                    if (crownPiece(botPiece) && !botPiece.isCrowned())
                                    {
                                        botPiece.makeCrowned();
                                    }

                                    bv.invalidate();

                                    ArrayList<Cell> possibleCells = isAnotherCaptureAvailable(botPiece);

                                    if (!possibleCells.isEmpty())
                                    {
                                        Random random = new Random();
                                        int randIndex = random.nextInt(possibleCells.size());
                                        Cell pCell = possibleCells.get(randIndex);

                                        if (canCapturePiece(botPiece, pCell))
                                        {
                                            continueBotCapture(botPiece, pCell, origin);

                                            return;
                                        }


                                    }

                                    currentMove.setFromSquareRowB(origin.getRow());
                                    currentMove.setFromSquareColB(origin.getCol());
                                    currentMove.setToSquareRowB(finalLeftCellCapture.getRow());
                                    currentMove.setToSquareColB(finalLeftCellCapture.getCol());
                                    currentMove.setTurnNumber(turnCounter);
                                    matchMoves.add(currentMove);

                                    int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                                    int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                                    int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                                    int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                                    tv_j_uF.setText(String.valueOf(uFrom));
                                    tv_j_uT.setText(String.valueOf(uTo));
                                    tv_j_bF.setText(String.valueOf(bFrom));
                                    tv_j_bT.setText(String.valueOf(bTo));

                                    currentMove = new Move();

                                    tv_j_userTurn.setVisibility(View.VISIBLE);

                                    turnCounter++;
                                    playerTurn = true;
                                    botTurn = false;

                                    boolean isStuck = canNoLongerMoveOrNoMorePieces();
                                    if (isStuck)
                                    {
                                        timerTask.cancel();
                                        timer.cancel();
                                        tv_j_time.setText(getTimerText());
                                        int rounded = Math.round(time);

                                        cons_j_gameOver.setVisibility(View.VISIBLE);
                                        gameOver = true;
                                        tv_j_numTurns.setText(String.valueOf(turnCounter));

                                        Log.d("Game", "GAME OVER");

                                        int diffId = 0;

                                        if (SessionData.easyModeSelected)
                                        {
                                            diffId = 1;
                                        }
                                        else
                                        {
                                            diffId = 2;
                                        }

                                        currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                        currentMatch.setTime(rounded);currentMatch.setDifficultyId(diffId);
                                        currentMatch.setResult(tv_j_result.getText().toString());
                                        dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                    }



                                }


                            });

                            return;

                        }
                        else if (canCaptureAndMoveLeftUp)
                        {
                            Cell origin = botPiece.getCell();
                            botPiece.animatePiece(botPiece, botPiece.getCell(), leftCellUpperCapture, bv);

                            botPiece.objectMoveAnimator.removeAllListeners();

                            Cell finalLeftCellUpperCapture = leftCellUpperCapture;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(origin.getRow(), origin.getCol(), finalLeftCellUpperCapture.getRow(), finalLeftCellUpperCapture.getCol());

                                    if (capturedPiece != null)
                                    {
                                        capturedPiece.getCell().removePiece();
                                        capturedPiece = null;
                                    }

                                    if (crownPiece(botPiece) && !botPiece.isCrowned())
                                    {
                                        botPiece.makeCrowned();
                                    }

                                    bv.invalidate();

                                    ArrayList<Cell> possibleCells = isAnotherCaptureAvailable(botPiece);

                                    if (!possibleCells.isEmpty())
                                    {
                                        Random random = new Random();
                                        int randIndex = random.nextInt(possibleCells.size());
                                        Cell pCell = possibleCells.get(randIndex);

                                        if (canCapturePiece(botPiece, pCell))
                                        {
                                            continueBotCapture(botPiece, pCell, origin);

                                            return;
                                        }


                                    }

                                    currentMove.setFromSquareRowB(origin.getRow());
                                    currentMove.setFromSquareColB(origin.getCol());
                                    currentMove.setToSquareRowB(finalLeftCellUpperCapture.getRow());
                                    currentMove.setToSquareColB(finalLeftCellUpperCapture.getCol());
                                    currentMove.setTurnNumber(turnCounter);
                                    matchMoves.add(currentMove);

                                    int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                                    int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                                    int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                                    int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                                    tv_j_uF.setText(String.valueOf(uFrom));
                                    tv_j_uT.setText(String.valueOf(uTo));
                                    tv_j_bF.setText(String.valueOf(bFrom));
                                    tv_j_bT.setText(String.valueOf(bTo));

                                    currentMove = new Move();

                                    tv_j_userTurn.setVisibility(View.VISIBLE);

                                    turnCounter++;
                                    playerTurn = true;
                                    botTurn = false;

                                    boolean isStuck = canNoLongerMoveOrNoMorePieces();
                                    if (isStuck)
                                    {
                                        timerTask.cancel();
                                        timer.cancel();
                                        tv_j_time.setText(getTimerText());
                                        int rounded = Math.round(time);

                                        cons_j_gameOver.setVisibility(View.VISIBLE);
                                        gameOver = true;
                                        tv_j_numTurns.setText(String.valueOf(turnCounter));

                                        Log.d("Game", "GAME OVER");

                                        int diffId = 0;

                                        if (SessionData.easyModeSelected)
                                        {
                                            diffId = 1;
                                        }
                                        else
                                        {
                                            diffId = 2;
                                        }

                                        currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                        currentMatch.setTime(rounded);currentMatch.setDifficultyId(diffId);
                                        currentMatch.setResult(tv_j_result.getText().toString());
                                        dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                    }



                                }


                            });

                            return;
                        }
                        else
                        {
                            if (rightCellCapture != null)
                            {
                                canCaptureAndMoveRight = canCapturePiece(botPiece, rightCellCapture);
                            }

                            if (rightCellUpperCapture != null)
                            {
                                canCaptureAndMoveRightUp = canCapturePiece(botPiece, rightCellUpperCapture);
                            }

                            if (canCaptureAndMoveRight)
                            {
                                Cell origin = botPiece.getCell();
                                botPiece.animatePiece(botPiece, botPiece.getCell(), rightCellCapture, bv);

                                botPiece.objectMoveAnimator.removeAllListeners();

                                Cell finalRightCellCapture = rightCellCapture;
                                botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                                {
                                    @Override
                                    public void onAnimationEnd(Animator animation)
                                    {
                                        Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                        //Update board state AFTER animation completes
                                        board.movePiece(origin.getRow(), origin.getCol(), finalRightCellCapture.getRow(), finalRightCellCapture.getCol());

                                        if (capturedPiece != null)
                                        {
                                            capturedPiece.getCell().removePiece();
                                            capturedPiece = null;
                                        }

                                        if (crownPiece(botPiece) && !botPiece.isCrowned())
                                        {
                                            botPiece.makeCrowned();
                                        }

                                        bv.invalidate();

                                        ArrayList<Cell> possibleCells = isAnotherCaptureAvailable(botPiece);

                                        if (!possibleCells.isEmpty())
                                        {
                                            Random random = new Random();
                                            int randIndex = random.nextInt(possibleCells.size());
                                            Cell pCell = possibleCells.get(randIndex);

                                            if (canCapturePiece(botPiece, pCell))
                                            {
                                                continueBotCapture(botPiece, pCell, origin);

                                                return;
                                            }


                                        }

                                        currentMove.setFromSquareRowB(origin.getRow());
                                        currentMove.setFromSquareColB(origin.getCol());
                                        currentMove.setToSquareRowB(finalRightCellCapture.getRow());
                                        currentMove.setToSquareColB(finalRightCellCapture.getCol());
                                        currentMove.setTurnNumber(turnCounter);
                                        matchMoves.add(currentMove);

                                        int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                                        int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                                        int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                                        int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                                        tv_j_uF.setText(String.valueOf(uFrom));
                                        tv_j_uT.setText(String.valueOf(uTo));
                                        tv_j_bF.setText(String.valueOf(bFrom));
                                        tv_j_bT.setText(String.valueOf(bTo));

                                        currentMove = new Move();

                                        tv_j_userTurn.setVisibility(View.VISIBLE);

                                        turnCounter++;
                                        playerTurn = true;
                                        botTurn = false;

                                        boolean isStuck = canNoLongerMoveOrNoMorePieces();
                                        if (isStuck)
                                        {
                                            timerTask.cancel();
                                            timer.cancel();
                                            tv_j_time.setText(getTimerText());
                                            int rounded = Math.round(time);

                                            cons_j_gameOver.setVisibility(View.VISIBLE);
                                            gameOver = true;
                                            tv_j_numTurns.setText(String.valueOf(turnCounter));

                                            Log.d("Game", "GAME OVER");

                                            int diffId = 0;

                                            if (SessionData.easyModeSelected)
                                            {
                                                diffId = 1;
                                            }
                                            else
                                            {
                                                diffId = 2;
                                            }

                                            currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                            currentMatch.setTime(rounded);currentMatch.setDifficultyId(diffId);
                                            currentMatch.setResult(tv_j_result.getText().toString());
                                            dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                        }



                                    }


                                });

                                return;
                            }
                            else if (canCaptureAndMoveRightUp)
                            {
                                Cell origin = botPiece.getCell();
                                botPiece.animatePiece(botPiece, botPiece.getCell(), rightCellUpperCapture, bv);

                                botPiece.objectMoveAnimator.removeAllListeners();

                                Cell finalRightCellUpperCapture = rightCellUpperCapture;
                                botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                                {
                                    @Override
                                    public void onAnimationEnd(Animator animation)
                                    {
                                        Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                        //Update board state AFTER animation completes
                                        board.movePiece(origin.getRow(), origin.getCol(), finalRightCellUpperCapture.getRow(), finalRightCellUpperCapture.getCol());

                                        if (capturedPiece != null)
                                        {
                                            capturedPiece.getCell().removePiece();
                                            capturedPiece = null;
                                        }

                                        if (crownPiece(botPiece) && !botPiece.isCrowned())
                                        {
                                            botPiece.makeCrowned();
                                        }

                                        bv.invalidate();

                                        ArrayList<Cell> possibleCells = isAnotherCaptureAvailable(botPiece);

                                        if (!possibleCells.isEmpty())
                                        {
                                            Random random = new Random();
                                            int randIndex = random.nextInt(possibleCells.size());
                                            Cell pCell = possibleCells.get(randIndex);

                                            if (canCapturePiece(botPiece, pCell))
                                            {
                                                continueBotCapture(botPiece, pCell, origin);

                                                return;
                                            }


                                        }

                                        currentMove.setFromSquareRowB(origin.getRow());
                                        currentMove.setFromSquareColB(origin.getCol());
                                        currentMove.setToSquareRowB(finalRightCellUpperCapture.getRow());
                                        currentMove.setToSquareColB(finalRightCellUpperCapture.getCol());
                                        currentMove.setTurnNumber(turnCounter);
                                        matchMoves.add(currentMove);

                                        int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                                        int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                                        int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                                        int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                                        tv_j_uF.setText(String.valueOf(uFrom));
                                        tv_j_uT.setText(String.valueOf(uTo));
                                        tv_j_bF.setText(String.valueOf(bFrom));
                                        tv_j_bT.setText(String.valueOf(bTo));

                                        currentMove = new Move();

                                        tv_j_userTurn.setVisibility(View.VISIBLE);

                                        turnCounter++;
                                        playerTurn = true;
                                        botTurn = false;

                                        boolean isStuck = canNoLongerMoveOrNoMorePieces();
                                        if (isStuck)
                                        {
                                            timerTask.cancel();
                                            timer.cancel();
                                            tv_j_time.setText(getTimerText());
                                            int rounded = Math.round(time);

                                            cons_j_gameOver.setVisibility(View.VISIBLE);
                                            gameOver = true;
                                            tv_j_numTurns.setText(String.valueOf(turnCounter));

                                            Log.d("Game", "GAME OVER");

                                            int diffId = 0;

                                            if (SessionData.easyModeSelected)
                                            {
                                                diffId = 1;
                                            }
                                            else
                                            {
                                                diffId = 2;
                                            }

                                            currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                            currentMatch.setTime(rounded);currentMatch.setDifficultyId(diffId);
                                            currentMatch.setResult(tv_j_result.getText().toString());
                                            dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                        }



                                    }


                                });

                                return;
                            }

                        }

                        if (!canMoveLeft)
                        {
                            if (rightCell != null)
                            {
                                canMoveRight = canPieceMove(botPiece, botPiece.getCell(), rightCell);
                            }

                            if (rightCellUpper != null)
                            {
                                canMoveRightUp = canPieceMove(botPiece, botPiece.getCell(), rightCellUpper);
                            }

                            if (canMoveRight)
                            {
                                Cell origin = botPiece.getCell();
                                botPiece.animatePiece(botPiece, botPiece.getCell(), rightCell, bv);

                                currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                                currentMove.setFromSquareColB(botPiece.getCell().getCol());
                                currentMove.setToSquareRowB(rightCell.getRow());
                                currentMove.setToSquareColB(rightCell.getCol());
                                currentMove.setTurnNumber(turnCounter);
                                matchMoves.add(currentMove);

                                int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                                int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                                int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                                int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                                tv_j_uF.setText(String.valueOf(uFrom));
                                tv_j_uT.setText(String.valueOf(uTo));
                                tv_j_bF.setText(String.valueOf(bFrom));
                                tv_j_bT.setText(String.valueOf(bTo));

                                currentMove = new Move();

                                Cell finalRightCell = rightCell;
                                botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                                {
                                    @Override
                                    public void onAnimationEnd(Animator animation)
                                    {
                                        Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalRightCell.getRow() + "," + finalRightCell.getCol());
                                        //Update board state AFTER animation completes
                                        board.movePiece(origin.getRow(), origin.getCol(), finalRightCell.getRow(), finalRightCell.getCol());


                                        bv.invalidate();

                                        tv_j_userTurn.setVisibility(View.VISIBLE);

                                        turnCounter++;
                                        playerTurn = true;
                                        botTurn = false;

                                        boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                        if (isStuck)
                                        {
                                            timerTask.cancel();
                                            timer.cancel();
                                            tv_j_time.setText(getTimerText());
                                            int rounded = Math.round(time);

                                            cons_j_gameOver.setVisibility(View.VISIBLE);
                                            gameOver = true;
                                            tv_j_numTurns.setText(String.valueOf(turnCounter));

                                            Log.d("Game", "GAME OVER");

                                            int diffId = 0;

                                            if (SessionData.easyModeSelected)
                                            {
                                                diffId = 1;
                                            }
                                            else
                                            {
                                                diffId = 2;
                                            }

                                            currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                            currentMatch.setTime(rounded);
                                            currentMatch.setDifficultyId(diffId);
                                            currentMatch.setResult(tv_j_result.getText().toString());
                                            dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                        }


                                    }
                                });

                                return;
                            }
                            else if (canMoveRightUp)
                            {
                                Cell origin = botPiece.getCell();
                                botPiece.animatePiece(botPiece, botPiece.getCell(), rightCellUpper, bv);

                                currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                                currentMove.setFromSquareColB(botPiece.getCell().getCol());
                                currentMove.setToSquareRowB(rightCellUpper.getRow());
                                currentMove.setToSquareColB(rightCellUpper.getCol());
                                currentMove.setTurnNumber(turnCounter);
                                matchMoves.add(currentMove);

                                int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                                int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                                int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                                int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                                tv_j_uF.setText(String.valueOf(uFrom));
                                tv_j_uT.setText(String.valueOf(uTo));
                                tv_j_bF.setText(String.valueOf(bFrom));
                                tv_j_bT.setText(String.valueOf(bTo));

                                currentMove = new Move();

                                Cell finalRightCellUpper = rightCellUpper;
                                botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                                {
                                    @Override
                                    public void onAnimationEnd(Animator animation)
                                    {
                                        Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalRightCellUpper.getRow() + "," + finalRightCellUpper.getCol());
                                        //Update board state AFTER animation completes
                                        board.movePiece(origin.getRow(), origin.getCol(), finalRightCellUpper.getRow(), finalRightCellUpper.getCol());


                                        bv.invalidate();

                                        tv_j_userTurn.setVisibility(View.VISIBLE);

                                        turnCounter++;
                                        playerTurn = true;
                                        botTurn = false;

                                        boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                        if (isStuck)
                                        {
                                            timerTask.cancel();
                                            timer.cancel();
                                            tv_j_time.setText(getTimerText());
                                            int rounded = Math.round(time);

                                            cons_j_gameOver.setVisibility(View.VISIBLE);
                                            gameOver = true;
                                            tv_j_numTurns.setText(String.valueOf(turnCounter));

                                            Log.d("Game", "GAME OVER");

                                            int diffId = 0;

                                            if (SessionData.easyModeSelected)
                                            {
                                                diffId = 1;
                                            }
                                            else
                                            {
                                                diffId = 2;
                                            }

                                            currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                            currentMatch.setTime(rounded);
                                            currentMatch.setDifficultyId(diffId);
                                            currentMatch.setResult(tv_j_result.getText().toString());
                                            dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                        }

                                    }
                                });

                                return;
                            }
                        }
                        else if (canMoveLeftUp)
                        {
                            Cell origin = botPiece.getCell();
                            botPiece.animatePiece(botPiece, botPiece.getCell(), leftCellUpper, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(leftCellUpper.getRow());
                            currentMove.setToSquareColB(leftCellUpper.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                            int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                            int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                            int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                            tv_j_uF.setText(String.valueOf(uFrom));
                            tv_j_uT.setText(String.valueOf(uTo));
                            tv_j_bF.setText(String.valueOf(bFrom));
                            tv_j_bT.setText(String.valueOf(bTo));

                            currentMove = new Move();

                            Cell finalLeftCellUpper = leftCellUpper;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalLeftCellUpper.getRow() + "," + finalLeftCellUpper.getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(origin.getRow(), origin.getCol(), finalLeftCellUpper.getRow(), finalLeftCellUpper.getCol());


                                    bv.invalidate();

                                    tv_j_userTurn.setVisibility(View.VISIBLE);

                                    turnCounter++;
                                    playerTurn = true;
                                    botTurn = false;

                                    boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                    if (isStuck)
                                    {
                                        timerTask.cancel();
                                        timer.cancel();
                                        tv_j_time.setText(getTimerText());
                                        int rounded = Math.round(time);

                                        cons_j_gameOver.setVisibility(View.VISIBLE);
                                        gameOver = true;
                                        tv_j_numTurns.setText(String.valueOf(turnCounter));

                                        Log.d("Game", "GAME OVER");

                                        int diffId = 0;

                                        if (SessionData.easyModeSelected)
                                        {
                                            diffId = 1;
                                        }
                                        else
                                        {
                                            diffId = 2;
                                        }

                                        currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                        currentMatch.setTime(rounded);
                                        currentMatch.setDifficultyId(diffId);
                                        currentMatch.setResult(tv_j_result.getText().toString());
                                        dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                    }

                                }
                            });

                            return;
                        }
                        else
                        {
                            Cell origin = botPiece.getCell();
                            botPiece.animatePiece(botPiece, botPiece.getCell(), leftCell, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(leftCell.getRow());
                            currentMove.setToSquareColB(leftCell.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            turnCounter++;

                            int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                            int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                            int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                            int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                            tv_j_uF.setText(String.valueOf(uFrom));
                            tv_j_uT.setText(String.valueOf(uTo));
                            tv_j_bF.setText(String.valueOf(bFrom));
                            tv_j_bT.setText(String.valueOf(bTo));

                            currentMove = new Move();

                            Cell finalLeftCell = leftCell;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalLeftCell.getRow() + "," + finalLeftCell.getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(origin.getRow(), origin.getCol(), finalLeftCell.getRow(), finalLeftCell.getCol());


                                    bv.invalidate();

                                    tv_j_userTurn.setVisibility(View.VISIBLE);


                                    playerTurn = true;
                                    botTurn = false;

                                    boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                    if (isStuck)
                                    {
                                        timerTask.cancel();
                                        timer.cancel();
                                        tv_j_time.setText(getTimerText());
                                        int rounded = Math.round(time);

                                        cons_j_gameOver.setVisibility(View.VISIBLE);
                                        gameOver = true;
                                        tv_j_numTurns.setText(String.valueOf(turnCounter));

                                        Log.d("Game", "GAME OVER");

                                        int diffId = 0;

                                        if (SessionData.easyModeSelected)
                                        {
                                            diffId = 1;
                                        }
                                        else
                                        {
                                            diffId = 2;
                                        }

                                        currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                        currentMatch.setTime(rounded);
                                        currentMatch.setDifficultyId(diffId);
                                        currentMatch.setResult(tv_j_result.getText().toString());
                                        dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                    }


                                }
                            });

                            return;
                        }

                    }
                }
            }
            else
            {
                for (Piece botPiece : optimalPieces)
                {
                    if (!botPiece.isCrowned())
                    {

                        //LOWER
                        //2 rows and 2 cols away
                        rightCellRowCapture = botPiece.getCell().getRow() + 2;
                        rightCellColCapture = botPiece.getCell().getCol() + 2;
                        rightCellCapture = board.getCell(rightCellRowCapture, rightCellColCapture);

                        leftCellRowCapture = botPiece.getCell().getRow() + 2;
                        leftCellColCapture = botPiece.getCell().getCol() - 2;
                        leftCellCapture = board.getCell(leftCellRowCapture, leftCellColCapture);


                        //1 row and 1 col away
                        rightCellRow = botPiece.getCell().getRow() + 1;
                        rightCellCol = botPiece.getCell().getCol() + 1;
                        rightCell = board.getCell(rightCellRow, rightCellCol);

                        leftCellRow = botPiece.getCell().getRow() + 1;
                        leftCellCol = botPiece.getCell().getCol() - 1;
                        leftCell = board.getCell(leftCellRow, leftCellCol);

                        //piece, from, to
                        boolean canMoveLeft = false;
                        boolean canCaptureAndMoveLeft = false;
                        boolean canMoveRight = false;
                        boolean canCaptureAndMoveRight = false;
                        if (leftCell != null)
                        {
                            canMoveLeft = canPieceMove(botPiece, botPiece.getCell(), leftCell);
                        }

                        if (leftCellCapture != null)
                        {
                            canCaptureAndMoveLeft = canCapturePiece(botPiece, leftCellCapture);
                        }

                        if (canCaptureAndMoveLeft)
                        {
                            Cell origin = botPiece.getCell();
                            botPiece.animatePiece(botPiece, botPiece.getCell(), leftCellCapture, bv);

                            botPiece.objectMoveAnimator.removeAllListeners();

                            Cell finalLeftCellCapture = leftCellCapture;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(origin.getRow(), origin.getCol(), finalLeftCellCapture.getRow(), finalLeftCellCapture.getCol());

                                    if (capturedPiece != null)
                                    {
                                        capturedPiece.getCell().removePiece();
                                        capturedPiece = null;
                                    }

                                    if (crownPiece(botPiece) && !botPiece.isCrowned())
                                    {
                                        botPiece.makeCrowned();
                                    }

                                    bv.invalidate();

                                    ArrayList<Cell> possibleCells = isAnotherCaptureAvailable(botPiece);

                                    if (!possibleCells.isEmpty())
                                    {
                                        Random random = new Random();
                                        int randIndex = random.nextInt(possibleCells.size());
                                        Cell pCell = possibleCells.get(randIndex);

                                        if (canCapturePiece(botPiece, pCell))
                                        {
                                           continueBotCapture(botPiece, pCell, origin);

                                           return;
                                        }


                                    }

                                    currentMove.setFromSquareRowB(origin.getRow());
                                    currentMove.setFromSquareColB(origin.getCol());
                                    currentMove.setToSquareRowB(finalLeftCellCapture.getRow());
                                    currentMove.setToSquareColB(finalLeftCellCapture.getCol());
                                    currentMove.setTurnNumber(turnCounter);
                                    matchMoves.add(currentMove);

                                    int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                                    int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                                    int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                                    int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                                    tv_j_uF.setText(String.valueOf(uFrom));
                                    tv_j_uT.setText(String.valueOf(uTo));
                                    tv_j_bF.setText(String.valueOf(bFrom));
                                    tv_j_bT.setText(String.valueOf(bTo));

                                    currentMove = new Move();

                                    tv_j_userTurn.setVisibility(View.VISIBLE);

                                    turnCounter++;
                                    playerTurn = true;
                                    botTurn = false;

                                    boolean isStuck = canNoLongerMoveOrNoMorePieces();
                                    if (isStuck)
                                    {
                                        timerTask.cancel();
                                        timer.cancel();
                                        tv_j_time.setText(getTimerText());
                                        int rounded = Math.round(time);

                                        cons_j_gameOver.setVisibility(View.VISIBLE);
                                        gameOver = true;
                                        tv_j_numTurns.setText(String.valueOf(turnCounter));

                                        Log.d("Game", "GAME OVER");

                                        int diffId = 0;

                                        if (SessionData.easyModeSelected)
                                        {
                                            diffId = 1;
                                        }
                                        else
                                        {
                                            diffId = 2;
                                        }

                                        currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                        currentMatch.setTime(rounded);currentMatch.setDifficultyId(diffId);
                                        currentMatch.setResult(tv_j_result.getText().toString());
                                        dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                    }



                                }


                            });


                            return;
                        }
                        else
                        {
                            if (rightCellCapture != null)
                            {
                                canCaptureAndMoveRight = canCapturePiece(botPiece, rightCellCapture);
                            }

                            if (canCaptureAndMoveRight)
                            {
                                Cell origin = botPiece.getCell();
                                botPiece.animatePiece(botPiece, botPiece.getCell(), rightCellCapture, bv);

                                botPiece.objectMoveAnimator.removeAllListeners();

                                Cell finalRightCellCapture = rightCellCapture;
                                botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                                {
                                    @Override
                                    public void onAnimationEnd(Animator animation)
                                    {
                                        Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                        //Update board state AFTER animation completes
                                        board.movePiece(origin.getRow(), origin.getCol(), finalRightCellCapture.getRow(), finalRightCellCapture.getCol());

                                        if (capturedPiece != null)
                                        {
                                            capturedPiece.getCell().removePiece();
                                            capturedPiece = null;
                                        }

                                        if (crownPiece(botPiece) && !botPiece.isCrowned())
                                        {
                                            botPiece.makeCrowned();
                                        }

                                        bv.invalidate();

                                        ArrayList<Cell> possibleCells = isAnotherCaptureAvailable(botPiece);

                                        if (!possibleCells.isEmpty())
                                        {
                                            Random random = new Random();
                                            int randIndex = random.nextInt(possibleCells.size());
                                            Cell pCell = possibleCells.get(randIndex);

                                            if (canCapturePiece(botPiece, pCell))
                                            {
                                                continueBotCapture(botPiece, pCell, origin);

                                                return;
                                            }


                                        }

                                        currentMove.setFromSquareRowB(origin.getRow());
                                        currentMove.setFromSquareColB(origin.getCol());
                                        currentMove.setToSquareRowB(finalRightCellCapture.getRow());
                                        currentMove.setToSquareColB(finalRightCellCapture.getCol());
                                        currentMove.setTurnNumber(turnCounter);
                                        matchMoves.add(currentMove);

                                        int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                                        int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                                        int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                                        int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                                        tv_j_uF.setText(String.valueOf(uFrom));
                                        tv_j_uT.setText(String.valueOf(uTo));
                                        tv_j_bF.setText(String.valueOf(bFrom));
                                        tv_j_bT.setText(String.valueOf(bTo));

                                        currentMove = new Move();

                                        tv_j_userTurn.setVisibility(View.VISIBLE);

                                        turnCounter++;
                                        playerTurn = true;
                                        botTurn = false;

                                        boolean isStuck = canNoLongerMoveOrNoMorePieces();
                                        if (isStuck)
                                        {
                                            timerTask.cancel();
                                            timer.cancel();
                                            tv_j_time.setText(getTimerText());
                                            int rounded = Math.round(time);

                                            cons_j_gameOver.setVisibility(View.VISIBLE);
                                            gameOver = true;
                                            tv_j_numTurns.setText(String.valueOf(turnCounter));

                                            Log.d("Game", "GAME OVER");

                                            int diffId = 0;

                                            if (SessionData.easyModeSelected)
                                            {
                                                diffId = 1;
                                            }
                                            else
                                            {
                                                diffId = 2;
                                            }

                                            currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                            currentMatch.setTime(rounded);currentMatch.setDifficultyId(diffId);
                                            currentMatch.setResult(tv_j_result.getText().toString());
                                            dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                        }



                                    }


                                });

                                return;

                            }

                        }

                        if (!canMoveLeft)
                        {
                            if (rightCell != null)
                            {
                                canMoveRight = canPieceMove(botPiece, botPiece.getCell(), rightCell);
                            }

                            if (canMoveRight)
                            {
                                Cell origin = botPiece.getCell();
                                botPiece.animatePiece(botPiece, botPiece.getCell(), rightCell, bv);

                                currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                                currentMove.setFromSquareColB(botPiece.getCell().getCol());
                                currentMove.setToSquareRowB(rightCell.getRow());
                                currentMove.setToSquareColB(rightCell.getCol());
                                currentMove.setTurnNumber(turnCounter);
                                matchMoves.add(currentMove);

                                int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                                int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                                int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                                int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                                tv_j_uF.setText(String.valueOf(uFrom));
                                tv_j_uT.setText(String.valueOf(uTo));
                                tv_j_bF.setText(String.valueOf(bFrom));
                                tv_j_bT.setText(String.valueOf(bTo));

                                currentMove = new Move();

                                turnCounter++;

                                Cell finalRightCell = rightCell;
                                botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                                {
                                    @Override
                                    public void onAnimationEnd(Animator animation)
                                    {
                                        Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalRightCell.getRow() + "," + finalRightCell.getCol());
                                        //Update board state AFTER animation completes
                                        board.movePiece(origin.getRow(), origin.getCol(), finalRightCell.getRow(), finalRightCell.getCol());

                                        if (crownPiece(botPiece) && !botPiece.isCrowned())
                                        {
                                            botPiece.makeCrowned();
                                        }

                                        bv.invalidate();

                                        tv_j_userTurn.setVisibility(View.VISIBLE);


                                        playerTurn = true;
                                        botTurn = false;

                                        boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                        if (isStuck)
                                        {
                                            timerTask.cancel();
                                            timer.cancel();
                                            tv_j_time.setText(getTimerText());
                                            int rounded = Math.round(time);

                                            cons_j_gameOver.setVisibility(View.VISIBLE);
                                            gameOver = true;
                                            tv_j_numTurns.setText(String.valueOf(turnCounter));

                                            Log.d("Game", "GAME OVER");

                                            int diffId = 0;

                                            if (SessionData.easyModeSelected)
                                            {
                                                diffId = 1;
                                            }
                                            else
                                            {
                                                diffId = 2;
                                            }

                                            currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                            currentMatch.setTime(rounded);
                                            currentMatch.setDifficultyId(diffId);
                                            currentMatch.setResult(tv_j_result.getText().toString());
                                            dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                        }


                                    }
                                });

                                return;
                            }
                        }
                        else
                        {
                            Cell origin = botPiece.getCell();
                            botPiece.animatePiece(botPiece, botPiece.getCell(), leftCell, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(leftCell.getRow());
                            currentMove.setToSquareColB(leftCell.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                            int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                            int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                            int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                            tv_j_uF.setText(String.valueOf(uFrom));
                            tv_j_uT.setText(String.valueOf(uTo));
                            tv_j_bF.setText(String.valueOf(bFrom));
                            tv_j_bT.setText(String.valueOf(bTo));

                            currentMove = new Move();

                            turnCounter++;

                            Cell finalLeftCell = leftCell;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalLeftCell.getRow() + "," + finalLeftCell.getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(origin.getRow(), origin.getCol(), finalLeftCell.getRow(), finalLeftCell.getCol());

                                    if (crownPiece(botPiece) && !botPiece.isCrowned())
                                    {
                                        botPiece.makeCrowned();
                                    }

                                    bv.invalidate();

                                    tv_j_userTurn.setVisibility(View.VISIBLE);


                                    playerTurn = true;
                                    botTurn = false;

                                    boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                    if (isStuck)
                                    {
                                        timerTask.cancel();
                                        timer.cancel();
                                        tv_j_time.setText(getTimerText());
                                        int rounded = Math.round(time);

                                        cons_j_gameOver.setVisibility(View.VISIBLE);
                                        gameOver = true;
                                        tv_j_numTurns.setText(String.valueOf(turnCounter));

                                        Log.d("Game", "GAME OVER");

                                        int diffId = 0;

                                        if (SessionData.easyModeSelected)
                                        {
                                            diffId = 1;
                                        }
                                        else
                                        {
                                            diffId = 2;
                                        }

                                        currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                        currentMatch.setTime(rounded);
                                        currentMatch.setDifficultyId(diffId);
                                        currentMatch.setResult(tv_j_result.getText().toString());
                                        dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                    }


                                }
                            });

                            return;
                        }
                    }
                    else
                    {
                        //UPPER
                        //2 row and 2 col away
                        rightCellRowUpperCapture = botPiece.getCell().getRow() - 2;
                        rightCellColUpperCapture = botPiece.getCell().getCol() + 2;
                        rightCellUpperCapture = board.getCell(rightCellRowUpperCapture, rightCellColUpperCapture);
                        leftCellRowUpperCapture = botPiece.getCell().getRow() - 2;
                        leftCellColUpperCapture = botPiece.getCell().getCol() - 2;
                        leftCellUpperCapture = board.getCell(leftCellRowUpperCapture, leftCellColUpperCapture);

                        //1 row and 1 col away
                        rightCellRowUpper = botPiece.getCell().getRow() - 1;
                        rightCellColUpper = botPiece.getCell().getCol() + 1;
                        rightCellUpper = board.getCell(rightCellRowUpper, rightCellColUpper);
                        leftCellRowUpper = botPiece.getCell().getRow() - 1;
                        leftCellColUpper = botPiece.getCell().getCol() - 1;
                        leftCellUpper = board.getCell(leftCellRowUpper, leftCellColUpper);

                        //LOWER
                        //2 rows and 2 cols away
                        rightCellRowCapture = botPiece.getCell().getRow() + 2;
                        rightCellColCapture = botPiece.getCell().getCol() + 2;
                        rightCellCapture = board.getCell(rightCellRowCapture, rightCellColCapture);

                        leftCellRowCapture = botPiece.getCell().getRow() + 2;
                        leftCellColCapture = botPiece.getCell().getCol() - 2;
                        leftCellCapture = board.getCell(leftCellRowCapture, leftCellColCapture);


                        //1 row and 1 col away
                        rightCellRow = botPiece.getCell().getRow() + 1;
                        rightCellCol = botPiece.getCell().getCol() + 1;
                        rightCell = board.getCell(rightCellRow, rightCellCol);

                        leftCellRow = botPiece.getCell().getRow() + 1;
                        leftCellCol = botPiece.getCell().getCol() - 1;
                        leftCell = board.getCell(leftCellRow, leftCellCol);

                        //piece, from, to
                        boolean canMoveLeft = false;
                        boolean canMoveLeftUp = false;
                        boolean canCaptureAndMoveLeft = false;
                        boolean canCaptureAndMoveLeftUp = false;
                        boolean canMoveRight = false;
                        boolean canMoveRightUp = false;
                        boolean canCaptureAndMoveRight = false;
                        boolean canCaptureAndMoveRightUp = false;

                        if (leftCell != null)
                        {
                            canMoveLeft = canPieceMove(botPiece, botPiece.getCell(), leftCell);
                        }

                        if (leftCellUpper != null)
                        {
                            canMoveLeftUp = canPieceMove(botPiece, botPiece.getCell(), leftCellUpper);
                        }

                        if (leftCellCapture != null)
                        {
                            canCaptureAndMoveLeft = canCapturePiece(botPiece, leftCellCapture);
                        }

                        if (leftCellUpperCapture != null)
                        {
                            canCaptureAndMoveLeftUp = canCapturePiece(botPiece, leftCellUpperCapture);
                        }

                        if (canCaptureAndMoveLeft)
                        {
                            Cell origin = botPiece.getCell();
                            botPiece.animatePiece(botPiece, botPiece.getCell(), leftCellCapture, bv);

                            botPiece.objectMoveAnimator.removeAllListeners();

                            Cell finalLeftCellCapture = leftCellCapture;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(origin.getRow(), origin.getCol(), finalLeftCellCapture.getRow(), finalLeftCellCapture.getCol());

                                    if (capturedPiece != null)
                                    {
                                        capturedPiece.getCell().removePiece();
                                        capturedPiece = null;
                                    }

                                    if (crownPiece(botPiece) && !botPiece.isCrowned())
                                    {
                                        botPiece.makeCrowned();
                                    }

                                    bv.invalidate();

                                    ArrayList<Cell> possibleCells = isAnotherCaptureAvailable(botPiece);

                                    if (!possibleCells.isEmpty())
                                    {
                                        Random random = new Random();
                                        int randIndex = random.nextInt(possibleCells.size());
                                        Cell pCell = possibleCells.get(randIndex);

                                        if (canCapturePiece(botPiece, pCell))
                                        {
                                            continueBotCapture(botPiece, pCell, origin);

                                            return;
                                        }


                                    }

                                    currentMove.setFromSquareRowB(origin.getRow());
                                    currentMove.setFromSquareColB(origin.getCol());
                                    currentMove.setToSquareRowB(finalLeftCellCapture.getRow());
                                    currentMove.setToSquareColB(finalLeftCellCapture.getCol());
                                    currentMove.setTurnNumber(turnCounter);
                                    matchMoves.add(currentMove);

                                    int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                                    int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                                    int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                                    int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                                    tv_j_uF.setText(String.valueOf(uFrom));
                                    tv_j_uT.setText(String.valueOf(uTo));
                                    tv_j_bF.setText(String.valueOf(bFrom));
                                    tv_j_bT.setText(String.valueOf(bTo));

                                    currentMove = new Move();

                                    tv_j_userTurn.setVisibility(View.VISIBLE);

                                    turnCounter++;
                                    playerTurn = true;
                                    botTurn = false;

                                    boolean isStuck = canNoLongerMoveOrNoMorePieces();
                                    if (isStuck)
                                    {
                                        timerTask.cancel();
                                        timer.cancel();
                                        tv_j_time.setText(getTimerText());
                                        int rounded = Math.round(time);

                                        cons_j_gameOver.setVisibility(View.VISIBLE);
                                        gameOver = true;
                                        tv_j_numTurns.setText(String.valueOf(turnCounter));

                                        Log.d("Game", "GAME OVER");

                                        int diffId = 0;

                                        if (SessionData.easyModeSelected)
                                        {
                                            diffId = 1;
                                        }
                                        else
                                        {
                                            diffId = 2;
                                        }

                                        currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                        currentMatch.setTime(rounded);currentMatch.setDifficultyId(diffId);
                                        currentMatch.setResult(tv_j_result.getText().toString());
                                        dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                    }



                                }


                            });

                            return;

                        }
                        else if (canCaptureAndMoveLeftUp)
                        {
                            Cell origin = botPiece.getCell();
                            botPiece.animatePiece(botPiece, botPiece.getCell(), leftCellUpperCapture, bv);

                            botPiece.objectMoveAnimator.removeAllListeners();

                            Cell finalLeftCellUpperCapture = leftCellUpperCapture;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(origin.getRow(), origin.getCol(), finalLeftCellUpperCapture.getRow(), finalLeftCellUpperCapture.getCol());

                                    if (capturedPiece != null)
                                    {
                                        capturedPiece.getCell().removePiece();
                                        capturedPiece = null;
                                    }

                                    if (crownPiece(botPiece) && !botPiece.isCrowned())
                                    {
                                        botPiece.makeCrowned();
                                    }

                                    bv.invalidate();

                                    ArrayList<Cell> possibleCells = isAnotherCaptureAvailable(botPiece);

                                    if (!possibleCells.isEmpty())
                                    {
                                        Random random = new Random();
                                        int randIndex = random.nextInt(possibleCells.size());
                                        Cell pCell = possibleCells.get(randIndex);

                                        if (canCapturePiece(botPiece, pCell))
                                        {
                                            continueBotCapture(botPiece, pCell, origin);

                                            return;
                                        }


                                    }

                                    currentMove.setFromSquareRowB(origin.getRow());
                                    currentMove.setFromSquareColB(origin.getCol());
                                    currentMove.setToSquareRowB(finalLeftCellUpperCapture.getRow());
                                    currentMove.setToSquareColB(finalLeftCellUpperCapture.getCol());
                                    currentMove.setTurnNumber(turnCounter);
                                    matchMoves.add(currentMove);

                                    int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                                    int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                                    int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                                    int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                                    tv_j_uF.setText(String.valueOf(uFrom));
                                    tv_j_uT.setText(String.valueOf(uTo));
                                    tv_j_bF.setText(String.valueOf(bFrom));
                                    tv_j_bT.setText(String.valueOf(bTo));

                                    currentMove = new Move();

                                    tv_j_userTurn.setVisibility(View.VISIBLE);

                                    turnCounter++;
                                    playerTurn = true;
                                    botTurn = false;

                                    boolean isStuck = canNoLongerMoveOrNoMorePieces();
                                    if (isStuck)
                                    {
                                        timerTask.cancel();
                                        timer.cancel();
                                        tv_j_time.setText(getTimerText());
                                        int rounded = Math.round(time);

                                        cons_j_gameOver.setVisibility(View.VISIBLE);
                                        gameOver = true;
                                        tv_j_numTurns.setText(String.valueOf(turnCounter));

                                        Log.d("Game", "GAME OVER");

                                        int diffId = 0;

                                        if (SessionData.easyModeSelected)
                                        {
                                            diffId = 1;
                                        }
                                        else
                                        {
                                            diffId = 2;
                                        }

                                        currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                        currentMatch.setTime(rounded);currentMatch.setDifficultyId(diffId);
                                        currentMatch.setResult(tv_j_result.getText().toString());
                                        dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                    }



                                }


                            });

                            return;
                        }
                        else
                        {
                            if (rightCellCapture != null)
                            {
                                canCaptureAndMoveRight = canCapturePiece(botPiece, rightCellCapture);
                            }

                            if (rightCellUpperCapture != null)
                            {
                                canCaptureAndMoveRightUp = canCapturePiece(botPiece, rightCellUpperCapture);
                            }

                            if (canCaptureAndMoveRight)
                            {
                                Cell origin = botPiece.getCell();
                                botPiece.animatePiece(botPiece, botPiece.getCell(), rightCellCapture, bv);

                                botPiece.objectMoveAnimator.removeAllListeners();

                                Cell finalRightCellCapture = rightCellCapture;
                                botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                                {
                                    @Override
                                    public void onAnimationEnd(Animator animation)
                                    {
                                        Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                        //Update board state AFTER animation completes
                                        board.movePiece(origin.getRow(), origin.getCol(), finalRightCellCapture.getRow(), finalRightCellCapture.getCol());

                                        if (capturedPiece != null)
                                        {
                                            capturedPiece.getCell().removePiece();
                                            capturedPiece = null;
                                        }

                                        if (crownPiece(botPiece) && !botPiece.isCrowned())
                                        {
                                            botPiece.makeCrowned();
                                        }

                                        bv.invalidate();

                                        ArrayList<Cell> possibleCells = isAnotherCaptureAvailable(botPiece);

                                        if (!possibleCells.isEmpty())
                                        {
                                            Random random = new Random();
                                            int randIndex = random.nextInt(possibleCells.size());
                                            Cell pCell = possibleCells.get(randIndex);

                                            if (canCapturePiece(botPiece, pCell))
                                            {
                                                continueBotCapture(botPiece, pCell, origin);

                                                return;
                                            }


                                        }

                                        currentMove.setFromSquareRowB(origin.getRow());
                                        currentMove.setFromSquareColB(origin.getCol());
                                        currentMove.setToSquareRowB(finalRightCellCapture.getRow());
                                        currentMove.setToSquareColB(finalRightCellCapture.getCol());
                                        currentMove.setTurnNumber(turnCounter);
                                        matchMoves.add(currentMove);

                                        int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                                        int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                                        int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                                        int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                                        tv_j_uF.setText(String.valueOf(uFrom));
                                        tv_j_uT.setText(String.valueOf(uTo));
                                        tv_j_bF.setText(String.valueOf(bFrom));
                                        tv_j_bT.setText(String.valueOf(bTo));

                                        currentMove = new Move();

                                        tv_j_userTurn.setVisibility(View.VISIBLE);

                                        turnCounter++;
                                        playerTurn = true;
                                        botTurn = false;

                                        boolean isStuck = canNoLongerMoveOrNoMorePieces();
                                        if (isStuck)
                                        {
                                            timerTask.cancel();
                                            timer.cancel();
                                            tv_j_time.setText(getTimerText());
                                            int rounded = Math.round(time);

                                            cons_j_gameOver.setVisibility(View.VISIBLE);
                                            gameOver = true;
                                            tv_j_numTurns.setText(String.valueOf(turnCounter));

                                            Log.d("Game", "GAME OVER");

                                            int diffId = 0;

                                            if (SessionData.easyModeSelected)
                                            {
                                                diffId = 1;
                                            }
                                            else
                                            {
                                                diffId = 2;
                                            }

                                            currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                            currentMatch.setTime(rounded);currentMatch.setDifficultyId(diffId);
                                            currentMatch.setResult(tv_j_result.getText().toString());
                                            dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                        }



                                    }


                                });

                                return;
                            }
                            else if (canCaptureAndMoveRightUp)
                            {
                                Cell origin = botPiece.getCell();
                                botPiece.animatePiece(botPiece, botPiece.getCell(), rightCellUpperCapture, bv);

                                botPiece.objectMoveAnimator.removeAllListeners();

                                Cell finalRightCellUpperCapture = rightCellUpperCapture;
                                botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                                {
                                    @Override
                                    public void onAnimationEnd(Animator animation)
                                    {
                                        Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                        //Update board state AFTER animation completes
                                        board.movePiece(origin.getRow(), origin.getCol(), finalRightCellUpperCapture.getRow(), finalRightCellUpperCapture.getCol());

                                        if (capturedPiece != null)
                                        {
                                            capturedPiece.getCell().removePiece();
                                            capturedPiece = null;
                                        }

                                        if (crownPiece(botPiece) && !botPiece.isCrowned())
                                        {
                                            botPiece.makeCrowned();
                                        }

                                        bv.invalidate();

                                        ArrayList<Cell> possibleCells = isAnotherCaptureAvailable(botPiece);

                                        if (!possibleCells.isEmpty())
                                        {
                                            Random random = new Random();
                                            int randIndex = random.nextInt(possibleCells.size());
                                            Cell pCell = possibleCells.get(randIndex);

                                            if (canCapturePiece(botPiece, pCell))
                                            {
                                                continueBotCapture(botPiece, pCell, origin);

                                                return;
                                            }


                                        }

                                        currentMove.setFromSquareRowB(origin.getRow());
                                        currentMove.setFromSquareColB(origin.getCol());
                                        currentMove.setToSquareRowB(finalRightCellUpperCapture.getRow());
                                        currentMove.setToSquareColB(finalRightCellUpperCapture.getCol());
                                        currentMove.setTurnNumber(turnCounter);
                                        matchMoves.add(currentMove);

                                        int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                                        int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                                        int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                                        int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                                        tv_j_uF.setText(String.valueOf(uFrom));
                                        tv_j_uT.setText(String.valueOf(uTo));
                                        tv_j_bF.setText(String.valueOf(bFrom));
                                        tv_j_bT.setText(String.valueOf(bTo));

                                        currentMove = new Move();

                                        tv_j_userTurn.setVisibility(View.VISIBLE);

                                        turnCounter++;
                                        playerTurn = true;
                                        botTurn = false;

                                        boolean isStuck = canNoLongerMoveOrNoMorePieces();
                                        if (isStuck)
                                        {
                                            timerTask.cancel();
                                            timer.cancel();
                                            tv_j_time.setText(getTimerText());
                                            int rounded = Math.round(time);

                                            cons_j_gameOver.setVisibility(View.VISIBLE);
                                            gameOver = true;
                                            tv_j_numTurns.setText(String.valueOf(turnCounter));

                                            Log.d("Game", "GAME OVER");

                                            int diffId = 0;

                                            if (SessionData.easyModeSelected)
                                            {
                                                diffId = 1;
                                            }
                                            else
                                            {
                                                diffId = 2;
                                            }

                                            currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                            currentMatch.setTime(rounded);currentMatch.setDifficultyId(diffId);
                                            currentMatch.setResult(tv_j_result.getText().toString());
                                            dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                        }



                                    }


                                });

                                return;
                            }

                        }

                        if (!canMoveLeft)
                        {
                            if (rightCell != null)
                            {
                                canMoveRight = canPieceMove(botPiece, botPiece.getCell(), rightCell);
                            }

                            if (rightCellUpper != null)
                            {
                                canMoveRightUp = canPieceMove(botPiece, botPiece.getCell(), rightCellUpper);
                            }

                            if (canMoveRight)
                            {
                                Cell origin = botPiece.getCell();
                                botPiece.animatePiece(botPiece, botPiece.getCell(), rightCell, bv);

                                currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                                currentMove.setFromSquareColB(botPiece.getCell().getCol());
                                currentMove.setToSquareRowB(rightCell.getRow());
                                currentMove.setToSquareColB(rightCell.getCol());
                                currentMove.setTurnNumber(turnCounter);
                                matchMoves.add(currentMove);

                                int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                                int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                                int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                                int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                                tv_j_uF.setText(String.valueOf(uFrom));
                                tv_j_uT.setText(String.valueOf(uTo));
                                tv_j_bF.setText(String.valueOf(bFrom));
                                tv_j_bT.setText(String.valueOf(bTo));

                                currentMove = new Move();

                                Cell finalRightCell = rightCell;
                                botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                                {
                                    @Override
                                    public void onAnimationEnd(Animator animation)
                                    {
                                        Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalRightCell.getRow() + "," + finalRightCell.getCol());
                                        //Update board state AFTER animation completes
                                        board.movePiece(origin.getRow(), origin.getCol(), finalRightCell.getRow(), finalRightCell.getCol());


                                        bv.invalidate();

                                        tv_j_userTurn.setVisibility(View.VISIBLE);

                                        turnCounter++;
                                        playerTurn = true;
                                        botTurn = false;

                                        boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                        if (isStuck)
                                        {
                                            timerTask.cancel();
                                            timer.cancel();
                                            tv_j_time.setText(getTimerText());
                                            int rounded = Math.round(time);

                                            cons_j_gameOver.setVisibility(View.VISIBLE);
                                            gameOver = true;
                                            tv_j_numTurns.setText(String.valueOf(turnCounter));

                                            Log.d("Game", "GAME OVER");

                                            int diffId = 0;

                                            if (SessionData.easyModeSelected)
                                            {
                                                diffId = 1;
                                            }
                                            else
                                            {
                                                diffId = 2;
                                            }

                                            currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                            currentMatch.setTime(rounded);
                                            currentMatch.setDifficultyId(diffId);
                                            currentMatch.setResult(tv_j_result.getText().toString());
                                            dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                        }


                                    }
                                });

                                return;
                            }
                            else if (canMoveRightUp)
                            {
                                Cell origin = botPiece.getCell();
                                botPiece.animatePiece(botPiece, botPiece.getCell(), rightCellUpper, bv);

                                currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                                currentMove.setFromSquareColB(botPiece.getCell().getCol());
                                currentMove.setToSquareRowB(rightCellUpper.getRow());
                                currentMove.setToSquareColB(rightCellUpper.getCol());
                                currentMove.setTurnNumber(turnCounter);
                                matchMoves.add(currentMove);

                                int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                                int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                                int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                                int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                                tv_j_uF.setText(String.valueOf(uFrom));
                                tv_j_uT.setText(String.valueOf(uTo));
                                tv_j_bF.setText(String.valueOf(bFrom));
                                tv_j_bT.setText(String.valueOf(bTo));

                                currentMove = new Move();

                                Cell finalRightCellUpper = rightCellUpper;
                                botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                                {
                                    @Override
                                    public void onAnimationEnd(Animator animation)
                                    {
                                        Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalRightCellUpper.getRow() + "," + finalRightCellUpper.getCol());
                                        //Update board state AFTER animation completes
                                        board.movePiece(origin.getRow(), origin.getCol(), finalRightCellUpper.getRow(), finalRightCellUpper.getCol());


                                        bv.invalidate();

                                        tv_j_userTurn.setVisibility(View.VISIBLE);

                                        turnCounter++;
                                        playerTurn = true;
                                        botTurn = false;

                                        boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                        if (isStuck)
                                        {
                                            timerTask.cancel();
                                            timer.cancel();
                                            tv_j_time.setText(getTimerText());
                                            int rounded = Math.round(time);

                                            cons_j_gameOver.setVisibility(View.VISIBLE);
                                            gameOver = true;
                                            tv_j_numTurns.setText(String.valueOf(turnCounter));

                                            Log.d("Game", "GAME OVER");

                                            int diffId = 0;

                                            if (SessionData.easyModeSelected)
                                            {
                                                diffId = 1;
                                            }
                                            else
                                            {
                                                diffId = 2;
                                            }

                                            currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                            currentMatch.setTime(rounded);
                                            currentMatch.setDifficultyId(diffId);
                                            currentMatch.setResult(tv_j_result.getText().toString());
                                            dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                        }

                                    }
                                });

                                return;
                            }
                        }
                        else if (canMoveLeftUp)
                        {
                            Cell origin = botPiece.getCell();
                            botPiece.animatePiece(botPiece, botPiece.getCell(), leftCellUpper, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(leftCellUpper.getRow());
                            currentMove.setToSquareColB(leftCellUpper.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                            int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                            int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                            int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                            tv_j_uF.setText(String.valueOf(uFrom));
                            tv_j_uT.setText(String.valueOf(uTo));
                            tv_j_bF.setText(String.valueOf(bFrom));
                            tv_j_bT.setText(String.valueOf(bTo));


                            currentMove = new Move();

                            Cell finalLeftCellUpper = leftCellUpper;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalLeftCellUpper.getRow() + "," + finalLeftCellUpper.getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(origin.getRow(), origin.getCol(), finalLeftCellUpper.getRow(), finalLeftCellUpper.getCol());


                                    bv.invalidate();

                                    tv_j_userTurn.setVisibility(View.VISIBLE);

                                    turnCounter++;
                                    playerTurn = true;
                                    botTurn = false;

                                    boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                    if (isStuck)
                                    {
                                        timerTask.cancel();
                                        timer.cancel();
                                        tv_j_time.setText(getTimerText());
                                        int rounded = Math.round(time);

                                        cons_j_gameOver.setVisibility(View.VISIBLE);
                                        gameOver = true;
                                        tv_j_numTurns.setText(String.valueOf(turnCounter));

                                        Log.d("Game", "GAME OVER");

                                        int diffId = 0;

                                        if (SessionData.easyModeSelected)
                                        {
                                            diffId = 1;
                                        }
                                        else
                                        {
                                            diffId = 2;
                                        }

                                        currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                        currentMatch.setTime(rounded);
                                        currentMatch.setDifficultyId(diffId);
                                        currentMatch.setResult(tv_j_result.getText().toString());
                                        dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                    }

                                }
                            });

                            return;
                        }
                        else
                        {
                            Cell origin = botPiece.getCell();
                            botPiece.animatePiece(botPiece, botPiece.getCell(), leftCell, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(leftCell.getRow());
                            currentMove.setToSquareColB(leftCell.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                            int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                            int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                            int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                            tv_j_uF.setText(String.valueOf(uFrom));
                            tv_j_uT.setText(String.valueOf(uTo));
                            tv_j_bF.setText(String.valueOf(bFrom));
                            tv_j_bT.setText(String.valueOf(bTo));

                            currentMove = new Move();

                            Cell finalLeftCell = leftCell;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalLeftCell.getRow() + "," + finalLeftCell.getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(origin.getRow(), origin.getCol(), finalLeftCell.getRow(), finalLeftCell.getCol());


                                    bv.invalidate();

                                    tv_j_userTurn.setVisibility(View.VISIBLE);

                                    turnCounter++;
                                    playerTurn = true;
                                    botTurn = false;

                                    boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                    if (isStuck)
                                    {
                                        timerTask.cancel();
                                        timer.cancel();
                                        tv_j_time.setText(getTimerText());
                                        int rounded = Math.round(time);

                                        cons_j_gameOver.setVisibility(View.VISIBLE);
                                        gameOver = true;
                                        tv_j_numTurns.setText(String.valueOf(turnCounter));

                                        Log.d("Game", "GAME OVER");

                                        int diffId = 0;

                                        if (SessionData.easyModeSelected)
                                        {
                                            diffId = 1;
                                        }
                                        else
                                        {
                                            diffId = 2;
                                        }

                                        currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                        currentMatch.setTime(rounded);
                                        currentMatch.setDifficultyId(diffId);
                                        currentMatch.setResult(tv_j_result.getText().toString());
                                        dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                    }


                                }
                            });

                            return;
                        }

                    }
                }
            }


        }

    }

    private void easyDifficultyBotTurn()
    {
        botPieces.clear();

        if (playerTurn || !botTurn)
        {
            return;
        }

        for (int row = 0; row < 8; row++)
        {
            for (int col = 0; col < 8; col++)
            {
                Cell cell = board.getCell(row, col);
                if (cell.containsPiece())
                {
                    Piece piece = cell.getPiece();
                    if (piece.getColor().equals("Dark"))
                    {
                        botPieces.add(piece);
                    }
                }
            }
        }

        if (botPieces != null && botTurn)
        {
            int rightCellRow = -1;
            int rightCellCol = -1;
            Cell rightCell;

            int leftCellRow = -1;
            int leftCellCol = -1;
            Cell leftCell;

            int rightCellRowCapture = -1;
            int rightCellColCapture = -1;
            Cell rightCellCapture;

            int leftCellRowCapture = -1;
            int leftCellColCapture = -1;
            Cell leftCellCapture;

            //2 row and 2 col away
            int rightCellRowUpperCapture = -1;
            int rightCellColUpperCapture = -1;
            Cell rightCellUpperCapture;
            int leftCellRowUpperCapture = -1;
            int leftCellColUpperCapture = -1;
            Cell leftCellUpperCapture;

            //1 row and 1 col away
            int rightCellRowUpper = -1;
            int rightCellColUpper = -1;
            Cell rightCellUpper;
            int leftCellRowUpper = -1;
            int leftCellColUpper = -1;
            Cell leftCellUpper;

            //boolean canCapture = false;

            Collections.shuffle(botPieces);

            for(Piece botPiece : botPieces)
            {
                if (!botPiece.isCrowned())
                {

                    //LOWER
                    //2 rows and 2 cols away
                    rightCellRowCapture = botPiece.getCell().getRow() + 2;
                    rightCellColCapture = botPiece.getCell().getCol() + 2;
                    rightCellCapture = board.getCell(rightCellRowCapture, rightCellColCapture);

                    leftCellRowCapture = botPiece.getCell().getRow() + 2;
                    leftCellColCapture = botPiece.getCell().getCol() - 2;
                    leftCellCapture = board.getCell(leftCellRowCapture, leftCellColCapture);


                    //1 row and 1 col away
                    rightCellRow = botPiece.getCell().getRow() + 1;
                    rightCellCol = botPiece.getCell().getCol() + 1;
                    rightCell = board.getCell(rightCellRow, rightCellCol);

                    leftCellRow = botPiece.getCell().getRow() + 1;
                    leftCellCol = botPiece.getCell().getCol() - 1;
                    leftCell = board.getCell(leftCellRow, leftCellCol);

                    //piece, from, to
                    boolean canMoveLeft = false;
                    boolean canCaptureAndMoveLeft = false;
                    boolean canMoveRight = false;
                    boolean canCaptureAndMoveRight = false;
                    if (leftCell != null)
                    {
                        canMoveLeft = canPieceMove(botPiece, botPiece.getCell(), leftCell);
                    }

                    if (leftCellCapture != null)
                    {
                        canCaptureAndMoveLeft = canCapturePiece(botPiece, leftCellCapture);
                    }

                    if (canCaptureAndMoveLeft)
                    {
                        Cell origin = botPiece.getCell();
                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCellCapture, bv);

                        currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                        currentMove.setFromSquareColB(botPiece.getCell().getCol());
                        currentMove.setToSquareRowB(leftCellCapture.getRow());
                        currentMove.setToSquareColB(leftCellCapture.getCol());
                        currentMove.setTurnNumber(turnCounter);
                        matchMoves.add(currentMove);

                        int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                        int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                        int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                        int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                        tv_j_uF.setText(String.valueOf(uFrom));
                        tv_j_uT.setText(String.valueOf(uTo));
                        tv_j_bF.setText(String.valueOf(bFrom));
                        tv_j_bT.setText(String.valueOf(bTo));

                        currentMove = new Move();

                        Cell finalLeftCellCapture = leftCellCapture;
                        botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd(Animator animation)
                            {
                                Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                //Update board state AFTER animation completes
                                board.movePiece(origin.getRow(), origin.getCol(), finalLeftCellCapture.getRow(), finalLeftCellCapture.getCol());

                                if (capturedPiece != null)
                                {
                                    capturedPiece.getCell().removePiece();
                                    capturedPiece = null;
                                }

                                if (crownPiece(botPiece) && !botPiece.isCrowned())
                                {
                                    botPiece.makeCrowned();
                                }

                                bv.invalidate();


                                tv_j_userTurn.setVisibility(View.VISIBLE);

                                turnCounter++;
                                playerTurn = true;
                                botTurn = false;

                                boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                if (isStuck)
                                {
                                    timerTask.cancel();
                                    timer.cancel();
                                    tv_j_time.setText(getTimerText());
                                    int rounded = Math.round(time);

                                    cons_j_gameOver.setVisibility(View.VISIBLE);
                                    gameOver = true;
                                    tv_j_numTurns.setText(String.valueOf(turnCounter));

                                    Log.d("Game", "GAME OVER");

                                    int diffId = 0;

                                    if (SessionData.easyModeSelected)
                                    {
                                        diffId = 1;
                                    }
                                    else
                                    {
                                        diffId = 2;
                                    }

                                    currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                    currentMatch.setTime(rounded);
                                    currentMatch.setDifficultyId(diffId);
                                    currentMatch.setResult(tv_j_result.getText().toString());
                                    dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                }


                            }
                        });

                        return;

                    }
                    else
                    {
                        if (rightCellCapture != null)
                        {
                            canCaptureAndMoveRight = canCapturePiece(botPiece, rightCellCapture);
                        }

                        if (canCaptureAndMoveRight)
                        {
                            Cell origin = botPiece.getCell();
                            botPiece.animatePiece(botPiece, botPiece.getCell(), rightCellCapture, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(rightCellCapture.getRow());
                            currentMove.setToSquareColB(rightCellCapture.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                            int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                            int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                            int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                            tv_j_uF.setText(String.valueOf(uFrom));
                            tv_j_uT.setText(String.valueOf(uTo));
                            tv_j_bF.setText(String.valueOf(bFrom));
                            tv_j_bT.setText(String.valueOf(bTo));

                            currentMove = new Move();

                            Cell finalRightCellCapture = rightCellCapture;

                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(origin.getRow(), origin.getCol(), finalRightCellCapture.getRow(), finalRightCellCapture.getCol());

                                    if (capturedPiece != null)
                                    {
                                        capturedPiece.getCell().removePiece();
                                        capturedPiece = null;
                                    }

                                    if (crownPiece(botPiece) && !botPiece.isCrowned())
                                    {
                                        botPiece.makeCrowned();
                                    }

                                    bv.invalidate();

                                    tv_j_userTurn.setVisibility(View.VISIBLE);

                                    turnCounter++;
                                    playerTurn = true;
                                    botTurn = false;

                                    boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                    if (isStuck)
                                    {
                                        timerTask.cancel();
                                        timer.cancel();
                                        tv_j_time.setText(getTimerText());
                                        int rounded = Math.round(time);

                                        cons_j_gameOver.setVisibility(View.VISIBLE);
                                        gameOver = true;
                                        tv_j_numTurns.setText(String.valueOf(turnCounter));

                                        Log.d("Game", "GAME OVER");

                                        int diffId = 0;

                                        if (SessionData.easyModeSelected)
                                        {
                                            diffId = 1;
                                        }
                                        else
                                        {
                                            diffId = 2;
                                        }

                                        currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                        currentMatch.setTime(rounded);
                                        currentMatch.setDifficultyId(diffId);
                                        currentMatch.setResult(tv_j_result.getText().toString());
                                        dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                    }


                                }
                            });

                            return;
                        }

                    }

                    if (!canMoveLeft)
                    {
                        if (rightCell != null)
                        {
                            canMoveRight = canPieceMove(botPiece, botPiece.getCell(), rightCell);
                        }

                        if (canMoveRight)
                        {
                            Cell origin = botPiece.getCell();
                            botPiece.animatePiece(botPiece, botPiece.getCell(), rightCell, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(rightCell.getRow());
                            currentMove.setToSquareColB(rightCell.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                            int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                            int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                            int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                            tv_j_uF.setText(String.valueOf(uFrom));
                            tv_j_uT.setText(String.valueOf(uTo));
                            tv_j_bF.setText(String.valueOf(bFrom));
                            tv_j_bT.setText(String.valueOf(bTo));

                            currentMove = new Move();

                            Cell finalRightCell = rightCell;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalRightCell.getRow() + "," + finalRightCell.getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(origin.getRow(), origin.getCol(), finalRightCell.getRow(), finalRightCell.getCol());

                                    if (crownPiece(botPiece) && !botPiece.isCrowned())
                                    {
                                        botPiece.makeCrowned();
                                    }

                                    bv.invalidate();

                                    tv_j_userTurn.setVisibility(View.VISIBLE);

                                    turnCounter++;
                                    playerTurn = true;
                                    botTurn = false;

                                    boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                    if (isStuck)
                                    {
                                        timerTask.cancel();
                                        timer.cancel();
                                        tv_j_time.setText(getTimerText());
                                        int rounded = Math.round(time);

                                        cons_j_gameOver.setVisibility(View.VISIBLE);
                                        gameOver = true;
                                        tv_j_numTurns.setText(String.valueOf(turnCounter));

                                        Log.d("Game", "GAME OVER");

                                        int diffId = 0;

                                        if (SessionData.easyModeSelected)
                                        {
                                            diffId = 1;
                                        }
                                        else
                                        {
                                            diffId = 2;
                                        }

                                        currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                        currentMatch.setTime(rounded);
                                        currentMatch.setDifficultyId(diffId);
                                        currentMatch.setResult(tv_j_result.getText().toString());
                                        dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                    }


                                }
                            });

                            return;
                        }
                    }
                    else
                    {
                        Cell origin = botPiece.getCell();
                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCell, bv);

                        currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                        currentMove.setFromSquareColB(botPiece.getCell().getCol());
                        currentMove.setToSquareRowB(leftCell.getRow());
                        currentMove.setToSquareColB(leftCell.getCol());
                        currentMove.setTurnNumber(turnCounter);
                        matchMoves.add(currentMove);

                        int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                        int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                        int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                        int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                        tv_j_uF.setText(String.valueOf(uFrom));
                        tv_j_uT.setText(String.valueOf(uTo));
                        tv_j_bF.setText(String.valueOf(bFrom));
                        tv_j_bT.setText(String.valueOf(bTo));

                        currentMove = new Move();

                        Cell finalLeftCell = leftCell;
                        botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd(Animator animation)
                            {
                                Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalLeftCell.getRow() + "," + finalLeftCell.getCol());
                                //Update board state AFTER animation completes
                                board.movePiece(origin.getRow(), origin.getCol(), finalLeftCell.getRow(), finalLeftCell.getCol());

                                if (crownPiece(botPiece) && !botPiece.isCrowned())
                                {
                                    botPiece.makeCrowned();
                                }

                                bv.invalidate();

                                tv_j_userTurn.setVisibility(View.VISIBLE);

                                turnCounter++;
                                playerTurn = true;
                                botTurn = false;

                                boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                if (isStuck)
                                {
                                    timerTask.cancel();
                                    timer.cancel();
                                    tv_j_time.setText(getTimerText());
                                    int rounded = Math.round(time);

                                    cons_j_gameOver.setVisibility(View.VISIBLE);
                                    gameOver = true;
                                    tv_j_numTurns.setText(String.valueOf(turnCounter));

                                    Log.d("Game", "GAME OVER");

                                    int diffId = 0;

                                    if (SessionData.easyModeSelected)
                                    {
                                        diffId = 1;
                                    }
                                    else
                                    {
                                        diffId = 2;
                                    }

                                    currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                    currentMatch.setTime(rounded);
                                    currentMatch.setDifficultyId(diffId);
                                    currentMatch.setResult(tv_j_result.getText().toString());
                                    dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                }


                            }
                        });

                        return;
                    }
                }
                else
                {
                    //UPPER
                    //2 row and 2 col away
                    rightCellRowUpperCapture = botPiece.getCell().getRow() - 2;
                    rightCellColUpperCapture = botPiece.getCell().getCol() + 2;
                    rightCellUpperCapture = board.getCell(rightCellRowUpperCapture, rightCellColUpperCapture);
                    leftCellRowUpperCapture = botPiece.getCell().getRow() - 2;
                    leftCellColUpperCapture = botPiece.getCell().getCol() - 2;
                    leftCellUpperCapture = board.getCell(leftCellRowUpperCapture, leftCellColUpperCapture);

                    //1 row and 1 col away
                    rightCellRowUpper = botPiece.getCell().getRow() - 1;
                    rightCellColUpper = botPiece.getCell().getCol() + 1;
                    rightCellUpper = board.getCell(rightCellRowUpper, rightCellColUpper);
                    leftCellRowUpper = botPiece.getCell().getRow() - 1;
                    leftCellColUpper = botPiece.getCell().getCol() - 1;
                    leftCellUpper = board.getCell(leftCellRowUpper, leftCellColUpper);

                    //LOWER
                    //2 rows and 2 cols away
                    rightCellRowCapture = botPiece.getCell().getRow() + 2;
                    rightCellColCapture = botPiece.getCell().getCol() + 2;
                    rightCellCapture = board.getCell(rightCellRowCapture, rightCellColCapture);

                    leftCellRowCapture = botPiece.getCell().getRow() + 2;
                    leftCellColCapture = botPiece.getCell().getCol() - 2;
                    leftCellCapture = board.getCell(leftCellRowCapture, leftCellColCapture);


                    //1 row and 1 col away
                    rightCellRow = botPiece.getCell().getRow() + 1;
                    rightCellCol = botPiece.getCell().getCol() + 1;
                    rightCell = board.getCell(rightCellRow, rightCellCol);

                    leftCellRow = botPiece.getCell().getRow() + 1;
                    leftCellCol = botPiece.getCell().getCol() - 1;
                    leftCell = board.getCell(leftCellRow, leftCellCol);

                    //piece, from, to
                    boolean canMoveLeft = false;
                    boolean canMoveLeftUp = false;
                    boolean canCaptureAndMoveLeft = false;
                    boolean canCaptureAndMoveLeftUp = false;
                    boolean canMoveRight = false;
                    boolean canMoveRightUp = false;
                    boolean canCaptureAndMoveRight = false;
                    boolean canCaptureAndMoveRightUp = false;

                    if (leftCell != null)
                    {
                        canMoveLeft = canPieceMove(botPiece, botPiece.getCell(), leftCell);
                    }

                    if (leftCellUpper != null)
                    {
                        canMoveLeftUp = canPieceMove(botPiece, botPiece.getCell(), leftCellUpper);
                    }

                    if (leftCellCapture != null)
                    {
                        canCaptureAndMoveLeft = canCapturePiece(botPiece, leftCellCapture);
                    }

                    if (leftCellUpperCapture != null)
                    {
                        canCaptureAndMoveLeftUp = canCapturePiece(botPiece, leftCellUpperCapture);
                    }

                    if (canCaptureAndMoveLeft)
                    {
                        Cell origin = botPiece.getCell();
                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCellCapture, bv);

                        currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                        currentMove.setFromSquareColB(botPiece.getCell().getCol());
                        currentMove.setToSquareRowB(leftCellCapture.getRow());
                        currentMove.setToSquareColB(leftCellCapture.getCol());
                        currentMove.setTurnNumber(turnCounter);
                        matchMoves.add(currentMove);

                        int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                        int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                        int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                        int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                        tv_j_uF.setText(String.valueOf(uFrom));
                        tv_j_uT.setText(String.valueOf(uTo));
                        tv_j_bF.setText(String.valueOf(bFrom));
                        tv_j_bT.setText(String.valueOf(bTo));

                        currentMove = new Move();

                        Cell finalLeftCellCapture = leftCellCapture;
                        botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd(Animator animation)
                            {
                                Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                //Update board state AFTER animation completes
                                board.movePiece(origin.getRow(), origin.getCol(), finalLeftCellCapture.getRow(), finalLeftCellCapture.getCol());

                                if (capturedPiece != null)
                                {
                                    capturedPiece.getCell().removePiece();
                                    capturedPiece = null;
                                }


                                bv.invalidate();

                                tv_j_userTurn.setVisibility(View.VISIBLE);

                                turnCounter++;
                                playerTurn = true;
                                botTurn = false;

                                boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                if (isStuck)
                                {
                                    timerTask.cancel();
                                    timer.cancel();
                                    tv_j_time.setText(getTimerText());
                                    int rounded = Math.round(time);

                                    cons_j_gameOver.setVisibility(View.VISIBLE);
                                    gameOver = true;
                                    tv_j_numTurns.setText(String.valueOf(turnCounter));

                                    Log.d("Game", "GAME OVER");

                                    int diffId = 0;

                                    if (SessionData.easyModeSelected)
                                    {
                                        diffId = 1;
                                    }
                                    else
                                    {
                                        diffId = 2;
                                    }

                                    currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                    currentMatch.setTime(rounded);
                                    currentMatch.setDifficultyId(diffId);
                                    currentMatch.setResult(tv_j_result.getText().toString());
                                    dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                }


                            }
                        });

                        return;

                    }
                    else if (canCaptureAndMoveLeftUp)
                    {
                        Cell origin = botPiece.getCell();
                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCellUpperCapture, bv);

                        currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                        currentMove.setFromSquareColB(botPiece.getCell().getCol());
                        currentMove.setToSquareRowB(leftCellUpperCapture.getRow());
                        currentMove.setToSquareColB(leftCellUpperCapture.getCol());
                        currentMove.setTurnNumber(turnCounter);
                        matchMoves.add(currentMove);

                        int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                        int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                        int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                        int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                        tv_j_uF.setText(String.valueOf(uFrom));
                        tv_j_uT.setText(String.valueOf(uTo));
                        tv_j_bF.setText(String.valueOf(bFrom));
                        tv_j_bT.setText(String.valueOf(bTo));

                        currentMove = new Move();

                        Cell finalLeftCellUpperCapture = leftCellUpperCapture;
                        botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd(Animator animation)
                            {
                                Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                //Update board state AFTER animation completes
                                board.movePiece(origin.getRow(), origin.getCol(), finalLeftCellUpperCapture.getRow(), finalLeftCellUpperCapture.getCol());

                                if (capturedPiece != null)
                                {
                                    capturedPiece.getCell().removePiece();
                                    capturedPiece = null;
                                }



                                bv.invalidate();

                                tv_j_userTurn.setVisibility(View.VISIBLE);

                                turnCounter++;
                                playerTurn = true;
                                botTurn = false;

                                boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                if (isStuck)
                                {
                                    timerTask.cancel();
                                    timer.cancel();
                                    tv_j_time.setText(getTimerText());
                                    int rounded = Math.round(time);

                                    cons_j_gameOver.setVisibility(View.VISIBLE);
                                    gameOver = true;
                                    tv_j_numTurns.setText(String.valueOf(turnCounter));

                                    Log.d("Game", "GAME OVER");

                                    int diffId = 0;

                                    if (SessionData.easyModeSelected)
                                    {
                                        diffId = 1;
                                    }
                                    else
                                    {
                                        diffId = 2;
                                    }

                                    currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                    currentMatch.setTime(rounded);
                                    currentMatch.setDifficultyId(diffId);
                                    currentMatch.setResult(tv_j_result.getText().toString());
                                    dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                }


                            }
                        });

                        return;
                    }
                    else
                    {
                        if (rightCellCapture != null)
                        {
                            canCaptureAndMoveRight = canCapturePiece(botPiece, rightCellCapture);
                        }

                        if (rightCellUpperCapture != null)
                        {
                            canCaptureAndMoveRightUp = canCapturePiece(botPiece, rightCellUpperCapture);
                        }

                        if (canCaptureAndMoveRight)
                        {
                            Cell origin = botPiece.getCell();
                            botPiece.animatePiece(botPiece, botPiece.getCell(), rightCellCapture, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(rightCellCapture.getRow());
                            currentMove.setToSquareColB(rightCellCapture.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                            int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                            int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                            int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                            tv_j_uF.setText(String.valueOf(uFrom));
                            tv_j_uT.setText(String.valueOf(uTo));
                            tv_j_bF.setText(String.valueOf(bFrom));
                            tv_j_bT.setText(String.valueOf(bTo));

                            currentMove = new Move();

                            Cell finalRightCellCapture = rightCellCapture;

                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(origin.getRow(), origin.getCol(), finalRightCellCapture.getRow(), finalRightCellCapture.getCol());

                                    if (capturedPiece != null)
                                    {
                                        capturedPiece.getCell().removePiece();
                                        capturedPiece = null;
                                    }

                                    bv.invalidate();

                                    tv_j_userTurn.setVisibility(View.VISIBLE);

                                    turnCounter++;
                                    playerTurn = true;
                                    botTurn = false;

                                    boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                    if (isStuck)
                                    {
                                        timerTask.cancel();
                                        timer.cancel();
                                        tv_j_time.setText(getTimerText());
                                        int rounded = Math.round(time);

                                        cons_j_gameOver.setVisibility(View.VISIBLE);
                                        gameOver = true;
                                        tv_j_numTurns.setText(String.valueOf(turnCounter));

                                        Log.d("Game", "GAME OVER");

                                        int diffId = 0;

                                        if (SessionData.easyModeSelected)
                                        {
                                            diffId = 1;
                                        }
                                        else
                                        {
                                            diffId = 2;
                                        }

                                        currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                        currentMatch.setTime(rounded);
                                        currentMatch.setDifficultyId(diffId);
                                        currentMatch.setResult(tv_j_result.getText().toString());
                                        dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                    }


                                }
                            });

                            return;
                        }
                        else if (canCaptureAndMoveRightUp)
                        {
                            Cell origin = botPiece.getCell();
                            botPiece.animatePiece(botPiece, botPiece.getCell(), rightCellUpperCapture, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(rightCellUpperCapture.getRow());
                            currentMove.setToSquareColB(rightCellUpperCapture.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                            int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                            int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                            int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                            tv_j_uF.setText(String.valueOf(uFrom));
                            tv_j_uT.setText(String.valueOf(uTo));
                            tv_j_bF.setText(String.valueOf(bFrom));
                            tv_j_bT.setText(String.valueOf(bTo));

                            currentMove = new Move();

                            Cell finalRightCellUpperCapture = rightCellUpperCapture;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(origin.getRow(), origin.getCol(), finalRightCellUpperCapture.getRow(), finalRightCellUpperCapture.getCol());


                                    if (capturedPiece != null)
                                    {
                                        capturedPiece.getCell().removePiece();
                                        capturedPiece = null;
                                    }

                                    bv.invalidate();

                                    tv_j_userTurn.setVisibility(View.VISIBLE);

                                    turnCounter++;
                                    playerTurn = true;
                                    botTurn = false;

                                    boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                    if (isStuck)
                                    {
                                        timerTask.cancel();
                                        timer.cancel();
                                        tv_j_time.setText(getTimerText());
                                        int rounded = Math.round(time);

                                        cons_j_gameOver.setVisibility(View.VISIBLE);
                                        gameOver = true;
                                        tv_j_numTurns.setText(String.valueOf(turnCounter));

                                        Log.d("Game", "GAME OVER");

                                        int diffId = 0;

                                        if (SessionData.easyModeSelected)
                                        {
                                            diffId = 1;
                                        }
                                        else
                                        {
                                            diffId = 2;
                                        }

                                        currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                        currentMatch.setTime(rounded);
                                        currentMatch.setDifficultyId(diffId);
                                        currentMatch.setResult(tv_j_result.getText().toString());
                                        dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                    }


                                }
                            });

                            return;
                        }

                    }

                    if (!canMoveLeft)
                    {
                        if (rightCell != null)
                        {
                            canMoveRight = canPieceMove(botPiece, botPiece.getCell(), rightCell);
                        }

                        if (rightCellUpper != null)
                        {
                            canMoveRightUp = canPieceMove(botPiece, botPiece.getCell(), rightCellUpper);
                        }

                        if (canMoveRight)
                        {
                            Cell origin = botPiece.getCell();
                            botPiece.animatePiece(botPiece, botPiece.getCell(), rightCell, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(rightCell.getRow());
                            currentMove.setToSquareColB(rightCell.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                            int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                            int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                            int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                            tv_j_uF.setText(String.valueOf(uFrom));
                            tv_j_uT.setText(String.valueOf(uTo));
                            tv_j_bF.setText(String.valueOf(bFrom));
                            tv_j_bT.setText(String.valueOf(bTo));

                            currentMove = new Move();

                            Cell finalRightCell = rightCell;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalRightCell.getRow() + "," + finalRightCell.getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(origin.getRow(), origin.getCol(), finalRightCell.getRow(), finalRightCell.getCol());


                                    bv.invalidate();

                                    tv_j_userTurn.setVisibility(View.VISIBLE);

                                    turnCounter++;
                                    playerTurn = true;
                                    botTurn = false;

                                    boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                    if (isStuck)
                                    {
                                        timerTask.cancel();
                                        timer.cancel();
                                        tv_j_time.setText(getTimerText());
                                        int rounded = Math.round(time);

                                        cons_j_gameOver.setVisibility(View.VISIBLE);
                                        gameOver = true;
                                        tv_j_numTurns.setText(String.valueOf(turnCounter));

                                        Log.d("Game", "GAME OVER");

                                        int diffId = 0;

                                        if (SessionData.easyModeSelected)
                                        {
                                            diffId = 1;
                                        }
                                        else
                                        {
                                            diffId = 2;
                                        }

                                        currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                        currentMatch.setTime(rounded);
                                        currentMatch.setDifficultyId(diffId);
                                        currentMatch.setResult(tv_j_result.getText().toString());
                                        dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                    }


                                }
                            });

                            return;
                        }
                        else if (canMoveRightUp)
                        {
                            Cell origin = botPiece.getCell();
                            botPiece.animatePiece(botPiece, botPiece.getCell(), rightCellUpper, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(rightCellUpper.getRow());
                            currentMove.setToSquareColB(rightCellUpper.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                            int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                            int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                            int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                            tv_j_uF.setText(String.valueOf(uFrom));
                            tv_j_uT.setText(String.valueOf(uTo));
                            tv_j_bF.setText(String.valueOf(bFrom));
                            tv_j_bT.setText(String.valueOf(bTo));

                            currentMove = new Move();

                            Cell finalRightCellUpper = rightCellUpper;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalRightCellUpper.getRow() + "," + finalRightCellUpper.getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(origin.getRow(), origin.getCol(), finalRightCellUpper.getRow(), finalRightCellUpper.getCol());


                                    bv.invalidate();

                                    tv_j_userTurn.setVisibility(View.VISIBLE);

                                    turnCounter++;
                                    playerTurn = true;
                                    botTurn = false;

                                    boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                    if (isStuck)
                                    {
                                        timerTask.cancel();
                                        timer.cancel();
                                        tv_j_time.setText(getTimerText());
                                        int rounded = Math.round(time);

                                        cons_j_gameOver.setVisibility(View.VISIBLE);
                                        gameOver = true;
                                        tv_j_numTurns.setText(String.valueOf(turnCounter));

                                        Log.d("Game", "GAME OVER");

                                        int diffId = 0;

                                        if (SessionData.easyModeSelected)
                                        {
                                            diffId = 1;
                                        }
                                        else
                                        {
                                            diffId = 2;
                                        }

                                        currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                        currentMatch.setTime(rounded);
                                        currentMatch.setDifficultyId(diffId);
                                        currentMatch.setResult(tv_j_result.getText().toString());
                                        dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                    }

                                }
                            });

                            return;
                        }
                    }
                    else if (canMoveLeftUp)
                    {
                        Cell origin = botPiece.getCell();
                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCellUpper, bv);

                        currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                        currentMove.setFromSquareColB(botPiece.getCell().getCol());
                        currentMove.setToSquareRowB(leftCellUpper.getRow());
                        currentMove.setToSquareColB(leftCellUpper.getCol());
                        currentMove.setTurnNumber(turnCounter);
                        matchMoves.add(currentMove);

                        int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                        int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                        int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                        int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                        tv_j_uF.setText(String.valueOf(uFrom));
                        tv_j_uT.setText(String.valueOf(uTo));
                        tv_j_bF.setText(String.valueOf(bFrom));
                        tv_j_bT.setText(String.valueOf(bTo));

                        currentMove = new Move();

                        Cell finalLeftCellUpper = leftCellUpper;
                        botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd(Animator animation)
                            {
                                Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalLeftCellUpper.getRow() + "," + finalLeftCellUpper.getCol());
                                //Update board state AFTER animation completes
                                board.movePiece(origin.getRow(), origin.getCol(), finalLeftCellUpper.getRow(), finalLeftCellUpper.getCol());


                                bv.invalidate();

                                tv_j_userTurn.setVisibility(View.VISIBLE);

                                turnCounter++;
                                playerTurn = true;
                                botTurn = false;

                                boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                if (isStuck)
                                {
                                    timerTask.cancel();
                                    timer.cancel();
                                    tv_j_time.setText(getTimerText());
                                    int rounded = Math.round(time);

                                    cons_j_gameOver.setVisibility(View.VISIBLE);
                                    gameOver = true;
                                    tv_j_numTurns.setText(String.valueOf(turnCounter));

                                    Log.d("Game", "GAME OVER");

                                    int diffId = 0;

                                    if (SessionData.easyModeSelected)
                                    {
                                        diffId = 1;
                                    }
                                    else
                                    {
                                        diffId = 2;
                                    }

                                    currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                    currentMatch.setTime(rounded);
                                    currentMatch.setDifficultyId(diffId);
                                    currentMatch.setResult(tv_j_result.getText().toString());
                                    dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                }

                            }
                        });

                        return;
                    }
                    else
                    {
                        Cell origin = botPiece.getCell();
                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCell, bv);

                        currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                        currentMove.setFromSquareColB(botPiece.getCell().getCol());
                        currentMove.setToSquareRowB(leftCell.getRow());
                        currentMove.setToSquareColB(leftCell.getCol());
                        currentMove.setTurnNumber(turnCounter);
                        matchMoves.add(currentMove);

                        int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                        int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                        int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                        int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                        tv_j_uF.setText(String.valueOf(uFrom));
                        tv_j_uT.setText(String.valueOf(uTo));
                        tv_j_bF.setText(String.valueOf(bFrom));
                        tv_j_bT.setText(String.valueOf(bTo));

                        currentMove = new Move();

                        Cell finalLeftCell = leftCell;
                        botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd(Animator animation)
                            {
                                Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalLeftCell.getRow() + "," + finalLeftCell.getCol());
                                //Update board state AFTER animation completes
                                board.movePiece(origin.getRow(), origin.getCol(), finalLeftCell.getRow(), finalLeftCell.getCol());


                                bv.invalidate();

                                tv_j_userTurn.setVisibility(View.VISIBLE);

                                turnCounter++;
                                playerTurn = true;
                                botTurn = false;

                                boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                if (isStuck)
                                {
                                    timerTask.cancel();
                                    timer.cancel();
                                    tv_j_time.setText(getTimerText());
                                    int rounded = Math.round(time);

                                    cons_j_gameOver.setVisibility(View.VISIBLE);
                                    gameOver = true;
                                    tv_j_numTurns.setText(String.valueOf(turnCounter));

                                    Log.d("Game", "GAME OVER");

                                    int diffId = 0;

                                    if (SessionData.easyModeSelected)
                                    {
                                        diffId = 1;
                                    }
                                    else
                                    {
                                        diffId = 2;
                                    }

                                    currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                    currentMatch.setTime(rounded);
                                    currentMatch.setDifficultyId(diffId);
                                    currentMatch.setResult(tv_j_result.getText().toString());
                                    dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                }


                            }
                        });

                        return;
                    }

                }

            }
        }
    }



    private void onSelectedPiece()
    {
        bv.setOnCellClickListener(new OnCellClickListener()
        {
            @Override
            public void onCellClicked(int row, int col)
            {

                if (isAnimating)
                {
                    return;
                }
                bv.invalidate();
                Cell cell = board.getCell(row, col);

                try
                {
                    if (playerTurn)
                    {
                        if (!canSelectMoveCell)
                        {

                            if (cell.containsPiece() && cell.getPiece().getColor().equals("Light"))
                            {
                                bv.drawSelectionRing(cell.getCol(), cell.getRow());
                                currentPiece = cell.getPiece();
                                from = cell;
                                canSelectMoveCell = true;
                                Log.d("Game", "Selected piece at: " + from.getRow() + "," + from.getCol());
                            }
                            return;
                        }
                        else
                        {
                            try
                            {
                                to = cell;
                                if (from != null && currentPiece != null && to != null)
                                {
                                    boolean canMove = canPieceMove(currentPiece, from, to);
                                    boolean canCaptureAndMove = canCapturePiece(currentPiece, to);

                                    if (canCaptureAndMove && capturedPiece != null)
                                    {
                                        bv.removeSelectionRing();
                                        isAnimating = true;
                                        currentPiece.animatePiece(currentPiece, from, to, bv);

                                        currentPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                                        {
                                            @Override
                                            public void onAnimationEnd(Animator animation)
                                            {
                                                try
                                                {

                                                    Log.d("Game: ", "attempting to capture");
                                                    //Update board state AFTER animation completes
                                                    board.movePiece(from.getRow(), from.getCol(), to.getRow(), to.getCol());

                                                    if (crownPiece(currentPiece) && !currentPiece.isCrowned())
                                                    {
                                                        currentPiece.makeCrowned();
                                                    }

                                                    bv.invalidate();

                                                    playerTurn = true;
                                                    botTurn = false;


                                                    capturedPiece.getCell().removePiece();
                                                    capturedPiece = null;

                                                    chainStarted = true;

                                                    bv.invalidate();

                                                    //Keep piece selected at new location
                                                    Cell originalFrom = from;
                                                    from = to;
                                                    currentPiece = from.getPiece();



                                                    ArrayList<Cell> possibleCells = isAnotherCaptureAvailable(currentPiece);

                                                    if (!possibleCells.isEmpty())
                                                    {
                                                        for(Cell pCell : possibleCells)
                                                        {
                                                            if (canCapturePiece(currentPiece, pCell))
                                                            {
                                                                bv.drawSelectionRing(currentPiece.getCell().getCol(), currentPiece.getCell().getRow());
                                                                tv_j_captureAlert.setVisibility(View.VISIBLE);
                                                                canSelectMoveCell = true;
                                                                Log.d("Game", "Another capture available!");
                                                                playerTurn = true;
                                                                botTurn = false;


                                                            }

                                                        }
                                                    }
                                                    else
                                                    {
                                                        bv.removeSelectionRing();
                                                        chainStarted = false;
                                                        currentMove.setFromSquareRowU(originalFrom.getRow());
                                                        currentMove.setFromSquareColU(originalFrom.getCol());
                                                        currentMove.setToSquareRowU(to.getRow());
                                                        currentMove.setToSquareColU(to.getCol());

                                                        if (crownPiece(currentPiece) && !currentPiece.isCrowned())
                                                        {
                                                            currentPiece.makeCrowned();
                                                        }

                                                        //No more captures, end turn
                                                        from = null;
                                                        currentPiece = null;
                                                        canSelectMoveCell = false;
                                                        playerTurn = false;
                                                        botTurn = true;

                                                        tv_j_userTurn.setVisibility(View.INVISIBLE);
                                                        tv_j_captureAlert.setVisibility(View.INVISIBLE);

                                                        boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                                        if (isStuck)
                                                        {
                                                            timerTask.cancel();
                                                            timer.cancel();
                                                            tv_j_time.setText(getTimerText());
                                                            int rounded = Math.round(time);

                                                            cons_j_gameOver.setVisibility(View.VISIBLE);
                                                            gameOver = true;
                                                            tv_j_numTurns.setText(String.valueOf(turnCounter));

                                                            Log.d("Game", "GAME OVER");

                                                            int diffId = 0;

                                                            if (SessionData.easyModeSelected)
                                                            {
                                                                diffId = 1;
                                                            }
                                                            else
                                                            {
                                                                diffId = 2;
                                                            }

                                                            currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                                            currentMatch.setTime(rounded);
                                                            currentMatch.setDifficultyId(diffId);
                                                            currentMatch.setResult(tv_j_result.getText().toString());
                                                            dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                                        }

                                                        if (SessionData.easyModeSelected)
                                                        {
                                                            easyDifficultyBotTurn();
                                                        }
                                                        else
                                                        {
                                                            intermediateDifficultyBotTurn();
                                                        }

                                                    }
                                                }
                                                finally
                                                {
                                                    isAnimating = false;
                                                }


                                            }
                                        });



                                        return;


                                    }

                                    if (canMove && !chainStarted)
                                    {
                                        bv.removeSelectionRing();
                                        currentPiece.animatePiece(currentPiece, from, to, bv);
                                        isAnimating = true;



                                        currentPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                                        {
                                            @Override
                                            public void onAnimationEnd(Animator animation)
                                            {
                                                try
                                                {
                                                    //public void movePiece(int fromRow, int fromCol, int toRow, int toCol)
                                                    board.movePiece(from.getRow(), from.getCol(), to.getRow(), to.getCol());

                                                    currentMove.setFromSquareRowU(from.getRow());
                                                    currentMove.setFromSquareColU(from.getCol());
                                                    currentMove.setToSquareRowU(to.getRow());
                                                    currentMove.setToSquareColU(to.getCol());

                                                    Log.d("Game: ", "moved from: " + from.getRow() + "," + from.getCol() + ", to: " + to.getRow() + "," + to.getCol());

                                                    if (crownPiece(currentPiece) && !currentPiece.isCrowned())
                                                    {
                                                        currentPiece.makeCrowned();
                                                    }

                                                    from = null;
                                                    to = null;
                                                    currentPiece = null;
                                                    canSelectMoveCell = false;
                                                    bv.invalidate();
                                                    playerTurn = false;
                                                    botTurn = true;


                                                    tv_j_userTurn.setVisibility(View.INVISIBLE);
                                                    tv_j_captureAlert.setVisibility(View.INVISIBLE);

                                                    boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                                    if (isStuck)
                                                    {
                                                        timerTask.cancel();
                                                        timer.cancel();
                                                        tv_j_time.setText(getTimerText());
                                                        int rounded = Math.round(time);

                                                        cons_j_gameOver.setVisibility(View.VISIBLE);
                                                        gameOver = true;
                                                        tv_j_numTurns.setText(String.valueOf(turnCounter));

                                                        Log.d("Game", "GAME OVER");

                                                        int diffId = 0;

                                                        if (SessionData.easyModeSelected)
                                                        {
                                                            diffId = 1;
                                                        }
                                                        else
                                                        {
                                                            diffId = 2;
                                                        }

                                                        currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                                        currentMatch.setTime(rounded);
                                                        currentMatch.setDifficultyId(diffId);
                                                        currentMatch.setResult(tv_j_result.getText().toString());
                                                        dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                                    }


                                                    if (SessionData.easyModeSelected)
                                                    {
                                                        easyDifficultyBotTurn();
                                                    }
                                                    else
                                                    {
                                                        intermediateDifficultyBotTurn();
                                                    }
                                                }
                                                finally
                                                {
                                                    isAnimating = false;
                                                }


                                            }
                                        });

                                    }
                                    else
                                    {
                                        bv.removeSelectionRing();
                                        from = null;
                                        to = null;
                                        currentPiece = null;
                                        canSelectMoveCell = false;
                                        playerTurn = true;

                                    }
                                }
                            }
                            catch (NullPointerException npe)
                            {
                                Log.d("Game: ", "error " + npe);
                            }


                        }
                    }
                }
                catch (NullPointerException np)
                {
                    Log.d("Game: ", "error " + np);
                }



                //logBoardForDebugging();
            }
        });
    }

    private void logBoardForDebugging()
    {
        for (int row = 0; row < 8; row++)
        {
            for (int col = 0; col < 8; col++)
            {
                Cell cell = board.getCell(row, col);
                Log.d("Game Board:", "Cell " + row + "," + col + " contains piece? " + cell.containsPiece());
            }
        }
    }

    private boolean canNoLongerMoveOrNoMorePieces()
    {
        int rightCellRow = -1;
        int rightCellCol = -1;
        Cell rightCell;
        int leftCellRow = -1;
        int leftCellCol = -1;
        Cell leftCell;

        int rightCellRowCapture = -1;
        int rightCellColCapture = -1;
        Cell rightCellCapture;
        int leftCellRowCapture = -1;
        int leftCellColCapture = -1;
        Cell leftCellCapture;

        //LOWER

        int lowerRightCellRow = -1;
        int lowerRightCellCol = -1;
        Cell lowerRightCell = null;
        int lowerLeftCellRow = -1;
        int lowerLeftCellCol = -1;
        Cell lowerLeftCell = null;

        int rightCellRowLowerCapture = -1;
        int rightCellColLowerCapture = -1;
        Cell rightCellLowerCapture;
        int leftCellRowLowerCapture = -1;
        int leftCellColLowerCapture = -1;
        Cell leftCellLowerCapture;

        boolean canMoveLeft = false;
        boolean canMoveRight = false;
        boolean canMoveLeftOther = false;
        boolean canMoveRightOther = false;

        boolean canCaptureLeft = false;
        boolean canCaptureRight = false;
        boolean canCaptureRightOther = false;
        boolean canCaptureLeftOther = false;

        ArrayList<Piece> darkPieces = new ArrayList<>();
        ArrayList<Piece> lightPieces = new ArrayList<>();

        for (int row = 0; row < 8; row++)
        {
            for (int col = 0; col < 8; col++)
            {
                Cell cell = board.getCell(row, col);
                if (cell.containsPiece())
                {
                    Piece piece = cell.getPiece();
                    if (piece.getColor().equals("Dark"))
                    {
                        darkPieces.add(piece);
                    }
                }
            }
        }

        for (int row = 0; row < 8; row++)
        {
            for (int col = 0; col < 8; col++)
            {
                Cell cell = board.getCell(row, col);
                if (cell.containsPiece())
                {
                    Piece piece = cell.getPiece();
                    if (piece.getColor().equals("Light"))
                    {
                        lightPieces.add(piece);
                    }
                }
            }
        }

        if (darkPieces.isEmpty())
        {
            //bot lost all pieces
            currentMove.setFromSquareRowB(-2);
            currentMove.setFromSquareColB(-2);
            currentMove.setToSquareRowB(-2);
            currentMove.setToSquareColB(-2);

            currentMove.setTurnNumber(turnCounter);
            matchMoves.add(currentMove);

            turnCounter++;

            tv_j_result.setText("Won");
            return true;
        }
        if (lightPieces.isEmpty())
        {

            tv_j_result.setText("Lost");
            return true;
        }

        if (playerTurn)
        {
            for (Piece piece : lightPieces)
            {
                canMoveLeft = false;
                canMoveRight = false;
                canMoveLeftOther = false;
                canMoveRightOther = false;

                if (!piece.isCrowned())
                {

                    rightCellRow = piece.getCell().getRow() - 1;
                    rightCellCol = piece.getCell().getCol() + 1;
                    rightCell = board.getCell(rightCellRow, rightCellCol);
                    leftCellRow = piece.getCell().getRow() - 1;
                    leftCellCol = piece.getCell().getCol() - 1;
                    leftCell = board.getCell(leftCellRow, leftCellCol);

                    rightCellRowCapture = piece.getCell().getRow() - 2;
                    rightCellColCapture = piece.getCell().getCol() + 2;
                    rightCellCapture = board.getCell(rightCellRowCapture, rightCellColCapture);
                    leftCellRowCapture = piece.getCell().getRow() - 2;
                    leftCellColCapture = piece.getCell().getCol() - 2;
                    leftCellCapture = board.getCell(leftCellRowCapture, leftCellColCapture);


                    if (leftCell != null)
                    {
                        canMoveLeft = canPieceMove(piece, piece.getCell(), leftCell);
                        canCaptureLeft = canCapturePiece(piece, leftCellCapture);
                    }

                    if (rightCell != null)
                    {
                        canMoveRight = canPieceMove(piece, piece.getCell(), rightCell);
                        canCaptureRight = canCapturePiece(piece, rightCellCapture);
                    }

                    if (canMoveLeft || canMoveRight || canCaptureRight || canCaptureLeft)
                    {
                        //they can still move
                        return false;
                    }



                }
                else
                {
                    //LOWER

                    lowerRightCellRow = piece.getCell().getRow() + 1;
                    lowerRightCellCol = piece.getCell().getCol() + 1;
                    lowerRightCell = board.getCell(lowerRightCellRow, lowerRightCellCol);
                    lowerLeftCellRow = piece.getCell().getRow() + 1;
                    lowerLeftCellCol = piece.getCell().getCol() - 1;
                    lowerLeftCell = board.getCell(lowerLeftCellRow, lowerLeftCellCol);

                    rightCellRowLowerCapture = piece.getCell().getRow() - 2;
                    rightCellColLowerCapture = piece.getCell().getCol() + 2;
                    rightCellLowerCapture = board.getCell(rightCellRowLowerCapture, rightCellColLowerCapture);
                    leftCellRowLowerCapture = piece.getCell().getRow() - 2;
                    leftCellColLowerCapture = piece.getCell().getCol() - 2;
                    leftCellLowerCapture = board.getCell(leftCellRowLowerCapture, leftCellColLowerCapture);


                    //UPPER
                    //2 row and 2 col away
                    rightCellRow = piece.getCell().getRow() - 1;
                    rightCellCol = piece.getCell().getCol() + 1;
                    rightCell = board.getCell(rightCellRow, rightCellCol);
                    leftCellRow = piece.getCell().getRow() - 1;
                    leftCellCol = piece.getCell().getCol() - 1;
                    leftCell = board.getCell(leftCellRow, leftCellCol);

                    rightCellRowCapture = piece.getCell().getRow() - 2;
                    rightCellColCapture = piece.getCell().getCol() + 2;
                    rightCellCapture = board.getCell(rightCellRowCapture, rightCellColCapture);
                    leftCellRowCapture = piece.getCell().getRow() - 2;
                    leftCellColCapture = piece.getCell().getCol() - 2;
                    leftCellCapture = board.getCell(leftCellRowCapture, leftCellColCapture);



                    if (leftCell != null)
                    {
                        canMoveLeft = canPieceMove(piece, piece.getCell(), leftCell);
                        canCaptureLeft = canCapturePiece(piece, leftCellCapture);
                    }

                    if (rightCell != null)
                    {
                        canMoveRight = canPieceMove(piece, piece.getCell(), rightCell);
                        canCaptureRight = canCapturePiece(piece, rightCellCapture);
                    }

                    if (lowerRightCell != null)
                    {
                        canMoveRightOther = canPieceMove(piece, piece.getCell(), lowerRightCell);
                        canCaptureRightOther = canCapturePiece(piece, rightCellLowerCapture);
                    }

                    if (lowerLeftCell != null)
                    {
                        canMoveLeftOther = canPieceMove(piece, piece.getCell(), lowerLeftCell);
                        canCaptureLeftOther = canCapturePiece(piece, leftCellLowerCapture);
                    }

                    if (canMoveLeft || canMoveRight || canMoveRightOther || canMoveLeftOther || canCaptureRightOther || canCaptureLeftOther || canCaptureLeft || canCaptureRight)
                    {
                        //they can still move
                        return false;
                    }

                }


            }

            botWon = true;
            tv_j_result.setText("Lost");
            return true;
        }
        else if (botTurn)
        {
            for (Piece piece : darkPieces)
            {
                canMoveLeft = false;
                canMoveRight = false;
                canMoveLeftOther = false;
                canMoveRightOther = false;

                if (!piece.isCrowned())
                {
                    rightCellRow = piece.getCell().getRow() + 1;
                    rightCellCol = piece.getCell().getCol() + 1;
                    rightCell = board.getCell(rightCellRow, rightCellCol);

                    leftCellRow = piece.getCell().getRow() + 1;
                    leftCellCol = piece.getCell().getCol() - 1;
                    leftCell = board.getCell(leftCellRow, leftCellCol);

                    rightCellRowCapture = piece.getCell().getRow() + 2;
                    rightCellColCapture = piece.getCell().getCol() + 2;
                    rightCellCapture = board.getCell(rightCellRowCapture, rightCellColCapture);
                    leftCellRowCapture = piece.getCell().getRow() + 2;
                    leftCellColCapture = piece.getCell().getCol() - 2;
                    leftCellCapture = board.getCell(leftCellRowCapture, leftCellColCapture);

                    if (leftCell != null)
                    {
                        canMoveLeft = canPieceMove(piece, piece.getCell(), leftCell);
                        canCaptureLeft = canCapturePiece(piece, leftCellCapture);
                    }

                    if (rightCell != null)
                    {
                        canMoveRight = canPieceMove(piece, piece.getCell(), rightCell);
                        canCaptureRight = canCapturePiece(piece, rightCellCapture);
                    }

                    if (canMoveLeft || canMoveRight || canCaptureRight || canCaptureLeft)
                    {
                        //they can still move
                        return false;
                    }


                }
                else
                {
                    //LOWER

                    lowerRightCellRow = piece.getCell().getRow() + 1;
                    lowerRightCellCol = piece.getCell().getCol() + 1;
                    lowerRightCell = board.getCell(lowerRightCellRow, lowerRightCellCol);
                    lowerLeftCellRow = piece.getCell().getRow() + 1;
                    lowerLeftCellCol = piece.getCell().getCol() - 1;
                    lowerLeftCell = board.getCell(lowerLeftCellRow, lowerLeftCellCol);

                    rightCellRowLowerCapture = piece.getCell().getRow() + 2;
                    rightCellColLowerCapture = piece.getCell().getCol() + 2;
                    rightCellLowerCapture = board.getCell(rightCellRowLowerCapture, rightCellColLowerCapture);
                    leftCellRowLowerCapture = piece.getCell().getRow() + 2;
                    leftCellColLowerCapture = piece.getCell().getCol() - 2;
                    leftCellLowerCapture = board.getCell(leftCellRowLowerCapture, leftCellColLowerCapture);


                    //UPPER

                    rightCellRow = piece.getCell().getRow() - 1;
                    rightCellCol = piece.getCell().getCol() + 1;
                    rightCell = board.getCell(rightCellRow, rightCellCol);
                    leftCellRow = piece.getCell().getRow() - 1;
                    leftCellCol = piece.getCell().getCol() - 1;
                    leftCell = board.getCell(leftCellRow, leftCellCol);

                    rightCellRowCapture = piece.getCell().getRow() - 2;
                    rightCellColCapture = piece.getCell().getCol() + 2;
                    rightCellCapture = board.getCell(rightCellRowCapture, rightCellColCapture);
                    leftCellRowCapture = piece.getCell().getRow() - 2;
                    leftCellColCapture = piece.getCell().getCol() - 2;
                    leftCellCapture = board.getCell(leftCellRowCapture, leftCellColCapture);


                    if (leftCell != null)
                    {
                        canMoveLeft = canPieceMove(piece, piece.getCell(), leftCell);
                        canCaptureLeft = canCapturePiece(piece, leftCellCapture);
                    }

                    if (rightCell != null)
                    {
                        canMoveRight = canPieceMove(piece, piece.getCell(), rightCell);
                        canCaptureRight = canCapturePiece(piece, rightCellCapture);
                    }

                    if (lowerRightCell != null)
                    {
                        canMoveRightOther = canPieceMove(piece, piece.getCell(), lowerRightCell);
                        canCaptureRightOther = canCapturePiece(piece, rightCellLowerCapture);
                    }

                    if (lowerLeftCell != null)
                    {
                        canMoveLeftOther = canPieceMove(piece, piece.getCell(), lowerLeftCell);
                        canCaptureLeftOther = canCapturePiece(piece, leftCellLowerCapture);
                    }

                    if (canMoveLeft || canMoveRight || canMoveRightOther || canMoveLeftOther || canCaptureRightOther || canCaptureLeftOther || canCaptureLeft || canCaptureRight)
                    {
                        //they can still move
                        return false;
                    }



                }
            }

            //bot was trapped
            currentMove.setFromSquareRowB(-1);
            currentMove.setFromSquareColB(-1);
            currentMove.setToSquareRowB(-1);
            currentMove.setToSquareColB(-1);

            currentMove.setTurnNumber(turnCounter);
            matchMoves.add(currentMove);

            turnCounter++;

            tv_j_result.setText("Won");
            userWon = true;
            return true;

        }


        return false;
    }



    private ArrayList<Cell> isAnotherCaptureAvailable(Piece piece)
    {
        int rightCellRow = -1;
        int rightCellCol = -1;
        Cell rightCell;
        int leftCellRow = -1;
        int leftCellCol = -1;
        Cell leftCell;

        //LOWER
        //2 row and 2 col away
        int lowerRightCellRow = -1;
        int lowerRightCellCol = -1;
        Cell lowerRightCell = null;
        int lowerLeftCellRow = -1;
        int lowerLeftCellCol = -1;
        Cell lowerLeftCell = null;

        ArrayList<Cell> cells = new ArrayList<Cell>();

        if (piece.getColor().equals("Light") && !piece.isCrowned())
        {
            //2 row and 2 col away
            rightCellRow = piece.getCell().getRow() - 2;
            rightCellCol = piece.getCell().getCol() + 2;
            rightCell = board.getCell(rightCellRow, rightCellCol);
            leftCellRow = piece.getCell().getRow() - 2;
            leftCellCol = piece.getCell().getCol() - 2;
            leftCell = board.getCell(leftCellRow, leftCellCol);


        }
        else if (piece.getColor().equals("Dark") && !piece.isCrowned())
        {
            //2 rows and 2 cols away
            rightCellRow = piece.getCell().getRow() + 2;
            rightCellCol = piece.getCell().getCol() + 2;
            rightCell = board.getCell(rightCellRow, rightCellCol);

            leftCellRow = piece.getCell().getRow() + 2;
            leftCellCol = piece.getCell().getCol() - 2;
            leftCell = board.getCell(leftCellRow, leftCellCol);
        }
        else
        {
            //LOWER
            //2 row and 2 col away
            lowerRightCellRow = piece.getCell().getRow() + 2;
            lowerRightCellCol = piece.getCell().getCol() + 2;
            lowerRightCell = board.getCell(lowerRightCellRow, lowerRightCellCol);
            lowerLeftCellRow = piece.getCell().getRow() + 2;
            lowerLeftCellCol = piece.getCell().getCol() - 2;
            lowerLeftCell = board.getCell(lowerLeftCellRow, lowerLeftCellCol);

            //UPPER
            //2 row and 2 col away
            rightCellRow = piece.getCell().getRow() - 2;
            rightCellCol = piece.getCell().getCol() + 2;
            rightCell = board.getCell(rightCellRow, rightCellCol);
            leftCellRow = piece.getCell().getRow() - 2;
            leftCellCol = piece.getCell().getCol() - 2;
            leftCell = board.getCell(leftCellRow, leftCellCol);




        }


        if (piece.isCrowned())
        {
            boolean canCaptureLeftUp = false;
            boolean canCaptureRightUp = false;
            boolean canCaptureRightDown = false;
            boolean canCaptureLeftDown = false;

            if (leftCell != null)
            {
                canCaptureLeftUp = canCapturePiece(piece, leftCell);
            }

            if (rightCell != null)
            {
                canCaptureRightUp = canCapturePiece(piece, rightCell);
            }

            if (lowerRightCell != null)
            {
                canCaptureRightDown = canCapturePiece(piece, lowerRightCell);
            }

            if (lowerLeftCell != null)
            {
                canCaptureLeftDown = canCapturePiece(piece, lowerLeftCell);
            }

            if (canCaptureLeftUp)
            {
                cells.add(leftCell);
            }
            if (canCaptureRightUp)
            {
                cells.add(rightCell);
            }

            if (canCaptureLeftDown)
            {
                cells.add(lowerLeftCell);
            }

            if (canCaptureRightDown)
            {
                cells.add(lowerRightCell);
            }

            return cells;
        }
        else
        {
            boolean canCaptureLeft = false;
            boolean canCaptureRight = false;
            if (leftCell != null)
            {
                canCaptureLeft = canCapturePiece(piece, leftCell);
            }

            if (rightCell != null)
            {
                canCaptureRight = canCapturePiece(piece, rightCell);
            }

            if (canCaptureLeft)
            {
                cells.add(leftCell);
            }
            if (canCaptureRight)
            {
                cells.add(rightCell);
            }


            return cells;
        }

    }

    private void tryToCrown(Piece piece)
    {

        int rightCellRow = -1;
        int rightCellCol = -1;
        Cell rightCell;

        int leftCellRow = -1;
        int leftCellCol = -1;
        Cell leftCell;

        //this will only be used on dark pieces
        if (!piece.isCrowned() && piece.getCell().getRow() == 6)
        {
            //1 row and 1 col away
            rightCellRow = piece.getCell().getRow() + 1;
            rightCellCol = piece.getCell().getCol() + 1;
            rightCell = board.getCell(rightCellRow, rightCellCol);

            leftCellRow = piece.getCell().getRow() + 1;
            leftCellCol = piece.getCell().getCol() - 1;
            leftCell = board.getCell(leftCellRow, leftCellCol);


            if (leftCell != null)
            {
                if (!leftCell.containsPiece())
                {
                    Cell origin = piece.getCell();
                    piece.animatePiece(piece, piece.getCell(), leftCell, bv);

                    currentMove.setFromSquareRowB(piece.getCell().getRow());
                    currentMove.setFromSquareColB(piece.getCell().getCol());
                    currentMove.setToSquareRowB(leftCell.getRow());
                    currentMove.setToSquareColB(leftCell.getCol());
                    currentMove.setTurnNumber(turnCounter);
                    matchMoves.add(currentMove);

                    int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                    int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                    int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                    int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                    tv_j_uF.setText(String.valueOf(uFrom));
                    tv_j_uT.setText(String.valueOf(uTo));
                    tv_j_bF.setText(String.valueOf(bFrom));
                    tv_j_bT.setText(String.valueOf(bTo));

                    currentMove = new Move();

                    turnCounter++;

                    Cell finalLeftCell = leftCell;
                    piece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            Log.d("Game: ", "BOT moved from: " + piece.getCell().getRow() + "," + piece.getCell().getCol() + ", to: " + finalLeftCell.getRow() + "," + finalLeftCell.getCol());
                            //Update board state AFTER animation completes
                            board.movePiece(origin.getRow(), origin.getCol(), finalLeftCell.getRow(), finalLeftCell.getCol());

                            if (crownPiece(piece))
                            {
                                piece.makeCrowned();
                            }

                            bv.invalidate();

                            tv_j_userTurn.setVisibility(View.VISIBLE);


                            playerTurn = true;
                            botTurn = false;

                            boolean isStuck = canNoLongerMoveOrNoMorePieces();

                            if (isStuck)
                            {
                                timerTask.cancel();
                                timer.cancel();
                                tv_j_time.setText(getTimerText());
                                int rounded = Math.round(time);

                                cons_j_gameOver.setVisibility(View.VISIBLE);
                                gameOver = true;
                                tv_j_numTurns.setText(String.valueOf(turnCounter));

                                Log.d("Game", "GAME OVER");

                                int diffId = 0;

                                if (SessionData.easyModeSelected)
                                {
                                    diffId = 1;
                                }
                                else
                                {
                                    diffId = 2;
                                }

                                currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                currentMatch.setTime(rounded);
                                currentMatch.setDifficultyId(diffId);
                                currentMatch.setResult(tv_j_result.getText().toString());
                                dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                            }


                        }
                    });

                    moveMade = true;

                }
            }

            if (rightCell != null)
            {
                if (!rightCell.containsPiece())
                {
                    Cell origin = piece.getCell();
                    piece.animatePiece(piece, piece.getCell(), rightCell, bv);

                    currentMove.setFromSquareRowB(piece.getCell().getRow());
                    currentMove.setFromSquareColB(piece.getCell().getCol());
                    currentMove.setToSquareRowB(rightCell.getRow());
                    currentMove.setToSquareColB(rightCell.getCol());
                    currentMove.setTurnNumber(turnCounter);
                    matchMoves.add(currentMove);

                    int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                    int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                    int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                    int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                    tv_j_uF.setText(String.valueOf(uFrom));
                    tv_j_uT.setText(String.valueOf(uTo));
                    tv_j_bF.setText(String.valueOf(bFrom));
                    tv_j_bT.setText(String.valueOf(bTo));

                    currentMove = new Move();

                    turnCounter++;

                    Cell finalRightCell = rightCell;
                    piece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            Log.d("Game: ", "BOT moved from: " + piece.getCell().getRow() + "," + piece.getCell().getCol() + ", to: " + finalRightCell.getRow() + "," + finalRightCell.getCol());
                            //Update board state AFTER animation completes
                            board.movePiece(origin.getRow(), origin.getCol(), finalRightCell.getRow(), finalRightCell.getCol());

                            if (crownPiece(piece))
                            {
                                piece.makeCrowned();
                            }

                            bv.invalidate();

                            tv_j_userTurn.setVisibility(View.VISIBLE);


                            playerTurn = true;
                            botTurn = false;

                            boolean isStuck = canNoLongerMoveOrNoMorePieces();

                            if (isStuck)
                            {
                                timerTask.cancel();
                                timer.cancel();
                                tv_j_time.setText(getTimerText());
                                int rounded = Math.round(time);

                                cons_j_gameOver.setVisibility(View.VISIBLE);
                                gameOver = true;
                                tv_j_numTurns.setText(String.valueOf(turnCounter));

                                Log.d("Game", "GAME OVER");

                                int diffId = 0;

                                if (SessionData.easyModeSelected)
                                {
                                    diffId = 1;
                                }
                                else
                                {
                                    diffId = 2;
                                }

                                currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                currentMatch.setTime(rounded);
                                currentMatch.setDifficultyId(diffId);
                                currentMatch.setResult(tv_j_result.getText().toString());
                                dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                            }


                        }
                    });

                    moveMade = true;

                }
            }


        }

    }

    private void checkForPotentialCaptures()
    {
        //will only be used for dark pieces
        //For intermediate difficulty, scan the board for light pieces. If they are capturable
        //and safe to capture, go for it and end the turn if you can't chain capture.

        int rightCellRow = -1;
        int rightCellCol = -1;
        Cell rightCell;

        int leftCellRow = -1;
        int leftCellCol = -1;
        Cell leftCell;


        //1 row and 1 col away
        int rightCellRowUpper = -1;
        int rightCellColUpper = -1;
        Cell rightCellUpper;
        int leftCellRowUpper = -1;
        int leftCellColUpper = -1;
        Cell leftCellUpper;


        ArrayList<Piece> lightPieces = new ArrayList<>();

        for (int row = 0; row < 8; row++)
        {
            for (int col = 0; col < 8; col++)
            {
                Cell cell = board.getCell(row, col);
                if (cell.containsPiece())
                {
                    Piece piece = cell.getPiece();
                    if (piece.getColor().equals("Light"))
                    {
                        lightPieces.add(piece);
                    }
                }
            }
        }

        for (Piece piece : lightPieces)
        {

            //1 row and 1 col away
            rightCellRowUpper = piece.getCell().getRow() - 1;
            rightCellColUpper = piece.getCell().getCol() + 1;
            rightCellUpper = board.getCell(rightCellRowUpper, rightCellColUpper);
            leftCellRowUpper = piece.getCell().getRow() - 1;
            leftCellColUpper = piece.getCell().getCol() - 1;
            leftCellUpper = board.getCell(leftCellRowUpper, leftCellColUpper);

            //1 row and 1 col away
            rightCellRow = piece.getCell().getRow() + 1;
            rightCellCol = piece.getCell().getCol() + 1;
            rightCell = board.getCell(rightCellRow, rightCellCol);

            leftCellRow = piece.getCell().getRow() + 1;
            leftCellCol = piece.getCell().getCol() - 1;
            leftCell = board.getCell(leftCellRow, leftCellCol);

            if (leftCell != null)
            {
                if (leftCell.containsPiece() && leftCell.getPiece().getColor().equals("Dark"))
                {
                    if (rightCellUpper != null)
                    {
                        if (!rightCellUpper.containsPiece())
                        {

                            boolean isSafe = isMoveOrCaptureSafe(leftCell.getPiece(), rightCellUpper, false, true, "Dark");

                            if (isSafe)
                            {
                                if (canCapturePiece(leftCell.getPiece(), rightCellUpper))
                                {
                                    Piece botPiece = leftCell.getPiece();
                                    Cell origin = botPiece.getCell();

                                    botPiece.animatePiece(botPiece, botPiece.getCell(), rightCellUpper, bv);

                                    currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                                    currentMove.setFromSquareColB(botPiece.getCell().getCol());
                                    currentMove.setToSquareRowB(rightCellUpper.getRow());
                                    currentMove.setToSquareColB(rightCellUpper.getCol());
                                    currentMove.setTurnNumber(turnCounter);
                                    matchMoves.add(currentMove);

                                    int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                                    int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                                    int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                                    int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                                    tv_j_uF.setText(String.valueOf(uFrom));
                                    tv_j_uT.setText(String.valueOf(uTo));
                                    tv_j_bF.setText(String.valueOf(bFrom));
                                    tv_j_bT.setText(String.valueOf(bTo));

                                    currentMove = new Move();

                                    turnCounter++;

                                    Cell finalRightCellUpper = rightCellUpper;

                                    botPiece.objectMoveAnimator.removeAllListeners();

                                    botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                                    {
                                        @Override
                                        public void onAnimationEnd(Animator animation)
                                        {
                                            Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalRightCellUpper.getRow() + "," + finalRightCellUpper.getCol());
                                            //Update board state AFTER animation completes
                                            board.movePiece(origin.getRow(), origin.getCol(), finalRightCellUpper.getRow(), finalRightCellUpper.getCol());

                                            if (capturedPiece != null)
                                            {
                                                capturedPiece.getCell().removePiece();
                                                capturedPiece = null;
                                            }

                                            if (crownPiece(botPiece) && !botPiece.isCrowned())
                                            {
                                                botPiece.makeCrowned();
                                            }

                                            bv.invalidate();

                                            ArrayList<Cell> possibleCells = isAnotherCaptureAvailable(botPiece);

                                            if (!possibleCells.isEmpty())
                                            {
                                                Random random = new Random();
                                                int randIndex = random.nextInt(possibleCells.size());
                                                Cell pCell = possibleCells.get(randIndex);

                                                if (canCapturePiece(botPiece, pCell))
                                                {
                                                    continueBotCapture(botPiece, pCell, origin);

                                                    return;
                                                }


                                            }
                                            bv.invalidate();

                                            tv_j_userTurn.setVisibility(View.VISIBLE);


                                            playerTurn = true;
                                            botTurn = false;

                                            boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                            if (isStuck)
                                            {
                                                timerTask.cancel();
                                                timer.cancel();
                                                tv_j_time.setText(getTimerText());
                                                int rounded = Math.round(time);

                                                cons_j_gameOver.setVisibility(View.VISIBLE);
                                                gameOver = true;
                                                tv_j_numTurns.setText(String.valueOf(turnCounter));

                                                Log.d("Game", "GAME OVER");

                                                int diffId = 0;

                                                if (SessionData.easyModeSelected)
                                                {
                                                    diffId = 1;
                                                }
                                                else
                                                {
                                                    diffId = 2;
                                                }

                                                currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                                currentMatch.setTime(rounded);
                                                currentMatch.setDifficultyId(diffId);
                                                currentMatch.setResult(tv_j_result.getText().toString());
                                                dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                            }


                                        }
                                    });

                                    captureMade = true;
                                    return;
                                }
                            }
                        }
                    }
                }

                if (rightCell != null)
                {
                    if (rightCell.containsPiece() && rightCell.getPiece().getColor().equals("Dark"))
                    {
                        if (leftCellUpper != null)
                        {
                            if (!leftCellUpper.containsPiece())
                            {
                                boolean isSafe = isMoveOrCaptureSafe(rightCell.getPiece(), leftCellUpper, false, true, "Dark");

                                if (isSafe)
                                {
                                    if (canCapturePiece(rightCell.getPiece(), leftCellUpper))
                                    {
                                        Piece botPiece = rightCell.getPiece();
                                        Cell origin = botPiece.getCell();

                                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCellUpper, bv);

                                        currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                                        currentMove.setFromSquareColB(botPiece.getCell().getCol());
                                        currentMove.setToSquareRowB(leftCellUpper.getRow());
                                        currentMove.setToSquareColB(leftCellUpper.getCol());
                                        currentMove.setTurnNumber(turnCounter);
                                        matchMoves.add(currentMove);

                                        int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                                        int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                                        int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                                        int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                                        tv_j_uF.setText(String.valueOf(uFrom));
                                        tv_j_uT.setText(String.valueOf(uTo));
                                        tv_j_bF.setText(String.valueOf(bFrom));
                                        tv_j_bT.setText(String.valueOf(bTo));

                                        currentMove = new Move();

                                        turnCounter++;

                                        Cell finalLeftCellUpper = leftCellUpper;

                                        botPiece.objectMoveAnimator.removeAllListeners();

                                        botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                                        {
                                            @Override
                                            public void onAnimationEnd(Animator animation)
                                            {
                                                Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalLeftCellUpper.getRow() + "," + finalLeftCellUpper.getCol());
                                                //Update board state AFTER animation completes
                                                board.movePiece(origin.getRow(), origin.getCol(), finalLeftCellUpper.getRow(), finalLeftCellUpper.getCol());

                                                if (capturedPiece != null)
                                                {
                                                    capturedPiece.getCell().removePiece();
                                                    capturedPiece = null;
                                                }

                                                if (crownPiece(botPiece) && !botPiece.isCrowned())
                                                {
                                                    botPiece.makeCrowned();
                                                }

                                                bv.invalidate();

                                                ArrayList<Cell> possibleCells = isAnotherCaptureAvailable(botPiece);

                                                if (!possibleCells.isEmpty())
                                                {
                                                    Random random = new Random();
                                                    int randIndex = random.nextInt(possibleCells.size());
                                                    Cell pCell = possibleCells.get(randIndex);

                                                    if (canCapturePiece(botPiece, pCell))
                                                    {
                                                        continueBotCapture(botPiece, pCell, origin);

                                                        return;
                                                    }


                                                }
                                                bv.invalidate();

                                                tv_j_userTurn.setVisibility(View.VISIBLE);


                                                playerTurn = true;
                                                botTurn = false;

                                                boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                                if (isStuck)
                                                {
                                                    timerTask.cancel();
                                                    timer.cancel();
                                                    tv_j_time.setText(getTimerText());
                                                    int rounded = Math.round(time);

                                                    cons_j_gameOver.setVisibility(View.VISIBLE);
                                                    gameOver = true;
                                                    tv_j_numTurns.setText(String.valueOf(turnCounter));

                                                    Log.d("Game", "GAME OVER");

                                                    int diffId = 0;

                                                    if (SessionData.easyModeSelected)
                                                    {
                                                        diffId = 1;
                                                    }
                                                    else
                                                    {
                                                        diffId = 2;
                                                    }

                                                    currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                                    currentMatch.setTime(rounded);
                                                    currentMatch.setDifficultyId(diffId);
                                                    currentMatch.setResult(tv_j_result.getText().toString());
                                                    dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                                }


                                            }
                                        });

                                        captureMade = true;
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }

                if (leftCellUpper != null)
                {
                    if (leftCellUpper.containsPiece() && leftCellUpper.getPiece().getColor().equals("Dark"))
                    {
                        if (rightCell != null)
                        {
                            if (!rightCell.containsPiece())
                            {
                                boolean isSafe = isMoveOrCaptureSafe(leftCellUpper.getPiece(), rightCell, false, true, "Dark");

                                if (isSafe)
                                {
                                    if (canCapturePiece(leftCellUpper.getPiece(), rightCell))
                                    {
                                        Piece botPiece = leftCellUpper.getPiece();
                                        Cell origin = botPiece.getCell();

                                        botPiece.animatePiece(botPiece, botPiece.getCell(), rightCell, bv);

                                        currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                                        currentMove.setFromSquareColB(botPiece.getCell().getCol());
                                        currentMove.setToSquareRowB(rightCell.getRow());
                                        currentMove.setToSquareColB(rightCell.getCol());
                                        currentMove.setTurnNumber(turnCounter);
                                        matchMoves.add(currentMove);

                                        int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                                        int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                                        int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                                        int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                                        tv_j_uF.setText(String.valueOf(uFrom));
                                        tv_j_uT.setText(String.valueOf(uTo));
                                        tv_j_bF.setText(String.valueOf(bFrom));
                                        tv_j_bT.setText(String.valueOf(bTo));

                                        currentMove = new Move();

                                        turnCounter++;

                                        Cell finalRightCell = rightCell;

                                        botPiece.objectMoveAnimator.removeAllListeners();

                                        botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                                        {
                                            @Override
                                            public void onAnimationEnd(Animator animation)
                                            {
                                                Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalRightCell.getRow() + "," + finalRightCell.getCol());
                                                //Update board state AFTER animation completes
                                                board.movePiece(origin.getRow(), origin.getCol(), finalRightCell.getRow(), finalRightCell.getCol());

                                                if (capturedPiece != null)
                                                {
                                                    capturedPiece.getCell().removePiece();
                                                    capturedPiece = null;
                                                }

                                                if (crownPiece(botPiece) && !botPiece.isCrowned())
                                                {
                                                    botPiece.makeCrowned();
                                                }

                                                bv.invalidate();

                                                ArrayList<Cell> possibleCells = isAnotherCaptureAvailable(botPiece);

                                                if (!possibleCells.isEmpty())
                                                {
                                                    Random random = new Random();
                                                    int randIndex = random.nextInt(possibleCells.size());
                                                    Cell pCell = possibleCells.get(randIndex);

                                                    if (canCapturePiece(botPiece, pCell))
                                                    {
                                                        continueBotCapture(botPiece, pCell, origin);

                                                        return;
                                                    }


                                                }
                                                bv.invalidate();

                                                tv_j_userTurn.setVisibility(View.VISIBLE);


                                                playerTurn = true;
                                                botTurn = false;

                                                boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                                if (isStuck)
                                                {
                                                    timerTask.cancel();
                                                    timer.cancel();
                                                    tv_j_time.setText(getTimerText());
                                                    int rounded = Math.round(time);

                                                    cons_j_gameOver.setVisibility(View.VISIBLE);
                                                    gameOver = true;
                                                    tv_j_numTurns.setText(String.valueOf(turnCounter));

                                                    Log.d("Game", "GAME OVER");

                                                    int diffId = 0;

                                                    if (SessionData.easyModeSelected)
                                                    {
                                                        diffId = 1;
                                                    }
                                                    else
                                                    {
                                                        diffId = 2;
                                                    }

                                                    currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                                    currentMatch.setTime(rounded);
                                                    currentMatch.setDifficultyId(diffId);
                                                    currentMatch.setResult(tv_j_result.getText().toString());
                                                    dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                                }


                                            }
                                        });

                                        captureMade = true;
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }

                if (rightCellUpper != null)
                {
                    if (rightCellUpper.containsPiece() && rightCellUpper.getPiece().getColor().equals("Dark"))
                    {
                        if (leftCell != null)
                        {
                            if (!leftCell.containsPiece())
                            {
                                boolean isSafe = isMoveOrCaptureSafe(rightCellUpper.getPiece(), leftCell, false, true, "Dark");

                                if (isSafe)
                                {
                                    if (canCapturePiece(rightCellUpper.getPiece(), leftCell))
                                    {
                                        Piece botPiece = rightCellUpper.getPiece();
                                        Cell origin = botPiece.getCell();

                                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCell, bv);

                                        currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                                        currentMove.setFromSquareColB(botPiece.getCell().getCol());
                                        currentMove.setToSquareRowB(leftCell.getRow());
                                        currentMove.setToSquareColB(leftCell.getCol());
                                        currentMove.setTurnNumber(turnCounter);
                                        matchMoves.add(currentMove);

                                        int uFrom = getSquareNumber(currentMove.getFromSquareRowU(), currentMove.getFromSquareColU());
                                        int uTo = getSquareNumber(currentMove.getToSquareRowU(), currentMove.getToSquareColU());
                                        int bFrom = getSquareNumber(currentMove.getFromSquareRowB(), currentMove.getFromSquareColB());
                                        int bTo = getSquareNumber(currentMove.getToSquareRowB(), currentMove.getToSquareColB());

                                        tv_j_uF.setText(String.valueOf(uFrom));
                                        tv_j_uT.setText(String.valueOf(uTo));
                                        tv_j_bF.setText(String.valueOf(bFrom));
                                        tv_j_bT.setText(String.valueOf(bTo));

                                        currentMove = new Move();

                                        turnCounter++;

                                        Cell finalLeftCell = leftCell;

                                        botPiece.objectMoveAnimator.removeAllListeners();

                                        botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                                        {
                                            @Override
                                            public void onAnimationEnd(Animator animation)
                                            {
                                                Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalLeftCell.getRow() + "," + finalLeftCell.getCol());
                                                //Update board state AFTER animation completes
                                                board.movePiece(origin.getRow(), origin.getCol(), finalLeftCell.getRow(), finalLeftCell.getCol());

                                                if (capturedPiece != null)
                                                {
                                                    capturedPiece.getCell().removePiece();
                                                    capturedPiece = null;
                                                }

                                                if (crownPiece(botPiece) && !botPiece.isCrowned())
                                                {
                                                    botPiece.makeCrowned();
                                                }

                                                bv.invalidate();

                                                ArrayList<Cell> possibleCells = isAnotherCaptureAvailable(botPiece);

                                                if (!possibleCells.isEmpty())
                                                {
                                                    Random random = new Random();
                                                    int randIndex = random.nextInt(possibleCells.size());
                                                    Cell pCell = possibleCells.get(randIndex);

                                                    if (canCapturePiece(botPiece, pCell))
                                                    {
                                                        continueBotCapture(botPiece, pCell, origin);

                                                        return;
                                                    }


                                                }
                                                bv.invalidate();

                                                tv_j_userTurn.setVisibility(View.VISIBLE);


                                                playerTurn = true;
                                                botTurn = false;


                                                boolean isStuck = canNoLongerMoveOrNoMorePieces();

                                                if (isStuck)
                                                {
                                                    timerTask.cancel();
                                                    timer.cancel();
                                                    tv_j_time.setText(getTimerText());
                                                    int rounded = Math.round(time);

                                                    cons_j_gameOver.setVisibility(View.VISIBLE);
                                                    gameOver = true;
                                                    tv_j_numTurns.setText(String.valueOf(turnCounter));

                                                    Log.d("Game", "GAME OVER");

                                                    int diffId = 0;

                                                    if (SessionData.easyModeSelected)
                                                    {
                                                        diffId = 1;
                                                    }
                                                    else
                                                    {
                                                        diffId = 2;
                                                    }

                                                    currentMatch.setUsername(SessionData.getSignedInUser().getUsername());
                                                    currentMatch.setTime(rounded);
                                                    currentMatch.setDifficultyId(diffId);
                                                    currentMatch.setResult(tv_j_result.getText().toString());
                                                    dbHelper.addNewMatchToDBGivenUsername(SessionData.getSignedInUser().getUsername(), currentMatch, matchMoves);
                                                }


                                            }
                                        });

                                        captureMade = true;
                                        return;
                                    }

                                }
                            }
                        }
                    }
                }

            }

        }
    }




}

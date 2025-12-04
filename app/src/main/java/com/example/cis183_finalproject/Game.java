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

        botPieces = new ArrayList<Piece>();

        bv = findViewById(R.id.boardView);
        board = bv.getBoard();

        tv_j_username = findViewById(R.id.tv_v_game_username);
        tv_j_userTurn = findViewById(R.id.tv_v_game_turnAlert);
        tv_j_captureAlert = findViewById(R.id.tv_v_game_captureAlert);
        tv_j_timePassed = findViewById(R.id.tv_v_game_timePassed);
        tv_j_time = findViewById(R.id.tv_v_game_time);
        tv_j_numTurns = findViewById(R.id.tv_v_game_turns);
        tv_j_result = findViewById(R.id.tv_v_game_result);
        img_j_backArrow = findViewById(R.id.img_v_game_backArrow);
        cons_j_gameOver = findViewById(R.id.cons_v_game_gameOver);

        btn_j_quit = findViewById(R.id.btn_v_game_quit);
        btn_j_retry = findViewById(R.id.btn_v_game_retry);

        onSelectedPiece();

        tv_j_username.setText(SessionData.getSignedInUser().getUsername());

        buttonClickListener();

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

    private boolean isMoveOrCaptureIsSafe(Cell potenialTo)
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
    }


    private void intermediateDifficultyBotTurn()
    {
        //can capture more than one piece at a time
        //will check to make sure it is safe before capturing or moving
        //if piece is not crowned, it will attempt to move it closer to end of grid


        botPieces.clear();

        if (playerTurn)
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

        if (botPieces != null)
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

                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCellCapture, bv);

                        currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                        currentMove.setFromSquareColB(botPiece.getCell().getCol());
                        currentMove.setToSquareRowB(leftCellCapture.getRow());
                        currentMove.setToSquareColB(leftCellCapture.getCol());
                        currentMove.setTurnNumber(turnCounter);
                        matchMoves.add(currentMove);

                        currentMove = new Move();

                        Cell finalLeftCellCapture = leftCellCapture;
                        botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd(Animator animation)
                            {
                                Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                //Update board state AFTER animation completes
                                board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalLeftCellCapture.getRow(), finalLeftCellCapture.getCol());

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
                            botPiece.animatePiece(botPiece, botPiece.getCell(), rightCellCapture, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(rightCellCapture.getRow());
                            currentMove.setToSquareColB(rightCellCapture.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            currentMove = new Move();

                            Cell finalRightCellCapture = rightCellCapture;

                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalRightCellCapture.getRow(), finalRightCellCapture.getCol());

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
                            botPiece.animatePiece(botPiece, botPiece.getCell(), rightCell, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(rightCell.getRow());
                            currentMove.setToSquareColB(rightCell.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            currentMove = new Move();

                            Cell finalRightCell = rightCell;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalRightCell.getRow() + "," + finalRightCell.getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalRightCell.getRow(), finalRightCell.getCol());

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
                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCell, bv);

                        currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                        currentMove.setFromSquareColB(botPiece.getCell().getCol());
                        currentMove.setToSquareRowB(leftCell.getRow());
                        currentMove.setToSquareColB(leftCell.getCol());
                        currentMove.setTurnNumber(turnCounter);
                        matchMoves.add(currentMove);

                        currentMove = new Move();

                        Cell finalLeftCell = leftCell;
                        botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd(Animator animation)
                            {
                                Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalLeftCell.getRow() + "," + finalLeftCell.getCol());
                                //Update board state AFTER animation completes
                                board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalLeftCell.getRow(), finalLeftCell.getCol());

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

                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCellCapture, bv);

                        currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                        currentMove.setFromSquareColB(botPiece.getCell().getCol());
                        currentMove.setToSquareRowB(leftCellCapture.getRow());
                        currentMove.setToSquareColB(leftCellCapture.getCol());
                        currentMove.setTurnNumber(turnCounter);
                        matchMoves.add(currentMove);

                        currentMove = new Move();

                        Cell finalLeftCellCapture = leftCellCapture;
                        botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd(Animator animation)
                            {
                                Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                //Update board state AFTER animation completes
                                board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalLeftCellCapture.getRow(), finalLeftCellCapture.getCol());

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
                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCellUpperCapture, bv);

                        currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                        currentMove.setFromSquareColB(botPiece.getCell().getCol());
                        currentMove.setToSquareRowB(leftCellUpperCapture.getRow());
                        currentMove.setToSquareColB(leftCellUpperCapture.getCol());
                        currentMove.setTurnNumber(turnCounter);
                        matchMoves.add(currentMove);

                        currentMove = new Move();

                        Cell finalLeftCellUpperCapture = leftCellUpperCapture;
                        botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd(Animator animation)
                            {
                                Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                //Update board state AFTER animation completes
                                board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalLeftCellUpperCapture.getRow(), finalLeftCellUpperCapture.getCol());

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
                            botPiece.animatePiece(botPiece, botPiece.getCell(), rightCellCapture, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(rightCellCapture.getRow());
                            currentMove.setToSquareColB(rightCellCapture.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            currentMove = new Move();

                            Cell finalRightCellCapture = rightCellCapture;

                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalRightCellCapture.getRow(), finalRightCellCapture.getCol());

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
                            botPiece.animatePiece(botPiece, botPiece.getCell(), rightCellUpperCapture, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(rightCellUpperCapture.getRow());
                            currentMove.setToSquareColB(rightCellUpperCapture.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            currentMove = new Move();

                            Cell finalRightCellUpperCapture = rightCellUpperCapture;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalRightCellUpperCapture.getRow(), finalRightCellUpperCapture.getCol());


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
                            botPiece.animatePiece(botPiece, botPiece.getCell(), rightCell, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(rightCell.getRow());
                            currentMove.setToSquareColB(rightCell.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            currentMove = new Move();

                            Cell finalRightCell = rightCell;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalRightCell.getRow() + "," + finalRightCell.getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalRightCell.getRow(), finalRightCell.getCol());


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
                            botPiece.animatePiece(botPiece, botPiece.getCell(), rightCellUpper, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(rightCellUpper.getRow());
                            currentMove.setToSquareColB(rightCellUpper.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            currentMove = new Move();

                            Cell finalRightCellUpper = rightCellUpper;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalRightCellUpper.getRow() + "," + finalRightCellUpper.getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalRightCellUpper.getRow(), finalRightCellUpper.getCol());


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
                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCellUpper, bv);

                        currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                        currentMove.setFromSquareColB(botPiece.getCell().getCol());
                        currentMove.setToSquareRowB(leftCellUpper.getRow());
                        currentMove.setToSquareColB(leftCellUpper.getCol());
                        currentMove.setTurnNumber(turnCounter);
                        matchMoves.add(currentMove);

                        currentMove = new Move();

                        Cell finalLeftCellUpper = leftCellUpper;
                        botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd(Animator animation)
                            {
                                Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalLeftCellUpper.getRow() + "," + finalLeftCellUpper.getCol());
                                //Update board state AFTER animation completes
                                board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalLeftCellUpper.getRow(), finalLeftCellUpper.getCol());


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
                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCell, bv);

                        currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                        currentMove.setFromSquareColB(botPiece.getCell().getCol());
                        currentMove.setToSquareRowB(leftCell.getRow());
                        currentMove.setToSquareColB(leftCell.getCol());
                        currentMove.setTurnNumber(turnCounter);
                        matchMoves.add(currentMove);

                        currentMove = new Move();

                        Cell finalLeftCell = leftCell;
                        botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd(Animator animation)
                            {
                                Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalLeftCell.getRow() + "," + finalLeftCell.getCol());
                                //Update board state AFTER animation completes
                                board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalLeftCell.getRow(), finalLeftCell.getCol());


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

    private void easyDifficultyBotTurn()
    {
        botPieces.clear();

        if (playerTurn)
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

        if (botPieces != null)
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

                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCellCapture, bv);

                        currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                        currentMove.setFromSquareColB(botPiece.getCell().getCol());
                        currentMove.setToSquareRowB(leftCellCapture.getRow());
                        currentMove.setToSquareColB(leftCellCapture.getCol());
                        currentMove.setTurnNumber(turnCounter);
                        matchMoves.add(currentMove);

                        currentMove = new Move();

                        Cell finalLeftCellCapture = leftCellCapture;
                        botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd(Animator animation)
                            {
                                Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                //Update board state AFTER animation completes
                                board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalLeftCellCapture.getRow(), finalLeftCellCapture.getCol());

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
                            botPiece.animatePiece(botPiece, botPiece.getCell(), rightCellCapture, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(rightCellCapture.getRow());
                            currentMove.setToSquareColB(rightCellCapture.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            currentMove = new Move();

                            Cell finalRightCellCapture = rightCellCapture;

                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalRightCellCapture.getRow(), finalRightCellCapture.getCol());

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
                            botPiece.animatePiece(botPiece, botPiece.getCell(), rightCell, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(rightCell.getRow());
                            currentMove.setToSquareColB(rightCell.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            currentMove = new Move();

                            Cell finalRightCell = rightCell;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalRightCell.getRow() + "," + finalRightCell.getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalRightCell.getRow(), finalRightCell.getCol());

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
                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCell, bv);

                        currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                        currentMove.setFromSquareColB(botPiece.getCell().getCol());
                        currentMove.setToSquareRowB(leftCell.getRow());
                        currentMove.setToSquareColB(leftCell.getCol());
                        currentMove.setTurnNumber(turnCounter);
                        matchMoves.add(currentMove);

                        currentMove = new Move();

                        Cell finalLeftCell = leftCell;
                        botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd(Animator animation)
                            {
                                Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalLeftCell.getRow() + "," + finalLeftCell.getCol());
                                //Update board state AFTER animation completes
                                board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalLeftCell.getRow(), finalLeftCell.getCol());

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

                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCellCapture, bv);

                        currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                        currentMove.setFromSquareColB(botPiece.getCell().getCol());
                        currentMove.setToSquareRowB(leftCellCapture.getRow());
                        currentMove.setToSquareColB(leftCellCapture.getCol());
                        currentMove.setTurnNumber(turnCounter);
                        matchMoves.add(currentMove);

                        currentMove = new Move();

                        Cell finalLeftCellCapture = leftCellCapture;
                        botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd(Animator animation)
                            {
                                Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                //Update board state AFTER animation completes
                                board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalLeftCellCapture.getRow(), finalLeftCellCapture.getCol());

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
                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCellUpperCapture, bv);

                        currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                        currentMove.setFromSquareColB(botPiece.getCell().getCol());
                        currentMove.setToSquareRowB(leftCellUpperCapture.getRow());
                        currentMove.setToSquareColB(leftCellUpperCapture.getCol());
                        currentMove.setTurnNumber(turnCounter);
                        matchMoves.add(currentMove);

                        currentMove = new Move();

                        Cell finalLeftCellUpperCapture = leftCellUpperCapture;
                        botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd(Animator animation)
                            {
                                Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                //Update board state AFTER animation completes
                                board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalLeftCellUpperCapture.getRow(), finalLeftCellUpperCapture.getCol());

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
                            botPiece.animatePiece(botPiece, botPiece.getCell(), rightCellCapture, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(rightCellCapture.getRow());
                            currentMove.setToSquareColB(rightCellCapture.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            currentMove = new Move();

                            Cell finalRightCellCapture = rightCellCapture;

                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalRightCellCapture.getRow(), finalRightCellCapture.getCol());

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
                            botPiece.animatePiece(botPiece, botPiece.getCell(), rightCellUpperCapture, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(rightCellUpperCapture.getRow());
                            currentMove.setToSquareColB(rightCellUpperCapture.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            currentMove = new Move();

                            Cell finalRightCellUpperCapture = rightCellUpperCapture;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalRightCellUpperCapture.getRow(), finalRightCellUpperCapture.getCol());


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
                            botPiece.animatePiece(botPiece, botPiece.getCell(), rightCell, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(rightCell.getRow());
                            currentMove.setToSquareColB(rightCell.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            currentMove = new Move();

                            Cell finalRightCell = rightCell;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalRightCell.getRow() + "," + finalRightCell.getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalRightCell.getRow(), finalRightCell.getCol());


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
                            botPiece.animatePiece(botPiece, botPiece.getCell(), rightCellUpper, bv);

                            currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                            currentMove.setFromSquareColB(botPiece.getCell().getCol());
                            currentMove.setToSquareRowB(rightCellUpper.getRow());
                            currentMove.setToSquareColB(rightCellUpper.getCol());
                            currentMove.setTurnNumber(turnCounter);
                            matchMoves.add(currentMove);

                            currentMove = new Move();

                            Cell finalRightCellUpper = rightCellUpper;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalRightCellUpper.getRow() + "," + finalRightCellUpper.getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalRightCellUpper.getRow(), finalRightCellUpper.getCol());


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
                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCellUpper, bv);

                        currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                        currentMove.setFromSquareColB(botPiece.getCell().getCol());
                        currentMove.setToSquareRowB(leftCellUpper.getRow());
                        currentMove.setToSquareColB(leftCellUpper.getCol());
                        currentMove.setTurnNumber(turnCounter);
                        matchMoves.add(currentMove);

                        currentMove = new Move();

                        Cell finalLeftCellUpper = leftCellUpper;
                        botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd(Animator animation)
                            {
                                Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalLeftCellUpper.getRow() + "," + finalLeftCellUpper.getCol());
                                //Update board state AFTER animation completes
                                board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalLeftCellUpper.getRow(), finalLeftCellUpper.getCol());


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
                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCell, bv);

                        currentMove.setFromSquareRowB(botPiece.getCell().getRow());
                        currentMove.setFromSquareColB(botPiece.getCell().getCol());
                        currentMove.setToSquareRowB(leftCell.getRow());
                        currentMove.setToSquareColB(leftCell.getCol());
                        currentMove.setTurnNumber(turnCounter);
                        matchMoves.add(currentMove);

                        currentMove = new Move();

                        Cell finalLeftCell = leftCell;
                        botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd(Animator animation)
                            {
                                Log.d("Game: ", "BOT moved from: " + botPiece.getCell().getRow() + "," + botPiece.getCell().getCol() + ", to: " + finalLeftCell.getRow() + "," + finalLeftCell.getCol());
                                //Update board state AFTER animation completes
                                board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalLeftCell.getRow(), finalLeftCell.getCol());


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

                bv.invalidate();
                Cell cell = board.getCell(row, col);

                if (playerTurn)
                {
                    if (!canSelectMoveCell)
                    {

                        if (cell.containsPiece() && cell.getPiece().getColor().equals("Light"))
                        {
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
                                    currentPiece.animatePiece(currentPiece, from, to, bv);

                                    currentPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                                    {
                                        @Override
                                        public void onAnimationEnd(Animator animation)
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

                                            board.movePiece(from.getRow(), from.getCol(), to.getRow(), to.getCol());
                                            capturedPiece.getCell().removePiece();
                                            capturedPiece = null;

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
                                                easyDifficultyBotTurn();

                                            }

                                        }
                                    });



                                    return;


                                }

                                if (canMove)
                                {
                                    currentPiece.animatePiece(currentPiece, from, to, bv);




                                    currentPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                                    {
                                        @Override
                                        public void onAnimationEnd(Animator animation)
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



                                            easyDifficultyBotTurn();
                                        }
                                    });

                                }
                                else
                                {
                                    from = null;
                                    to = null;
                                    currentPiece = null;
                                    canSelectMoveCell = false;
                                    playerTurn = true;

                                }
                            }
                        }
                        catch (NullPointerException np)
                        {
                            Log.d("Game: ", "error " + np);
                        }


                    }
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

        //LOWER
        //2 row and 2 col away
        int lowerRightCellRow = -1;
        int lowerRightCellCol = -1;
        Cell lowerRightCell = null;
        int lowerLeftCellRow = -1;
        int lowerLeftCellCol = -1;
        Cell lowerLeftCell = null;

        boolean canMoveLeft = false;
        boolean canMoveRight = false;
        boolean canMoveLeftOther = false;
        boolean canMoveRightOther = false;

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

        if (darkPieces.isEmpty())
        {

            tv_j_result.setText("Won");
            return true;
        }
        else
        {
            for (Piece piece : darkPieces)
            {
                if (!piece.isCrowned())
                {
                    rightCellRow = piece.getCell().getRow() + 1;
                    rightCellCol = piece.getCell().getCol() + 1;
                    rightCell = board.getCell(rightCellRow, rightCellCol);

                    leftCellRow = piece.getCell().getRow() + 1;
                    leftCellCol = piece.getCell().getCol() - 1;
                    leftCell = board.getCell(leftCellRow, leftCellCol);

                    if (leftCell != null)
                    {
                        canMoveLeft = canPieceMove(piece, piece.getCell(), leftCell);
                    }

                    if (rightCell != null)
                    {
                        canMoveRight = canPieceMove(piece, piece.getCell(), rightCell);
                    }

                    if (canMoveLeft || canMoveRight)
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

                    //UPPER

                    rightCellRow = piece.getCell().getRow() - 1;
                    rightCellCol = piece.getCell().getCol() + 1;
                    rightCell = board.getCell(rightCellRow, rightCellCol);
                    leftCellRow = piece.getCell().getRow() - 1;
                    leftCellCol = piece.getCell().getCol() - 1;
                    leftCell = board.getCell(leftCellRow, leftCellCol);


                    if (leftCell != null)
                    {
                        canMoveLeft = canPieceMove(piece, piece.getCell(), leftCell);
                    }

                    if (rightCell != null)
                    {
                        canMoveRight = canPieceMove(piece, piece.getCell(), rightCell);
                    }

                    if (lowerRightCell != null)
                    {
                        canMoveRightOther = canPieceMove(piece, piece.getCell(), lowerRightCell);
                    }

                    if (lowerLeftCell != null)
                    {
                        canMoveLeftOther = canPieceMove(piece, piece.getCell(), lowerLeftCell);
                    }

                    if (canMoveLeft || canMoveRight || canMoveRightOther || canMoveLeftOther)
                    {
                        //they can still move
                        return false;
                    }



                }
            }

            userWon = true;

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

        if (lightPieces.isEmpty())
        {

            tv_j_result.setText("Lost");
            return true;
        }
        else
        {
            for (Piece piece : lightPieces)
            {
                if (!piece.isCrowned())
                {
                    //2 row and 2 col away
                    rightCellRow = piece.getCell().getRow() - 1;
                    rightCellCol = piece.getCell().getCol() + 1;
                    rightCell = board.getCell(rightCellRow, rightCellCol);
                    leftCellRow = piece.getCell().getRow() - 1;
                    leftCellCol = piece.getCell().getCol() - 1;
                    leftCell = board.getCell(leftCellRow, leftCellCol);



                    if (leftCell != null)
                    {
                        canMoveLeft = canPieceMove(piece, piece.getCell(), leftCell);
                    }

                    if (rightCell != null)
                    {
                        canMoveRight = canPieceMove(piece, piece.getCell(), rightCell);
                    }

                    if (canMoveLeft || canMoveRight)
                    {
                        //they can still move
                        return false;
                    }



                }
                else
                {
                    //LOWER
                    //2 row and 2 col away
                    lowerRightCellRow = piece.getCell().getRow() + 1;
                    lowerRightCellCol = piece.getCell().getCol() + 1;
                    lowerRightCell = board.getCell(lowerRightCellRow, lowerRightCellCol);
                    lowerLeftCellRow = piece.getCell().getRow() + 1;
                    lowerLeftCellCol = piece.getCell().getCol() - 1;
                    lowerLeftCell = board.getCell(lowerLeftCellRow, lowerLeftCellCol);

                    //UPPER
                    //2 row and 2 col away
                    rightCellRow = piece.getCell().getRow() - 1;
                    rightCellCol = piece.getCell().getCol() + 1;
                    rightCell = board.getCell(rightCellRow, rightCellCol);
                    leftCellRow = piece.getCell().getRow() - 1;
                    leftCellCol = piece.getCell().getCol() - 1;
                    leftCell = board.getCell(leftCellRow, leftCellCol);



                    if (leftCell != null)
                    {
                        canMoveLeft = canPieceMove(piece, piece.getCell(), leftCell);
                    }

                    if (rightCell != null)
                    {
                        canMoveRight = canPieceMove(piece, piece.getCell(), rightCell);
                    }

                    if (lowerRightCell != null)
                    {
                        canMoveRightOther = canPieceMove(piece, piece.getCell(), lowerRightCell);
                    }

                    if (lowerLeftCell != null)
                    {
                        canMoveLeftOther = canPieceMove(piece, piece.getCell(), lowerLeftCell);
                    }

                    if (canMoveLeft || canMoveRight || canMoveRightOther || canMoveLeftOther)
                    {
                        //they can still move
                        return false;
                    }

                }


            }

            botWon = true;
        }

        if (botWon)
        {
            tv_j_result.setText("Lost");
            return true;
        }
        else if (userWon)
        {
            //bot was trapped
            currentMove.setFromSquareRowB(-1);
            currentMove.setFromSquareColB(-1);
            currentMove.setToSquareRowB(-1);
            currentMove.setToSquareColB(-1);
            matchMoves.add(currentMove);

            currentMove = new Move();

            tv_j_result.setText("Won");
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





}

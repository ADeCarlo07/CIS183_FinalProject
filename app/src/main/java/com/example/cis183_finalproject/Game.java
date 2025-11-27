package com.example.cis183_finalproject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

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

        botPieces = new ArrayList<Piece>();

        bv = findViewById(R.id.boardView);
        board = bv.getBoard();

        onSelectedPiece();

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

    private void intermediateDifficultyBotTurn()
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

                                playerTurn = true;
                                botTurn = false;


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

                                    playerTurn = true;
                                    botTurn = false;


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

                                    playerTurn = true;
                                    botTurn = false;


                                }
                            });

                            return;
                        }
                    }
                    else
                    {
                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCell, bv);

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

                                playerTurn = true;
                                botTurn = false;


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

                                playerTurn = true;
                                botTurn = false;


                            }
                        });

                        return;

                    }
                    else if (canCaptureAndMoveLeftUp)
                    {
                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCellUpperCapture, bv);

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

                                playerTurn = true;
                                botTurn = false;


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

                                    playerTurn = true;
                                    botTurn = false;


                                }
                            });

                            return;
                        }
                        else if (canCaptureAndMoveRightUp)
                        {
                            botPiece.animatePiece(botPiece, botPiece.getCell(), rightCellUpperCapture, bv);

                            Cell finalRightCellUpperCapture = rightCellUpperCapture;
                            botPiece.objectMoveAnimator.addListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    Log.d("Game: ", "BOT attempting to capture left at " + capturedPiece.getCell().getRow() + "," + capturedPiece.getCell().getCol());
                                    //Update board state AFTER animation completes
                                    board.movePiece(botPiece.getCell().getRow(), botPiece.getCell().getCol(), finalRightCellUpperCapture.getRow(), finalRightCellUpperCapture.getCol());



                                    bv.invalidate();

                                    playerTurn = true;
                                    botTurn = false;


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

                                    playerTurn = true;
                                    botTurn = false;


                                }
                            });

                            return;
                        }
                        else if (canMoveRightUp)
                        {
                            botPiece.animatePiece(botPiece, botPiece.getCell(), rightCellUpper, bv);

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

                                    playerTurn = true;
                                    botTurn = false;


                                }
                            });

                            return;
                        }
                    }
                    else if (canMoveLeftUp)
                    {
                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCellUpper, bv);

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

                                playerTurn = true;
                                botTurn = false;


                            }
                        });

                        return;
                    }
                    else
                    {
                        botPiece.animatePiece(botPiece, botPiece.getCell(), leftCell, bv);

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

                                playerTurn = true;
                                botTurn = false;


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
                                            from = to;
                                            currentPiece = from.getPiece();



                                            ArrayList<Cell> possibleCells = isAnotherCaptureAvailable(currentPiece);

                                            if (!possibleCells.isEmpty())
                                            {
                                                for(Cell pCell : possibleCells)
                                                {
                                                    if (canCapturePiece(currentPiece, pCell))
                                                    {
                                                        canSelectMoveCell = true;
                                                        Log.d("Game", "Another capture available!");
                                                        playerTurn = true;
                                                        botTurn = false;


                                                    }

                                                }
                                            }
                                            else
                                            {
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


    //Problem with bot capturing piece at 4,3 when bot was crowned and my piece was crowned, tried to get a normal piece, didnt get deleted, next turn jumped over same piece again and my piece got deleted?.
    //LOG: 13:36:52.327 11655-11655 Game:                   com.example.cis183_finalproject      D  moved from: 5,2, to: 4,3
    //2025-11-27 13:36:52.794 11655-11655 Game:                   com.example.cis183_finalproject      D  BOT attempting to capture left at 4,3
    //Piece didn't get deleted and wrong piece might've? Hard to tell.
    //Didn't see any errors




}

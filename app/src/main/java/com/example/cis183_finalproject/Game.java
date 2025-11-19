package com.example.cis183_finalproject;

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

public class Game extends AppCompatActivity
{
    BoardView bv;
    Board board;

    Cell to;
    Cell from;
    Piece currentPiece;
    boolean canSelectMoveCell = false;

    boolean playerTurn = true;

    Piece capturedPiece = null;

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

            if(!leftContainsPiece && piecesTouchingLeft)
            {
                leftSpecial = true;
                canCapture = true;
            }
            else if(!rightContainsPiece && piecesTouchingRight)
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
            else if(!rightContainsPiece && piecesTouchingRight)
            {
                rightSpecial = true;
                canCapture = true;
            }
            else if(!lowerRightContainsPiece && lowerPiecesTouchingRight)
            {
                lowerRightSpecial = true;
                canCapture = true;
            }
            else if(!lowerLeftContainsPiece && lowerPiecesTouchingLeft)
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


    private boolean crownPiece(Piece piece, Cell to)
    {
        return false;
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

                if (!canSelectMoveCell)
                {
                    if (cell.containsPiece())
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
                                Log.d("Game: ", "attempting to capture");
                                board.movePiece(from.getRow(), from.getCol(), to.getRow(), to.getCol());
                                capturedPiece.getCell().removePiece();
                                capturedPiece = null;

                                bv.invalidate();

                                // Keep piece selected at new location
                                from = to;
                                currentPiece = from.getPiece();
                                canSelectMoveCell = true; // still waiting for next destination

                                // Check if another capture is possible
                                if (canCapturePiece(currentPiece, from))
                                {
                                    Log.d("Game", "Another capture available!");
                                    // Do NOT reset state — wait for next click
                                    playerTurn = true;
                                }
                                else
                                {
                                    // No more captures, end turn
                                    from = null;
                                    currentPiece = null;
                                    canSelectMoveCell = false;
                                    playerTurn = false;
                                }
                                return;

                            }

                            if (canMove)
                            {
                                //public void movePiece(int fromRow, int fromCol, int toRow, int toCol)
                                board.movePiece(from.getRow(), from.getCol(), to.getRow(), to.getCol());
                                Log.d("Game: ", "moved from: " + from.getRow() + "," + from.getCol() + ", to: " + to.getRow() + "," + to.getCol());

                                from = null;
                                to = null;
                                currentPiece = null;
                                canSelectMoveCell = false;
                                bv.invalidate();
                                playerTurn = false;
                            }
                            else
                            {
                                from = null;
                                to = null;
                                currentPiece = null;
                                canSelectMoveCell = false;
                            }
                        }
                    }
                    catch (NullPointerException np)
                    {
                        Log.d("Game: ", "error " + np);
                    }


                }

            }
        });
    }









}

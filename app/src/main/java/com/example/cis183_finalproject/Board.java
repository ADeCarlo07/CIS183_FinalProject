package com.example.cis183_finalproject;

public class Board
{
    private Cell[][] cells = new Cell[8][8];
    private Piece selectedPiece;

    public Board()
    {
        for (int row = 0; row < 8; row++)
        {
            for (int col = 0; col < 8; col++)
            {
                boolean isDark = (row + col) % 2 == 1;
                cells[row][col] = new Cell(row, col, isDark);
            }
        }
    }

    public Cell getCell(int row, int col)
    {
        if (row < 0 || row >= 8 || col < 0 || col >= 8)
        {
            return null;
        }
        return cells[row][col];
    }



    public void placePiece(int row, int col, Piece piece)
    {
        Cell cell = getCell(row, col);
        if (cell != null)
        {
            cell.placePiece(piece);
            piece.setCell(cell);
        }
    }

    public void setSelectedPiece(int row, int col)
    {
        Cell cell = getCell(row, col);
        if (cell != null && cell.containsPiece())
        {
            selectedPiece = cell.getPiece();
        }
    }

    public Piece getSelectedPiece()
    {
        return selectedPiece;
    }

    public Board clone()
    {
        Board newBoard = new Board();

        for (int row = 0; row < 8; row++)
        {
            for (int col = 0; col < 8; col++)
            {

                Cell originalCell = this.getCell(row, col);
                Cell newCell = newBoard.getCell(row, col);

                if (originalCell.containsPiece())
                {
                    Piece originalPiece = originalCell.getPiece();

                    Piece newPiece = new Piece(originalPiece.getColor());
                    if (originalPiece.isCrowned())
                    {

                        newPiece.makeCrowned();
                    }

                    newCell.placePiece(newPiece);
                }
                else
                {
                    newCell.removePiece();
                }
            }
        }

        return newBoard;
    }



    public void movePiece(int fromRow, int fromCol, int toRow, int toCol)
    {
        Cell from = getCell(fromRow, fromCol);
        Cell to = getCell(toRow, toCol);

        if (from != null && to != null && from.containsPiece())
        {
            Piece piece = from.getPiece();
            from.removePiece();
            to.placePiece(piece);
            piece.setCell(to);
        }
    }
}

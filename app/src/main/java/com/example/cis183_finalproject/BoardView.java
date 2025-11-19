package com.example.cis183_finalproject;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class BoardView extends View
{
    Bitmap darkPiece = BitmapFactory.decodeResource(getResources(), R.drawable.checkers_dark);
    Bitmap lightPiece = BitmapFactory.decodeResource(getResources(), R.drawable.checkers_light);
    Bitmap darkCrown = BitmapFactory.decodeResource(getResources(), R.drawable.checkers_dark_crown);
    Bitmap lightCrown = BitmapFactory.decodeResource(getResources(), R.drawable.checkers_light_crown);
    private float scaleFactor = 1.0f;
    private float cellSide = 130f;
    private float originX = 20f;
    private float originY = 200f;
    private Paint paint;
    private final int lightColor = Color.parseColor("#F7E8CA");
    private final int darkColor = Color.parseColor("#744E52");

    private Board board;

    private OnCellClickListener listener;


    public BoardView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        board = new Board();
        paint = new Paint();

        setupInitialPieces();
    }

    public Board getBoard()
    {
        return board;
    }

    private void setupInitialPieces()
    {
        //Example: place dark pieces on rows 0–2, light pieces on rows 5–7
        for (int row = 0; row < 3; row++)
        {
            for (int col = 0; col < 8; col++)
            {
                Cell cell = board.getCell(row, col);
                if (cell.isDark())
                {
                    cell.placePiece(new Piece("Dark"));
                }
            }
        }
        for (int row = 5; row < 8; row++)
        {
            for (int col = 0; col < 8; col++)
            {
                Cell cell = board.getCell(row, col);
                if (cell.isDark())
                {
                    cell.placePiece(new Piece("Light"));
                }
            }
        }
    }

    public void setOnCellClickListener(OnCellClickListener l)
    {
        this.listener = l;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            float x = event.getX();
            float y = event.getY();


            int col = (int)((x - originX) / cellSide);
            int row = (int)((y - originY) / cellSide);

            //Bounds check
            if (row >= 0 && row < 8 && col >= 0 && col < 8)
            {
                Cell cell = board.getCell(row, col);
                if (listener != null)
                {
                    listener.onCellClicked(row, col);
                }
            }
        }
        return true;


    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if (canvas == null) return;

        float boardSide = Math.min(getWidth(), getHeight()) * scaleFactor;
        cellSide = boardSide / 8f;
        originX = (getWidth() - boardSide) / 2f;
        originY = (getHeight() - boardSide) / 2f;

        drawBoard(canvas);


    }

    private void drawBoard(Canvas canvas)
    {
        for (int row = 0; row < 8; row++)
        {
            for (int col = 0; col < 8; col++)
            {
                Cell cell = board.getCell(row, col);
                boolean isDark = (col + row) % 2 == 1;
                drawSquareAt(canvas, col, row, isDark);
                if (cell.containsPiece())
                {
                    drawPieceAt(canvas, col, row, cell.getPiece());
                }
            }
        }
    }

    private void drawSquareAt(Canvas canvas, int col, int row, boolean isDark)
    {

        paint.setColor(isDark ? darkColor : lightColor);
        canvas.drawRect(originX + col * cellSide, originY + row * cellSide, originX + (col + 1) * cellSide, originY + (row + 1) * cellSide, paint);

    }

    private void drawPieceAt(Canvas canvas, int col, int row, Piece piece)
    {
        float left = originX + col * cellSide;
        float top = originY + row * cellSide;
        float margin = cellSide * 0.1f;
        Bitmap bmp = null;
        if (piece.getColor().equals("Dark"))
        {

            if (piece.isCrowned())
            {
                bmp = darkCrown;
            }
            else
            {
                bmp = darkPiece;
            }
        }
        else if (piece.getColor().equals("Light"))
        {

            if (piece.isCrowned())
            {
                bmp = lightCrown;
            }
            else
            {
                bmp = lightPiece;
            }
        }

        //Scale to fit cell
        Rect dest = new Rect(Math.round(left + margin), Math.round(top + margin), Math.round(left + cellSide - margin), Math.round(top + cellSide - margin));
        canvas.drawBitmap(bmp, null, dest, null);
    }
}


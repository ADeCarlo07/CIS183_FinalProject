package com.example.cis183_finalproject;

import android.animation.ObjectAnimator;
import android.app.Notification;
import android.graphics.Point;

public class Piece
{
    private String color;
    private boolean isCrowned;
    private Cell cell;

    public ObjectAnimator objectMoveAnimator;

    private Point centerPoint;

    public Point getCenterPoint()
    {
        return centerPoint;
    }

    public void setCenterPoint(Point p)
    {
        centerPoint = p;

    }

    public Piece(String c)
    {
        color = c;
        isCrowned = false;
        cell = null;
    }

    public String getColor()
    {
        return color;
    }

    public void animatePiece(Piece piece, Cell from, Cell to, BoardView bv)
    {

        Point fromPoint = new Point((int)(bv.getOriginX() + from.getCol() * bv.getCellSide() + bv.getCellSide()/2), (int)(bv.getOriginY() + from.getRow() * bv.getCellSide() + bv.getCellSide()/2));

        Point toPoint = new Point((int)(bv.getOriginX() + to.getCol() * bv.getCellSide() + bv.getCellSide()/2), (int)(bv.getOriginY() + to.getRow() * bv.getCellSide() + bv.getCellSide()/2));

        objectMoveAnimator = ObjectAnimator.ofObject(piece, "centerPoint", new PointEvaluator(), fromPoint, toPoint);
        objectMoveAnimator.setDuration(450);
        objectMoveAnimator.addUpdateListener(a -> bv.invalidate());
        objectMoveAnimator.start();
    }

    public boolean isCrowned()
    {
        return isCrowned;
    }

    public void makeCrowned()
    {
        isCrowned = true;
    }

    public Cell getCell()
    {
        return cell;
    }

    public void setCell(Cell c)
    {
        cell = c;
    }

}

package com.example.cis183_finalproject;

import android.animation.TypeEvaluator;
import android.graphics.Point;

//credits:
//https://stackoverflow.com/questions/23603813/how-to-translate-animation-on-an-image-diagonally
public class PointEvaluator implements TypeEvaluator<Point>
{
    @Override
    public Point evaluate(float t, Point startPoint, Point endPoint)
    {
        int x = (int) (startPoint.x + t * (endPoint.x - startPoint.x));
        int y = (int) (startPoint.y + t * (endPoint.y - startPoint.y));
        return new Point(x,y);
    }
}

package project3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class Line2Df
{
    public Point2Df pointA, pointB;
    
    public Line2Df(Point2Df pointA, Point2Df pointB)
    {
        this.pointA = pointA;
        this.pointB = pointB;
    }
    
    public String toString()
    {
        return pointA + " " + pointB;
    }
}

/**
 *
 * @author Jacob Zelek
 */
public class Bresenham
{
    // Uses the Bresenham algorithm to get all points of the lines in the
    // polygon
    public static ArrayList<Point2Di> getLinePoints(ArrayList<Line2Df> lines)
    {
        ArrayList<Point2Di> linePoints = new ArrayList<>();
        
        for(Line2Df line : lines)
        {
            // @ref http://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
            // @ref http://rosettacode.org/wiki/Bitmap/Bresenham%27s_line_algorithm#Java
            int d = 0;

            int x0 = Math.round(line.pointA.x);
            int x1 = Math.round(line.pointB.x);
            int y0 = Math.round(line.pointA.y);
            int y1 = Math.round(line.pointB.y);

            int x0_orig = x0;
            int x1_orig = x1;
            int y0_orig = y0;
            int y1_orig = y1;
            
            float i0 = line.pointA.i_p;
            float i1 = line.pointB.i_p;
            
            int dx = Math.abs(x1 - x0);
            int dy = Math.abs(y1 - y0);

            int dy2 = (dy << 1);
            int dx2 = (dx << 1);

            // Choose direction
            int ix = x0 < x1 ? 1 : -1;
            int iy = y0 < y1 ? 1 : -1;
            
            if(dy <= dx)
            {
                for(;;)
                {
                    if(x0 == x1)
                    {
                        break;
                    }
                    float i_p = i0 - ((i0-i1) * (y0_orig-y0)/(y0_orig-y1_orig));
                    linePoints.add(new Point2Di(x0,y0,i_p));
                    x0 += ix;
                    d += dy2;
                    if(d > dx)
                    {
                        y0 += iy;
                        d -= dx2;
                    }
                }
            } else {
                for(;;)
                {
                    if(y0 == y1)
                    {
                        break;
                    }
                    float i_p = i0 - ((i0-i1) * (y0_orig-y0)/(y0_orig-y1_orig));
                    linePoints.add(new Point2Di(x0,y0,i_p));
                    y0 += iy;
                    d += dx2;
                    if(d > dy)
                    {
                        x0 += ix;
                        d -= dy2;
                    }
                }
            }   
        }
        
        Collections.sort(linePoints, new Comparator<Point2Di>() {
            @Override public int compare(Point2Di p1, Point2Di p2) {
                return (((p2.y - p1.y) != 0.0f) ? p2.y - p1.y : p1.x - p2.x);
            }
        });
        
        return linePoints;
    }
    
    // Returns an ArrayList of all the lines comprising the face
    public static ArrayList<Line2Df> getLines(ArrayList<Point2Df> points)
    {
        ArrayList<Line2Df> lines = new ArrayList<Line2Df>();
        int numPoints = points.size();
        
        if(numPoints > 2)
        {
            for(int i=0; i < numPoints-1; i++)
            {
                Point2Df pointA = points.get(i);
                Point2Df pointB = points.get(i+1);

                Line2Df line = new Line2Df(pointA, pointB);

                lines.add(line);
            }

            Line2Df line = new Line2Df(points.get(points.size()-1), points.get(0));
            lines.add(line);
        }
        else
        {
            Point2Df pointA = points.get(0);
            Point2Df pointB = points.get(1);

            Line2Df line = new Line2Df(pointA, pointB);

            lines.add(line);
        }
            
        return lines;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class Point2Df {
    public float x,y;
    
    public Point2Df(float x, float y)
    {
        this.x = x;
        this.y = y;
    }
    
    public boolean equals(Point2Df a)
    {
        if(a.x == x && a.y == y)
        {
            return true;
        }
            
        return false;
    }
}

class Point2Di {
    public int x,y;
    
    public Point2Di(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public Point2Di(Point2Df p)
    {
        this.x = Math.round(p.x);
        this.y = Math.round(p.y);
    }
    
    public boolean equals(Point2Di a)
    {
        if(a.x == x && a.y == y)
        {
            return true;
        }
            
        return false;
    }
}

class Line2Df {
    public Point2Df pointA, pointB;
    
    public Line2Df(Point2Df pointA, Point2Df pointB)
    {
        this.pointA = pointA;
        this.pointB = pointB;
    }
}

public class Polygon {
    final static int DDA = 0;
    final static int BRESENHAM = 1;
    
    private ArrayList<Point2Df> points;
    private String name;
    private boolean selected;
    private Point2Df centerPoint;
    private int numPoints;
    private float maxX, maxY, minX, minY;
    private int type;
    
    public Polygon()
    {
        init();
    }
    
    public Polygon(String name)
    {
        init();
        this.name = name;
    }
    
    private void init()
    {
        maxX = 0.0f;
        maxY = 0.0f;
        minY = 0.0f;
        minX = 0.0f;
        numPoints = 0;
        selected = false;
        points = new ArrayList<Point2Df>();
        type = BRESENHAM;
    }
    
    public boolean isLine()
    {
        return (numPoints == 2) ? true : false;
    }
    
    // Returns whether line type is DDA or Bresenham
    public int getType()
    {
        return type;
    }
    
    // Sets whether line type is DDA or Bresenham
    public void setType(int type)
    {
        this.type = type;
    }
    
    public boolean getSelected()
    {
        return selected;
    }
    
    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }
    
    public void addPoint(Point2Df p) {
        points.add(p);
        recalc();
    }
    
    // Recalculates number of points, center point, and polygon boundries
    // @ref http://gis.stackexchange.com/questions/77425/how-to-calculate-centroid-of-a-polygon-defined-by-a-list-of-longitude-latitude-p
    private void recalc()
    {
        numPoints = points.size();
        
        float x = 0.0f;
        float y = 0.0f;
        
        for(Point2Df p : getPoints())
        {
            x += p.x;
            y += p.y;
            
            if(p.x > maxX)
            {
                maxX = p.x;
            }
            if(p.x < minX)
            {
                minX = p.x;
            }
            if(p.y > maxY)
            {
                maxY = p.y;
            }
            if(p.y < minY)
            {
                minY = p.y;
            }
        }
        
        centerPoint = new Point2Df(x/numPoints, y/numPoints);
        
        if(isLine())
        {
            Point2Df a = points.get(0);
            Point2Df b = points.get(1);
            
            centerPoint = new Point2Df(((a.x - b.x)/2) + a.x, ((a.y - b.y)/2) + a.y);
        }
    }
    
    public ArrayList<Point2Df> getPoints() {
        return this.points;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String toString()
    {
        return getName();
    }
    
    public boolean pointExists(Point2Df point)
    {
        for(Point2Df p : getPoints())
        {
            if(p.equals(point))
            {
                return true;
            }
        }
        
        return false;
    }
    
    private float getMaxY()
    {
        return maxY;
    }
    
    private float getMinY()
    {
        return minY;
    }
    
    private float getMaxX()
    {
        return maxX;
    }
    
    private float getMinX()
    {
        return minX;
    }
    
    private Point2Df getCenter()
    {
        return centerPoint;
    }
    
    // @ref http://onedayitwillmake.com/blog/2011/09/rotate-vector-line-segment-polygon-around-arbitrary-point/
    public void rotate(double angle)
    {
        recalc();
        
        for(Point2Df p : getPoints())
        {
            float tempX = p.x - getCenter().x;
            float tempY = p.y - getCenter().y;
            
            float oldX = tempX;
            
            tempX = (float) ((float) (tempX * Math.cos(angle)) - (tempY * Math.sin(angle)));
            tempY = (float) ((float) (oldX * Math.sin(angle)) + (tempY * Math.cos(angle)));
            
            p.x = tempX + getCenter().x;
            p.y = tempY + getCenter().y;
        }
    }
    
    public void translate(Point2Df vector)
    {
        recalc();
        
        for(Point2Df p : getPoints())
        {
            float tempX = p.x - getCenter().x;
            float tempY = p.y - getCenter().y;
            
            tempX += vector.x;
            tempY += vector.y;
            
            p.x = tempX + getCenter().x;
            p.y = tempY + getCenter().y;
        }
    }
    
    public void scale(Point2Df factor)
    {
        recalc();
        
        for(Point2Df p : getPoints())
        {
            float tempX = p.x - getCenter().x;
            float tempY = p.y - getCenter().y;
            
            tempX = tempX*factor.x;
            tempY = tempY*factor.y;
            
            p.x = tempX + getCenter().x;
            p.y = tempY + getCenter().y;
        }
    }
    
    public ArrayList<Point2Di> getLinePoints()
    {
        if(type == DDA)
        {
            return getDLinePoints();
        }
        
        return getBLinePoints();
    }
    
    // Uses the DDA algorithm to get all points of the lines in the
    // polygon
    private ArrayList<Point2Di> getDLinePoints()
    {
        ArrayList<Point2Di> linePoints = new ArrayList<Point2Di>();
        
        // @ref http://en.wikipedia.org/wiki/Digital_differential_analyzer_%28graphics_algorithm%29
        for(Line2Df line : getLines())
        {
            float x0 = line.pointA.x;
            float x1 = line.pointB.x;
            float y0 = line.pointA.y;
            float y1 = line.pointB.y;
            
            float dx = x1-x0;
            float dy = y1-y0;

            if(Math.abs(y1-y0) <= Math.abs(x1-x0))
            {
                if(x0 == x1 && y0 == y1)
                {
                    linePoints.add(new Point2Di(Math.round(x0),Math.round(y0)));
                } else {
                    if(x1 < x0)
                    {
                        float tmp = x1;
                        x1 = x0;
                        x0 = tmp;
                        
                        tmp = y1;
                        y1 = y0;
                        y1 = tmp;
                    }
                        
                    float m = dy/dx;
                    float y = y0;

                    for(int x=Math.round(x0); x <= x1; x++)
                    {
                        linePoints.add(new Point2Di(x,Math.round(y)));
                        y = y + m;
                    }
                }
            } else {
                if(y1 < y0)
                {
                    float tmp = x1;
                    x1 = x0;
                    x0 = tmp;

                    tmp = y1;
                    y1 = y0;
                    y1 = tmp;
                }

                float m = dx/dy;
                float x = x0;

                for(int y=Math.round(y0); y <= y1; y++)
                {
                    linePoints.add(new Point2Di(Math.round(x), y));
                    x = x + m;
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
    
    // Uses the Bresenham algorithm to get all points of the lines in the
    // polygon
    private ArrayList<Point2Di> getBLinePoints()
    {
        ArrayList<Point2Di> linePoints = new ArrayList<Point2Di>();
        
        for(Line2Df line : getLines())
        {
            // @ref http://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
            // @ref http://rosettacode.org/wiki/Bitmap/Bresenham%27s_line_algorithm#Java
            int d = 0;

            int x0 = Math.round(line.pointA.x);
            int x1 = Math.round(line.pointB.x);
            int y0 = Math.round(line.pointA.y);
            int y1 = Math.round(line.pointB.y);

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
                    linePoints.add(new Point2Di(x0,y0));
                    if(x0 == x1)
                    {
                        break;
                    }
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
                    linePoints.add(new Point2Di(x0,y0));
                    if(y0 == y1)
                    {
                        break;
                    }
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
    
    // Returns an ArrayList of all the lines comprising the polygon
    public ArrayList<Line2Df> getLines()
    {
        ArrayList<Line2Df> lines = new ArrayList<Line2Df>();
        ArrayList<Point2Df> points = getPoints();
        
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
        } else {
            Point2Df pointA = points.get(0);
            Point2Df pointB = points.get(1);

            Line2Df line = new Line2Df(pointA, pointB);

            lines.add(line);
        }
            
        return lines;
    }
}
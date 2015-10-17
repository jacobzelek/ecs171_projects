package project3;

import java.util.ArrayList;

/**
 *
 * @author Jacob Zelek
 */
public class Clipping
{
    private static int clipXMax, clipXMin, clipYMax, clipYMin;
    
    private static byte getClipBits(Point2Di p)
    {
        byte bits = 0;
        
        if(p.y  > clipYMax)
        {
            if(p.x > clipXMax)
            {
                bits = 5;
            } else if(p.x < clipXMax && p.x > clipXMin) {
                bits = 1;
            } else if(p.x < clipXMin) {
                bits = 9;
            }
        } else if(p.y < clipYMax && p.y > clipYMin) {
            if(p.x > clipXMax)
            {
                bits = 4;
            } else if(p.x < clipXMax && p.x > clipXMin) {
                bits = 0;
            } else if(p.x < clipXMin) {
                bits = 8;
            }
        } else if(p.y < clipYMin) {
            if(p.x > clipXMax)
            {
                bits = 6;
            } else if(p.x < clipXMax && p.x > clipXMin) {
                bits = 2;
            } else if(p.x < clipXMin) {
                bits = 10;
            }
        }
        
        return bits;
    }
    
    public static void setClippingPlane(int clipXMin, int clipXMax,
            int clipYMin, int clipYMax)
    {
        Clipping.clipXMin = clipXMin;
        Clipping.clipYMin = clipYMin;
        Clipping.clipXMax = clipXMax;
        Clipping.clipYMax = clipYMax;
    }
    
    // Returns new clipped face
    // @ref http://www.cs.helsinki.fi/group/goa/viewing/leikkaus/lineClip.html
    // @ref http://lodev.org/cgtutor/lineclipping.html
    public static ArrayList<Point2Df> clip(ArrayList<Point2Df> points)
    {
        ArrayList<Point2Df> newPoints = new ArrayList<>();
        
        for(Line2Df line : Bresenham.getLines(points))
        {
            Point2Df pointA = line.pointA;
            Point2Df pointB = line.pointB;
            boolean accepted = false;
            
            do
            {
                byte pointACode = getClipBits(new Point2Di(pointA));
                byte pointBCode = getClipBits(new Point2Di(pointB));

                if((pointACode & pointBCode) != 0)
                {
                    accepted = true;
                }

                if((pointACode | pointBCode) == 0)
                {
                    newPoints.add(pointA);
                    newPoints.add(pointB);
                    accepted = true;
                }

                if(!accepted)
                {
                    float x1 = pointA.x;
                    float x2 = pointB.x;
                    float y1 = pointA.y;
                    float y2 = pointB.y;

                    float x,y;
                    byte pointCode;

                    if(pointACode > 0)
                    {
                        pointCode = pointACode;
                    } else {
                        pointCode = pointBCode;
                    }

                    if((pointCode & 0x1) == 1)
                    {
                        x = x1 + (x2 - x1) * (clipYMax - y1) / (y2 - y1);
                        y = clipYMax - 1;
                    } else if((pointCode & 0x2) == 2) {
                        x = x1 + (x2 - x1) * -y1 / (y2 - y1);
                        y = 0;
                    } else if((pointCode & 0x4) == 4) {
                        y = y1 + (y2 - y1) * (clipXMax - x1) / (x2 - x1);
                        x = clipXMax - 1;
                    } else {
                        y = y1 + (y2 - y1) * -x1 / (x2 - x1);
                        x = 0;
                    }

                    if(pointACode > 0)
                    {
                        pointA = new Point2Df(x,y,pointA.i_p);
                    } else {
                        pointB = new Point2Df(x,y,pointB.i_p);
                    }
                }
            } while(!accepted);
        }
        
        return newPoints;
    }
}

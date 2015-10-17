/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project1;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

class Color
{
    public float r,g,b;
    public Color(float r, float g, float b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
    }
}

public class Scene implements GLEventListener {
    private ArrayList<Polygon> polygons;
    private int width, height;
    private float[] buffer;
    private int clipXMax, clipXMin, clipYMax, clipYMin;
    
    public Scene()
    {
        polygons = new ArrayList<Polygon>();
        setViewSize(new Point2Di(500,500));
        clearBuffer();
    }
    
    // Clear the OpenGL buffer
    private void clearBuffer()
    {
        buffer = new float[width*height*3];
    }
    
    public Point2Di getViewSize()
    {
        return new Point2Di(width,height);
    }
    
    public Point2Di getClipMin()
    {
        return new Point2Di(clipXMin,clipYMin);
    }
    
    public Point2Di getClipMax()
    {
        return new Point2Di(clipXMax,clipYMax);
    }
    
    public void setClipMin(Point2Di min)
    {
        clipXMin = min.x;
        clipYMin = min.y;
    }
    
    public void setClipMax(Point2Di max)
    {
        clipXMax = max.x;
        clipYMax = max.y;
    }
    
    public void setViewSize(Point2Di size)
    {
        width = size.x;
        height = size.y;
        setClipMin(new Point2Di(0,0));
        setClipMax(new Point2Di(width, height));
    }
    
    public void addPolygon(Polygon p) {
        polygons.add(p);
    }
    
    public ArrayList<Polygon> getPolygons()
    {
        return polygons;
    }
    
    public void clearPolygons() {
        polygons.clear();
    }
    
    private void drawPixel(int x, int y, float[] pixelBuf, Color color)
    {
        int pos = y*width+x;
        
        pixelBuf[pos*3] = color.r;
        pixelBuf[pos*3+1] = color.g;
        pixelBuf[pos*3+2] = color.b;
    }
    
    // Draw horizontal line
    private void drawHLine(int y, int i1, int i2, float[] pixelBuf, Color color)
    {
        for(int i=i1; i < i2; i++)
        {
            drawPixel(i, y, pixelBuf, color);
        }
    }
    
    // Draw vertical line
    private void drawVLine(int x, int i1, int i2, float[] pixelBuf, Color color)
    {
        for(int i=i1; i < i2; i++)
        {
            drawPixel(x, i, pixelBuf, color);
        }
    }
    
    // Draw line
    private void drawLine(Polygon line)
    {
        Color color = (line.getSelected()) ?
                new Color(0.7f,0.3f,0.3f) : new Color(0.7f,0.7f,0.7f);
        
        ArrayList<Point2Di> linePoints = line.getLinePoints();
        
        for(int i=0; i < linePoints.size(); i++)
        {
            Point2Di point = linePoints.get(i);
            drawPixel(point.x,point.y,buffer,color);
        }
    }
    
    // Draw polygon
    private void drawPolygon(Polygon polygon)
    {
        Color color = (polygon.getSelected()) ?
                new Color(0.7f,0.3f,0.3f) : new Color(0.7f,0.7f,0.7f);
        
        ArrayList<Point2Di> linePoints = polygon.getLinePoints();
        
        int count = 0;
        
        for(int i=0; i < linePoints.size()-1; i++)
        {
            Point2Di i1 = linePoints.get(i);
            Point2Di i2 = linePoints.get(i+1);
            
            if(i1.y == i2.y)
            {
                if(i1.x != i2.x && i1.x+1.0f != i2.x)
                {
                    count++;
                }

                if(count % 2 == 0)
                {
                    int startX = Math.min(i1.x, i2.x);
                    int finishX = Math.max(i1.x, i2.x);
                    
                    for(int j=startX; j <= finishX; j++)
                    {
                        drawPixel(j,i1.y,buffer,color);
                    }
                }
            } else {
                count = 1;
            }
        }
    }
    
    private byte getClipBits(Point2Di p)
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
    
    // Returns new clipped polygon
    // @ref http://www.cs.helsinki.fi/group/goa/viewing/leikkaus/lineClip.html
    // @ref http://lodev.org/cgtutor/lineclipping.html
    private Polygon clipPolygon(Polygon p)
    {
        Polygon newPoly = new Polygon();
        newPoly.setSelected(p.getSelected());
        newPoly.setType(p.getType());
        
        for(Line2Df line : p.getLines())
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
                    // @todo Might be a problem with references
                    newPoly.addPoint(pointA);
                    newPoly.addPoint(pointB);
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
                        pointA = new Point2Df(x,y);
                    } else {
                        pointB = new Point2Df(x,y);
                    }
                }
            } while(!accepted);
        }
        
        return newPoly;
    }
    
    @Override
    public void display(GLAutoDrawable drawable) {
        update();
        render(drawable);
    }
    
    private void update() {
        
    }
    
    private void render(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        
        clearBuffer();
        
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
                
        for(Polygon polygon : polygons)
        {
            Polygon newPoly = clipPolygon(polygon);
            
            if(newPoly.isLine())
            {
                drawLine(newPoly);
            } else {
                drawPolygon(newPoly);
            }
        }
        
        Color clipLineColor = new Color(0.7f,0.7f,0.7f);
        
        if(clipXMin > 0 || clipXMax < width)
        {
            drawVLine(clipXMin, clipYMin, clipYMax, buffer, clipLineColor);
            drawVLine(clipXMax, clipYMin, clipYMax, buffer, clipLineColor);
        }
        
        if(clipYMin > 0 || clipYMax < height)
        {
            drawHLine(clipYMin, clipXMin, clipXMax, buffer, clipLineColor);
            drawHLine(clipYMax, clipXMin, clipXMax, buffer, clipLineColor);
        }
        
        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
        
        gl.glDrawPixels(width,
                height,
                GL.GL_RGB,
                GL.GL_FLOAT,
                FloatBuffer.wrap(buffer));
        
        gl.glFlush();
    }
    
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        setViewSize(new Point2Di(width,height));
    }

    @Override
    public void init(GLAutoDrawable glad) {
    
    }

    @Override
    public void dispose(GLAutoDrawable glad) {

    }
}

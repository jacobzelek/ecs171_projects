/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project4;

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
    
    public static Color random()
    {
        return new Color((float)Math.random(),(float)Math.random(),(float)Math.random());
    }
}

public class Scene implements GLEventListener
{
    final static Color offColor = new Color(0.2f,0.2f,0.2f);
    final static Color onColor = new Color(0.7f,0.7f,0.7f);
    
    private ArrayList<Curve> curves;
    public int width, height;
    private int displayResolution;
    
    public Scene()
    {
        curves = new ArrayList<>();
    }
    
    public void addCurve(Curve p) {
        curves.add(p);
    }
    
    public ArrayList<Curve> getCurves()
    {
        return curves;
    }
    
    public void setCurves(ArrayList<Curve> curves)
    {
        this.curves = curves;
    }
      
    public void clearObjects() {
        curves.clear();
    }

    public void setDisplayResolution(int n)
    {
        this.displayResolution = n;
    }
    
    @Override
    public void display(GLAutoDrawable drawable) {
        render(drawable);
    }
    
    private Point2Df worldToNDC(Point2Di p)
    {
        return new Point2Df(((float)p.x/(float)width)-1.0f,
                ((float)p.y/(float)height)-1.0f);
    }
    
    private void drawCurve(Curve c, GL2 gl)
    {
        gl.glLineWidth(2.0f);
        
        ArrayList<Point2Di> bpoints = new ArrayList<Point2Di>();
        
        if(c.getType() == Curve.BEIZER)
        {
            bpoints = c.getBezierPoints(displayResolution);
        } else {
            bpoints = c.getBSplinePoints(displayResolution);
        }
        
        if(c.isSelected())
        {
            gl.glColor3f(0.6f, 0.6f, 0.3f);
            gl.glPointSize(10.0f);
            for(Point2Di p : c.getPoints())
            {
                Point2Df np = worldToNDC(p);
                gl.glBegin(GL.GL_POINTS);
                gl.glVertex2f(np.x, np.y);
                gl.glEnd();
            }
        }
        
        if(c.isSelected())
        {
            gl.glColor3f(0.6f, 0.3f, 0.3f);
        } else {
            gl.glColor3f(0.3f, 0.3f, 0.6f);
        }
        
        for(int i=0; i < bpoints.size()-1; i++)
        {
            Point2Df np0 = worldToNDC(bpoints.get(i));
            Point2Df np1 = worldToNDC(bpoints.get(i+1));
            gl.glBegin(GL.GL_LINES);
            gl.glVertex2f(np0.x, np0.y);
            gl.glVertex2f(np1.x, np1.y);
            gl.glEnd();
        }
    }
    
    private void render(GLAutoDrawable drawable)
    {   
        GL2 gl = drawable.getGL().getGL2();
        
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
                
        for(Curve curve : curves)
        {
  
            drawCurve(curve, gl);
        }
        
        gl.glFlush();
    }
    
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    @Override
    public void init(GLAutoDrawable glad)
    {
        glad.getGL().setSwapInterval(1);
    }

    @Override
    public void dispose(GLAutoDrawable glad) {

    }
}

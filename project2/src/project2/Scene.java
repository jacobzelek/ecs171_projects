/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project2;

import com.jogamp.opengl.util.gl2.GLUT;
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

public class Scene implements GLEventListener
{
    final static int NONE = 0;
    final static int XYPlane = 1;
    final static int XZPlane = 2;
    final static int YZPlane = 3;
    
    private ArrayList<Polygon> polygons;
    private int projType;
    private float f, n, t, b, l, r;
    private float animationAngle;
    private boolean animation;
    private Point3Df[] rotateLine;
    
    public Scene()
    {
        polygons = new ArrayList<Polygon>();
        projType = Scene.NONE;
        animationAngle = 0.0f;
        animation = false;
        rotateLine = new Point3Df[2];
        rotateLine[0] = new Point3Df(-1.0f,-1.0f,-1.0f);
        rotateLine[1] = new Point3Df(1.0f,1.0f,1.0f);
    }
    
    public Scene(int projType)
    {
        this();
        this.projType = projType;
    }
    
    public void addPolygon(Polygon p) {
        polygons.add(p);
    }
    
    public ArrayList<Polygon> getPolygons()
    {
        return polygons;
    }
    
    public void setPolygons(ArrayList<Polygon> newPolys)
    {
        polygons = newPolys;
    }
    
    public void setRotateLine(Point3Df[] rotateLine)
    {
        this.rotateLine = rotateLine;
    }
    
    
    public void resetRotationAnimation()
    {
        animationAngle = 0.0f;
    }
            
    public void clearPolygons() {
        polygons.clear();
    }
    
    public void setAnimation(boolean animation)
    {
        this.animation = animation;
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        update();
        render(drawable);
    }
    
    private void update()
    {
        if(animation)
        {
            animationAngle += 0.01f;
        }
    }
    
    public Point3Df applyAnimationRotation(Point3Df p)
    {
        Point3Df point = new Point3Df(0.0f,0.0f,0.0f);
        
        float u = rotateLine[1].x - rotateLine[0].x;
        float v = rotateLine[1].y - rotateLine[0].y;
        float w = rotateLine[1].z - rotateLine[0].z;
        
        float length = (float) Math.sqrt(u*u + w*w + v*v);
        
        u = u/length;
        v = v/length;
        w = w/length;
        
        float a = rotateLine[0].x;
        float b = rotateLine[0].y;
        float c = rotateLine[0].z;
        
        float cos = (float) Math.cos(animationAngle);
        float sin = (float) Math.sin(animationAngle);

        float x = p.x;
        float y = p.y;
        float z = p.z;

        point.x = (a*(v*v+w*w) - u*(b*v+c*w-u*x-v*y-w*z))*(1-cos) +
                (x*cos) + (-c*v+b*w-w*y+v*z)*sin;

        point.y = (b*(u*u+w*w) - v*(a*u+c*w-u*x-v*y-w*z))*(1-cos) +
                (y*cos) + (c*u-a*w+w*x-u*z)*sin;

        point.z = (c*(u*u+v*v) - w*(a*u+b*v-u*x-v*y-w*z))*(1-cos) +
                (z*cos) + (-b*u+a*v-v*x+u*y)*sin;
        
        return point;
    }
    
    private Point3Df project(Point3Df point)
    {
        if(projType == XYPlane)
        {
            return new Point3Df(point.x, point.y, 0);
        } else if(projType == XZPlane) {
            return new Point3Df(point.x, point.z, 0);
        } else if(projType == YZPlane) {
            return new Point3Df(point.y, point.z, 0);
        }
        
        return new Point3Df(point.x, point.y, point.z);
    }
    
    private void drawAxis(GL2 gl)
    {
        gl.glColor3f(0.8f,0.0f,0.0f);
        gl.glRasterPos2f(-0.95f,0.90f);
        
        if(projType != NONE)
        {
        
            String text = "";

            switch(projType)
            {
                case XYPlane: text = "XY"; break;
                case YZPlane: text = "YZ"; break;
                case XZPlane: text = "XZ"; break;
            }
        
            GLUT glut = new GLUT();
            glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, text);
        }
    }
    
    private void drawRotationLine(GL2 gl)
    {
        gl.glColor3f(0.5f, 0.0f, 0.0f);
        
        Point3Df a = project(rotateLine[0]);
        Point3Df b = project(rotateLine[1]);
        
        gl.glLineWidth(2.0f);
        gl.glBegin(gl.GL_LINES);
            gl.glVertex3f(a.x,a.y,a.z);
            gl.glVertex3f(b.x,b.y,b.z);
        gl.glEnd();
    }
    
    private void render(GLAutoDrawable drawable)
    {   
        GL2 gl = drawable.getGL().getGL2();
        
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        
        drawRotationLine(gl);
        
        for(Polygon polygon : polygons)
        {   
            if(projType != NONE)
            {
                for(Edge edge : polygon.getEdges())
                {
                    Point3Df pointA = project(
                            applyAnimationRotation(polygon.getPoint(edge.p1)));
                    Point3Df pointB = project(
                            applyAnimationRotation(polygon.getPoint(edge.p2)));
                    
                    Color color = (polygon.getSelected()) ?
                        new Color(0.7f,0.3f,0.3f) : edge.color;

                    gl.glColor3f(color.r, color.g, color.b);

                    gl.glLineWidth(2.0f);
                    gl.glBegin(gl.GL_LINES);
                        gl.glVertex3f(pointA.x, pointA.y, pointA.z);
                        gl.glVertex3f(pointB.x, pointB.y, pointB.z);
                    gl.glEnd();
                }
            }
        }
        
        drawAxis(gl);
        
        gl.glFlush();
    }
    
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

    }

    @Override
    public void init(GLAutoDrawable glad) {
        glad.getGL().setSwapInterval(1);
    }

    @Override
    public void dispose(GLAutoDrawable glad) {

    }
}

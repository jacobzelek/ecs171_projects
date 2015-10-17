/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project3;

import com.jogamp.opengl.util.gl2.GLUT;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    final static int NONE = 0;
    final static int XYPlane = 1;
    final static int XZPlane = 2;
    final static int YZPlane = 3;
    
    final static Color offColor = new Color(0.2f,0.2f,0.2f);
    final static Color onColor = new Color(0.7f,0.7f,0.7f);
    
    private ArrayList<Object> objects;
    private int projType;
    private int width, height;
    private float animationAngle;
    private boolean animation;
    private Point3Df[] rotateLine;
    
    // Lighting constants
    public Point3Df lightSource;
    public Point3Df from;
    public float k_a, k_d, k_s;
    public float i_a, i_l;
    public float k;
    public int phong;
    
    public boolean halftone;
    
    public Scene()
    {
        objects = new ArrayList<>();
        projType = Scene.NONE;
        animationAngle = 0.0f;
        animation = false;
        rotateLine = new Point3Df[2];
        rotateLine[0] = new Point3Df(-1.0f,-1.0f,-1.0f);
        rotateLine[1] = new Point3Df(1.0f,1.0f,1.0f);
        halftone = false;
        
        if(projType == XYPlane)
        {
            from = new Point3Df(0.0f,0.0f,1.0f);
        } else if(projType == XZPlane) {
            from = new Point3Df(0.0f,1.0f,0.0f);
        } else if(projType == YZPlane) {
            from = new Point3Df(1.0f,0.0f,0.0f);
        }
        
        from = new Point3Df(0.0f,0.0f,1.0f);
    }
    
    public Scene(int projType)
    {
        this();
        this.projType = projType;
    }
    
    public void addObject(Object p) {
        objects.add(p);
    }
    
    public ArrayList<Object> getObjects()
    {
        return objects;
    }
    
    public void setObjects(ArrayList<Object> objects)
    {
        this.objects = objects;
    }
    
    public void setRotateLine(Point3Df[] rotateLine)
    {
        this.rotateLine = rotateLine;
    }
    
    public void resetRotationAnimation()
    {
        animationAngle = 0.0f;
    }
            
    public void clearObjects() {
        objects.clear();
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
        Point3Df point = new Point3Df(0.0f,0.0f,0.0f,p.i_p);
        
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
    
    private Point2Df worldToScreen(Point2Df p)
    {
        return (halftone) ? new Point2Df(
                ((p.x + 1) * width * 0.5f)/3,
                ((p.y + 1) * height * 0.5f)/3,
                p.i_p) : new Point2Df(
                ((p.x + 1) * width * 0.5f),
                ((p.y + 1) * height * 0.5f),
                p.i_p);
    }
    
    // Projects a 3D point onto a 2D plane
    private Point2Df project(Point3Df point)
    {
        if(projType == XYPlane)
        {
            return new Point2Df(point.x, point.y, point.i_p);
        } else if(projType == XZPlane) {
            return new Point2Df(point.x, point.z, point.i_p);
        } else if(projType == YZPlane) {
            return new Point2Df(point.y, point.z, point.i_p);
        }
        
        return new Point2Df(point.x, point.y, point.i_p);
    }
    
    // Draws an individual pixel
    private void drawPixel(int x, int y,
            float[] pixelBuf, Color color)
    {
        int pos = y*width+x;
        
        if(x < width && y < height && x > 0 && y > 0)
        {
            pixelBuf[pos*3] = color.r;
            pixelBuf[pos*3+1] = color.g;
            pixelBuf[pos*3+2] = color.b;
        }
    }
    
    private void drawVirtualPixels(int x, int y, float[] pixelBuf, float i_p)
    {
        int pixels = Math.round((float)Math.floor(i_p*10));
        int count = 0;
        
        Color[] color = new Color[9];
        
        for(int i=0; i < 9; i++)
        {
            if(count < pixels)
            {
                color[i] = onColor;
                count++;
            }
            else
            {
                color[i] = offColor;
            }
        }
        
        for(int i=0; i < 3; i++)
        {
            for(int j=0; j < 3; j++)
            {
                drawPixel((x*3)+i, (y*3)+j, pixelBuf, color[(i*3)+j]);
            }
        }
    }
    
    private Point3Df computeNormal(Object o, Face face)
    {
        Point3Df a = o.getVertex(face.getVertexIndex(0));
        Point3Df b = o.getVertex(face.getVertexIndex(1));
        Point3Df c = o.getVertex(face.getVertexIndex(2));
        
        Point3Df ba = Point3Df.sub(b, a);
        Point3Df bc = Point3Df.sub(b, c);
        
        Point3Df normal = Point3Df.cross(ba, bc);
        normal.normalize();
        return normal;
    }
    
    private Point3Df computeReflection(Point3Df normal, Point3Df light)
    {
        return Point3Df.sub(
                Point3Df.multiply(
                        Point3Df.dot(normal,light)*2,
                        normal), light);
    }
    
    // Compute lighting intensity
    private void computePhong(Object o, Face face)
    {   
        Point3Df normal = computeNormal(o, face);
        
        float block1 = k_a * i_a;
        
        for(int i : face.getVertexIndexes())
        {   
            Point3Df p = o.getVertex(i);
            
            Point3Df temp = Point3Df.sub(lightSource, p);
            
            Point3Df lightVector =
                    Point3Df.multiply(
                            (1/temp.length()), temp);
            
            Point3Df reflectionVector = computeReflection(normal, lightVector);
            
            Point3Df fMinusP = Point3Df.sub(from, p);
            
            Point3Df viewVector =
                    Point3Df.multiply(
                            (1/temp.length()), fMinusP);
            
            float block2 = i_l / (fMinusP.length() + k);
            
            float lDotN = Point3Df.dot(lightVector, normal);
            
            float rDotV = Point3Df.dot(reflectionVector, viewVector);
            
            float block3a = k_d * lDotN;
                    
            float block3b = k_s * (float) Math.pow(rDotV, phong);
            
            float i_p = block1 + (block2 * (block3a + block3b));
            
            o.getVertex(i).i_p = i_p;
        }
        
    }
    
    // Draws a triangle/quad
    private void drawFace(Object o, Face face, float[] pixelBuf)
    {
        ArrayList<Point2Df> projected = new ArrayList<>();
        
        for(int i : face.getVertexIndexes())
        {
            Point2Df projected_vertex = 
                    project(o.getVertex(i));
            
            projected.add(worldToScreen(projected_vertex));
        }
        
        ArrayList<Point2Di> linePoints = 
                Bresenham.getLinePoints(
                        Bresenham.getLines(Clipping.clip(projected)));
        
        float l = linePoints.get(0).i_p;
        
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
                    Point2Di startP;
                    Point2Di finishP;
                    
                    startP = i2;
                    finishP = i1;
                    
                    if(i1.x <= i2.x)
                    {
                        startP = i1;
                        finishP = i2;
                    }
                    
                    for(int j=startP.x; j <= finishP.x; j++)
                    {
                        float i_p = l - (finishP.i_p-startP.i_p) *
                                (finishP.x-j)/(finishP.x - startP.x);
                        if(halftone)
                        {
                            drawVirtualPixels(j,i1.y,pixelBuf,i_p);
                        } else {
                            Color color =
                                    new Color(0.5f*i_p,0.5f*i_p,0.5f*i_p);
                            drawPixel(j,i1.y,pixelBuf,color);
                        }
                    }
                }
            } else {
                count = 1;
            }
        }
    }
    
    private void drawObject(Object o, float[] pixelBuf)
    {
        ArrayList<Face> faces = o.getFaces();
        
        ArrayList<Face> newFaces = new ArrayList<>();
        
        for(Face face : faces)
        {
            Point3Df normal = computeNormal(o,face);
            
            if(projType == XYPlane)
            {
                if(normal.z > 0) { newFaces.add(face); }
            } else if(projType == XZPlane) {
                if(normal.y > 0) { newFaces.add(face); }
            } else if(projType == YZPlane) {
                if(normal.x > 0) { newFaces.add(face); }
            }
        }
        
        faces = newFaces;
        
        for(Face face : faces)
        {
            computePhong(o, face);
        }
        
        // Normalize lighting intensities for object
        o.normalize_i_p();
        
        for(Face face : faces)
        {
            drawFace(o, face, pixelBuf);
        }
    }
    
    private void render(GLAutoDrawable drawable)
    {   
        GL2 gl = drawable.getGL().getGL2();
        
        float[] pixelBuf = new float[width*height*3];
        
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        
        // Painters algorithm
        Collections.sort(objects, new Comparator<Object>()
        {
            @Override
            public int compare(Object o1, Object o2)
            {
                if(projType == XYPlane)
                {
                    return (o2.maxZ == o1.maxZ) ? 0 : (o2.maxZ > o1.maxZ) ? -1 : 1;
                } else if(projType == XZPlane) {
                    return (o2.maxY == o1.maxY) ? 0 : (o2.maxY > o1.maxY) ? -1 : 1;
                } else if(projType == YZPlane) {
                    return (o2.maxX == o1.maxX) ? 0 : (o2.maxX > o1.maxX) ? -1 : 1;
                }
                
                return 0;
            }
        });
        
        for(Object object : objects)
        {
            Object newObject = new Object();
            newObject.setFaces(object.getFaces());
            
            for(Point3Df p : object.getVertexes())
            {
                newObject.addVertex(applyAnimationRotation(p));
            }
            
            drawObject(newObject, pixelBuf);
        }

        //drawAxis(gl);
        
        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
        
        gl.glDrawPixels(width,
                height,
                GL.GL_RGB,
                GL.GL_FLOAT,
                FloatBuffer.wrap(pixelBuf));
        
        gl.glFlush();
    }
    
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
    {
        this.width = width;
        this.height = height;
        Clipping.setClippingPlane(0, width, 0, height);
        //System.out.println(projType + " - " + new Point2Di(width,height));
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

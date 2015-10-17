/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project2;

import java.util.ArrayList;

class Edge
{
    public int p1, p2;
    public Color color;
    
    public Edge(int p1, int p2)
    {
        this.p1 = p1;
        this.p2 = p2;
        this.color = new Color((float)Math.random(),
                            (float)Math.random(), 
                            (float)Math.random());
    }
    
    public Edge(int p1, int p2, Color color)
    {
        this.p1 = p1;
        this.p2 = p2;
        this.color = color;
    }
}

class Point2Di
{
    public float x,y;
    
    public Point2Di(float x, float y)
    {
        this.x = x;
        this.y = y;
    }
}

class Point2Df
{
    public float x,y;
    
    public Point2Df(float x, float y)
    {
        this.x = x;
        this.y = y;
    }
    
    public boolean equals(Point3Df a)
    {
        if(a.x == x && a.y == y)
        {
            return true;
        }
            
        return false;
    }
}

class Point3Df
{
    public float x,y,z;
    
    public Point3Df(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public boolean equals(Point3Df a)
    {
        if(a.x == x && a.y == y && a.z == z)
        {
            return true;
        }
            
        return false;
    }
    
    public String toString()
    {
        return x + ", " + y + ", " + z;
    }
    
    public void normalize()
    {
        float length = (float) Math.sqrt(x*x + y*y + z*z);
        x = x/length;
        y = y/length;
        z = z/length;
    }
}

public class Polygon {
    private ArrayList<Point3Df> points;
    private ArrayList<Edge> edges;
    private String name;
    private boolean selected;
    private Point3Df centerPoint;
    private int numPoints;
    private float maxX, maxY, minX, minY, minZ, maxZ;
    
    public Polygon()
    {
        init();
    }
    
    public Polygon(String name)
    {
        init();
        this.name = name;
    }
    
    public void printPoints()
    {
        System.out.println("[" + name + "]");
        for(Point3Df p : points)
        {
            System.out.println(p);
        }
    }    
    
    private void init()
    {
        maxZ = 0.0f;
        minZ = 0.0f;
        maxX = 0.0f;
        maxY = 0.0f;
        minY = 0.0f;
        minX = 0.0f;
        numPoints = 0;
        selected = false;
        points = new ArrayList<Point3Df>();
        edges = new ArrayList<Edge>();
    }
    
    public boolean getSelected()
    {
        return selected;
    }
    
    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }
    
    public void addPoint(Point3Df p) {
        points.add(p);
    }
    
    public void addEdge(Edge e) {
        edges.add(e);
    }
    
    // Recalculates number of points, center point, and polygon boundries
    // @ref http://gis.stackexchange.com/questions/77425/how-to-calculate-centroid-of-a-polygon-defined-by-a-list-of-longitude-latitude-p
    public void recalc()
    {
        numPoints = points.size();
        
        float x = 0.0f;
        float y = 0.0f;
        float z = 0.0f;
        
        minZ = points.get(0).z;
        maxZ = points.get(0).z;
        minY = points.get(0).y;
        maxY = points.get(0).y;
        minX = points.get(0).x;
        maxX = points.get(0).x;
        
        for(Point3Df p : getPoints())
        {
            x += p.x;
            y += p.y;
            z += p.z;
            
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
            if(p.z > maxZ)
            {
                maxZ = p.z;
            }
            if(p.y < minZ)
            {
                minZ = p.z;
            }
        }
        
        /*
        System.out.print(getName() + " ");
        System.out.print(minX + "," + maxX + " ");
        System.out.print(minY + "," + maxY + " ");
        System.out.println(minZ + "," + maxZ + " ");
        */
        
        centerPoint = new Point3Df(x/numPoints, y/numPoints, z/numPoints);
    }
    
    public ArrayList<Point3Df> getPoints() {
        return this.points;
    }
    
    public Point3Df getPoint(int i)
    {
        return points.get(i);
    }
    
    public ArrayList<Edge> getEdges() {
        return this.edges;
    }
    
    public void setEdges(ArrayList<Edge> edges) {
        this.edges = edges;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String toString()
    {
        return getName();
    }
    
    public boolean pointExists(Point3Df point)
    {
        for(Point3Df p : getPoints())
        {
            if(p.equals(point))
            {
                return true;
            }
        }
        
        return false;
    }
    
    public float getMaxZ()
    {
        return maxZ;
    }
    
    public float getMinZ()
    {
        return minZ;
    }
    
    public float getMaxY()
    {
        return maxY;
    }
    
    public float getMinY()
    {
        return minY;
    }
    
    public float getMaxX()
    {
        return maxX;
    }
    
    public float getMinX()
    {
        return minX;
    }
    
    public Point3Df getCenter()
    {
        return centerPoint;
    }
    
    // http://inside.mines.edu/fs_home/gmurray/ArbitraryAxisRotation/
    public void rotate(Point3Df pa, Point3Df pb, double angle)
    {   
        float u = pb.x - pa.x;
        float v = pb.y - pa.y;
        float w = pb.z - pa.z;
        
        float length = (float) Math.sqrt(u*u + v*v + w*w);
        
        u = u/length;
        v = v/length;
        w = w/length;
        
        float a = pa.x;
        float b = pa.y;
        float c = pa.z;
        
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        
        recalc();
        
        Point3Df center = new Point3Df(0.0f,0.0f,0.0f);
        
        for(Point3Df p : getPoints())
        {
            float tempX = p.x - center.x;
            float tempY = p.y - center.y;
            float tempZ = p.z - center.z;
            
            float x = tempX;
            float y = tempY;
            float z = tempZ;
            
            tempX = (a*(v*v+w*w) - u*(b*v+c*w-u*x-v*y-w*z))*(1-cos) +
                    (x*cos) + (-c*v+b*w-w*y+v*z)*sin;
            
            tempY = (b*(u*u+w*w) - v*(a*u+c*w-u*x-v*y-w*z))*(1-cos) +
                    (y*cos) + (c*u-a*w+w*x-u*z)*sin;
            
            tempZ = (c*(u*u+v*v) - w*(a*u+b*v-u*x-v*y-w*z))*(1-cos) +
                    (z*cos) + (-b*u+a*v-v*x+u*y)*sin;
            
            p.x = tempX + center.x;
            p.y = tempY + center.y;
            p.z = tempZ + center.z;
        }
        
        recalc();
    }
    
    public void translate(Point3Df vector)
    {
        recalc();
        
        vector.normalize();
        vector.x = vector.x * (maxX - minX);
        vector.y = vector.y * (maxY - minY);
        vector.z = vector.z * (maxZ - minZ);
        
        for(Point3Df p : getPoints())
        {
            float tempX = p.x - getCenter().x;
            float tempY = p.y - getCenter().y;
            float tempZ = p.z - getCenter().z;
            
            tempX += vector.x;
            tempY += vector.y;
            tempZ += vector.z;
            
            p.x = tempX + getCenter().x;
            p.y = tempY + getCenter().y;
            p.z = tempZ + getCenter().z;
        }
        
        recalc();
    }
    
    public void scale(Point3Df factor)
    {
        recalc();
        
        for(Point3Df p : getPoints())
        {
            float tempX = p.x - getCenter().x;
            float tempY = p.y - getCenter().y;
            float tempZ = p.z - getCenter().z;
            
            tempX = tempX*factor.x;
            tempY = tempY*factor.y;
            tempZ = tempZ*factor.z;
            
            p.x = tempX + getCenter().x;
            p.y = tempY + getCenter().y;
            p.z = tempZ + getCenter().y;
        }
        
        recalc();
    }
}
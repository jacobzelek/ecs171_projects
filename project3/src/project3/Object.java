/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project3;

import java.util.ArrayList;

class Face
{
    public ArrayList<Integer> vertexIndexes;
    public float maxX, maxY, minX, minY, minZ, maxZ;
    
    public Face()
    {
        vertexIndexes = new ArrayList<>();
        maxZ = 0.0f;
        minZ = 0.0f;
        maxX = 0.0f;
        maxY = 0.0f;
        minY = 0.0f;
        minX = 0.0f;
    }

    public void addVertexIndex(int i)
    {
        vertexIndexes.add(i);
    }
    
    public ArrayList<Integer> getVertexIndexes()
    {
        return vertexIndexes;
    }
    
    public int getVertexIndex(int i)
    {
        return vertexIndexes.get(i);
    }
    
    public String toString()
    {
        String str = "[Face]\n";
        
        for(int i : vertexIndexes)
        {
            str += i + " ";
        }
        str += "\n";
        
        return str;
    }
}

public class Object
{
    private ArrayList<Point3Df> vertexes;
    private ArrayList<Face> faces;
    private String name;
    private boolean selected;
    private Point3Df centerPoint;
    private int numPoints;
    public float maxX, maxY, minX, minY, minZ, maxZ;
    
    public Object()
    {
        init();
    }
    
    public Object(String name)
    {
        init();
        this.name = name;
    }
    
    public void printPoints()
    {
        System.out.println("[" + name + "]");
        for(Point3Df p : vertexes)
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
        vertexes = new ArrayList<Point3Df>();
        faces = new ArrayList<Face>();
    }
    
    public boolean getSelected()
    {
        return selected;
    }
    
    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }
    
    public void addVertex(Point3Df p) {
        vertexes.add(p);
    }
    
    public void addFace(Face f) {
        faces.add(f);
    }
    
    // Recalculates number of points, center point, and polygon boundries
    // @ref http://gis.stackexchange.com/questions/77425/how-to-calculate-centroid-of-a-polygon-defined-by-a-list-of-longitude-latitude-p
    public void recalc()
    {
        numPoints = vertexes.size();
        
        float x = 0.0f;
        float y = 0.0f;
        float z = 0.0f;
        
        minZ = vertexes.get(0).z;
        maxZ = vertexes.get(0).z;
        minY = vertexes.get(0).y;
        maxY = vertexes.get(0).y;
        minX = vertexes.get(0).x;
        maxX = vertexes.get(0).x;
        
        for(Point3Df p : getVertexes())
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
        
        // Calculate face mins and maxes
        for(Face face : faces)
        {
            float fminZ = vertexes.get(face.vertexIndexes.get(0)).z;
            float fmaxZ = vertexes.get(face.vertexIndexes.get(0)).z;
            float fminY = vertexes.get(face.vertexIndexes.get(0)).y;
            float fmaxY = vertexes.get(face.vertexIndexes.get(0)).y;
            float fminX = vertexes.get(face.vertexIndexes.get(0)).x;
            float fmaxX = vertexes.get(face.vertexIndexes.get(0)).x;
            
            for(int vertexIndex : face.getVertexIndexes())
            {
                Point3Df p = vertexes.get(vertexIndex);
                
                if(p.x > fmaxX)
                {
                    fmaxX = p.x;
                }
                if(p.x < fminX)
                {
                    fminX = p.x;
                }
                if(p.y > fmaxY)
                {
                    fmaxY = p.y;
                }
                if(p.y < fminY)
                {
                    fminY = p.y;
                }
                if(p.z > fmaxZ)
                {
                    fmaxZ = p.z;
                }
                if(p.y < fminZ)
                {
                    fminZ = p.z;
                }
            }
            
            face.maxX = fmaxX;
            face.maxY = fmaxY;
            face.maxZ = fmaxZ;
            face.minX = fminX;
            face.minY = fminY;
            face.minZ = fminZ;
        }
        
        /*
        System.out.print(getName() + " ");
        System.out.print(minX + "," + maxX + " ");
        System.out.print(minY + "," + maxY + " ");
        System.out.println(minZ + "," + maxZ + " ");
        */
        
        centerPoint = new Point3Df(x/numPoints, y/numPoints, z/numPoints);
    }
    
    public ArrayList<Point3Df> getVertexes() {
        return this.vertexes;
    }
    
    public Point3Df getVertex(int i)
    {
        return vertexes.get(i);
    }
    
    public ArrayList<Face> getFaces() {
        return this.faces;
    }
    
    public Face getFace(int i)
    {
        return this.faces.get(i);
    }
    
    public void setFaces(ArrayList<Face> faces) {
        this.faces = faces;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String toString()
    {
        return getName();
    }
    
    public boolean vertexExists(Point3Df point)
    {
        for(Point3Df p : getVertexes())
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
        
        for(Point3Df p : getVertexes())
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
        
        for(Point3Df p : getVertexes())
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
        
        for(Point3Df p : getVertexes())
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
    
    public void normalize_i_p()
    {
        float max_i_p = vertexes.get(0).i_p;
        float min_i_p = vertexes.get(0).i_p;
        
        for(Point3Df p : vertexes)
        {
            if(max_i_p < p.i_p)
            {
                max_i_p = p.i_p;
            }
            
            if(max_i_p > p.i_p)
            {
                min_i_p = p.i_p;
            }
        }
        
        float span = max_i_p - min_i_p;
        
        for(Point3Df p : vertexes)
        {
            p.i_p = p.i_p / span;
        }
    }
}
package project4;

class Point2Di
{
    public int x,y;
    public boolean selected;
    
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
    
    public Point2Df toPoint2Df()
    {
        return new Point2Df(x,y);
    }
    
    public String toString()
    {
        return "(" + this.x + ", " + this.y + ")";
    }
    
    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }
    
    public boolean isSelected() { return this.selected; }
}

class Point2Df
{
    public float x,y;
    
    public Point2Df(float x, float y)
    {
        this.x = x;
        this.y = y;
    }
    
    public Point2Df(Point2Df p)
    {
        this.x = p.x;
        this.y = p.y;
    }
    
    public boolean equals(Point3Df a)
    {
        if(a.x == x && a.y == y)
        {
            return true;
        }
            
        return false;
    }
    
    public Point2Di toPoint2Di()
    {
        return new Point2Di(Math.round(x),Math.round(y));
    }
    
    public String toString()
    {
        return "(" + this.x + ", " + this.y + ")";
    }
}

public class Point3Df
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
    
    public static Point3Df sub(Point3Df b, Point3Df a)
    {
        return new Point3Df(b.x-a.x, b.y-a.y, b.z-a.z);
    }
    
    public static float dot(Point3Df b, Point3Df a)
    {
        return b.x*a.x + b.y*a.y + b.z*a.z;
    }
    
    public static Point3Df multiply(float c, Point3Df a)
    {
        return new Point3Df(c*a.x, c*a.y, c*a.z);
    }
    
    public static Point3Df cross(Point3Df a, Point3Df b)
    {
        return new Point3Df(a.y*b.z - a.z*b.y,
                a.z*b.x - a.x*b.z,
                a.x*b.y - a.y*b.x);
    }
    
    public float length()
    {
        return (float) Math.sqrt(x*x + y*y + z*z);
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
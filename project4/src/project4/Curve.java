package project4;

import java.util.ArrayList;

// @ref http://math.stackexchange.com/questions/43947/casteljaus-algorithm-practical-example
class Casteljau
{
    private double[] x;
    private double[] y;
    private int n;

    private double[][] b;

    public Casteljau(double[] x, double[] y, int n) {
        //require x.length = y.length = n
        this.x = x;
        this.y = y;
        this.n = n;
        this.b = new double[n][n];
    }

    private void init(double[] initialValues) {
        for(int i = 0; i < n; i++) {
            b[0][i] = initialValues[i];
        }
    }

    private double evaluate(double t, double[] initialValues) {
        init(initialValues);
        for (int j = 1; j < n; j++) {
            for (int i = 0; i < n - j; i++) {
                b[j][i] = b[j-1][i] * (1-t) + b[j-1][i+1] * t;
            }
        }
        return(b[n-1][0]);
    }

    public Point2Di getXYvalues(double t) {
        double xVal = evaluate(t, x);
        double yVal = evaluate(t, y);
        return new Point2Di((int)Math.round(xVal),
            (int)Math.round(yVal));
    }
}


// @ref https://chi3x10.wordpress.com/2009/10/18/de-boor-algorithm-in-c/
// @ref https://github.com/SirVer/cagd5e/blob/master/ccode/deboor.c
class deBoor
{
    public static int getInterval(float x, float[] knot, int ti)
    {
        int index = 0;

        for(int i = 1; i <= ti - 1; i++)
        {
            if(x < knot[i])
            {
                index = i - 1;
                break;
            }
        }

        if(Math.round(x) == Math.round(knot[ti - 1])) {
            index = ti - 1;
        }

        return index;
    }
    
    public static float evaluate(
        int degree, float[] coeff, float[] knot, float u)
    {
        int i = deBoor.getInterval(u, knot, knot.length);
        int k,j;
        float t1,t2;
        float[] coeffa = new float[255];
        for(j=i-degree+1; j<=i+1; j++)
        {
            if(j < coeff.length)
            {
                coeffa[j]=coeff[j];
            }
        }
        
        for (k=1; k<= degree; k++)
        {
            for (j=i+1 ;j>=i-degree+k+1; j--)
            {
                t1= (knot[j+degree-k] - u )/(knot[j+degree-k]-knot[j-1]);
                t2= 1.0f-t1;
                coeffa[j]=t1* coeffa[j-1]+t2* coeffa[j];
            }
        }
        
        return coeffa[i+1];
    }
}

/**
 *
 * @author Jacob Zelek
 */
public class Curve
{
    public static final int BEIZER = 0;
    public static final int BSPLINE = 1;
    
    private ArrayList<Point2Di> points;
    private int type;
    
    private String name;
    private boolean selected;
    
    private int k;
    
    private float[] knots;
    
    private Color color;
    
    public Curve(int type)
    {
        this.points = new ArrayList<Point2Di>();
        this.type = type;
        this.color = Color.random();
        this.k = 2;
        this.knots = new float[2];
        this.knots[0] = 0.0f;
        this.knots[1] = 1.0f;
    }
    
    public Curve(String name, int type)
    {
        this(type);
        this.name = name;
    }
    
    public void addPoint(Point2Di p)
    {
        points.add(p);
    }
    
    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }
    
    public void setKnots(float[] knots) { this.knots = knots; }
    
    public float[] getKnots() { return this.knots; }
    
    public Color getColor() { return color; }
    
    public boolean isSelected() { return this.selected; }
    
    public int getType() { return this.type; }
    
    public String getName() { return this.name; }
    
    public ArrayList<Point2Di> getPoints() { return this.points; }
    
    public Point2Di getPoint(int i) { return this.points.get(i); }
    
    public void removeIndex(int i)
    {
        ArrayList<Point2Di> newPoints = new ArrayList<>();
        
        for(int j=0; j < points.size(); j++)
        {
            if(i != j)
            {
                newPoints.add(points.get(i));
            }
        }
        
        this.points = newPoints;
    }
    
    public void insertPoint(int i, Point2Di p) { this.points.add(i, p);  }
    
    public void setK(int k) { this.k = k; }
    
    public int getK() { return this.k; }
    
    public String toString()
    {
        String typeName = (type == 0) ? "Beizer" : "BSpline";
        
        return "[" + typeName + "] " + this.name;
    }
    
    public ArrayList<Point2Di> getBSplinePoints(int n)
    {
        ArrayList<Point2Di> bspline = new ArrayList<Point2Di>();
        int size = points.size();
        
        if(size > 2 && knots.length >= size &&
            knots.length >= this.k && n > 0)
        {
            float[] xs = new float[size];
            float[] ys = new float[size];
            
            for(int i=0; i < size; i++)
            {
                xs[i] = points.get(i).x;
                ys[i] = points.get(i).y;
            }
            
            float u = knots[this.k-1];
            float finish = knots[size-1];
            float inc = ((float)finish-(float)u)/(float)n;
        
            while(u <= finish)
            {
                float x = deBoor.evaluate(k, xs, knots, u);
                float y = deBoor.evaluate(k, ys, knots, u);
                Point2Df p = new Point2Df(x,y);
                bspline.add(p.toPoint2Di());
                u += inc;
            }
        }
        
        return bspline;
    }
    
    public ArrayList<Point2Di> getBezierPoints(int n)
    {
        ArrayList<Point2Di> bezier = new ArrayList<Point2Di>();
        int size = points.size();
        
        if(size > 2 && n > 0)
        {
            double[] x = new double[size];
            double[] y = new double[size];

            for(int i=0; i < size; i++)
            {
                Point2Di p = points.get(i);
                x[i] = p.x;
                y[i] = p.y;
            }

            Casteljau c = new Casteljau(x,y,size);

            double t = 0.0;

            while(t <= 1.0)
            {
                Point2Di p = c.getXYvalues(t);
                bezier.add(p);
                t += (double)1/n;
            }
        }
        
        return bezier;
    }
    
    public static float[] parseKnotsStr(String knotsStr)
    {
        String[] knotStrs = knotsStr.split(",");
        float[] knotsRet = new float[knotStrs.length];

        int count = 0;
        for(String str : knotStrs)
        {
            knotsRet[count] = Float.valueOf(str);
            count++;
        }
        
        return knotsRet;
    }
    
    public static String getKnotsStr(float[] knots)
    {
        String ret = "";
        for(int i=0; i < knots.length-1; i++)
        {
            ret += knots[i] + ",";
        }
        ret += knots[knots.length-1];
        
        return ret;
    }
}

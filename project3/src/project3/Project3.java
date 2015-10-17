/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project3;

import com.jogamp.opengl.util.Animator;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;
import javax.media.opengl.*;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Jacob Zelek
 */
public class Project3
{
    private static Scene scene[] = new Scene[4];
    private static GLCanvas canvas[] = new GLCanvas[3];
    private static Animator animator[] = new Animator[3];
    private static ArrayList<Object> objects;
    private static Point3Df[] rotateLine = new Point3Df[2]; 
    
    // Lighting constants
    public static Point3Df lightSource;
    public static float k_a, k_d, k_s;
    public static float i_a, i_l;
    public static float k;
    public static int phong;
    
    public static boolean halftone = false;
    
    // The list model that contains the polygons we're working with
    private static DefaultListModel listModel;
    
    // The file that we're working witht that contains our polygons
    private static File saveFile;
    
    // The title of the application
    private static String appTitle = "ECS175 Project 3";
    
    private static void rebound()
    {
        float f = 0.0f;
        float n = 0.0f;
        float b = 0.0f;
        float t = 0.0f;
        float l = 0.0f;
        float r = 0.0f;
        
        for(Object poly : objects)
        {
            poly.recalc();
            
            if(poly.getMinZ() < f) { f = poly.getMinZ(); }
            if(poly.getMaxZ() > n) { n = poly.getMaxZ(); }
            if(poly.getMinY() < b) { b = poly.getMinY(); }
            if(poly.getMaxY() > t) { t = poly.getMaxY(); }
            if(poly.getMinX() < l) { l = poly.getMinX(); }
            if(poly.getMaxX() > r) { r = poly.getMaxX(); }
        }
        
        float maxSpan = Math.max(Math.max(f-n, t-b), r-l);
        
        for(Object poly : objects)
        {
            for(Point3Df p : poly.getVertexes())
            {
                p.x = p.x * (2/maxSpan) - (r+l)/(r-l);
                p.y = p.y * (2/maxSpan) - (t+b)/(t-b);
                p.z = p.z * (2/maxSpan) - (n+f)/(n-f);
            }
            
            poly.recalc();
        }
    }
    
    private static void refreshCanvas()
    {   
        for(int i=0; i < 3; i++)
        {
            scene[i].lightSource = lightSource;
            scene[i].k_a = k_a;
            scene[i].k_d = k_d;
            scene[i].k_s = k_s;
            scene[i].i_a = i_a;
            scene[i].i_l = i_a;
            scene[i].k = k;
            scene[i].phong = phong;
            scene[i].halftone = halftone;
        }
                
        scene[0].setObjects(objects);
        scene[1].setObjects(objects);
        scene[2].setObjects(objects);
        
        scene[0].setRotateLine(rotateLine);
        scene[1].setRotateLine(rotateLine);
        scene[2].setRotateLine(rotateLine);
        
        canvas[0].display();
        canvas[1].display();
        canvas[2].display();
    }
    
    // This refreshes the list model, the one that displays the
    // polygons we're working on the left side of the frame
    private static void refreshPolygons()
    {
        listModel.clear();
        
        for(Object p : objects)
        {   
            if(!listModel.contains(p))
            {
                listModel.addElement(p);
            }
        }
    }
    
    private static void open(File file)
    {
        if(saveFile.exists())
        {
            objects.clear();
            
            try {
                Scanner fileIn = new Scanner(new FileReader(saveFile));
                
                do
                {
                    if(!fileIn.hasNextLine())
                    {
                        break;
                    }
                    
                    String name = fileIn.nextLine();

                    int size = Integer.valueOf(fileIn.nextLine());
                    
                    Object o = new Object(name);
                    
                    for(int i=0; i < size; i++)
                    {
                        String line = fileIn.nextLine();
                        String[] pointParts = line.split(",");
                        float x = Float.valueOf(pointParts[0]);
                        float y = Float.valueOf(pointParts[1]);
                        float z = Float.valueOf(pointParts[2]);
                        
                        o.addVertex(new Point3Df(x,y,z));
                    }
                    
                    size = Integer.valueOf(fileIn.nextLine());
                    
                    for(int i=0; i < size; i++)
                    {
                        Face face = new Face();
                        
                        String line = fileIn.nextLine();
                        String[] faceIndexes = line.split(",");
                        
                        for(int j=0; j < faceIndexes.length; j++)
                        {
                            face.addVertexIndex(
                                Integer.valueOf(faceIndexes[j]));
                        }
                        
                        o.addFace(face);
                    }
                    
                    objects.add(o);
                } while(true);
                
                rebound();
                
                fileIn.close();       
            } catch (FileNotFoundException ex) {
                // Can't find file
            }
        } else {
            // File doesn't exist
        }
    }
    
    // Loads polygon from Ply file
    // @ref http://people.sc.fsu.edu/~jburkardt/data/ply/ply.html
    // @todo Check for Ply file type ascii/binary
    // @todo Check for datatypes as per spec
    private static void openPLY(File ply)
    {
        if(ply.exists())
        {   
            try {
                Scanner fileIn = new Scanner(new FileReader(ply));
                
                int vertexSize = 0;
                int faceSize = 0;
                
                do
                {
                    if(!fileIn.hasNextLine())
                    {
                        break;
                    } else {
                        String line = fileIn.nextLine();
                        
                        String[] lineParts = line.split("\\s+");
                        
                        if(lineParts[0].equals("element"))
                        {
                            if(lineParts[1].equals("vertex"))
                            {
                                vertexSize = Integer.valueOf(lineParts[2]);
                            }
                            else if(lineParts[1].equals("face"))
                            {
                                faceSize = Integer.valueOf(lineParts[2]);
                            }
                        } else if(lineParts[0].equals("end_header")) {
                            break;
                        }
                    }
                } while(true);

                Object o = new Object(ply.getName());

                for(int i=0; i < vertexSize; i++)
                {
                    String line = fileIn.nextLine();
                    String[] pointParts = line.split("\\s+");
                    float x = Float.valueOf(pointParts[0]);
                    float y = Float.valueOf(pointParts[1]);
                    float z = Float.valueOf(pointParts[2]);

                    o.addVertex(new Point3Df(x,y,z));
                }
                
                for(int i=0; i < faceSize; i++)
                {
                    String line = fileIn.nextLine();
                    String[] edgeParts = line.split("\\s+");
                    
                    int edgeComp = Integer.valueOf(edgeParts[0]);
                    
                    Face face = new Face();
                    
                    for(int j=1; j <= edgeComp; j++)
                    {
                        int index = Integer.valueOf(edgeParts[j]);

                        face.addVertexIndex(index);
                    }
                    
                    o.addFace(face);
                }

                objects.add(o);
                
                rebound();
                
                fileIn.close();       
            } catch (FileNotFoundException ex) {
                // Can't find file
            }
        } else {
            // File doesn't exist
        }
    }
    
    private static void save()
    {
        if(!saveFile.exists())
        {
            try {
                saveFile.createNewFile();
            } catch (IOException ex) {
                // Can't create file
            }
        }

        try {
            PrintStream fileOut = new PrintStream(saveFile);
            for(Object o : objects)
            {
                fileOut.println(o.getName());
                fileOut.println(o.getVertexes().size());
                for(Point3Df q : o.getVertexes())
                {
                    fileOut.print(q.x);
                    fileOut.print(",");
                    fileOut.print(q.y);
                    fileOut.print(",");
                    fileOut.println(q.z);
                }
                
                fileOut.println(o.getFaces().size());
                for(Face q : o.getFaces())
                {
                    for(int i=0; i < q.getVertexIndexes().size()-1; i++)
                    {
                        fileOut.print(q.getVertexIndex(i));
                        fileOut.print(",");
                    }
                    
                    fileOut.println(
                            q.getVertexIndex(
                                    q.getVertexIndexes().size()-1));
                }
            }
            fileOut.close();
        } catch (FileNotFoundException ex) {
           // Can't find file
        }
    }
    
    public static void main(String[] args)
    {
        objects = new ArrayList<Object>();
        rotateLine[0] = new Point3Df(-1.0f,-1.0f,-1.0f);
        rotateLine[1] = new Point3Df(1.0f,1.0f,1.0f);
        
        lightSource = new Point3Df(0.0f,1.0f,0.0f);
        k_a = 0.3f;
        k_d = 0.3f;
        k_s = 0.3f;
        i_a = 0.3f;
        i_l = 0.3f;
        k = 0.3f;
        phong = 1;
        
        // Prepare all our OpenGL canvas
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        
        canvas[0] = new GLCanvas(caps);
        canvas[1] = new GLCanvas(caps);
        canvas[2] = new GLCanvas(caps);
        
        // Prepare the main frame for the application
        JFrame frame = new JFrame(appTitle);
        
        // The file chooser for opening and saving files
        final JFileChooser fc = new JFileChooser();
        
        // Since the app started, set the save file to null
        saveFile = null;
        
        // This is our scene, which contains our polygons and rendering functions
        scene[0] = new Scene(Scene.XZPlane);
        scene[1] = new Scene(Scene.YZPlane);
        scene[2] = new Scene(Scene.XYPlane);
        
        // This is our list model and JList that will allow us to select the
        // polygons we wish to transform
        listModel = new DefaultListModel();
        JList listPolys = new JList(listModel);
        listPolys.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listPolys.setLayoutOrientation(JList.VERTICAL);
        listPolys.setVisibleRowCount(-1);
        JScrollPane listScrollerPolys = new JScrollPane(listPolys);
        refreshPolygons();
        
        // This listener waits for selections in the JList and updates the polygons
        // in the scene with "selected" markers
        listPolys.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(e.getValueIsAdjusting() == false)
                {
                    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                    
                    int minIndex = lsm.getMinSelectionIndex();
                    int maxIndex = lsm.getMaxSelectionIndex();
                    
                    for(Object p : objects)
                    {
                        p.setSelected(false);
                    }
                    
                    for(int i=minIndex; i <= maxIndex; i++)
                    {
                        if(lsm.isSelectedIndex(i))
                        {
                            ((Object)listPolys.getModel().getElementAt(i)).setSelected(true);
                        }
                    }
                    
                    refreshCanvas();
                }
            }
        });
        
        JSplitPane oneTwo = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvas[0], canvas[1]);
        JSplitPane threeFour = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvas[2], listScrollerPolys);
        
        oneTwo.setDividerLocation(0.50);
        threeFour.setDividerLocation(0.50);
        
        JSplitPane canvases = new JSplitPane(JSplitPane.VERTICAL_SPLIT, oneTwo, threeFour);
        
        canvases.setDividerLocation(0.50);
        
        // Main menu for this application
        JMenuBar menu = new JMenuBar();
        
        JMenu menuFile = new JMenu("File");
        JMenuItem menuOpen = new JMenuItem(new AbstractAction("Open") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showOpenDialog(frame);

                if(returnVal == JFileChooser.APPROVE_OPTION)
                {
                    saveFile = fc.getSelectedFile();
                    
                    open(fc.getSelectedFile());
                    
                    refreshPolygons();
                    
                    refreshCanvas();
                    
                    frame.setTitle(appTitle + " - " + saveFile.getAbsolutePath());
                }
            }
        });
        
        JMenuItem menuOpenPly = new JMenuItem(new AbstractAction("Open Ply") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showOpenDialog(frame);

                if(returnVal == JFileChooser.APPROVE_OPTION)
                {   
                    openPLY(fc.getSelectedFile());
                    
                    refreshPolygons();
                    
                    refreshCanvas();
                }
            }
        });
        
        JMenuItem menuSave = new JMenuItem(new AbstractAction("Save") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(saveFile != null)
                {
                    save();
                } else {
                    int returnVal = fc.showSaveDialog(frame);
                    
                    if(returnVal == JFileChooser.APPROVE_OPTION)
                    {
                        saveFile = fc.getSelectedFile();

                        save();
                        
                        frame.setTitle(appTitle + " - " + saveFile.getAbsolutePath());
                    }
                }
            }
        });
                
        JMenuItem menuSaveAs = new JMenuItem(new AbstractAction("Save As") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showSaveDialog(frame);
                
                if(returnVal == JFileChooser.APPROVE_OPTION)
                {
                    saveFile = fc.getSelectedFile();
                    
                    save();
                    
                    frame.setTitle(appTitle + " - " + saveFile.getAbsolutePath());
                }
            }
        });
        
        JMenuItem menuExit = new JMenuItem(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        
        menuFile.add(menuOpen);
        menuFile.add(menuOpenPly);
        menuFile.add(menuSave);
        menuFile.add(menuSaveAs);
        menuFile.add(menuExit);
        menuFile.getPopupMenu().setLightWeightPopupEnabled(false);
        
        JMenu menuLighting = new JMenu("Lighting");
        JMenuItem menuSetSource = new JMenuItem(new AbstractAction("Set Light Source") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String vector = (String) JOptionPane.showInputDialog(
                    frame,
                    "Light Origin Point: x, y, z",
                    lightSource.toString()
                    );
                
                String[] vectorParts = vector.split(",", 3);
                float x = Float.valueOf(vectorParts[0].trim());
                float y = Float.valueOf(vectorParts[1].trim());
                float z = Float.valueOf(vectorParts[2].trim());
                
                lightSource = new Point3Df(x,y,z);      
                
                refreshCanvas();
            }
        });
        
        JMenuItem menuSetCoefficents = new JMenuItem(new AbstractAction("Set Coefficents") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String vector = (String) JOptionPane.showInputDialog(
                    frame,
                    "Ambiant, Diffuse, Specular : k_a, k_d, k_s",
                    k_a + ", " + k_d + ", " + k_s
                    );
                
                String[] vectorParts = vector.split(",", 3);
                k_a = Float.valueOf(vectorParts[0].trim());
                k_d = Float.valueOf(vectorParts[1].trim());
                k_s = Float.valueOf(vectorParts[2].trim());

                refreshCanvas();
            }
        });
        
        JMenuItem menuSetIntensities = new JMenuItem(new AbstractAction("Set Intensities") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String vector = (String) JOptionPane.showInputDialog(
                    frame,
                    "Ambiant Light Intensity, Light Source Intensity, Specular : i_a, i_l",
                    i_a + "," + i_l
                    );
                
                String[] vectorParts = vector.split(",", 2);
                i_a = Float.valueOf(vectorParts[0].trim());
                i_l = Float.valueOf(vectorParts[1].trim());

                refreshCanvas();
            }
        });
        
        JMenuItem menuSetPhong = new JMenuItem(new AbstractAction("Set Phong Coefficent") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String vector = (String) JOptionPane.showInputDialog(
                    frame,
                    "Phone Constant : n",
                    phong
                    );
                
                phong = Integer.valueOf(vector);

                refreshCanvas();
            }
        });
        
        JMenuItem menuSetK = new JMenuItem(new AbstractAction("Set K") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String vector = (String) JOptionPane.showInputDialog(
                    frame,
                    "Average Distance : K",
                    k
                    );
                
                k = Float.valueOf(vector);

                refreshCanvas();
            }
        });
        
        menuLighting.add(menuSetSource);
        menuLighting.add(menuSetCoefficents);
        menuLighting.add(menuSetIntensities);
        menuLighting.add(menuSetPhong);
        menuLighting.add(menuSetK);
        menuLighting.getPopupMenu().setLightWeightPopupEnabled(false);
        
        JMenu menuTransform = new JMenu("Transform");
        JMenuItem menuTranslate = new JMenuItem(new AbstractAction("Translate") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String vector = (String) JOptionPane.showInputDialog(
                    frame,
                    "Input Translation Vector: x,y,z"
                    );
                
                String[] vectorParts = vector.split(",", 3);
                float x = Float.valueOf(vectorParts[0]);
                float y = Float.valueOf(vectorParts[1]);
                float z = Float.valueOf(vectorParts[2]);
                
                for(Object p : objects)
                {   
                    if(p.getSelected())
                    {
                        p.translate(new Point3Df(x,y,z));
                        rebound();
                    }
                }
                
                refreshCanvas();
            }
        });
        
        JMenuItem menuScale = new JMenuItem(new AbstractAction("Scale") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String vector = (String) JOptionPane.showInputDialog(
                    frame,
                    "Input Scale Factor: x,y,z"
                    );
                
                String[] vectorParts = vector.split(",", 3);
                float x = Float.valueOf(vectorParts[0]);
                float y = Float.valueOf(vectorParts[1]);
                float z = Float.valueOf(vectorParts[2]);
                
                for(Object p : objects)
                {   
                    if(p.getSelected())
                    {
                        p.scale(new Point3Df(x,y,z));
                        rebound();
                    }
                }
                
                refreshCanvas();
            }
        });
        
        JMenuItem menuRotateLine = new JMenuItem(new AbstractAction("Set Rotate Line") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String vector = (String) JOptionPane.showInputDialog(
                    frame,
                    "Input Line Point 0: x0,y0,z0"
                    );
                
                String[] vectorParts = vector.split(",", 3);
                float x0 = Float.valueOf(vectorParts[0]);
                float y0 = Float.valueOf(vectorParts[1]);
                float z0 = Float.valueOf(vectorParts[2]);
                
                vector = (String) JOptionPane.showInputDialog(
                    frame,
                    "Input Line Point 1: x1,y1,z1"
                    );
                
                vectorParts = vector.split(",", 3);
                float x1 = Float.valueOf(vectorParts[0]);
                float y1 = Float.valueOf(vectorParts[1]);
                float z1 = Float.valueOf(vectorParts[2]);
                
                rotateLine[0] = new Point3Df(x0,y0,z0);
                rotateLine[1] = new Point3Df(x1,y1,z1);
                
                refreshCanvas();
            }
        });
        
        JMenuItem menuRotate = new JMenuItem(new AbstractAction("Rotate") {
            @Override
            public void actionPerformed(ActionEvent e) {  
                String sangle = (String) JOptionPane.showInputDialog(
                    frame,
                    "Input Rotation Angle in Degrees:"
                    );
                
                float angle = Float.valueOf(sangle);
                
                for(Object p : objects)
                {   
                    if(p.getSelected())
                    {
                        p.rotate(rotateLine[0],
                                rotateLine[1],
                                angle*(Math.PI/180));
                        rebound();
                    }
                }
                
                refreshCanvas();
            }
        });
        
        menuTransform.add(menuTranslate);
        menuTransform.add(menuScale);
        menuTransform.add(menuRotateLine);
        menuTransform.add(menuRotate);
        menuTransform.getPopupMenu().setLightWeightPopupEnabled(false);
        
        JMenu menuAnimate = new JMenu("Animate");
        JMenuItem menuAnimateStart = new JMenuItem(new AbstractAction("Start") {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(int i=0; i < 3; i++)
                {
                    scene[i].setAnimation(true);
                    animator[i].start();
                }
            }
        });
        JMenuItem menuAnimateStop = new JMenuItem(new AbstractAction("Stop") {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(int i=0; i < 3; i++)
                {
                    scene[i].setAnimation(false);
                    animator[i].stop();
                }
            }
        });
        JMenuItem menuAnimateReset = new JMenuItem(new AbstractAction("Reset") {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(int i=0; i < 3; i++)
                {
                    scene[i].resetRotationAnimation();
                }
                
                refreshCanvas();
            }
        });
        
        menuAnimate.add(menuAnimateStart);
        menuAnimate.add(menuAnimateStop);
        menuAnimate.add(menuAnimateReset);
        menuAnimate.getPopupMenu().setLightWeightPopupEnabled(false);
        
        JMenu menuHalftone = new JMenu("Halftone");
        JMenuItem menuToogle = new JMenuItem(new AbstractAction("Toogle") {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                halftone = !halftone;
                
                refreshCanvas();
            }
        });
        
        menuHalftone.add(menuToogle);
        menuHalftone.getPopupMenu().setLightWeightPopupEnabled(false);
        
        menu.add(menuFile);
        menu.add(menuTransform);
        menu.add(menuLighting);
        menu.add(menuAnimate);
        menu.add(menuHalftone);
        
        // Set the menu for the frame
        frame.setJMenuBar(menu);
        
        // Maximize the frame on startup
        frame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        
        // Add our splitpane to the frame
        frame.getContentPane().add(canvases);
        frame.setVisible(true);
        
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        
        canvases.addComponentListener(new ComponentListener() {
            // We do this to keep the splitpane the same proportion
            public void componentResized(ComponentEvent e) {
                oneTwo.setDividerLocation(0.50);
                threeFour.setDividerLocation(0.50);
                canvases.setDividerLocation(0.50);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });
        
        frame.addComponentListener(new ComponentListener() {
            // We do this to keep the splitpane the same proportion
            public void componentResized(ComponentEvent e) {
                canvases.setDividerLocation(0.50);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });
        
        canvas[0].addGLEventListener(scene[0]);
        canvas[1].addGLEventListener(scene[1]);
        canvas[2].addGLEventListener(scene[2]);
        
        animator[0] = new Animator(canvas[0]);
        animator[1] = new Animator(canvas[1]);
        animator[2] = new Animator(canvas[2]);
    }
}
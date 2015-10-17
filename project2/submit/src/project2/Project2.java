/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project2;

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
public class Project2 {
    private static Scene scene[] = new Scene[4];
    private static GLCanvas canvas[] = new GLCanvas[3];
    private static Animator animator[] = new Animator[3];
    private static ArrayList<Polygon> polygons;
    private static Point3Df[] rotateLine = new Point3Df[2]; 
    
    // The list model that contains the polygons we're working with
    private static DefaultListModel listModel;
    
    // The file that we're working witht that contains our polygons
    private static File saveFile;
    
    // The title of the application
    private static String appTitle = "ECS175 Project 2";
    
    private static void rebound()
    {
        float f = 0.0f;
        float n = 0.0f;
        float b = 0.0f;
        float t = 0.0f;
        float l = 0.0f;
        float r = 0.0f;
        
        for(Polygon poly : polygons)
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
        
        for(Polygon poly : polygons)
        {
            for(Point3Df p : poly.getPoints())
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
        scene[0].setPolygons(polygons);
        scene[1].setPolygons(polygons);
        scene[2].setPolygons(polygons);
        
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
        
        for(Polygon p : polygons)
        {   
            if(!listModel.contains(p))
            {
                listModel.addElement(p);
            }
        }
    }
    
    // Loads polygons into the scene from the saveFile
    private static void openPolygons()
    {
        if(saveFile.exists())
        {
            polygons.clear();
            
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
                    
                    Polygon p = new Polygon(name);
                    
                    for(int i=0; i < size; i++)
                    {
                        String line = fileIn.nextLine();
                        String[] pointParts = line.split(",");
                        float x = Float.valueOf(pointParts[0]);
                        float y = Float.valueOf(pointParts[1]);
                        float z = Float.valueOf(pointParts[2]);
                        
                        p.addPoint(new Point3Df(x,y,z));
                    }
                    
                    size = Integer.valueOf(fileIn.nextLine());
                    
                    for(int i=0; i < size; i++)
                    {
                        String line = fileIn.nextLine();
                        String[] edgeParts = line.split(",");
                        int p1 = Integer.valueOf(edgeParts[0]);
                        int p2 = Integer.valueOf(edgeParts[1]);
                        
                        p.addEdge(new Edge(p1,p2));
                    }
                    
                    polygons.add(p);
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
    private static void importPly(File ply)
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

                Polygon p = new Polygon(ply.getName());

                for(int i=0; i < vertexSize; i++)
                {
                    String line = fileIn.nextLine();
                    String[] pointParts = line.split("\\s+");
                    float x = Float.valueOf(pointParts[0]);
                    float y = Float.valueOf(pointParts[1]);
                    float z = Float.valueOf(pointParts[2]);

                    p.addPoint(new Point3Df(x,y,z));
                }
                
                for(int i=0; i < faceSize; i++)
                {
                    String line = fileIn.nextLine();
                    String[] edgeParts = line.split("\\s+");
                    
                    int edgeComp = Integer.valueOf(edgeParts[0]);
                    
                    for(int j=0; j < edgeComp-1; j++)
                    {
                        int p1 = Integer.valueOf(edgeParts[j+1]);
                        int p2 = Integer.valueOf(edgeParts[j+2]);

                        p.addEdge(new Edge(p1,p2));
                    }
                }

                polygons.add(p);
                
                rebound();
                
                fileIn.close();       
            } catch (FileNotFoundException ex) {
                // Can't find file
            }
        } else {
            // File doesn't exist
        }
    }
    
    // Writes polygons to the saveFile
    private static void savePolygons()
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
            for(Polygon p : polygons)
            {
                fileOut.println(p.getName());
                fileOut.println(p.getPoints().size());
                for(Point3Df q : p.getPoints())
                {
                    fileOut.print(q.x);
                    fileOut.print(",");
                    fileOut.print(q.y);
                    fileOut.print(",");
                    fileOut.println(q.z);
                }
                
                fileOut.println(p.getEdges().size());
                for(Edge q : p.getEdges())
                {
                    fileOut.print(q.p1);
                    fileOut.print(",");
                    fileOut.println(q.p2);
                }
            }
            fileOut.close();
        } catch (FileNotFoundException ex) {
           // Can't find file
        }
    }
    
    public static void main(String[] args)
    {
        polygons = new ArrayList<Polygon>();
        rotateLine[0] = new Point3Df(-1.0f,-1.0f,-1.0f);
        rotateLine[1] = new Point3Df(1.0f,1.0f,1.0f);
        
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
                    
                    for(Polygon p : polygons)
                    {
                        p.setSelected(false);
                    }
                    
                    for(int i=minIndex; i <= maxIndex; i++)
                    {
                        if(lsm.isSelectedIndex(i))
                        {
                            ((Polygon)listPolys.getModel().getElementAt(i)).setSelected(true);
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
                    
                    openPolygons();
                    
                    refreshPolygons();
                    
                    refreshCanvas();
                    
                    frame.setTitle(appTitle + " - " + saveFile.getAbsolutePath());
                }
            }
        });
        
        JMenuItem menuImportPly = new JMenuItem(new AbstractAction("Import Ply") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showOpenDialog(frame);

                if(returnVal == JFileChooser.APPROVE_OPTION)
                {   
                    importPly(fc.getSelectedFile());
                    
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
                    savePolygons();
                } else {
                    int returnVal = fc.showSaveDialog(frame);
                    
                    if(returnVal == JFileChooser.APPROVE_OPTION)
                    {
                        saveFile = fc.getSelectedFile();

                        savePolygons();
                        
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
                    
                    savePolygons();
                    
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
        menuFile.add(menuImportPly);
        menuFile.add(menuSave);
        menuFile.add(menuSaveAs);
        menuFile.add(menuExit);
        
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
                
                for(Polygon p : polygons)
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
                
                for(Polygon p : polygons)
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
                
                for(Polygon p : polygons)
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
        
        menu.add(menuFile);
        menu.add(menuTransform);
        menu.add(menuAnimate);
        
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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project1;

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
public class Project1 {
    private static Scene scene;
    
    // The list model that contains the polygons we're working with
    private static DefaultListModel listModel;
    
    // The file that we're working witht that contains our polygons
    private static File saveFile;
    
    // The title of the application
    private static String appTitle = "ECS175 Project 1";
    
    // This refreshes the list model, the one that displays the
    // polygons we're working on the left side of the frame
    private static void refreshPolygons()
    {
        listModel.clear();
        
        for(Polygon p : scene.getPolygons())
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
            scene.clearPolygons();
            
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
                        
                        p.addPoint(new Point2Df(x,y));
                    }
                    
                    scene.addPolygon(p);
                } while(true);
                
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
            for(Polygon p : scene.getPolygons())
            {
                fileOut.println(p.getName());
                fileOut.println(p.getPoints().size());
                for(Point2Df q : p.getPoints())
                {
                    fileOut.print(q.x);
                    fileOut.print(",");
                    fileOut.println(q.y);
                }
            }
            fileOut.close();
        } catch (FileNotFoundException ex) {
           // Can't find file
        }
    }
    
    public static void main(String[] args) {
        // Prepare all our OpenGL canvas
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        GLCanvas canvas = new GLCanvas(caps);
        
        // Prepare the main frame for the application
        JFrame frame = new JFrame(appTitle);
        
        // The file chooser for opening and saving files
        final JFileChooser fc = new JFileChooser();
        
        // Since the app started, set the save file to null
        saveFile = null;
        
        // This is our scene, which contains our polygons and rendering functions
        scene = new Scene();
        
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
                    
                    for(Polygon p : scene.getPolygons())
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
                    
                    canvas.display();
                }
            }
        });
        
        // This splits the frame to have the list of polygons/status on the left and
        // OpenGL canvas on the right
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollerPolys, canvas);
        
        // Give the list of polygons 20% of the frame
        splitPane.setDividerLocation(0.20);
        
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
                    
                    canvas.display();
                    
                    frame.setTitle(appTitle + " - " + saveFile.getAbsolutePath());
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
        menuFile.add(menuSave);
        menuFile.add(menuSaveAs);
        menuFile.add(menuExit);
        
        JMenu menuTransform = new JMenu("Transform");
        JMenuItem menuTranslate = new JMenuItem(new AbstractAction("Translate") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String vector = (String) JOptionPane.showInputDialog(
                    frame,
                    "Input Translation Vector: x,y"
                    );
                
                String[] vectorParts = vector.split(",", 2);
                float x = Float.valueOf(vectorParts[0]);
                float y = Float.valueOf(vectorParts[1]);
                
                for(Polygon p : scene.getPolygons())
                {   
                    if(p.getSelected())
                    {
                        p.translate(new Point2Df(x,y));
                        canvas.display();
                    }
                }
            }
        });
        
        JMenuItem menuScale = new JMenuItem(new AbstractAction("Scale") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String vector = (String) JOptionPane.showInputDialog(
                    frame,
                    "Input Scale Factor: x,y"
                    );
                
                String[] vectorParts = vector.split(",", 2);
                float x = Float.valueOf(vectorParts[0]);
                float y = Float.valueOf(vectorParts[1]);
                
                for(Polygon p : scene.getPolygons())
                {   
                    if(p.getSelected())
                    {
                        p.scale(new Point2Df(x,y));
                        canvas.display();
                    }
                }
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
                
                for(Polygon p : scene.getPolygons())
                {   
                    if(p.getSelected())
                    {
                        p.rotate(angle*(Math.PI/180));
                        canvas.display();
                    }
                }
            }
        });
        
        menuTransform.add(menuTranslate);
        menuTransform.add(menuScale);
        menuTransform.add(menuRotate);
        
        JMenu menuDraw = new JMenu("Draw");
        JMenuItem menuDrawDLine = new JMenuItem(new AbstractAction("Draw DDA Line") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String points = (String) JOptionPane.showInputDialog(
                    frame,
                    "Enter points for line: x0,y0,x1,y1"
                    );
                
                String lineName = (String) JOptionPane.showInputDialog(
                    frame,
                    "Enter name for line:"
                    );
                
                if(points != null && lineName != null)
                {
                    String[] pointParts = points.split(",");

                    float x0 = Float.valueOf(pointParts[0]);
                    float y0 = Float.valueOf(pointParts[1]);
                    float x1 = Float.valueOf(pointParts[2]);
                    float y1 = Float.valueOf(pointParts[3]);
                    
                    Polygon p = new Polygon(lineName);
                    
                    p.addPoint(new Point2Df(x0,y0));
                    p.addPoint(new Point2Df(x1,y1));
                    p.setType(Polygon.DDA);
                    
                    scene.addPolygon(p);
                    
                    refreshPolygons();
                    
                    canvas.display();
                }
            }
        });
        
        JMenuItem menuDrawBLine = new JMenuItem(new AbstractAction("Draw Bresenham Line") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String points = (String) JOptionPane.showInputDialog(
                    frame,
                    "Enter points for line: x0,y0,x1,y1"
                    );
                String lineName = (String) JOptionPane.showInputDialog(
                    frame,
                    "Enter name for line:"
                    );
                
                if(points != null && lineName != null)
                {
                    String[] pointParts = points.split(",");

                    float x0 = Float.valueOf(pointParts[0]);
                    float y0 = Float.valueOf(pointParts[1]);
                    float x1 = Float.valueOf(pointParts[2]);
                    float y1 = Float.valueOf(pointParts[3]);
                    
                    Polygon p = new Polygon(lineName);
                    
                    p.addPoint(new Point2Df(x0,y0));
                    p.addPoint(new Point2Df(x1,y1));
                    p.setType(Polygon.BRESENHAM);
                    
                    scene.addPolygon(p);
                    
                    refreshPolygons();
                    
                    canvas.display();
                }
            }   
        });
        
        JMenuItem menuDrawPolygon = new JMenuItem(new AbstractAction("Draw Polygon") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String pointsStr = (String) JOptionPane.showInputDialog(
                    frame,
                    "Enter points for polygons: x0,y0,x1,y1,x2,y2,..."
                    );
                
                String polyName = (String) JOptionPane.showInputDialog(
                    frame,
                    "Enter name for polygon:"
                    );

                if(pointsStr != null && polyName != null)
                {
                    String[] points = pointsStr.split(",");

                    Polygon p = new Polygon(polyName);

                    for(int i=0; i < points.length/2; i++)
                    {
                        float x = Float.valueOf(points[i*2]);
                        float y = Float.valueOf(points[i*2+1]);

                        p.addPoint(new Point2Df(x,y));
                    }

                    scene.addPolygon(p);

                    refreshPolygons();

                    canvas.display();
                }
            }
        });
        
        menuDraw.add(menuDrawDLine);
        menuDraw.add(menuDrawBLine);
        menuDraw.add(menuDrawPolygon);
        
        JMenu menuDisplay = new JMenu("Settings");
        JMenuItem menuClip = new JMenuItem(new AbstractAction("Set Clipping") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Point2Di clipMin = scene.getClipMin();
                Point2Di clipMax = scene.getClipMax();
                
                String clipping = (String) JOptionPane.showInputDialog(
                    frame,
                    "Input clipping boundries: x-min,y-min,x-max,y-max",
                    clipMin.x + "," +
                    clipMin.y + "," +
                    clipMax.x + "," +
                    clipMax.y);
                
                if(clipping != null)
                {
                    String[] clipParts = clipping.split(",");

                    scene.setClipMin(new Point2Di(Integer.valueOf(clipParts[0]),
                        Integer.valueOf(clipParts[1])));

                    scene.setClipMax(new Point2Di(Integer.valueOf(clipParts[2]),
                        Integer.valueOf(clipParts[3])));

                    canvas.display();
                }
            }
                
        });
        JMenuItem menuViewport = new JMenuItem(new AbstractAction("Adjust Viewport") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Point2Di viewSize = scene.getViewSize();
                
                String viewport = (String) JOptionPane.showInputDialog(
                    frame,
                    "Input viewpoint size: width,height",
                    viewSize.x + "," + viewSize.y
                    );
                
                if(viewport != null)
                {
                    String[] viewParts = viewport.split(",");

                    canvas.reshape(canvas.getX(), canvas.getY(),
                            Integer.valueOf(viewParts[0]),
                            Integer.valueOf(viewParts[1]));

                    canvas.display();
                }
            }
        });
        
        menuDisplay.add(menuClip);
        menuDisplay.add(menuViewport);
        
        menu.add(menuFile);
        menu.add(menuTransform);
        menu.add(menuDraw);
        menu.add(menuDisplay);
        
        // Set the menu for the frame
        frame.setJMenuBar(menu);
        
        // Maximize the frame on startup
        frame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        
        // Add our splitpane to the frame
        frame.getContentPane().add(splitPane);
        frame.setVisible(true);
        
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        
        frame.addComponentListener(new ComponentListener() {
            // We do this to keep the splitpane the same proportion
            public void componentResized(ComponentEvent e) {
                splitPane.setDividerLocation(0.20);
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
        
        canvas.addGLEventListener(scene);
        
        menuViewport.doClick();
    }   
}
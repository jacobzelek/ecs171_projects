/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project4;

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
public class Project4
{
    private static Scene scene;
    private static GLCanvas canvas;
    private static ArrayList<Curve> curves;
    private static JFrame frame;
    
    private static int displayResolution = 2;
    
    // The list model that contains the polygons we're working with
    private static DefaultListModel listModelCurves;
    
    // The list model that contains the points we're working with
    private static DefaultListModel listModelPoints;
    
    // The file that we're working witht that contains our polygons
    private static File saveFile;
    
    // The title of the application
    private static String appTitle = "ECS175 Project 4";
    
    private static Curve getSelectedCurve()
    {
        for(Curve c : curves)
        {
            if(c.isSelected())
            {
                return c;
            }
        }
        
        return null;
    }
    
    private static Point2Di getSelectedPoint()
    {
        for(Point2Di p : getSelectedCurve().getPoints())
        {
            if(p.isSelected())
            {
                return p;
            }
        }
        
        return null;
    }
    
    private static void refreshCanvas()
    {           
        scene.setCurves(curves);
        scene.setDisplayResolution(displayResolution);
        
        canvas.display();
    }
    
    // This refreshes the list model, the one that displays the
    // polygons we're working on the left side of the frame
    private static void refreshCurves()
    {
        listModelCurves.clear();
        
        for(Curve p : curves)
        {   
            if(!listModelCurves.contains(p))
            {
                listModelCurves.addElement(p);
            }
        }
    }
    
    private static void refreshPoints()
    {
        listModelPoints.clear();
        
        for(Curve c : curves)
        {   
            if(c.isSelected())
            {
                for(Point2Di p : c.getPoints())
                if(!listModelPoints.contains(p))
                {
                    listModelPoints.addElement(p);
                }
            }
        }
    }
    
    private static void open(File file)
    {
        if(saveFile.exists())
        {
            curves.clear();
            
            try {
                Scanner fileIn = new Scanner(new FileReader(saveFile));
                
                do
                {
                    if(!fileIn.hasNextLine())
                    {
                        break;
                    }
                    
                    String name = fileIn.nextLine();
                    int type = Integer.valueOf(fileIn.nextLine());
                    
                    int k = 0;
                    float[] knots = {};
                    if(type == Curve.BSPLINE)
                    {
                        k = Integer.valueOf(fileIn.nextLine());
                        knots = Curve.parseKnotsStr(fileIn.nextLine());
                    }

                    int size = Integer.valueOf(fileIn.nextLine());
                    
                    Curve o = new Curve(name, type);
                    o.setK(k);
                    o.setKnots(knots);
                    
                    for(int i=0; i < size; i++)
                    {
                        String line = fileIn.nextLine();
                        String[] pointParts = line.split(",");
                        int x = Integer.valueOf(pointParts[0]);
                        int y = Integer.valueOf(pointParts[1]);
                        
                        o.addPoint(new Point2Di(x,y));
                    }
                    
                    curves.add(o);
                } while(true);
                
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
            for(Curve o : curves)
            {
                fileOut.println(o.getName());
                fileOut.println(o.getType());
                if(o.getType() == Curve.BSPLINE)
                {
                    fileOut.println(o.getK());
                    
                    fileOut.println(Curve.getKnotsStr(o.getKnots()));
                }
                fileOut.println(o.getPoints().size());
                for(Point2Di q : o.getPoints())
                {
                    fileOut.print(q.x);
                    fileOut.print(",");
                    fileOut.print(q.y);
                    fileOut.println("");
                }
            }
            fileOut.close();
        } catch (FileNotFoundException ex) {
           // Can't find file
        }
    }
    
    private static void updateTitle()
    {
        if(saveFile != null)
        {
            frame.setTitle(appTitle + " - " + saveFile.getAbsolutePath() +
                " - " + scene.width + "x" + scene.height);
        } else {
            frame.setTitle(appTitle + " - " + scene.width + "x" + scene.height);
        }
    }
    
    public static void main(String[] args)
    {
        curves = new ArrayList<Curve>();
        
        // Prepare all our OpenGL canvas
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        
        canvas = new GLCanvas(caps);
        
        // Prepare the main frame for the application
        frame = new JFrame(appTitle);
        
        // The file chooser for opening and saving files
        final JFileChooser fc = new JFileChooser();
        
        // Since the app started, set the save file to null
        saveFile = null;
        
        // This is our scene, which contains our polygons and rendering functions
        scene = new Scene();
        
        // This is our list model and JList that will allow us to select the
        // polygons we wish to transform
        listModelCurves = new DefaultListModel();
        JList listPolys = new JList(listModelCurves);
        listPolys.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listPolys.setLayoutOrientation(JList.VERTICAL);
        listPolys.setVisibleRowCount(-1);
        JScrollPane listScrollerPolys = new JScrollPane(listPolys);
        
        listModelPoints = new DefaultListModel();
        JList listPoints = new JList(listModelPoints);
        listPolys.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listPolys.setLayoutOrientation(JList.VERTICAL);
        listPolys.setVisibleRowCount(-1);
        JScrollPane listScrollerPoints = new JScrollPane(listPoints);
        
        refreshCurves();
        refreshPoints();
        
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
                    
                    for(Curve p : curves)
                    {
                        p.setSelected(false);
                    }
                    
                    for(int i=minIndex; i <= maxIndex; i++)
                    {
                        if(lsm.isSelectedIndex(i))
                        {
                            ((Curve)listPolys.getModel().getElementAt(i)).setSelected(true);
                            refreshPoints();
                        }
                    }
                    
                    refreshCanvas();
                }
            }
        });
        
        // This listener waits for selections in the JList and updates the polygons
        // in the scene with "selected" markers
        listPoints.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(e.getValueIsAdjusting() == false)
                {
                    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                    
                    int minIndex = lsm.getMinSelectionIndex();
                    int maxIndex = lsm.getMaxSelectionIndex();
                    
                    for(Curve c : curves)
                    {
                        for(Point2Di p : c.getPoints())
                        {
                            p.setSelected(false);
                        }
                    }
                    
                    for(int i=minIndex; i <= maxIndex; i++)
                    {
                        if(lsm.isSelectedIndex(i))
                        {
                            ((Point2Di)listPoints.getModel().getElementAt(i)).setSelected(true);
                        }
                    }
                    
                    refreshCanvas();
                }
            }
        });
        
        JSplitPane selectorPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listScrollerPolys, listScrollerPoints);
        JSplitPane splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, selectorPane, canvas);
        
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
                    
                    refreshCurves();
                    
                    refreshCanvas();
                    
                    updateTitle();
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
                        
                        updateTitle();
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
                    
                    updateTitle();
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
        menuFile.getPopupMenu().setLightWeightPopupEnabled(false);
        
        JMenu menuCurves = new JMenu("Curves");
        JMenuItem menuAddCurve = new JMenuItem(new AbstractAction("Add Curve") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = (String) JOptionPane.showInputDialog(
                    frame,
                    "Enter name of new curve: "
                    );
                
                int type = Integer.valueOf(JOptionPane.showInputDialog(
                    frame,
                    "Enter type of curve 0 = Beizer, 1 = B-Spline: "
                    ));
                
                if(type == 0 || type == 1)
                {
                    curves.add(new Curve(name, type));
                }
                
                refreshCurves();
                refreshCanvas();
            }
        });
        
        JMenuItem menuSetK = new JMenuItem(new AbstractAction("Set K") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Curve c = getSelectedCurve();
                
                if(c != null)
                {
                    int vector = Integer.valueOf(JOptionPane.showInputDialog(
                        frame,
                        "Enter K for the selected curve: ",
                        c.getK()
                        ));

                    c.setK(vector);
                    
                    refreshCurves();
                    refreshCanvas();
                }
            }
        });
        
        JMenuItem menuSetKnots = new JMenuItem(new AbstractAction("Set Knots") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Curve c = getSelectedCurve();
                
                if(c != null)
                {
                    String vector = JOptionPane.showInputDialog(
                        frame,
                        "Enter Knots for the selected curve: a,b,c,d,....",
                        Curve.getKnotsStr(c.getKnots())
                        );

                    c.setKnots(Curve.parseKnotsStr(vector));
                    
                    refreshCurves();
                    refreshCanvas();
                }
            }
        });
        
        menuCurves.add(menuAddCurve);
        menuCurves.add(menuSetK);
        menuCurves.add(menuSetKnots);
        menuCurves.getPopupMenu().setLightWeightPopupEnabled(false);

        JMenu menuPoints = new JMenu("Points");
        
        JMenuItem menuAddPoint = new JMenuItem(new AbstractAction("Add Point") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Curve c = getSelectedCurve();
                
                if(c != null)
                {
                    String vector = (String) JOptionPane.showInputDialog(
                        frame,
                        "Enter point: x, y"
                        );

                    String[] vectorParts = vector.split(",", 2);
                    int x = Integer.valueOf(vectorParts[0].trim());
                    int y = Integer.valueOf(vectorParts[1].trim());    

                    c.addPoint(new Point2Di(x,y));

                    refreshCurves();
                    refreshPoints();
                    refreshCanvas();
                }
            }
        });
                
        JMenuItem menuInsertPointBefore = new JMenuItem(new AbstractAction("Insert Point Before") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String vector = (String) JOptionPane.showInputDialog(
                        frame,
                        "Enter point: x, y"
                        );

                String[] vectorParts = vector.split(",", 2);
                int x = Integer.valueOf(vectorParts[0].trim());
                int y = Integer.valueOf(vectorParts[1].trim());
                
                Curve c = getSelectedCurve();
                
                if(c != null)
                {
                    for(int i=0; i < c.getPoints().size(); i++)
                    {
                        if(c.getPoint(i).isSelected())
                        {
                            c.insertPoint(i, new Point2Di(x,y));
                            break;
                        }
                    }
                    
                    refreshCurves();
                    refreshPoints();
                    refreshCanvas();
                }
            }
        });
        
        JMenuItem menuInsertPointAfter = new JMenuItem(new AbstractAction("Insert Point After") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String vector = (String) JOptionPane.showInputDialog(
                        frame,
                        "Enter point: x, y"
                        );

                String[] vectorParts = vector.split(",", 2);
                int x = Integer.valueOf(vectorParts[0].trim());
                int y = Integer.valueOf(vectorParts[1].trim());
                
                Curve c = getSelectedCurve();
                
                if(c != null)
                {
                    for(int i=0; i < c.getPoints().size(); i++)
                    {
                        if(c.getPoint(i).isSelected())
                        {
                            c.insertPoint(i+1, new Point2Di(x,y));
                            break;
                        }
                    }
                    
                    refreshCurves();
                    refreshPoints();
                    refreshCanvas();
                }
            }
        });
        
        JMenuItem menuModifyPoint = new JMenuItem(new AbstractAction("Modify Point") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Point2Di p = getSelectedPoint();
                
                if(p != null)
                {
                    String vector = (String) JOptionPane.showInputDialog(
                    frame,
                    "Enter point: x, y",
                    p.x + ", " + p.y
                    );

                    String[] vectorParts = vector.split(",", 2);
                    int x = Integer.valueOf(vectorParts[0].trim());
                    int y = Integer.valueOf(vectorParts[1].trim());    

                    p.x = x;
                    p.y = y;

                    refreshCurves();
                    refreshPoints();
                    refreshCanvas();
                }
            }
        });
        
        JMenuItem menuDeletePoint = new JMenuItem(new AbstractAction("Delete Point") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Curve c = getSelectedCurve();
                
                if(c != null)
                {
                    for(int i=0; i < c.getPoints().size(); i++)
                    {
                        if(c.getPoint(i).isSelected())
                        {
                            c.removeIndex(i);
                        }
                    }
                    
                    refreshCurves();
                    refreshPoints();
                    refreshCanvas();
                }
            }
        }); 
        
        menuPoints.add(menuAddPoint);
        menuPoints.add(menuInsertPointBefore);
        menuPoints.add(menuInsertPointAfter);
        menuPoints.add(menuModifyPoint);
        menuPoints.add(menuDeletePoint);
        menuPoints.getPopupMenu().setLightWeightPopupEnabled(false);
        
        JMenu menuSettings = new JMenu("Settings");
        
        JMenuItem menuDisplayResolution = new JMenuItem(new AbstractAction("Display Resolution") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String vector = (String) JOptionPane.showInputDialog(
                    frame,
                    "Enter Display Resolution: ",
                    displayResolution);

                displayResolution = Integer.valueOf(vector);

                refreshCanvas();
            }
        });
        
        menuSettings.add(menuDisplayResolution);
        menuSettings.getPopupMenu().setLightWeightPopupEnabled(false);
        
        menu.add(menuFile);
        menu.add(menuCurves);
        menu.add(menuPoints);
        menu.add(menuSettings);
        
        // Set the menu for the frame
        frame.setJMenuBar(menu);
        
        // Maximize the frame on startup
        frame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        
        // Add our splitpane to the frame
        frame.getContentPane().add(splitpane);
        frame.setVisible(true);
        
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        
        frame.addComponentListener(new ComponentListener() {
            // We do this to keep the splitpane the same proportion
            public void componentResized(ComponentEvent e) {
                selectorPane.setDividerLocation(0.50);
                splitpane.setDividerLocation(0.20);
                updateTitle();
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
    }
}
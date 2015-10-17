# ECS175 Project 3

## Info
Winter 2015
UC Davis
Author: Jacob Zelek
Email: jzelek@ucdavis.edu

### Prerequesites
1) An OpenGL 2.1 capable graphics card
2) A Java Virtual Machine capable of Java 1.8

### Dependancies
The lib folder is required for dependencies (JOGL, GLUEGEN).

When running project3.jar make sure the lib/ folder is in the same folder as project3.jar and start.sh

### Source Code
The source code is available in src/ folder.

The application calculates light intensities for every vertex using the computePhong function in Scene.java.
The intensities are interpolated when calculating line points in the getLinePoints function in Bresenham.java.
Finally the intensities are interpolated at the scan line during rasterization in the drawFace function in Scene.java.
Objects are sorted by depth in the render function in Scene.java (painter's algorithm). Faces are back culled by
removing all those faces with a negative depth component on their normal vector, this is done in drawObject in Scene.java
Half toning uses a different drawPixel function called drawVirtualPixel in Scene.java

## Running
Run the start.sh script

OR

Run java -jar project3.jar

## Usage

### Sample file
A sample file named polyhedra.txt exists. There are 3 polyhedra.

### Importing
Ply files may be imported by selecting File->Import Ply. Three sample Ply files are provided and are the same objects in the sample file.

### File Operations
Opening, saving, and exiting the application are all available under the File menu.

### Transformations
Transformations are under Transformation menu.

### Animation
Animation is under the Animation menu and rotation animation is along the rotation line set in the transformation menu.

### Lighting
All lighting coefficents and vectors can be set under the Lighting menu.

### Half-toning
Half-toning can be toogled on or off under the Halftone menu.
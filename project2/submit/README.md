# ECS175 Project 2

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

When running project2.jar make sure the lib/ folder is in the same folder as project2.jar and start.sh

### Source Code
The source code is available in src/ folder. The code for 3D transformations can be found in Polygon.java

## Running
Run the start.sh script

OR

Run java -jar project2.jar

## Usage

### Sample file
A sample file named polygons.txt exists. There are 3 polygons.

### Importing
Ply files may be imported by selecting File->Import Ply. Three sample Ply files are provided and are the same objects in the sample file.

### Transformations
To transform a polygon(s) select desired objects using the list panel in the bottom right corner of the application. Next, select the desired option under the Transform menu.

#### Rotations
To apply a rotation, a rotation line must be specified first using Transform->Set Rotate Line. The rotation line is set as (0,0,0)(1,1,1) by default. After a rotation line has been set, use Tranform->Rotate to specify an angle of rotation and apply the rotation to the selected polygon(s).

### File Operations
Opening, saving, and exiting the application are all available under the File menu.

## Additional Information

### Extra credit
The application can animate rotation under the animate menu. The rotation is about the line (0,0,0),(1,1,1).

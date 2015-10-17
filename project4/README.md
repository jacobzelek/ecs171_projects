# ECS175 Project 4

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

When running project4.jar make sure the lib/ folder is in the same folder as project3.jar and start.sh

### Source Code
The source code is available in src/ folder.

The application draws Bezier and BSpline curves using Casteljau and deBoor algorithms, respectively.
Both these algorithms are in curve.java under src/project4/

## Running
Run the start.sh script

OR

Run java -jar project4.jar

## Usage
The screen is partioned into three segements. The right most (and largest) segement is the OpenGL window. The left side is split into two lists.
The upper-left is the curve list and the lower-left is the point list. Upon selecting a curve from the curve list, the points for that respective curve
will appear in the point list and will also appear as yellow points in the OpenGL window.

Curves will only appear if there are at least three points. Additionally, BSpline curves will only appear if the combination of: number of points, degree, and number of knots, allows for successful render.

The OpenGL window dimensions are displayed in the application title.

### Sample file
A sample file named curves exists. There are 2 curves, one Bezier curve and one BSpline curve. The display resolution is 2 by default. This can be changed in the settings menu.

### File Operations
Opening, saving, and exiting the application are all available under the File menu.

### Curve Operations

#### Add Curve
Adding a curve is done under the Curves->Add Curve menu item.

#### Setting K, Setting Knot
Both operations are under the Curve menu. You must select a curve from the curve list.

### Point Operations
Operations on points are done under the Points menu. You must select curve from the curve list and then specific point from the point list to perform an operation on a point.

### Settings
The settings menu has an item for changing the display resolution of the curves being rendered. The default display resolution is 2.
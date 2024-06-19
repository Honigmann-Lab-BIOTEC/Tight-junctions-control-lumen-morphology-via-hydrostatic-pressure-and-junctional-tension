# @ ImageJ ij
import eu.kiaru.limeseg.LimeSeg;
import eu.kiaru.limeseg.commands.SphereSeg;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.WaitForUserDialog;
import ij.plugin.frame.RoiManager;
import net.imagej.ImageJ;
import org.scijava.util.ColorRGB;
import eu.kiaru.limeseg.struct.Cell;
import ij.measure.ResultsTable;
import eu.kiaru.limeseg.struct.CellT;
import eu.kiaru.limeseg.struct.DotN;
import eu.kiaru.limeseg.opt.CurvaturesComputer;
import eu.kiaru.limeseg.io.IOXmlPlyLimeSeg;
import java.lang.Math;

int first_frame = 1
int last_frame = 13 //ADJUSTABLE
// FUNCTION: print measurements
// function here to be applied after segmented cells or frames
// function is to print out Surface and Volumes in pixel units
def printoutmeasure (ResultsTable table, cell_id) {
	Cell lumen = LimeSeg.currentCell;
	LimeSeg.constructMesh();
	d0 = (float) LimeSeg.opt.getOptParam("d_0");
        range = 2.5f;
	// Fetch CellT object:
	CellT my3Dobject = lumen.getCellTAt(LimeSeg.currentFrame);
	if (my3Dobject.freeEdges!=0) {
	    print("Mesh reconstruction problem, wrong surface and volume value!");
	    print("Free edges = "+my3Dobject.freeEdges+"\n"); 
	}
	float mc = 0.0;
	float gc = 0.0;
	int counter = 0;
	CurvaturesComputer cc = new CurvaturesComputer(my3Dobject.dots, d0, range);
	for (DotN dn:my3Dobject.dots) {
		//print("P=\t"+dn.pos.x+"\t"+dn.pos.y+"\t"+dn.pos.z+"\t");
		//print(" N=\t"+dn.Norm.x+"\t"+dn.Norm.y+"\t"+dn.Norm.z+"\t");
		print(" MC=\t"+dn.meanCurvature+" GC=\t"+dn.gaussianCurvature+"\t");
		mc = mc + Math.abs(dn.meanCurvature); 
		gc = gc + Math.abs(dn.gaussianCurvature); 
		counter = counter + 1;
	}
	// prints in script editor outputconsole 
	print("Surface = "+my3Dobject.getSurface()+"\n");
	print("Volume  = "+ my3Dobject.getVolume()+"\n");
	print("Frame = "+LimeSeg.currentFrame+"\n");
	// outputs a table that is later saved as excel file; xlsx or csv
    	table.incrementCounter();
    	table.addValue("Real Volume", my3Dobject.getVolume());
    	table.addValue("Real Surface", my3Dobject.getSurface());
    	table.addValue("CenterX", my3Dobject.center.x);
    	table.addValue("CenterY", my3Dobject.center.y);
    	table.addValue("CenterZ", my3Dobject.center.z/LimeSeg.opt.getOptParam("ZScale")+1); // IJ1 style numerotation
		table.addValue("MeanMeanCurvature", mc/counter);
		table.addValue("MeanGaussianCurvature", gc/counter);
    	table.addValue("Frame", LimeSeg.currentFrame);
    	table.addValue("Name",'T' + LimeSeg.currentFrame.toString() + "lumen_" + cell_id);
}

// PART1: open image in, show image, show LimeSeg Gui 
String working_directory = "C:\\Data\\Revision_data\\laserablation231112\\WT\\"
ImagePlus imp = IJ.openImage(working_directory + "Image_2_median.tif");
imp.show();
imp.setPosition(1, 35, first_frame);
ResultsTable table = new ResultsTable ();
String new_folder_name = "Lumen_test"
String pathNew= working_directory + new_folder_name  +  "/";		 		
File dirNew = new File(pathNew);
dirNew.mkdir();
	
// PART2: make ROI for segmentation
// make ROI: multipoint and polygonal selection this is to be carried out for SkeletonSegmentation
// !!! 
RoiManager roimanager = RoiManager.getRoiManager();
//roimanager.runCommand("Open", working_directory + "/RoiSet.zip");
new WaitForUserDialog("Please make ROI and afterwards click OK").show();
int numberofroi = roimanager.getCount();
System.out.println("number_roi=" + numberofroi);
// PART3: run SkeletonSegmentations 
// At the end, run 'printoutmeasure' and display resultstable and save as 'result.csv'
LimeSeg lms = new LimeSeg();    // initialize limeseg
lms.initialize();
LimeSeg.make3DViewVisible();
ij.command().run(SphereSeg.class,true,
            "d_0",4.2,//ADJUSTABLE 2.0 - 4.0
            "f_pressure",0.02,//ADJUSTABLE
            //"z_scale",2.53,
            "z_scale",10.15,
            "range_in_d0_units",2,//ADJUSTABLE, from ~ 0.5 to 20
            "show3D",true,
            "sameCell",true,
            "color", new ColorRGB(200,150,50),
            "numberOfIntegrationStep",-1,
            //"realXYPixelSize",0.395);
            "realXYPixelSize",0.197);

LimeSeg.currentFrame=first_frame;		 
float expansion = LimeSeg.opt.getOptParam("d_0")/2;

for (Cell c:LimeSeg.allCells) { // loops through cells
	LimeSeg.currentCell=c;
	// Fetch CellT object = single 3D object, rather obvious here because there is only one timepoint
	CellT ct = c.getCellTAt(LimeSeg.currentFrame);
	for (DotN dn: ct.dots) { // DotN object = surfel = a position and a normal vector
		dn.pos.x+=dn.Norm.x*expansion;
		dn.pos.y+=dn.Norm.y*expansion;
		dn.pos.z+=dn.Norm.z*expansion;
	}
}
LimeSeg.update3DDisplay(); // notifies 3D viewer that the dots have changed
//2D output
LimeSeg.clearOverlay();
LimeSeg.updateOverlay();
LimeSeg.addAllCellsToOverlay();
LimeSeg.updateOverlay();

new WaitForUserDialog("'Okay' me if u think it went well :)").show();
currentframe = first_frame
pathT= pathNew + "T_"+ currentframe.toString() + "/";		
dirT = new File(pathT);
dirT.mkdir(); // attempt to create the directory here
if (dirT.exists()) {
    for (Cell c:LimeSeg.allCells){
    	LimeSeg.currentCell=c;
    	CellT ct = c.getCellTAt(currentframe);
    	IOXmlPlyLimeSeg.saveCellTAsPly(ct, pathT + 'T' + currentframe.toString() + "lumen_"+ct.c.id_Cell +".ply");
    	printoutmeasure(table, ct.c.id_Cell);
	}
}
// PART4: Segment all timepoints
// No ROI required
// used obtained Dots from Frame=1 or first segmentation, instead of ROI
// At the end, At the end, run 'printoutmeasure' and display resultstable and save as 'result2.csv'
Cell myCell = LimeSeg.currentCell;

for (currentframe = first_frame + 1; currentframe <= last_frame; currentframe++) {
    // int currentframe = imp.getFrame()+1;
    // imp.setT(currentframe + 1);
    
    // Copy from previous frame to next frame
    LimeSeg.currentFrame = currentframe - 1;
    // Fetch CellT object:
    LimeSeg.currentCell = myCell;
	LimeSeg.copyDotsFromCellT();
	LimeSeg.currentFrame = currentframe;
	LimeSeg.pasteDotsToCellT();
	// Now the cell is initialized with the shape from the previous frame
	// Let's feed the Optimizer
    LimeSeg.clearOptimizer();
    LimeSeg.putCurrentCellTToOptimizer();
	// Done
	// can be put out of the loop : 
	// basically it says that there's no need to inflate the shape right now:
	// this is true if the shape from one timepoint to another is not too different
    LimeSeg.setOptimizerParameter("normalForce",-0.015); 	
    LimeSeg.runOptimisation(-1); 
	// Do the optimisation infinite (-1)
	//printoutmeasure(table);
    pathT= pathNew + "T_"+ currentframe.toString() + "/";		
    dirT = new File(pathT);
    dirT.mkdir(); // attempt to create the directory here
    if (dirT.exists()) {
       	for (Cell c:LimeSeg.allCells){
    		LimeSeg.currentCell=c;
    		CellT ct = c.getCellTAt(currentframe);
    		IOXmlPlyLimeSeg.saveCellTAsPly(ct, pathT + 'T' + currentframe.toString() + "lumen_"+ct.c.id_Cell +".ply");
    		printoutmeasure(table, ct.c.id_Cell);
		}
    }
}

// FINAL: save results
LimeSeg.clearOverlay();
LimeSeg.addAllCellsToOverlay();
LimeSeg.updateOverlay();
table.save(pathNew + "frames" + first_frame.toString() + "_" + last_frame.toString() + "table" + ".csv");
IJ.selectWindow("Results")
IJ.run("Close")
IJ.selectWindow("ROI Manager")
IJ.run("Close")	
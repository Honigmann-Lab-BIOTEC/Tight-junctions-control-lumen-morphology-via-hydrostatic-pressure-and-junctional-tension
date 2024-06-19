# Code for: "Tight junctions control-lumen morphology-via hydrostatic pressure and junctional tension"
Authors:Markus MukenhirnChen-Ho Wang, Tristan Guyomar, Matthew J. Bovyn, Michael F. Staddon, Rozemarijn E. van der Veen, Riccardo Maraspini, Linjie Lu, Cecilie Martin-Lemaitre, Masaki Sano, Martin Lehmann, Tetsuya Hiraiwa, Daniel Riveline, Alf Honigmann

## This repository contains:
1. Analysis code and example data for analysing Junctional recoil
2.  Analysis code and example data for Lumen/Cyst segmentation

## Latest release: 

## 1. Junctional recoil
## Contribution: Alf Honigmann
## Installation: 
System requirements: Programming language: MATLAB R2019a, Toolbox Image Processing required
OS Name: Windows 10 Enterprise LTSC
OS Type: 64-bit
## Run Junction recoil measurement
1. open Lasercutting_V4 in Matlab, add path for bfopen to open .czi example file file
2. run skript and open eg. Image 2 Block 1.czi and Image 2 Block 2.czi together
3. Mark each oposing tricellular junction before with a box before cut
4. a file called Image 2 Block 1-Recoil.mat is being created
5. open Calculate_recoil.m and run on all previously created files
6. get initial recoil curves and datapoints

## Lumen/Cyst segmentation
## Contribution: Anna Goncharova, Markus Mukenhirn, Alf Honigmann, Byung Ho Lee
## Installation: 
install Limeseg Fiji Plugin https://imagej.net/plugins/limeseg
follow limeseg documentation

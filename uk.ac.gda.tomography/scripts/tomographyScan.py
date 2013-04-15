"""
Performs software triggered tomography
"""

#from pcoDetectorWrapper import PCODetectorWrapper
from gda.jython.commands.ScannableCommands import createConcurrentScan
from gda.jython import InterfaceProvider

import sys
from gdascripts.parameters import beamline_parameters
from gdascripts.messages import handle_messages
from gda.scan import ScanPositionProvider
from gda.device.scannable import ScannableBase, ScannableUtils
from gda.device.scannable.scannablegroup import ScannableGroup
from java.lang import InterruptedException
from gdascripts.metadata.metadata_commands import setTitle

from gda.configuration.properties import LocalProperties

from gda.data.scan.datawriter.DataWriter import *
from gda.data.scan.datawriter.DefaultDataWriterFactory import createDataWriterFromFactory
from gda.data.scan.datawriter.IDataWriterExtender import *

from gda.data.scan.datawriter import *


class EnumPositionerDelegateScannable(ScannableBase):
    """
    Translate positions 0 and 1 to Close and Open
    """
    def __init__(self, name, delegate):
        self.name = name
        self.inputNames = [name]
        self.delegate = delegate
    def isBusy(self):
        return self.delegate.isBusy()
    def rawAsynchronousMoveTo(self, new_position):
        if int(new_position) == 1:
            self.delegate.asynchronousMoveTo("Open")
        elif int(new_position) == 0:
            self.delegate.asynchronousMoveTo("Close")
    def rawGetPosition(self):
        pos = self.delegate.getPosition()
        if pos == "Open":
            return 1 
        return 0


def make_tomoScanDevice(tomography_theta, tomography_shutter, tomography_translation,
                        tomography_optimizer, image_key, tomography_imageIndex):

    tomoScanDevice = ScannableGroup()
    tomoScanDevice.addGroupMember(tomography_theta)
    tomoScanDevice.addGroupMember(EnumPositionerDelegateScannable("tomography_shutter", tomography_shutter))
    tomoScanDevice.addGroupMember(tomography_translation)
    tomoScanDevice.addGroupMember(tomography_optimizer)
    tomoScanDevice.addGroupMember(image_key)
    tomoScanDevice.addGroupMember(tomography_imageIndex)
    tomoScanDevice.setName("tomoScanDevice")
    tomoScanDevice.configure()
    return tomoScanDevice

def generateScanPoints(inBeamPosition, outOfBeamPosition, theta_points, darkFieldInterval, flatFieldInterval,
              imagesPerDark, imagesPerFlat, optimizeBeamInterval, pattern="default"):
    numberSteps = len(theta_points) - 1
    optimizeBeamNo = 0
    optimizeBeamYes = 1
    shutterOpen = 1
    shutterClosed = 0
    shutterNoChange = 2
    scan_points = []
    if pattern == 'default' or pattern == 'DFPFD':
        print "Using scan-point pattern:", pattern
        theta_pos = theta_points[0]
        index = 0
        #Added shutterNoChange state for the shutter. The scan points are added using the (pseudo) ternary operator, 
        #if index is 0 then the shutterPosition is added to the scan point, else shutterNoChange is added to scan points.
        for i in range(imagesPerDark):
            scan_points.append((theta_pos, [shutterClosed, shutterNoChange][i != 0], inBeamPosition, optimizeBeamNo, image_key_dark, index)) #dark
            index = index + 1
        
        for i in range(imagesPerFlat): 
            scan_points.append((theta_pos, [shutterOpen, shutterNoChange][i != 0], outOfBeamPosition, optimizeBeamNo, image_key_flat, index)) #flat
            index = index + 1
        
        scan_points.append((theta_pos, shutterOpen, inBeamPosition, optimizeBeamNo, image_key_project, index)) #first
        index = index + 1
        imageSinceDark = 1
        imageSinceFlat = 1
        optimizeBeam = 0
        for i in range(numberSteps):
            theta_pos = theta_points[i + 1]
            scan_points.append((theta_pos, [shutterOpen, shutterNoChange][i != 0], inBeamPosition, optimizeBeamNo, image_key_project, index))#main image
            index = index + 1
            
            imageSinceFlat = imageSinceFlat + 1
            if imageSinceFlat == flatFieldInterval and flatFieldInterval != 0:
                for i in range(imagesPerFlat):
                    scan_points.append((theta_pos, [shutterOpen, shutterNoChange][i != 0], outOfBeamPosition, optimizeBeamNo, image_key_flat, index))
                    index = index + 1
                    imageSinceFlat = 0
            
            imageSinceDark = imageSinceDark + 1
            if imageSinceDark == darkFieldInterval and darkFieldInterval != 0:
                for i in range(imagesPerDark):
                    scan_points.append((theta_pos, [shutterClosed, shutterNoChange][i != 0], inBeamPosition, optimizeBeamNo, image_key_dark, index))
                    index = index + 1
                    imageSinceDark = 0
            
            optimizeBeam = optimizeBeam + 1
            if optimizeBeam == optimizeBeamInterval and optimizeBeamInterval != 0:
                scan_points.append((theta_pos, [shutterOpen, shutterNoChange][i != 0], inBeamPosition, optimizeBeamYes, image_key_project, index))
                index = index + 1
                optimizeBeam = 0
        
        #add dark and flat only if not done in last steps
        if imageSinceFlat != 0:
            for i in range(imagesPerFlat):
                scan_points.append((theta_pos, [shutterOpen, shutterNoChange][i != 0], outOfBeamPosition, optimizeBeamNo, image_key_flat, index)) #flat
                index = index + 1
        if imageSinceDark != 0:
            for i in range(imagesPerDark):
                scan_points.append((theta_pos, [shutterClosed, shutterNoChange][i != 0], inBeamPosition, optimizeBeamNo, image_key_dark, index)) #dark
                index = index + 1
    elif pattern == 'PFD':
        print "Using scan-point pattern:", pattern
        theta_pos = theta_points[0]
        index = 0
        
        # Don't take any dark or flat images at the beginning
        scan_points.append((theta_pos, shutterOpen, inBeamPosition, optimizeBeamNo, image_key_project, index)) #first
        index = index + 1
        imageSinceDark = 1
        imageSinceFlat = 1
        optimizeBeam = 0
        for i in range(numberSteps):
            theta_pos = theta_points[i + 1]
            scan_points.append((theta_pos, [shutterOpen, shutterNoChange][i != 0], inBeamPosition, optimizeBeamNo, image_key_project, index))#main image
            index = index + 1
            
            imageSinceFlat = imageSinceFlat + 1
            if imageSinceFlat == flatFieldInterval and flatFieldInterval != 0:
                for i in range(imagesPerFlat):
                    scan_points.append((theta_pos, [shutterOpen, shutterNoChange][i != 0], outOfBeamPosition, optimizeBeamNo, image_key_flat, index))
                    index = index + 1
                    imageSinceFlat = 0
            
            imageSinceDark = imageSinceDark + 1
            if imageSinceDark == darkFieldInterval and darkFieldInterval != 0:
                for i in range(imagesPerDark):
                    scan_points.append((theta_pos, [shutterClosed, shutterNoChange][i != 0], inBeamPosition, optimizeBeamNo, image_key_dark, index))
                    index = index + 1
                    imageSinceDark = 0
            
            optimizeBeam = optimizeBeam + 1
            if optimizeBeam == optimizeBeamInterval and optimizeBeamInterval != 0:
                scan_points.append((theta_pos, [shutterOpen, shutterNoChange][i != 0], inBeamPosition, optimizeBeamYes, image_key_project, index))
                index = index + 1
                optimizeBeam = 0
        
        #add dark and flat only if not done in last steps
        if imageSinceFlat != 0:
            for i in range(imagesPerFlat):
                scan_points.append((theta_pos, [shutterOpen, shutterNoChange][i != 0], outOfBeamPosition, optimizeBeamNo, image_key_flat, index)) #flat
                index = index + 1
        if imageSinceDark != 0:
            for i in range(imagesPerDark):
                scan_points.append((theta_pos, [shutterClosed, shutterNoChange][i != 0], inBeamPosition, optimizeBeamNo, image_key_dark, index)) #dark
                index = index + 1
    else:
        print "Unsupported scan-point pattern:", pattern
    
    return scan_points

def addNXTomoSubentry(scanObject, tomography_detector_name, tomography_theta_name):
    if scanObject is None:
        raise "Input scanObject must not be None"
    
    instrument_detector_data_target = "entry1/instrument/" + tomography_detector_name + "/image_data"
    sample_rotation_angle_target = "entry1/instrument/tomoScanDevice/" + tomography_theta_name
    #print "instrument_detector_data_target = " + instrument_detector_data_target
    #print "sample_rotation_angle_target = " + sample_rotation_angle_target
    
    nxLinkCreator = NXTomoEntryLinkCreator()
    nxLinkCreator.setInstrument_detector_data_target(instrument_detector_data_target)
    nxLinkCreator.setSample_rotation_angle_target(sample_rotation_angle_target)
    nxLinkCreator.setTitle_target("entry1/title")
    
    nxLinkCreator.afterPropertiesSet()
    
    dataWriter = createDataWriterFromFactory()
    subEntryWriter = NXSubEntryWriter(nxLinkCreator)
    dataWriter.addDataWriterExtender(subEntryWriter)
    scanObject.setDataWriter(dataWriter)

def reportJythonNamespaceMapping():
    jns=beamline_parameters.JythonNameSpaceMapping()
    objectOfInterest = {}
    objectOfInterest['tomography_theta'] = jns.tomography_theta
    objectOfInterest['tomography_shutter'] = jns.tomography_shutter
    objectOfInterest['tomography_translation'] = jns.tomography_translation
    objectOfInterest['tomography_detector'] = jns.tomography_detector
    objectOfInterest['tomography_camera_stage'] = jns.tomography_camera_stage
   
    for key, val in objectOfInterest.iteritems():
        print key + ' = ' + str(val)
    msg = "\n These mappings can be changed by editing a file named jythonNamespaceMapping_live, "
    msg += "\n located in GDA Client under Scripts: Config (this can be done by beamline staff)."
    print msg

class   tomoScan_positions(ScanPositionProvider):
    def __init__(self, start, stop, step, darkFieldInterval, imagesPerDark, flatFieldInterval, imagesPerFlat,
             inBeamPosition, outOfBeamPosition, optimizeBeamInterval, points):
        self.start = start
        self.stop = stop
        self.step = step
        self.darkFieldInterval = darkFieldInterval
        self.imagesPerDark = imagesPerDark
        self.flatFieldInterval = flatFieldInterval
        self.imagesPerFlat = imagesPerFlat
        self.inBeamPosition = inBeamPosition
        self.outOfBeamPosition = outOfBeamPosition
        self.optimizeBeamInterval = optimizeBeamInterval
        self.points = points

    def get(self, index):
        return self.points[index]
    
    def size(self):
        return len(self.points)
    
    def __str__(self):
        return "Start: %f Stop: %f Step: %f Darks every:%d imagesPerDark:%d Flats every:%d imagesPerFlat:%d InBeamPosition:%f OutOfBeamPosition:%f Optimize every:%d numImages %d " % \
            (self.start, self.stop, self.step, self.darkFieldInterval, self.imagesPerDark, self.flatFieldInterval, self.imagesPerFlat, self.inBeamPosition, self.outOfBeamPosition, self.optimizeBeamInterval, self.size()) 
    def toString(self):
        return self.__str__()

from gda.device.scannable import SimpleScannable
image_key_dark = 2
image_key_flat = 1 # also known as bright
image_key_project = 0 # also known as sample


"""
perform a simple tomography scan
"""
def tomoScan(description, inBeamPosition, outOfBeamPosition, exposureTime=1., start=0., stop=180., step=0.1, darkFieldInterval=0, flatFieldInterval=0,
              imagesPerDark=10, imagesPerFlat=10, optimizeBeamInterval=0, pattern="default", additionalScannables=[]):
    """
    Function to collect a tomogram
    Arguments:
    description - description of the scan(or the sample that is being scanned. This is generally user-specific information that may be used to map to this scan later and is available in the NeXus file)
    inBeamPosition - position of X drive to move sample into the beam to take a projection
    outOfBeamPosition - position of X drive to move sample out of the beam to take a flat field image
    exposureTime - exposure time in seconds (default = 1.0)
    start - first rotation angle (default=0.0)
    stop  - last rotation angle (default=180.0)
    step - rotation step size (default = 0.1)
    darkFieldInterval - number of projections between each dark-field sub-sequence. NOTE: at least 1 dark is ALWAYS taken both at the start and end of a tomogram (default=0: use this value if you DON'T want to take any darks between projections)
    flatFieldInterval - number of projections between each flat-field sub-sequence. NOTE: at least 1 flat is ALWAYS taken both at the start and end of a tomogram (default=0: use this value if you DON'T want to take any flats between projections)
    imagesPerDark - number of images to be taken for each dark-field sub-sequence (default=10)
    imagesPerFlat - number of images to be taken for each flat-field sub-sequence (default=10)
    
    General scan sequence is: D, F, P,..., P, F, D
    where D stands for dark field, F - for flat field, and P - for projection.
    """
    dataFormat = LocalProperties.get("gda.data.scan.datawriter.dataFormat")
    try:
        darkFieldInterval = int(darkFieldInterval)
        flatFieldInterval = int(flatFieldInterval)
        optimizeBeamInterval = int(optimizeBeamInterval)
        
        jns = beamline_parameters.JythonNameSpaceMapping(InterfaceProvider.getJythonNamespace())
        tomography_theta = jns.tomography_theta
        if tomography_theta is None:
            raise "tomography_theta is not defined in Jython namespace"
        tomography_shutter = jns.tomography_shutter
        if tomography_shutter is None:
            raise "tomography_shutter is not defined in Jython namespace"
        tomography_translation = jns.tomography_translation
        if tomography_translation is None:
            raise "tomography_translation is not defined in Jython namespace"
        
        tomography_detector = jns.tomography_detector
        if tomography_detector is None:
            raise "tomography_detector is not defined in Jython namespace"

        tomography_optimizer = jns.tomography_optimizer
        if tomography_optimizer is None:
            raise "tomography_optimizer is not defined in Jython namespace"

        tomography_time = jns.tomography_time
        if tomography_time is None:
            raise "tomography_time is not defined in Jython namespace"
        
        tomography_beammonitor = jns.tomography_beammonitor
        if tomography_beammonitor is None:
            raise "tomography_beammonitor is not defined in Jython namespace"
        
        tomography_camera_stage = jns.tomography_camera_stage
        if tomography_camera_stage is None:
            raise "tomography_camera_stage is not defined in Jython namespace"
        
        index = SimpleScannable()
        index.setCurrentPosition(0.0)
        index.setInputNames(["imageNumber"])
        index.setName("imageNumber")
        index.configure()
        
        image_key = SimpleScannable()
        image_key.setCurrentPosition(0.0)
        image_key.setInputNames(["image_key"])
        image_key.setName("image_key")
        image_key.configure()

        tomoScanDevice = make_tomoScanDevice(tomography_theta, tomography_shutter,
                                             tomography_translation, tomography_optimizer, image_key, index)

#        return tomoScanDevice
        #generate list of positions
        numberSteps = ScannableUtils.getNumberSteps(tomography_theta, start, stop, step)
        theta_points = []
        theta_points.append(start)
        previousPoint = start
        for i in range(numberSteps):
            nextPoint = ScannableUtils.calculateNextPoint(previousPoint, step);
            theta_points.append(nextPoint)
            previousPoint = nextPoint
        
        #generateScanPoints
        optimizeBeamNo = 0
        optimizeBeamYes = 1
        shutterOpen = 1
        shutterClosed = 0
        shutterNoChange = 2
        scan_points = []
        theta_pos = theta_points[0]
        index = 0
        #Added shutterNoChange state for the shutter. The scan points are added using the (pseudo) ternary operator, 
        #if index is 0 then the shutterPosition is added to the scan point, else shutterNoChange is added to scan points.
        for i in range(imagesPerDark):
            scan_points.append((theta_pos, [shutterClosed, shutterNoChange][i != 0], inBeamPosition, optimizeBeamNo, image_key_dark, index)) #dark
            index = index + 1
                    
        for i in range(imagesPerFlat): 
            scan_points.append((theta_pos, [shutterOpen, shutterNoChange][i != 0], outOfBeamPosition, optimizeBeamNo, image_key_flat, index)) #flat
            index = index + 1        
        scan_points.append((theta_pos, shutterOpen, inBeamPosition, optimizeBeamNo, image_key_project, index)) #first
        index = index + 1        
        imageSinceDark = 1
        imageSinceFlat = 1
        optimizeBeam = 0
        for i in range(numberSteps):
            theta_pos = theta_points[i + 1]
            scan_points.append((theta_pos, [shutterOpen, shutterNoChange][i != 0], inBeamPosition, optimizeBeamNo, image_key_project, index))#main image
            index = index + 1        
            
            imageSinceFlat = imageSinceFlat + 1
            if imageSinceFlat == flatFieldInterval and flatFieldInterval != 0:
                for i in range(imagesPerFlat):
                    scan_points.append((theta_pos, [shutterOpen, shutterNoChange][i != 0], outOfBeamPosition, optimizeBeamNo, image_key_flat, index))
                    index = index + 1        
                    imageSinceFlat = 0
            
            imageSinceDark = imageSinceDark + 1
            if imageSinceDark == darkFieldInterval and darkFieldInterval != 0:
                for i in range(imagesPerDark):
                    scan_points.append((theta_pos, [shutterClosed, shutterNoChange][i != 0], inBeamPosition, optimizeBeamNo, image_key_dark, index))
                    index = index + 1        
                    imageSinceDark = 0

            optimizeBeam = optimizeBeam + 1
            if optimizeBeam == optimizeBeamInterval and optimizeBeamInterval != 0:
                scan_points.append((theta_pos, [shutterOpen, shutterNoChange][i != 0], inBeamPosition, optimizeBeamYes, image_key_project, index))
                index = index + 1        
                optimizeBeam = 0
                
        #add dark and flat only if not done in last steps
        if imageSinceFlat != 0:
            for i in range(imagesPerFlat):
                scan_points.append((theta_pos, [shutterOpen, shutterNoChange][i != 0], outOfBeamPosition, optimizeBeamNo, image_key_flat, index)) #flat
                index = index + 1
        if imageSinceDark != 0:
            for i in range(imagesPerDark):
                scan_points.append((theta_pos, [shutterClosed, shutterNoChange][i != 0], inBeamPosition, optimizeBeamNo, image_key_dark, index)) #dark
                index = index + 1        
        scan_points1 = generateScanPoints(inBeamPosition, outOfBeamPosition, theta_points, darkFieldInterval, flatFieldInterval,
              imagesPerDark, imagesPerFlat, optimizeBeamInterval, pattern=pattern)
        if pattern == 'default' or pattern == 'DFPFD':
            i = 0
            for pt1 in scan_points1:
                pt = scan_points[i]
                if pt1 != pt:
                    print "Mismatch - please tell Kaz about your scan and its arguments!"
                    print "i = ", i
                    print "pt = ", pt
                    print "pt1 = ", pt1
                i += 1
        #return None
        positionProvider = tomoScan_positions(start, stop, step, darkFieldInterval, imagesPerDark, flatFieldInterval, imagesPerFlat, \
                                               inBeamPosition, outOfBeamPosition, optimizeBeamInterval, scan_points) 
        scan_args = [tomoScanDevice, positionProvider, tomography_time, tomography_beammonitor, tomography_detector, exposureTime, tomography_camera_stage]
        for scannable in additionalScannables:
            scan_args.append(scannable)
        ''' setting the description provided as the title'''
        if not description == None: 
            setTitle(description)
        else :
            setTitle("undefined")
        
        dataFormat = LocalProperties.get("gda.data.scan.datawriter.dataFormat")
        if not dataFormat == "NexusDataWriter":
            handle_messages.simpleLog("Data format inconsistent. Setting 'gda.data.scan.datawriter.dataFormat' to 'NexusDataWriter'")
            LocalProperties.set("gda.data.scan.datawriter.dataFormat", "NexusDataWriter")
        scanObject = createConcurrentScan(scan_args)
        addNXTomoSubentry(scanObject, tomography_detector.name, tomography_theta.name)
        scanObject.runScan()
        return scanObject;
    except InterruptedException:
        exceptionType, exception, traceback = sys.exc_info()
        handle_messages.log(None, "User interrupted the scan", exceptionType, exception, traceback, False)
        raise InterruptedException("User interrupted the scan")
    except:
        exceptionType, exception, traceback = sys.exc_info()
        handle_messages.log(None, "Error during tomography scan", exceptionType, exception, traceback, False)
        raise Exception("Error during tomography scan", exception)
    finally:
        handle_messages.simpleLog("Data Format reset to the original setting: " + dataFormat)
        LocalProperties.set("gda.data.scan.datawriter.dataFormat", dataFormat)

def __test1_tomoScan():
    jns = beamline_parameters.JythonNameSpaceMapping()    
    sc = tomoScan(step=5, darkFieldInterval=5, flatFieldInterval=5,
             inBeamPosition=0., outOfBeamPosition=10., exposureTime=1.)
    print `jns`
    lsdp = jns.lastScanDataPoint()
    positions = lsdp.getPositionsAsDoubles()
    if positions[0] != 180. or positions[4] != 54.:
        print "Error - points are not correct :" + `positions`
    return sc

def __test2_tomoScan():
    jns = beamline_parameters.JythonNameSpaceMapping()    
    sc = tomoScan(step=5, darkFieldInterval=5, flatFieldInterval=0,
             inBeamPosition=0., outOfBeamPosition=10., exposureTime=1.)
    lsdp = jns.lastScanDataPoint()
    positions = lsdp.getPositionsAsDoubles()
    if positions[0] != 180. or positions[4] != 47.:
        print "Error - points are not correct :" + `positions`
    return sc

def __test3_tomoScan():
    jns = beamline_parameters.JythonNameSpaceMapping()    
    sc = tomoScan(step=5, darkFieldInterval=0, flatFieldInterval=5,
             inBeamPosition=0., outOfBeamPosition=10., exposureTime=1.)
    lsdp = jns.lastScanDataPoint()
    positions = lsdp.getPositionsAsDoubles()
    if positions[0] != 180. or positions[4] != 47.:
        print "Error - points are not correct :" + `positions`
    return sc

def __test4_tomoScan():
    jns = beamline_parameters.JythonNameSpaceMapping()    
    sc = tomoScan(step=5, darkFieldInterval=0, flatFieldInterval=0,
             inBeamPosition=0., outOfBeamPosition=10., exposureTime=1.)
    lsdp = jns.lastScanDataPoint()
    positions = lsdp.getPositionsAsDoubles()
    if positions[0] != 180. or positions[4] != 40.:
        print "Error - points are not correct :" + `positions`
    return sc

def __test5_tomoScan():
    """
    Test optimizeBeamInterval=10
    """
    jns = beamline_parameters.JythonNameSpaceMapping()    
    sc = tomoScan(step=5, darkFieldInterval=0, flatFieldInterval=0,
             inBeamPosition=0., outOfBeamPosition=10., exposureTime=1., optimizeBeamInterval=10)
    lsdp = jns.lastScanDataPoint()
    positions = lsdp.getPositionsAsDoubles()
    if positions[0] != 180. or positions[4] != 43.:
        print "Error - points are not correct :" + `positions`
    return sc

def test_all():
    __test1_tomoScan()
    __test2_tomoScan()
    __test3_tomoScan()
    __test4_tomoScan()

def standardtomoScan():
    jns = beamline_parameters.JythonNameSpaceMapping()    
    sc = tomoScan(step=1, darkFieldInterval=0, flatFieldInterval=20,
             inBeamPosition=0., outOfBeamPosition=10., exposureTime=1.)
    lsdp = jns.lastScanDataPoint()
    positions = lsdp.getPositionsAsDoubles()
    if positions[0] != 180. or positions[4] != 40.:
        print "Error - points are not correct :" + `positions`
    return sc

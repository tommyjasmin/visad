import javax.swing.*;

import java.awt.*;

import java.awt.event.*;

import java.rmi.RemoteException;

import visad.*;

import visad.java2d.DisplayImplJ2D;

public class Test38
	extends TestSkeleton
{
  public Test38() { }

  public Test38(String args[])
	throws VisADException, RemoteException
  {
    super(args);
  }

  DisplayImpl[] setupData()
	throws VisADException, RemoteException
  {
    RealType[] types = {RealType.Latitude, RealType.Longitude};
    RealTupleType earth_location = new RealTupleType(types);
    RealType vis_radiance = new RealType("vis_radiance", null, null);
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);
    FunctionType image_tuple = new FunctionType(earth_location, radiance);

    int size = 64;
    FlatField imaget1 = FlatField.makeField(image_tuple, size, true);

    DisplayImpl display1 = new DisplayImplJ2D("display1");
    display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    display1.addMap(new ScalarMap(ir_radiance, Display.Green));
    display1.addMap(new ConstantMap(0.5, Display.Blue));
    display1.addMap(new ConstantMap(0.5, Display.Red));
    ScalarMap map1contour;
    map1contour = new ScalarMap(vis_radiance, Display.IsoContour);
    display1.addMap(map1contour);
    ContourControl control1contour;
    control1contour = (ContourControl) map1contour.getControl();
    control1contour.enableContours(true);

    JFrame jframe = new JFrame("irregular contours in Java2D");
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    jframe.setContentPane((JPanel) display1.getComponent());
    jframe.pack();
    jframe.setVisible(true);

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    display1.addReference(ref_imaget1, null);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  public String toString()
  {
    return ": colored contours from irregular grids in Java2D";
  }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test38 t = new Test38(args);
  }
}

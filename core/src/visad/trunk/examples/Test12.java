import javax.swing.*;

import java.awt.*;

import java.awt.event.*;

import java.rmi.RemoteException;

import visad.*;

import visad.util.LabeledRGBWidget;
import visad.java3d.DisplayImplJ3D;

public class Test12
	extends TestSkeleton
{
  public Test12() { }

  public Test12(String args[])
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

    int size = 32;
    FlatField imaget1 = FlatField.makeField(image_tuple, size, false);

    DisplayImpl display1;
    display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
    display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));

    ScalarMap color1map = new ScalarMap(ir_radiance, Display.RGB);
    display1.addMap(color1map);

    LabeledRGBWidget lw =
      new LabeledRGBWidget(color1map, 0.0f, 32.0f);

    JFrame jframe = new JFrame("VisAD Color Widget");
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });
    JPanel big_panel = new JPanel();
    big_panel.setLayout(new BorderLayout());
    big_panel.add("Center", lw);
    jframe.setContentPane(big_panel);
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
    return ": 2-D surface and ColorWidget";
  }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test12 t = new Test12(args);
  }
}

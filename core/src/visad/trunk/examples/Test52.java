import javax.swing.*;

import java.awt.*;

import java.awt.event.*;

import java.awt.image.BufferedImage;

import java.rmi.RemoteException;

import visad.*;

import visad.java2d.DisplayImplJ2D;

public class Test52
	extends TestSkeleton
{
  public Test52() { }

  public Test52(String args[])
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

    DisplayImpl display1 = new DisplayImplJ2D("display1", 300, 300);
    display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    display1.addMap(new ScalarMap(vis_radiance, Display.RGB));

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    display1.addReference(ref_imaget1, null);

/*
    JFrame jframe = new JFrame("capture image in Java2D");
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    jframe.setContentPane((JPanel) display1.getComponent());
    jframe.pack();
    jframe.setVisible(true);
*/

    JFrame jframe1 = new JFrame("captured image from Java2D");
    jframe1.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    JPanel panel1 = new JPanel();
    panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
    panel1.setAlignmentY(JPanel.TOP_ALIGNMENT);
    panel1.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    jframe1.setContentPane(panel1);
    jframe1.pack();
    jframe1.setVisible(true);
    jframe1.setSize(300, 300);

    while (true) {
      Graphics gp = panel1.getGraphics();
      BufferedImage image = display1.getImage();
      gp.drawImage(image, 0, 0, panel1);
      gp.dispose();
      try {
        Thread.sleep(1000);
      }
      catch (InterruptedException e) {
      }
    }

//    DisplayImpl[] dpys = new DisplayImpl[1];
//    dpys[0] = display1;

//    return dpys;
  }

  public String toString()
  {
    return ": test image capture from offscreen in Java2D";
  }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test52 t = new Test52(args);
  }
}

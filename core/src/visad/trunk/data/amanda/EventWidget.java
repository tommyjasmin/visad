package visad.data.amanda;

import java.rmi.RemoteException;

import java.text.DateFormat;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import visad.AnimationControl;
import visad.CellImpl;
import visad.DataReferenceImpl;
import visad.FieldImpl;
import visad.Real;
import visad.ScalarMap;
import visad.VisADException;

import visad.data.amanda.AmandaFile;
import visad.data.amanda.Event;

import visad.util.VisADSlider;

public class EventWidget
  extends JPanel
{
  private AmandaFile fileData;
  private DataReferenceImpl eventRef;
  private AnimationControl animCtl;

  private GregorianCalendar cal;
  private DateFormat fmt;

  private VisADSlider slider;
  private int sliderLength;

  private JLabel dateLabel;

  private TrackWidget trackWidget;
  private HistogramWidget histoWidget;

  private Event thisEvent;

  public EventWidget(AmandaFile fileData, DataReferenceImpl eventRef,
                     DataReferenceImpl trackRef, AnimationControl animCtl,
                     HistogramWidget histoWidget)
    throws RemoteException, VisADException
  {
    this(fileData, eventRef, trackRef, animCtl, null, histoWidget);
  }

  public EventWidget(AmandaFile fileData, DataReferenceImpl eventRef,
                     DataReferenceImpl trackRef, AnimationControl animCtl,
                     ScalarMap trackMap, HistogramWidget histoWidget)
    throws RemoteException, VisADException
  {
    super();

    this.fileData = fileData;
    this.eventRef = eventRef;
    this.animCtl = animCtl;
    this.histoWidget = histoWidget;

    cal = new GregorianCalendar();

    fmt =  DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.FULL);
    fmt.setTimeZone(TimeZone.getTimeZone("GMT"));

    thisEvent = null;

    // initialize before buildSlider() in case it triggers a reference to them
    if (trackMap == null) {
      trackWidget = null;
    } else {
      trackWidget = new TrackWidget(trackMap, trackRef);
    }
    dateLabel = new JLabel();

    slider = buildSlider(fileData.getNumberOfEvents());

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    add(histoWidget);
    add(slider);
    add(dateLabel);
    if (trackWidget != null) add(trackWidget);
  }

  private VisADSlider buildSlider(int initialLength)
    throws RemoteException, VisADException
  {
    final DataReferenceImpl eSliderRef = new DataReferenceImpl("eSlider");

    sliderLength = initialLength;

    VisADSlider slider = new VisADSlider("event", 0, initialLength - 1, 0, 1.0,
                                         eSliderRef, Event.indexType, true);
    slider.hardcodeSizePercent(110); // leave room for label changes

    // call setIndex() whenever slider changes
    CellImpl cell = new CellImpl() {
      public void doAction()
        throws RemoteException, VisADException
      {
        Real r = (Real )eSliderRef.getData();
        if (r != null) {
          int index = (int )r.getValue();
          if (index < 0) {
            index = 0;
          } else if (index > sliderLength) {
            index = sliderLength;
          }
          indexChanged(index);
        }
      }
    };
    cell.addReference(eSliderRef);

    return slider;
  }

  private final Date getDate(int year, int day, double time)
  {
    final int hr = (int )((time + 3599.0) / 3600.0);
    time -= (double )hr * 3600.0;

    final int min = (int )((time + 59.0) / 60.0);
    time -= (double )min * 60.0;

    final int sec = (int )time;
    time -= (double )sec;

    final int milli = (int )(time * 1000.0);

    cal.clear();

    cal.set(GregorianCalendar.YEAR, year);
    cal.set(GregorianCalendar.DAY_OF_YEAR, day);
    cal.set(GregorianCalendar.HOUR_OF_DAY, hr);
    cal.set(GregorianCalendar.MINUTE, min);
    cal.set(GregorianCalendar.SECOND, sec);
    cal.set(GregorianCalendar.MILLISECOND, milli);
    cal.set(GregorianCalendar.DST_OFFSET, 0);

    return cal.getTime();
  }

  public final Event getEvent() { return thisEvent; }
  public final JLabel getLabel() { return dateLabel; }
  public final VisADSlider getSlider() { return slider; }
  public final TrackWidget getTrackWidget() { return trackWidget; }

  /**
   * This method is called whenever the event index is changed
   */
  private void indexChanged(int index)
    throws RemoteException, VisADException
  {
    thisEvent = fileData.getEvent(index);
    if (thisEvent == null) {
      eventRef.setData(Event.missing);
      dateLabel.setText("*** NO DATE ***");
    } else {
      final FieldImpl hitSeq = thisEvent.makeHitSequence();
      eventRef.setData(hitSeq);

      animCtl.setSet(hitSeq.getDomainSet());

      Date date = getDate(thisEvent.getYear(), thisEvent.getDay(),
                          thisEvent.getTime());
      dateLabel.setText(fmt.format(date));

    }

    histoWidget.setEvent(thisEvent);

    if (trackWidget != null) trackWidget.setEvent(thisEvent);
    this.invalidate();
  }
}

package visad.util;

import java.util.ArrayList;

public class CmdlineParser
{
  private String mainName;
  private ArrayList list;

  public CmdlineParser(Object mainClass)
  {
    String className = mainClass.getClass().getName();
    int pt = className.lastIndexOf('.');
    final int ds = className.lastIndexOf('$');
    if (ds > pt) {
      pt = ds;
    }

    mainName = className.substring(pt == -1 ? 0 : pt + 1);
    list = null;

    if (mainClass instanceof CmdlineConsumer) {
      addConsumer((CmdlineConsumer )mainClass);
    }
  }

  /**
   * Add a command-line argument/keyword consumer.
   *
   * consumer Class which implements <tt>CmdlineConsumer</tt>.
   */
  public void addConsumer(CmdlineConsumer consumer)
  {
    if (list == null) {
      list = new ArrayList();
    }

    list.add(consumer);
  }

  /**
   * Get the name of the main class.
   *
   * @return main class name
   */
  public String getMainClassName() { return mainName; }

  public boolean processArgs(String[] args)
  {
    boolean usage = false;

    // if the are no consumers or arguments, we're done
    if (list == null || args == null) {
      return true;
    }

    // add consumers from newest to oldest
    CmdlineConsumer[] consumers = new CmdlineConsumer[list.size()];
    for (int c = 0; c < consumers.length; c++) {
      consumers[c] = (CmdlineConsumer )list.get(consumers.length - (c + 1));
    }

    for (int c = 0; c < consumers.length; c++) {
      consumers[c].initializeArgs();
    }

    for (int i = 0; !usage && i < args.length; i++) {
      if (args[i].length() > 0 && args[i].charAt(0) == '-') {
        char ch = args[i].charAt(1);

        String str, result;

        boolean strInOption = false;
        if (args[i].length() > 2) {
          str = args[i].substring(2);
          strInOption = true;
        } else if ((i + 1) < args.length) {
          str = args[i+1];
        } else {
          str = null;
        }

        int handled = 0;
        for (int c = 0; c < consumers.length; c++) {
          handled = consumers[c].checkExtraOption(mainName, ch, str);
          if (handled > 0) {
            if (handled > 1) {
              if (strInOption) {
                handled = 1;
              } else {
                handled = 2;
              }
            }
            i += (handled - 1);
            break;
          }
        }

        if (handled == 0) {
          System.err.println(mainName + ": Unknown option \"-" + ch + "\"");
          usage = true;
        }
      } else {
        int handled = 0;
        for (int c = 0; c < consumers.length; c++) {
          handled = consumers[c].checkExtraKeyword(mainName, i, args);
          if (handled > 0) {
            i += (handled - 1);
            break;
          }
        }

        if (handled == 0) {
          System.err.println(mainName + ": Unknown keyword \"" +
                             args[i] + "\"");
          usage = true;
        }
      }
    }

    for (int c = 0; !usage && c < consumers.length; c++) {
      usage &= !consumers[c].finalizeArgs(mainName);
    }

    if (usage) {
      StringBuffer buf = new StringBuffer("Usage: " + mainName);
      for (int c = 0; c < consumers.length; c++) {
        buf.append(consumers[c].extraOptionUsage());
      }
      for (int c = 0; c < consumers.length; c++) {
        buf.append(consumers[c].extraKeywordUsage());
      }
      System.err.println(buf.toString());
    }

    return !usage;
  }
}

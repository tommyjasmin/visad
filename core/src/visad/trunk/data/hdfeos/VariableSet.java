//
// VariableSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad.data.hdfeos;

import java.util.*;

public class VariableSet 
{
  Vector varSet;
  private boolean finished = false;

  VariableSet() 
  {
    varSet = new Vector();
  }

  public void add( Variable var ) 
  {
    varSet.addElement( var );
  }

  public void setToFinished() 
  {
    finished = true;
  }

  public int getSize() 
  {
    int size = varSet.size(); 
    return size;
  }

  public Variable getElement( int ii ) 
  {
    Variable obj = (Variable)varSet.elementAt( ii );
    return obj;
  }

  public Variable[] getElements()
  {
    Variable[] vars = new Variable[getSize()];
    for ( int ii = 0; ii < getSize(); ii++ ) {
      vars[ii] = getElement(ii);
    }
    return vars;
  }

  public Variable getByName( String varName ) 
  {
    int size = this.getSize();

    for ( int ii = 0; ii < size; ii++ ) 
    {
      Variable obj = (Variable) varSet.elementAt(ii);

      String name = obj.getName();

      if (  name.equals( varName ) ) {
        return obj;
      }
    }
    return null;
  }

  public VariableSet getSubset( DimensionSet d_set )
  {
    VariableSet v_set = new VariableSet();

    for ( int ii = 0; ii < this.getSize(); ii++ ) 
    {
      if( ((this.getElement(ii)).getDimSet()).sameSetSameOrder( d_set ) ) 
      {
        v_set.add( this.getElement(ii) );
      }
    }
       
    if ( v_set.getSize() == 0 ) {
      return null;
    }
    else {
      return v_set;
    }
  }

  public Variable isCoordVar( int index )
  {
    Variable var = getElement( index );
    if ( var.isCoordVar() ) {
      return var;
    }
    else {
      return null;
    }
  }

  public boolean isEmpty() 
  {
    return varSet.isEmpty();
  }

  public Enumeration getEnum()
  {
    Enumeration e = varSet.elements();
    return e;
  }

  public String toString() 
  {
    String str = "VariableSet: \n";

    for ( int ii = 0; ii < this.getSize(); ii++ )
    {
      str = str + "  "+((this.getElement(ii)).toString())+"\n";
    }
    return str;
  }
}

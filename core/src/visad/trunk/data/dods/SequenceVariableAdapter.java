/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Vector;
import visad.*;
import visad.data.*;
import visad.data.in.*;

/**
 * Provides support for adapting DODS {@link DSequence} variables to the
 * VisAD data-import context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class SequenceVariableAdapter
    extends	VariableAdapter
{
    private final FunctionType		funcType;
    private final VariableAdapter[]	adapters;
    private final SimpleSet[]		repSets;

    private SequenceVariableAdapter(
	    DSequence sequence,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	int		count = sequence.elementCount();
	ArrayList	setList = new ArrayList();
	adapters = new VariableAdapter[count];
	for (int i = 0; i < count; ++i)
	{
	    BaseType	template;
	    try
	    {
		template = sequence.getVar(i);
	    }
	    catch (NoSuchVariableException e)
	    {
		throw new BadFormException(
		    getClass().getName() + ".data(DSequence,...): " +
		    "Couldn't get sequence-variable " + i);
	    }
	    adapters[i] =
		factory.variableAdapter(
		    template, attributeTable(table, template));
	    SimpleSet[]	setArray = adapters[i].getRepresentationalSets();
	    for (int j = 0; j < setArray.length; ++j)
		setList.add(setArray[j]);
	}
	funcType = new FunctionType(RealType.Generic, mathType(adapters));
	repSets = (SimpleSet[])setList.toArray(new SimpleSet[0]);
    }

    /**
     * Returns an instance of this class corresponding to a DODS {@link 
     * DSequence}.
     *
     * @param sequence		The DODS variable.  Only the DODS metadata is 
     *				used: the variable needn't have any actual data.
     * @param table		The DODS attribute table associated with the
     *				variable.
     * @param factory		A factory for creating variable adapters.
     * @return			An instance of this class corresponding to the
     *				input arguments.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public static SequenceVariableAdapter sequenceVariableAdapter(
	    DSequence sequence,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new SequenceVariableAdapter(sequence, table, factory);
    }

    /**
     * Returns the VisAD {@link MathType} of this instance.
     *
     * @return			The MathType of this instance.
     */
    public MathType getMathType()
    {
	return funcType;
    }

    /**
     * Returns the VisAD {@link Set}s that will be used to represent this
     * instances data values in the range of a VisAD {@link FlatField}.  The
     * same array is returned each time, so modifications to the array will
     * affect all subsequent invocations of this method.
     *
     * @return			The VisAD Sets used to represent the data values
     *				in the range of a FlatField.  WARNING: Modify
     *				only under duress.
     */
    public SimpleSet[] getRepresentationalSets()
    {
	return repSets;
    }

    /**
     * Returns the VisAD {@link DataImpl} corresponding to a DODS {@link 
     * DSequence}.
     *
     * @param sequence		The DODS variable to have the corresponding
     *				VisAD data object returned.  The variable
     *				must be compatible with the variable used to
     *				construct this instance.
     * @return			The VisAD data object of this instance.  The
     *				class of the object will be {@link 
     *				FileFlatField}, {@link FlatField}, or {@link
     *				FieldImpl}.
     * @throws VisADException	VisAD failure.  Possibly the variable wasn't
     *				compatible with the variable used to construct
     *				this instance.
     * @throws RemoteException	Java RMI failure.
     */
    public DataImpl data(DSequence sequence)
	throws VisADException, RemoteException
    {
	SampledSet	domain = new Integer1DSet(sequence.getRowCount());
	FieldImpl	field;
	if (funcType.getFlat())
	{
	    /*
	     * TODO: Either modify FileFlatField or subclass it to support
	     * a domainFactor(...) method that uses FileFlatField-s.
	     */
	    field =
		new FileFlatField(
		    new SequenceAccessor(domain, sequence), getCacheStrategy());
	}
	else
	{
	    field = new FieldImpl(funcType, domain);
	    setField(sequence, field);
	}
	return field;
    }

    /**
     * Sets the range of a compatible VisAD {@link FieldImpl} from a DODS
     * {@link DSequence}.
     *
     * @param sequence		A DODS variable whose data values will be
     *				used to set the VisAD Field.
     * @param field		A VisAD field whose range values will be set.
     *				The field must be compatible with the DODS
     *				sequence.
     * @throws VisADException	VisAD failure.  Possibly the DODS variable and
     *				the VisAD field are incompatible.
     * @throws RemoteException	Java RMI failure.
     */
    protected void setField(DSequence sequence, FieldImpl field)
	throws VisADException, RemoteException
    {
	int		sampleCount = field.getLength();
	DataImpl	data;
	MathType	rangeType = funcType.getRange();
	for (int i = 0; i < sampleCount; ++i)
	{
	    Vector	row = sequence.getRow(i);
	    if (adapters.length == 1)
	    {
		data = adapters[0].data((BaseType)row.get(0));
	    }
	    else if (rangeType instanceof RealTupleType)
	    {
		Real[]	components = new Real[adapters.length];
		for (int j = 0; j < components.length; ++j)
		    components[j] =
			(Real)adapters[j].data((BaseType)row.get(j));
		data =
		    new RealTuple(
			(RealTupleType)rangeType, components, null);
	    }
	    else
	    {
		Data[]	components = new Data[adapters.length];
		for (int j = 0; j < components.length; ++j)
		    components[j] = adapters[j].data((BaseType)row.get(j));
		data = new Tuple((TupleType)rangeType, components);
	    }
	    field.setSample(i, data, /*copy=*/false);
	}
    }

    /**
     * Provides support for accessing a DODS DSequence as a VisAD {@link 
     * FileFlatField}.
     *
     * <P>Instances are immutable.</P>
     *
     * @author Steven R. Emmerson
     */
    protected class SequenceAccessor
	extends	FileAccessor
    {
	private final SampledSet	domain;
	private final DSequence		sequence;

	/**
	 * Constructs from a domain and a DODS {@link DSequence}.
	 *
	 * @param domain		The domain for the FileFlatField.
	 * @param sequence		The DODS variable.
	 */
	public SequenceAccessor(SampledSet domain, DSequence sequence)
	{
	    this.domain = domain;
	    this.sequence = sequence;
	}

	/*
	 * Returns the VisAD {@link FunctionType} of this instance.
	 *
	 * @return			The FunctionType of this instance.
	 */
	public FunctionType getFunctionType()
	{
	    return funcType;
	}

	/**
	 * Returns a VisAD {@link FlatField} corresponding to this instance.
	 *
	 * @return			A FlatField corresponding to the
	 *				construction arguments.
	 * @throws VisADException	VisAD failure.
	 * @throws RemoteException	Java RMI failure.
	 */
	public FlatField getFlatField()
	    throws VisADException, RemoteException
	{
	    FlatField	field =
		new FlatField(
		    funcType,
		    domain,
		    (CoordinateSystem[])null,
		    repSets,
		    (Unit[])null);
	    setField(sequence, field);
	    return field;
	}

	/**
	 * Throws a VisADError.
	 *
	 * @param values		Some values.
	 * @param template		A template FlatField.
	 * @param fileLocation		An array of positional parameters.
	 * @throws VisADError		This method does nothing and should not
	 *				have been invoked.  Always thrown.
	 */
	public void writeFlatField(
	    double[][] values, FlatField template, int[] fileLocation)
	{
	    throw new VisADError(
		getClass().getName() + ".writeFlatField(...): " +
		"Unimplemented method");
	}

	/**
	 * Throws a VisADError.
	 *
	 * @param template		A template FlatField.
	 * @param fileLocation		An array of positional parameters.
	 * @return			<code>null</code>.
	 * @throws VisADError		This method does nothing and should not
	 *				have been invoked.  Always thrown.
	 */
	public double[][] readFlatField(FlatField template, int[] fileLocation)
	{
	    throw new VisADError(
		getClass().getName() + ".readFlatField(...): " +
		"Unimplemented method");
	}

	/**
	 * Throws a VisADError.
	 *
	 * @param fileLocation		An array of positional parameters.
	 * @param range			The range of a FlatField.
	 * @throws VisADError		This method does nothing and should not
	 *				have been invoked.  Always thrown.
	 */
	public void writeFile(int[] fileLocation, Data range)
	{
	    throw new VisADError(
		getClass().getName() + ".writeFile(...): " +
		"Unimplemented method");
	}
    }
}
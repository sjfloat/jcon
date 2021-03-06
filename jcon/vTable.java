package jcon;

import java.util.*;

public final class vTable extends vStructure {

    private java.util.Hashtable<vValue,vValue> t;
    vValue dflt;



static int nextsn = 1;	// next serial number



// constructors

public static vTable New(vValue x)		{ return new vTable(x); }

private vTable(vValue dflt) {
    super(nextsn++);
    this.dflt = dflt;
    this.t = new Hashtable<vValue,vValue>();
}

private vTable(vTable x) {
    super(nextsn++);
    this.dflt = x.dflt;
    this.t = new Hashtable<vValue,vValue>(x.t);
}



public vValue get(vValue i) {
    return this.t.get(i);
}

public void put(vValue key, vValue val) {
    this.t.put(key, val);
}



private static vString typestring = vString.New("table");
public vString Type()		{ return typestring; }

int rank()		{ return 110; }		// tables rank after sets

public vInteger Size() {				// *T
    return vInteger.New(t.size());
}

public vValue Copy() {					// copy(T)
    return new vTable(this);
}



public vDescriptor Index(vDescriptor i) {		// T[x]
    return new vTableRef(this, i.Deref());
}

public vValue IndexVal(vDescriptor i) {			// .T[x]
    vValue key = i.Deref();
    vValue val = t.get(key);
    return (val == null) ? this.dflt : val;
}



public vDescriptor Select() {				// ?T
    if (t.size() == 0) {
	return null;
    }
    int index = (int) iKeyword.random.choose(t.size());
    java.util.Enumeration<vValue> e = t.keys();
    for (int k = 0; k < index; k++) {
	e.nextElement();
    }
    return new vTableRef(this, e.nextElement());
}



public vDescriptor Bang() {				// !T
    final vTableRef a[] = new vTableRef[t.size()];
    java.util.Enumeration<vValue> e = t.keys();
    int i = 0;
    while (e.hasMoreElements()) {
	a[i++] = new vTableRef(this, e.nextElement());
    }
    if (i == 0) { 
	return null; /*FAIL*/
    } else if (i == 1) {
	return a[0];  // only one member
    }

    return new vClosure() {
	{ retval = a[0]; }
	int j = 1;
	public vDescriptor Resume() {
	    while (j < a.length) {
		vTableRef v = a[j++];
		if (t.containsKey(v.key)) {	// if not stale
		    return v;			// suspend
		}
	    }
	    return null; /*FAIL*/
	}
    };
}



public vDescriptor Key() {
    final vValue a[] = new vValue[t.size()];
    java.util.Enumeration<vValue> e = t.keys();
    int i = 0;
    while (e.hasMoreElements()) {
	a[i++] = e.nextElement();
    }
    if (i == 0) {
	return null; /*FAIL*/
    } else if (i == 1) {
	return a[0];  // only one member
    }

    return new vClosure() {
	{ retval = a[0]; }
	int j = 1;
	public vDescriptor Resume() {
	    while (j < a.length) {
		vValue v = a[j++];
		if (t.containsKey(v)) {		// if not stale
		    return v;			// suspend
		}
	    }
	    return null; /*FAIL*/
	}
    };
}



public vValue Member(vDescriptor i) {
    i = i.Deref();
    return t.containsKey(i) ? (vValue) i : null;
}

public vValue Delete(vDescriptor i) {
    t.remove(i.Deref());
    return this;
}

public vValue Insert(vDescriptor i, vDescriptor val) {
    t.put(i.Deref(), val.Deref());
    return this;
}

public vList Sort(int n) {

    vTableElem a[] = new vTableElem[t.size()];	// make array of key/val pairs

    int i = 0;
    for (Enumeration<vValue> e = t.keys(); e.hasMoreElements(); ) {
	vValue key = e.nextElement();
	vValue val = t.get(key);
	if ((n & 1) != 0) {
	    a[i++] = new vTableElem(key, val);	// sort by key
	} else {
	    a[i++] = new vTableElem(val, key);	// sort by value
	}
    }
    iSort.sort(a);				// sort array of pairs

    vValue b[];
    if (n <= 2) {				// return list of lists
	b = new vValue[t.size()];
	vValue pair[] = new vValue[2];
	for (i = 0; i < t.size(); i++) {
	    if ((n & 1) != 0) {			// sorted by key
		pair[0] = a[i].sortkey;
		pair[1] = a[i].other;
	    } else {				// sorted by value
		pair[0] = a[i].other;
		pair[1] = a[i].sortkey;
	    }
	    b[i] = vList.New(pair);
	}

    } else {					// return 2x-long list
	b = new vValue[2 * t.size()];
	int j = 0;
	for (i = 0; i < t.size(); i++) {
	    if ((n & 1) != 0) {			// sorted by key
		b[j++] = a[i].sortkey;
		b[j++] = a[i].other;
	    } else {				// sorted by value
		b[j++] = a[i].other;
		b[j++] = a[i].sortkey;
	    }
	}
    }

    return vList.New(b);			// turn results into Icon list
}



} // class vTable



final class vTableRef extends vVariable {

    vTable table;
    vValue key;

    public vString report()	{
	return vString.New("(variable = " + this.table.report() +
	    "[" + this.key.report() + "])");
    }

    vTableRef(vTable table, vValue key) {
	this.table = table;
	this.key = key;
    }

    public vDescriptor DerefLocal() {
	return this;
    }

    public vDescriptor Return() {
	return this;
    }

    public vValue Deref() {
	vValue v = table.get(key);
	return (v == null) ? table.dflt : v;
    }

    public vVariable Assign(vDescriptor v) {
	table.put(key, v.Deref());
	return this;
    }

    public vString Name() {
	return key.image().surround("T[", "]");
    }

} // class vTableRef



final class vTableElem extends vValue {	// key/value pair for sorting

    vValue sortkey;	// value used for sorting (table key or value)
    vValue other;	// the other half of the pair

    vTableElem(vValue sortkey, vValue other) {	// constructor
	this.sortkey = sortkey;
	this.other = other;
    }

    int compareTo(vValue v)	{
	vValue vkey = ((vTableElem)v).sortkey;
	int d = sortkey.rank() - vkey.rank();
	if (d == 0) {
	    return sortkey.compareTo(vkey);
	} else {
	    return d;
	}
    }

    public vString image()  {			// not normally used
	return vString.New("(" + sortkey.image().mkString() + "," +
	    other.image().mkString() + ")");
    }

    static vString typestring = vString.New("telem");
    public vString Type()	{ return typestring; }
    int rank()		{ return -1; }		// never compared to other types

} // class vTableElem

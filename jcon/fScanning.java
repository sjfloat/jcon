// builtin string scanning functions

package rts;

class fScanning {} // dummy

class f$pos extends iValueClosure {			// pos(i)
    vDescriptor function(vDescriptor[] args) {
	long i = vInteger.argVal(args, 0);
	vString s = (vString) k$subject.self.deref();
	vInteger p = (vInteger) k$pos.self.deref();
	if (s.posEq(i) == p.value) {
	    return k$pos.self.deref();
	}
	return null;
    }
}

class f$any extends iValueClosure {			// any(c,s,i1,i2)
    vDescriptor function(vDescriptor[] args) {
	vCset c = vCset.argVal(args, 0);
	vString s = iRuntime.argSubject(args, 1);
	int i1 = s.posEq(iRuntime.argPos(args, 2));
	int i2 = s.posEq(vInteger.argVal(args, 3, 0));

	if (i1 > i2) {
	    int tmp = i1;
	    i1 = i2;
	    i2 = tmp;
	}
	if (i1 < i2 && c.member(s.value.charAt(i1-1))) {
	    return iNew.Integer(i1+1);
	}
	return null;
    }
}

class f$many extends iValueClosure {			// many(c,s,i1,i2)
    vDescriptor function(vDescriptor[] args) {
	vCset c = vCset.argVal(args, 0);
	vString s = iRuntime.argSubject(args, 1);
	int i1 = s.posEq(iRuntime.argPos(args, 2));
	int i2 = s.posEq(vInteger.argVal(args, 3, 0));
	if (i1 > i2) {
	    int tmp = i1;
	    i1 = i2;
	    i2 = tmp;
	}

	if (i1 >= i2) {
	    return null;
	}
	int i;
	for (i = i1; i < i2; i++) {
	    if (!c.member(s.value.charAt(i-1))) {
		break;
	    }
	}
	if (i == i1) {
	    return null;
	} else {
	    return iNew.Integer(i);
	}
    }
}

class f$match extends iValueClosure {		// match(s1,s2,i1,i2)
    vDescriptor function(vDescriptor[] args) {
	String s1 = vString.argVal(args, 0);
	vString s2 = iRuntime.argSubject(args, 1);
	int i1 = s2.posEq(iRuntime.argPos(args, 2));
	int i2 = s2.posEq(vInteger.argVal(args, 3, 0));

	if (i1 > i2) {
		int tmp = i1;
		i1 = i2;
		i2 = tmp;
	}
	if (i1 > i2-s1.length()) {
	    return null;
	}
	if (s2.value.length() < i1+s1.length()-1) {
	    return null;
	}
	if (s1.equals( s2.value.substring(i1-1, i1+s1.length()-1) )) {
	    return iNew.Integer(i1+s1.length());
	} else {
	    return null;
	}
    }
}

class f$find extends iClosure {				// find(s1,s2,i1,i2)

    String s1;
    vString s2;
    int i1;
    int i2;

    public void nextval() {

        if (s1 == null) {
	    for (int i = 0; i < arguments.length; i++) {
		arguments[i] = arguments[i].deref();
	    }
	    s1 = vString.argVal(arguments, 0);
	    s2 = iRuntime.argSubject(arguments, 1);
	    i1 = s2.posEq(iRuntime.argPos(arguments, 2));
	    i2 = s2.posEq(vInteger.argVal(arguments, 3, 0));
	    if (i1 > i2) {
		int tmp = i1;
		i1 = i2;
		i2 = tmp;
	    }
	}

	if (i1 > i2) {
	    retvalue = null;
	}
	if (s2.value.length() < i1+s1.length()-1) {
	    retvalue = null;
	}
	int i = s2.value.indexOf(s1, i1-1);
	if (i >= 0 && i+s1.length() < i2) {
	    i1 = i+2;
	    retvalue = iNew.Integer(i+1);
	} else {
	    retvalue = null;
	}
    }
}

class f$upto extends iClosure {				// upto(c,s2,i1,i2)

    vCset c;
    vString s;
    int i1;
    int i2;

    public void nextval() {

        if (c == null) {
	    for (int i = 0; i < arguments.length; i++) {
		arguments[i] = arguments[i].deref();
	    }
	    c = vCset.argVal(arguments, 0);
	    s = iRuntime.argSubject(arguments, 1);
	    i1 = s.posEq(iRuntime.argPos(arguments, 2));
	    i2 = s.posEq(vInteger.argVal(arguments, 3, 0));
	    if (i1 > i2) {
	        int tmp = i1;
	        i1 = i2;
	        i2 = tmp;
	    }
	}

	for (; i1 < i2; i1++) {
	    if (c.member(s.value.charAt(i1-1))) {
		i1 = i1+1;
	        retvalue = iNew.Integer(i1-1);
		return;
	    }
	}
	retvalue = null;
    }
}

class f$bal extends iClosure {				// bal(c1,c2,c3,s,i1,i2)

    vCset c1;
    vCset c2;
    vCset c3;
    vString s;
    int i1;
    int i2;

    public void nextval() {

        if (c1 == null) {
	    for (int i = 0; i < arguments.length; i++) {
		arguments[i] = arguments[i].deref();
	    }
	    // %#%#%# c1 should default to &cset....
	    c1 = vCset.argVal(arguments, 0, 0, vCset.MAX_VALUE);
	    c2 = vCset.argVal(arguments, 1, '(', '(');
	    c3 = vCset.argVal(arguments, 2, ')', ')');
	    s = iRuntime.argSubject(arguments, 3);
	    i1 = s.posEq(iRuntime.argPos(arguments, 4));
	    i2 = s.posEq(vInteger.argVal(arguments, 5, 0));
	    if (i1 > i2) {
		int tmp = i1;
		i1 = i2;
		i2 = tmp;
	    }
	}

	int balance = 0;
	for (; i1 < i2; i1++) {
	    if (balance == 0 && c1.member(s.value.charAt(i1-1))) {
		i1 = i1+1;
	        retvalue = iNew.Integer(i1-1);
		return;
	    }
	    if (c2.member(s.value.charAt(i1-1))) {
		balance++;
	    } else if (c3.member(s.value.charAt(i1-1))) {
		balance--;
		if (balance < 0) {
		    retvalue = null;
		    return;
		}
	    }
	}
	retvalue = null;
    }
}

class f$move extends iClosure {				// move(j)

    vInteger oldpos;

    public void nextval() {

        if (oldpos == null) {
	    oldpos = (vInteger) k$pos.self.deref();
	    int i = (int) oldpos.value;
	    int j = (int) vInteger.argVal(arguments, 0);
	    int k = i + j - 1;
	    vString s = (vString) k$subject.self.deref();
	    if (k < 0 || k > s.value.length()) {
		retvalue = null;
	    } else {
	        k$pos.self.Assign(iNew.Integer(i+j));
		if (j >= 0) {
		    retvalue = iNew.String(s.value.substring(i - 1, k));
		} else {
		    retvalue = iNew.String(s.value.substring(k, i - 1));
		}
	    }
	} else {
	    k$pos.self.Assign(oldpos);
	    retvalue = null;
	}
    }
}

class f$tab extends iClosure {				// tab(j)

    vInteger oldpos;

    public void nextval() {

        if (oldpos == null) {
	    oldpos = (vInteger) k$pos.self.deref();
	    vString s = (vString) k$subject.self.deref();
	    int i = (int) oldpos.value;
	    int j = (int) s.posEq(vInteger.argVal(arguments, 0));
	    if (j == 0) {
		retvalue = null;
	    } else {
	        k$pos.self.Assign(iNew.Integer(j));
		if (i < j) {
		    retvalue = iNew.String(s.value.substring(i - 1, j - 1));
		} else {
		    retvalue = iNew.String(s.value.substring(j - 1, i - 1));
		}
	    }
	} else {
	    k$pos.self.Assign(oldpos);
	    retvalue = null;
	}
    }
}

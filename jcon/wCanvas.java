//  wCanvas.java -- an Icon window canvas, including attributes

package rts;

import java.awt.*;
import java.util.*;



class wCanvas extends Canvas {

    Frame f;			// enclosing Frame object
    Image i;			// backing image for refreshing visable image

    vList evq;			// event queue
    wTTY tty;			// file I/O stuff

    Vector wlist;		// list of associated vWindows

    String visibility;		// value of "canvas" (visibility) attribute

    boolean have_set_width;	// was width set explicitly?
    boolean have_set_height;	// was height set explicitly?



//  new wCanvas(win, label, w, h) -- create new canvas
//
//  (canvas is created but not made visible)

wCanvas(vWindow win, String label, int w, int h) {

    wlist = new Vector();
    wlist.addElement(win);

    this.setSize(w, h);

    f = new Frame(label);
    f.setTitle("");
    f.add(this, "North");
    f.pack();

    i = this.createImage(w, h);

    evq = vList.New(0, null);			// create event queue
    tty = new wTTY();				// create TTY instance

    wEvent.register(this);			// register event handlers
}



//  config(win, multiplier, x, y, w, h) -- reconfigure geometry
//
//  #%#% for now, x and y are ignored; only size counts
//
//  arguments are *String* values a la WAttrib()
//  any particular argument can be null
//
//  returns failure (false) or success (true).

boolean config(vWindow win, int m, String x, String y, String w, String h) {
    Rectangle r = this.getBounds();

    try {
	if (x != null) { r.x = m * Integer.parseInt(x); }
	if (y != null) { r.y = m * Integer.parseInt(y); }
	if (w != null) { r.width = m * Integer.parseInt(w); }
	if (h != null) { r.height = m * Integer.parseInt(h); }
	// do the following only after we know both values parsed okay
	if (w != null) { have_set_width = true; }
	if (h != null) { have_set_height = true; }
    } catch (Exception e) {
	return false;
    }
    resize(win, r.width, r.height);
    return true;
}



//  defconfig -- set default configuration for window
//
//  If not explicitly set otherwise, the default window size is
//  12 rows x 80 columns.  This cannot be done until the font and
//  leading are known.

void defconfig(vWindow win) {
    if (have_set_width && have_set_height) {
	return;
    }
    Rectangle r = this.getBounds();
    if (! have_set_width) {
	r.width = 80 * win.Fwidth();
    }
    if (! have_set_width) {
	r.height = 12 * win.Leading();
    }
    resize(win, r.width, r.height);
}



//  resize(win, w, h) -- resize canvas

void resize(vWindow win, int w, int h) {

    Dimension d = this.getSize();		// get current size
    if (d.width == w && d.height == h) {
	return;					// nothing to do
    }
    this.setSize(w, h);				// alter size
    f.pack();					// alter size of enclosing frame

    int iw = i.getWidth(null);
    int ih = i.getHeight(null);

    if (iw >= w && ih >= h) {
	return;					// image is large enough
    }

    if (iw < w) { iw = w; }
    if (ih < h) { ih = h; }

    Image inew = this.createImage(iw, ih);
    Graphics g = inew.getGraphics();

    // install new graphics context in each associated window
    for (int j = 0; j < wlist.size(); j++) {
	((vWindow) wlist.elementAt(j)).newgcb(g);
    }

    // g has been cloned for use with each window
    // we can now use it without affecting anything else

    // clear the new image, then copy in the old portion
    g.setColor(win.getBg());
    g.fillRect(0, 0, iw, ih);
    g.drawImage(i, 0, 0, null);

    i = inew;
}



//  paint(g) -- refresh the canvas from the backing image

public void paint(Graphics g) {
    g.drawImage(i,0,0,null);
}



//  enqueue(a, b, c) -- enqueue three event values (synchronized)

synchronized void enqueue(vValue a, vValue b, vValue c) {
    evq.Put(a);
    evq.Put(b);
    evq.Put(c);
}



//  Canvas(win, s) -- set "canvas" (visibility) attribute

vString Canvas(vWindow win, String s) {
    if (s != null) {
	if (s.equals("hidden")) {
	    f.hide();
	} else if (s.equals("normal")) {
	    f.show();
	} else {
	    return null; /*FAIL*/
	    //#%#% still need to handle "maximal" and "iconic"
	}
	visibility = s;
    }
    if (visibility == null) {
	return null;
    } else {
	return vString.New(visibility);
    }
}


//  Label(win, s) -- set "label" attribute

vString Label(vWindow win, String s) {
    if (s != null) {
	f.setTitle(s);
    }
    return vString.New(f.getTitle());
}



} // wCanvas

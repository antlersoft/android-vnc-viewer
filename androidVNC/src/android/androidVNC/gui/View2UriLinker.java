package android.androidVNC.gui;

import java.util.ArrayList;

import android.androidVNC.gui.links.Link;

public class View2UriLinker {

  ArrayList<Link> fLinks = new ArrayList<Link>();
  
  public void add(Link l) {
   fLinks.add(l); 
  }

  public void copyFromGui2Model() {
    for (Link l : fLinks) {
      l.copyFromGui2Model();
    }
  }

}

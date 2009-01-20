package android.androidVNC.gui.links;

import android.androidVNC.COLORMODEL;

public class Colormodel2IntegerMapping implements BijectiveMapping<Integer, COLORMODEL> {
  public Integer from(COLORMODEL v) {
    int idx = 0;
    for (COLORMODEL c : COLORMODEL.values()) {
      if (v == c) {
        return idx;
      }
      idx++;
    }
    throw new RuntimeException("no mapping found");
  }

  public COLORMODEL to(Integer v) {
    return COLORMODEL.values()[v];
  }
}
package android.androidVNC.gui.links;

public interface BijectiveMapping<S, T> {
  T to(S v);
  S from(T v);
}


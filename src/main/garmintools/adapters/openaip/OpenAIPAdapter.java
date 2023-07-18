package garmintools.adapters.openaip;

import java.util.List;

import garmintools.Proto.NavigationData;
import garmintools.openaip.Airport;

public interface OpenAIPAdapter<T> {
  public T read(List<Airport> airports);
  public void write(T data, NavigationData.Builder builder);
}

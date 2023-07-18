package garmintools.adapters.openaip;

import java.util.List;

import com.google.common.collect.ImmutableList;

import garmintools.Proto;
import garmintools.Proto.NavigationData.Builder;
import garmintools.openaip.Airport;

public class CoordinateSystemOpenAIPAdapter implements OpenAIPAdapter<List<Proto.CoordinateSystem>> {

  @Override
  public List<Proto.CoordinateSystem> read(List<Airport> airports) {
    ImmutableList.Builder<Proto.CoordinateSystem> listBuilder = ImmutableList.builder();
    return listBuilder.build();
  }

  @Override
  public void write(List<Proto.CoordinateSystem> data, Builder builder) {
    // TODO Auto-generated method stub

  }

}

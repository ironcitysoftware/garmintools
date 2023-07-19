package garmintools.adapters.openaip;

import java.util.List;

import com.google.common.collect.ImmutableList;

import garmintools.Proto.NavigationData.Builder;
import garmintools.openaip.Airport;

public class DataLengthOpenAIPAdapter implements OpenAIPAdapter<List<Integer>> {

  @Override
  public List<Integer> read(List<Airport> airports) {
    ImmutableList.Builder<Integer> listBuilder = ImmutableList.builder();
    return listBuilder.build();
  }

  @Override
  public void write(List<Integer> data, Builder builder) {
    // TODO Auto-generated method stub

  }

}

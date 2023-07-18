package garmintools.adapters.openaip;

import java.util.List;

import com.google.common.collect.ImmutableList;

import garmintools.Proto.NavigationData.Builder;
import garmintools.openaip.Airport;

public class LookupTableOpenAIPAdapter implements OpenAIPAdapter<List<String>> {
  private final int sectionNumber;

  public LookupTableOpenAIPAdapter(int sectionNumber) {
    this.sectionNumber = sectionNumber;
  }

  @Override
  public List<String> read(List<Airport> airports) {
    ImmutableList.Builder<String> listBuilder = ImmutableList.builder();
    return listBuilder.build();
  }

  @Override
  public void write(List<String> data, Builder builder) {
    // TODO Auto-generated method stub

  }

}

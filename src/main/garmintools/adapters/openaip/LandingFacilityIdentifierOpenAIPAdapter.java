package garmintools.adapters.openaip;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import garmintools.Proto.NavigationData.Builder;
import garmintools.openaip.Airport;

public class LandingFacilityIdentifierOpenAIPAdapter implements OpenAIPAdapter<Map<Byte, Integer>> {

  @Override
  public Map<Byte, Integer> read(List<Airport> airports) {
    ImmutableMap.Builder<Byte, Integer> mapBuilder = ImmutableMap.builder();
    return mapBuilder.build();
  }

  @Override
  public void write(Map<Byte, Integer> data, Builder builder) {
    // TODO Auto-generated method stub

  }

}

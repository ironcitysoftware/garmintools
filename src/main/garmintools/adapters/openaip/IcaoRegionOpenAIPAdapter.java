package garmintools.adapters.openaip;

import java.util.List;

import com.google.common.collect.ImmutableList;

import garmintools.Proto;
import garmintools.Proto.NavigationData.Builder;
import garmintools.openaip.Airport;

public class IcaoRegionOpenAIPAdapter implements OpenAIPAdapter<List<Proto.IcaoRegion>> {

  @Override
  public List<Proto.IcaoRegion> read(List<Airport> airports) {
    ImmutableList.Builder<Proto.IcaoRegion> listBuilder = ImmutableList.builder();

    listBuilder.add(Proto.IcaoRegion.newBuilder().setLandingFacilityIdentifierPrefix("SA")
        .setRegion("ARGENTINA").build());

    return listBuilder.build();
  }

  @Override
  public void write(List<Proto.IcaoRegion> data, Builder builder) {
    // TODO Auto-generated method stub

  }

}

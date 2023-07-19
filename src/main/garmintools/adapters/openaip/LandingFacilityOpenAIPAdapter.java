package garmintools.adapters.openaip;

import java.util.List;

import com.google.common.collect.ImmutableList;

import garmintools.Proto;
import garmintools.Proto.NavigationData.Builder;
import garmintools.openaip.Airport;
import garmintools.wrappers.LandingFacility;

public class LandingFacilityOpenAIPAdapter implements OpenAIPAdapter<List<LandingFacility>> {

  @Override
  public List<LandingFacility> read(List<Airport> airports) {
    ImmutableList.Builder<LandingFacility> listBuilder = ImmutableList.builder();
    for (Airport airport : airports) {
      LandingFacility.Builder builder = LandingFacility.newBuilder();
      Proto.LandingFacility.Builder protoBuilder = Proto.LandingFacility.newBuilder();

      protoBuilder.setName(airport.name);
      protoBuilder.setLatitudeDegrees(airport.geometry.coordinates[0]);
      protoBuilder.setLongitudeDegrees(airport.geometry.coordinates[1]);
      protoBuilder.setIdentifier(airport._id);

      listBuilder.add(builder.withLandingFacility(protoBuilder.build()).build());
    }
    return listBuilder.build();
  }

  @Override
  public void write(List<LandingFacility> landingFacilities, Builder builder) {
    for (LandingFacility landingFacility : landingFacilities) {
      builder.addLandingFacility(landingFacility.protoLandingFacility);
    }
  }

}

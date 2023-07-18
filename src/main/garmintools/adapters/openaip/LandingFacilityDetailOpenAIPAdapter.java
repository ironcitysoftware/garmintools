package garmintools.adapters.openaip;

import java.util.List;

import com.google.common.collect.ImmutableList;

import garmintools.Proto.NavigationData.Builder;
import garmintools.openaip.Airport;
import garmintools.wrappers.LandingFacilityDetail;

public class LandingFacilityDetailOpenAIPAdapter implements OpenAIPAdapter<List<LandingFacilityDetail>> {

  @Override
  public List<LandingFacilityDetail> read(List<Airport> airports) {
    ImmutableList.Builder<LandingFacilityDetail> builder = ImmutableList.builder();
    return builder.build();
  }

  @Override
  public void write(List<LandingFacilityDetail> data, Builder builder) {
    // TODO Auto-generated method stub
    
  }

}

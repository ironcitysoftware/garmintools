package garmintools.adapters.openaip;

import java.util.List;

import garmintools.Proto.NavigationData.Builder;
import garmintools.Proto;
import garmintools.openaip.Airport;

public class UnparsedSectionOpenAIPAdapter implements OpenAIPAdapter<Proto.UnparsedSection> {
  private final int sectionNumber;

  public UnparsedSectionOpenAIPAdapter(int sectionNumber) {
    this.sectionNumber = sectionNumber;
  }

  @Override
  public Proto.UnparsedSection read(List<Airport> airports) {
    return Proto.UnparsedSection.newBuilder().build();
  }

  @Override
  public void write(Proto.UnparsedSection data, Builder builder) {
    // TODO Auto-generated method stub

  }

}

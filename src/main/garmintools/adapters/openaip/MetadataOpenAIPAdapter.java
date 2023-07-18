package garmintools.adapters.openaip;

import java.util.List;

import garmintools.Proto;
import garmintools.Proto.NavigationData.Builder;
import garmintools.openaip.Airport;

public class MetadataOpenAIPAdapter implements OpenAIPAdapter<Proto.Metadata> {

  @Override
  public Proto.Metadata read(List<Airport> airports) {
    Proto.Metadata.Builder builder = Proto.Metadata.newBuilder();

    builder.setCoverageRegion("ARGENTINA");
    builder.setEffectiveDate(createDate(2023, 6, 18));
    builder.setExpiresDate(createDate(2024, 6, 18));
    builder.setAeronauticalDataSnapshotDate(createDate(2023, 5, 18));

    return builder.build();
  }

  private Proto.Date createDate(int year, int month, int day) {
    return Proto.Date.newBuilder().setYear(year).setMonth(month).setDay(day).build();
  }

  @Override
  public void write(Proto.Metadata data, Builder builder) {
    // TODO Auto-generated method stub

  }

}

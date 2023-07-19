package garmintools.adapters.openaip;

import java.util.HashMap;
import java.util.List;

import garmintools.Proto.NavigationData.Builder;
import garmintools.openaip.Airport;
import garmintools.wrappers.TableOfContents;

public class TableOfContentsOpenAIPAdapter implements OpenAIPAdapter<TableOfContents> {

  @Override
  public TableOfContents read(List<Airport> airports) {
    return new TableOfContents(0, new HashMap<>(), new HashMap<>());
  }

  @Override
  public void write(TableOfContents data, Builder builder) {
    // TODO Auto-generated method stub

  }

}

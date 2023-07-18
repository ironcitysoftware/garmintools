package garmintools.files;

import garmintools.Proto;
import garmintools.sections.SectionManager;

public class OpenAIPNavigationDataFile {
  private final SectionManager sectionManager;
  
  OpenAIPNavigationDataFile(SectionManager sectionManager) {
    this.sectionManager = sectionManager;
  }
  
  public void writeToProto(Proto.NavigationData.Builder protoBuilder) {
    sectionManager.mergeToProto(protoBuilder);
  }
}

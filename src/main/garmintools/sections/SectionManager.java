/**
 *    Copyright 2016 Iron City Software LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package garmintools.sections;

import garmintools.Proto;
import garmintools.adapters.garmin.GarminOutput;
import garmintools.wrappers.TableOfContentsEntry;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

public class SectionManager {
  // This list is used when the proto does not specify an ordering.
  // This list can probably be dynamically calculated if we had a foreign key dependency graph.
  private static final int[] DEFAULT_SECTION_ORDER = new int[] {
    12000, 12001,
     0, 74, 43, 75, 27, 89,  1,  2, 86, 10, 42, 85, 90, 91, 92, 93, 94, 41, 62, 55, 56, 57, 58, 70,
    64, 71, 49, 52, 48, 47, 50, 51, 67, 59, 60, 54, 53, 76, 77, 79, 80, 81, 87, 78, 35, 34, 33, 31,
    30, 32, 14, 12, 11, 13, 18, 16, 15, 17, 19, 20, 24, 22, 21, 23, 25, 26,  9,  6,  4,  3,  5,  7,
     8, 69, 29, 28, 40, 37, 36, 38, 39, 68, 88, 45, 44, 46, 73, 72, 65
  };

  // TODO: this is the work to do :-(
  private static final int[] UNPARSED_SECTIONS = new int[] {
     4,  5,  7,  8, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
    30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 42, 43, 44, 45, 46, 50, 51, 52, 56, 57, 58, 61, 63,
    65, 66, 68, 69, 71, 72, 73, 74, 75, 78, 82, 83, 84, 85, 86, 88, 89, 90, 91, 92, 93, 94
  };

  private final Map<Integer, Section<?>> sections;
  private final AirspaceTable airspaceTable;
  private final RunwayNumberSuffixTable runwayNumberSuffixTable;

  private SectionManager(List<Section<?>> sections,
      AirspaceTable airspaceTable,
      RunwayNumberSuffixTable runwayNumberSuffixTable) {
    ImmutableMap.Builder<Integer, Section<?>> mapBuilder = ImmutableMap.builder();
    for (Section<?> section : sections) {
      mapBuilder.put(section.sectionNumber, section);
    }
    this.sections = mapBuilder.build();
    this.airspaceTable = new AirspaceTable();
    this.runwayNumberSuffixTable = new RunwayNumberSuffixTable();
  }

  public AirspaceTable getAirspaceTable() {
    return airspaceTable;
  }

  public RunwayNumberSuffixTable getRunwayNumberSuffixTable() {
    return runwayNumberSuffixTable;
  }

  public DataLengthSection getDataLengthSection() {
    return (DataLengthSection) sections.get(Ids.DATA_LENGTH_SECTION);
  }

  public IcaoRegionSection getIcaoRegionSection() {
    return (IcaoRegionSection) sections.get(Ids.ICAO_REGION_SECTION);
  }

  public StringSection getStringSection() {
    return (StringSection) sections.get(Ids.STRING_SECTION);
  }

  public LandingFacilityIdentifierIndexSection getLandingFacilityIdentifierIndexSection() {
    return (LandingFacilityIdentifierIndexSection) sections.get(Ids.LANDING_FACILITY_IDENTIFIER_INDEX_SECTION);
  }

  public LandingFacilityDetailSection getLandingFacilityDetailSection() {
    return (LandingFacilityDetailSection) sections.get(Ids.LANDING_FACILITY_DETAIL_SECTION);
  }

  public LookupTableSection getRunwayLightingSection() {
    return (LookupTableSection) sections.get(Ids.RUNWAY_LIGHTING_SECTION);
  }

  public LookupTableSection getRunwaySurfaceSection() {
    return (LookupTableSection) sections.get(Ids.RUNWAY_SURFACE_SECTION);
  }

  public LookupTableSection getGenericAirportStringSection1() {
    return (LookupTableSection) sections.get(Ids.GENERIC_AIRPORT_STRING_SECTION1);
  }

  public LookupTableSection getLandingFacilityTypeSection() {
    return (LookupTableSection) sections.get(Ids.LANDING_FACILITY_TYPE_SECTION);
  }

  public LookupTableSection getGenericAirportStringSection2() {
    return (LookupTableSection) sections.get(Ids.GENERIC_AIRPORT_STRING_SECTION2);
  }

  public TableOfContentsSection getTableOfContentsSection() {
    return (TableOfContentsSection) sections.get(Ids.TABLE_OF_CONTENTS_SECTION);
  }

  public MetadataSection getMetadataSection() {
    return (MetadataSection) sections.get(Ids.METADATA_SECTION);
  }

  public void mergeFromProto(Proto.NavigationData proto) {
    for (Section<?> section : sections.values()) {
      section.mergeFromProto(this, proto);
    }
  }

  public void mergeToProto(Proto.NavigationData.Builder protoBuilder) {
    for (Section<?> section : sections.values()) {
      section.mergeToProto(this, protoBuilder);
    }
  }

  public Map<Integer, GarminOutput> getGarminOutputs() {
    ImmutableMap.Builder<Integer, GarminOutput> sectionToBufferBuilder = ImmutableMap.builder();
    for (int sectionNumber : DEFAULT_SECTION_ORDER) {
      if (sectionNumber <= Ids.MAX_SECTION_NUMBER) {
        sectionToBufferBuilder.put(sectionNumber, sections.get(sectionNumber).getSectionBytes(this));
      }
    }
    return sectionToBufferBuilder.build();
  }

  private static final List<SectionFactory<?>> SECTION_FACTORIES_LIST = ImmutableList.<SectionFactory<?>>of(
      new MetadataSection.Factory(),
      new TableOfContentsSection.Factory(),
      new DataLengthSection.Factory(),
      new CoordinateSystemSection.Factory(),
      new IcaoRegionSection.Factory(),
      new StringSection.Factory(),
      new LandingFacilityIdentifierIndexSection.Factory(),
      new LandingFacilitySection.Factory(),
      new LandingFacilityDetailSection.Factory(),
      new LookupTableSection.Factory(Ids.RUNWAY_LIGHTING_SECTION),
      new LookupTableSection.Factory(Ids.RUNWAY_SURFACE_SECTION),
      new LookupTableSection.Factory(Ids.GENERIC_AIRPORT_STRING_SECTION1),
      new LookupTableSection.Factory(Ids.LHTU_SECTION),
      new LookupTableSection.Factory(Ids.APPROACH_TYPE_SECTION),
      new LookupTableSection.Factory(Ids.LANDING_FACILITY_TYPE_SECTION),
      new LookupTableSection.Factory(Ids.AIRSPACE_ABBREVIATION_SECTION1),
      new LookupTableSection.Factory(Ids.AIRSPACE_ABBREVIATION_SECTION2),
      new LookupTableSection.Factory(Ids.INSTRUMENT_APPROACH_TYPE_SECTION),
      new LookupTableSection.Factory(Ids.NESTB_SECTION),
      new LookupTableSection.Factory(Ids.BRIEF_AIRSPACE_TYPE_SECTION1),
      new LookupTableSection.Factory(Ids.RNAV_TYPE_SECTION),
      new LookupTableSection.Factory(Ids.GPS_APPROACH_TYPE_SECTION1),
      new LookupTableSection.Factory(Ids.GPS_APPROACH_TYPE_SECTION2),
      new LookupTableSection.Factory(Ids.GENERIC_AIRPORT_STRING_SECTION2),
      new LookupTableSection.Factory(Ids.EXPANDED_AIRSPACE_ABBREVIATION_SECTION1),
      new LookupTableSection.Factory(Ids.EXPANDED_AIRSPACE_ABBREVIATION_SECTION2),
      new LookupTableSection.Factory(Ids.BRIEF_AIRSPACE_TYPE_SECTION2)
  );

  private static final Map<Integer, SectionFactory<?>> SECTION_FACTORIES = createSectionFactories();

  private static Map<Integer, SectionFactory<?>> createSectionFactories() {
    Set<Integer> allIds = new HashSet<>(
        ContiguousSet.create(Range.closed(0, Ids.MAX_SECTION_NUMBER), DiscreteDomain.integers()));

    ImmutableMap.Builder<Integer, SectionFactory<?>> builder = ImmutableMap.builder();
    for (SectionFactory<?> sectionFactory : SECTION_FACTORIES_LIST) {
      builder.put(sectionFactory.getSectionNumber(), sectionFactory);
      allIds.remove(sectionFactory.getSectionNumber());
    }
    for (int unparsedSectionNumber : UNPARSED_SECTIONS) {
      builder.put(unparsedSectionNumber, new UnparsedSection.Factory(unparsedSectionNumber));
      allIds.remove(unparsedSectionNumber);
    }
    Preconditions.checkState(allIds.isEmpty(), "Unbound section(s): " + allIds);
    return builder.build();
  }

  public static class GarminBuilder {
    private List<Section<?>> sections = new ArrayList<>();

    public GarminBuilder readTableOfContents(InputStream inputStream, int inputFileLength) throws IOException {
      TableOfContentsSection.Factory factory = (TableOfContentsSection.Factory)
          SECTION_FACTORIES.get(Ids.TABLE_OF_CONTENTS_SECTION);
      sections.add(factory.createFromGarmin(inputStream, inputFileLength));
      return this;
    }

    public GarminBuilder addSection(TableOfContentsEntry entry, ByteBuffer byteBuffer) {
      Section<?> section = SECTION_FACTORIES.get(entry.sectionNumber)
          .createFromGarmin((DataLengthSection) getSection(Ids.DATA_LENGTH_SECTION), entry, byteBuffer);
      sections.add(section);
      return this;
    }

    public Section<?> getSection(int sectionNumber) {
      for (Section<?> section : sections) {
        if (section.sectionNumber == sectionNumber) {
          return section;
        }
      }
      return null;
    }

    public SectionManager build() {
      return new SectionManager(sections,
          new AirspaceTable(),
          new RunwayNumberSuffixTable());
    }
  }

  public static class ProtoBuilder {
    private final Proto.NavigationData proto;
    private final List<Section<?>> sections;

    public ProtoBuilder(Proto.NavigationData proto) {
      this.proto = proto;
      this.sections = new ArrayList<>();
    }

    public SectionManager build() {
      for (int sectionNumber : DEFAULT_SECTION_ORDER) { // proto.getMetadata().getSectionList()) {
        SectionFactory<?> sectionFactory = SECTION_FACTORIES.get(sectionNumber);
        // System.out.println("Reading section " + sectionNumber);
        Section<?> section = sectionFactory.createFromProto(proto);  // first pass
        sections.add(section);
      }
      SectionManager sectionManager = new SectionManager(sections,
          new AirspaceTable(),
          new RunwayNumberSuffixTable());
      sectionManager.mergeFromProto(proto);  // second pass
      return sectionManager;
    }
  }

  @Override
  public String toString() {
    List<String> strings = new ArrayList<>();
    for (int sectionNumber : sections.keySet()) {
      strings.add(String.format(">>> section %2d\n%s\n<<< section %2d",
          sectionNumber, sections.get(sectionNumber), sectionNumber));
    }
    return Joiner.on('\n').join(strings);
  }
}
